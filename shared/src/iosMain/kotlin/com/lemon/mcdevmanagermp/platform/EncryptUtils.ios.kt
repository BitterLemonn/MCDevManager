package com.lemon.mcdevmanagermp.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFErrorRef
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecKeyCreateEncryptedData
import platform.Security.SecKeyCreateWithData
import platform.Security.SecKeyRef
import platform.Security.kSecAttrKeyClass
import platform.Security.kSecAttrKeyClassPublic
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeRSA
import platform.Security.kSecKeyAlgorithmRSAEncryptionPKCS1

@OptIn(ExperimentalForeignApi::class)
actual fun rsaEncrypt(input: String, publicKeyStr: String): String {
    return memScoped {
        // 1. 将 Base64 公钥字符串转为 NSData
        val keyData = NSData.create(base64Encoding = publicKeyStr)
            ?: return "Error: Invalid Public Key Base64"

        // 2. 构造属性字典 (使用更稳健的 CF 类型转换)
        val keys = allocArrayOf(kSecAttrKeyType, kSecAttrKeyClass)
        val values = allocArrayOf(kSecAttrKeyTypeRSA, kSecAttrKeyClassPublic)

        val attributes: CFDictionaryRef = CFDictionaryCreate(
            null,
            keys.reinterpret(),
            values.reinterpret(),
            2,
            null,
            null
        ) ?: return "Error: Failed to create attributes"

        // 3. 生成 SecKeyRef
        // 注意：使用 CFDataRef 和 CFDictionaryRef 别名，避免出现 __CF 开头的内部类报错
        val secKey: SecKeyRef = SecKeyCreateWithData(
            keyData as CFDataRef,
            attributes,
            null
        ) ?: return "Error: Failed to create SecKey"

        // 4. 准备明文数据
        val inputData = (input as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: return "Error: Encoding failed"

        // 5. 执行加密 (使用 PKCS1 填充)
        var error: CFErrorRef? = null
        val cipherDataRef = SecKeyCreateEncryptedData(
            secKey,
            kSecKeyAlgorithmRSAEncryptionPKCS1,
            inputData as CFDataRef,
            null // 如果需要调试错误，可以传入 error.ptr
        )

        if (cipherDataRef == null) {
            return "Error: Encryption execution failed"
        }

        // 6. 转换为 Base64 字符串
        val cipherData = cipherDataRef as NSData
        cipherData.base64EncodedStringWithOptions(0UL)
    }
}