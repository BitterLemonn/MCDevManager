package com.lemon.mcdevmanager.utils

import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun rsaEncrypt(input: String, publicKeyStr: String): String {
    // 将公钥字符串转换为PublicKey对象
    val keyBytes = Base64.decode(publicKeyStr)
    val keySpec = X509EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKey = keyFactory.generatePublic(keySpec)

    // 使用公钥进行RSA加密
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val cipherText = cipher.doFinal(input.toByteArray())

    // 将加密后的字节数组转换为字符串
    return Base64.encode(cipherText)
}