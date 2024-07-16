package com.lemon.mcdevmanager.utils


import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.netease.login.PVArgs
import com.lemon.mcdevmanager.data.netease.login.PVInfo
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.internal.and
import org.bouncycastle.crypto.engines.SM4Engine
import org.bouncycastle.crypto.paddings.PKCS7Padding
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.net.URLEncoder
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Date
import javax.crypto.Cipher
import kotlin.concurrent.fixedRateTimer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.ceil
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

fun getRandomTid(): String {
    val charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    return List(32) { charSet.random(Random) }.joinToString("")
}


/**
 * VDF 算法
 */

fun vdfAsync(data: PVInfo): PVResultStrBean {
    val puzzle = data.args.puzzle
    val mod = BigInteger(data.args.mod, 16)
    var x = BigInteger(data.args.x, 16)
    val t = data.args.t
    val startTime = Date().time
    var count = 0

    while (count < t || Date().time - startTime < data.minTime) {
        x = x.multiply(x).mod(mod)
        count++
        val nowTime = Date().time
        if (nowTime - startTime > data.maxTime) {
            break
        }
    }

    val time = Date().time - startTime
    val signObj = mapOf(
        "runTimes" to count,
        "spendTime" to time,
        "t" to count,
        "x" to x.toString(16)
    )

    val sortedParams = listOf("runTimes", "spendTime", "t", "x")
    val encodedParams = sortedParams.joinToString("&") { key ->
        val value = signObj[key].toString()
        "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
    }

    val sign = powSign(encodedParams, count.toLong())

    return PVResultStrBean(
        maxTime = data.maxTime,
        puzzle = puzzle,
        spendTime = time.toInt(),
        runTimes = count,
        sid = data.sid,
        args = """{"x":"${x.toString(16)}","t":$count,"sign":"$sign"}"""
    )
}

fun powSign(key: String, seed: Long): Long {
    var h1b: Long
    var k1: Long

    val remainder = key.length and 3
    val bytes = key.length - remainder
    var h1 = seed
    val c1 = 0xcc9e2d51
    val c2 = 0x1b873593
    var i = 0

    while (i < bytes) {
        k1 = ((key[i].code and 0xff) or ((key[++i].code and 0xff) shl 8) or ((key[++i].code and 0xff) shl 16) or ((key[++i].code and 0xff) shl 24)).toLong()
        ++i
        k1 = ((k1 and 0xffff) * c1 + (((k1 ushr 16) * c1 and 0xffff) shl 16)) and 0xffffffff
        k1 = (k1 shl 15) or (k1 ushr 17)
        k1 = ((k1 and 0xffff) * c2 + (((k1 ushr 16) * c2 and 0xffff) shl 16)) and 0xffffffff
        h1 = h1 xor k1
        h1 = (h1 shl 13) or (h1 ushr 19)
        h1b = (((h1 and 0xffff) * 5 + (((h1 ushr 16) * 5 and 0xffff) shl 16)) and 0xffffffff)
        h1 = (h1b and 0xffff) + 0x6b64 + (((h1b ushr 16) + 0xe654 and 0xffff) shl 16)
    }
    k1 = 0
    when (remainder) {
        3 -> k1 = k1 xor ((key[i + 2].code and 0xff shl 16).toLong())
        2 -> k1 = k1 xor ((key[i + 1].code and 0xff shl 8).toLong())
        1 -> {
            k1 = k1 xor ((key[i].code and 0xff).toLong())
            k1 = ((k1 and 0xffff) * c1 + (((k1 ushr 16) * c1 and 0xffff) shl 16)) and 0xffffffff
            k1 = (k1 shl 15) or (k1 ushr 17)
            k1 = ((k1 and 0xffff) * c2 + (((k1 ushr 16) * c2 and 0xffff) shl 16)) and 0xffffffff
            h1 = h1 xor k1
        }
    }
    h1 = h1 xor key.length.toLong()
    h1 = h1 xor (h1 ushr 16)
    h1 = ((h1 and 0xffff) * 0x85ebca6b.toInt() + (((h1 ushr 16) * 0x85ebca6b and 0xffff) shl 16)) and 0xffffffff
    h1 = h1 xor (h1 ushr 13)
    h1 = ((h1 and 0xffff) * 0xc2b2ae35.toInt() + (((h1 ushr 16) * 0xc2b2ae35 and 0xffff) shl 16)) and 0xffffffff
    h1 = h1 xor (h1 ushr 16)
    return h1 ushr 0
}