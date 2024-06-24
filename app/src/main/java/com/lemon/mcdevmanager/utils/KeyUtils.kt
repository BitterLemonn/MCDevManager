package com.lemon.mcdevmanager.utils


import com.lemon.mcdevmanager.data.common.JSONConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.engines.SM4Engine
import org.bouncycastle.crypto.paddings.PKCS7Padding
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.util.encoders.Hex
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

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

fun sm4Encrypt(input: String, key: String): String {
    val keyBytes = Hex.decode(key)
    val inputBytes = input.toByteArray(Charsets.UTF_8)

    val cipher = PaddedBufferedBlockCipher(SM4Engine(), PKCS7Padding())
    cipher.init(true, KeyParameter(keyBytes))

    val outputBytes = ByteArray(cipher.getOutputSize(inputBytes.size))
    val length1 = cipher.processBytes(inputBytes, 0, inputBytes.size, outputBytes, 0)
    val length2 = cipher.doFinal(outputBytes, length1)

    return Hex.toHexString(outputBytes, 0, length1 + length2)
}

inline fun <reified T> dataJsonToString(data: T): String {
    return JSONConverter.encodeToString(data).trim()
}

fun getRandomTid(): String{
    val charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    return List(32) { charSet.random(Random) }.joinToString("")
}