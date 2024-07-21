package com.lemon.mcdevmanager.utils


import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.netease.login.PVArgs
import com.lemon.mcdevmanager.data.netease.login.PVInfo
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import com.orhanobut.logger.Logger
import kotlinx.coroutines.time.delay
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
import java.time.Duration
import java.time.temporal.TemporalUnit
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

suspend fun vdfAsync(data: PVInfo): PVResultStrBean {
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
        "runTimes" to count.toUInt(),
        "spendTime" to time.toUInt(),
        "t" to count.toUInt(),
        "x" to x.toString(16)
    )

    val sortedParams = listOf("runTimes", "spendTime", "t", "x")
    val encodedParams = sortedParams.joinToString("&") { key ->
        val value = signObj[key].toString()
        "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
    }

    Logger.d("encodedParams: $encodedParams, count: ${count.toUInt()}")
    val sign = murmurHash3(encodedParams, count.toUInt())

    return PVResultStrBean(
        maxTime = data.maxTime,
        puzzle = puzzle,
        spendTime = time.toInt(),
        runTimes = count,
        sid = data.sid,
        args = """{"x":"${x.toString(16)}","t":$count,"sign":"$sign"}"""
    )
}

fun murmurHash3(key: String, seed: UInt): UInt {
    val c1 = 0xcc9e2d51.toInt()
    val c2 = 0x1b873593.toInt()
    val r1 = 15
    val r2 = 13
    val m = 5
    val n = 0xe6546b64.toInt()

    var hash = seed.toInt()

    var k1: Int
    var k2: Int
    var i = 0
    while (i + 4 <= key.length) {
        k1 = (key[i + 3].code shl 24) or (key[i + 2].code shl 16) or (key[i + 1].code shl 8) or key[i].code
        i += 4

        k1 *= c1
        k1 = (k1 shl r1) or (k1 ushr (32 - r1))
        k1 *= c2

        hash = hash xor k1
        hash = (hash shl r2) or (hash ushr (32 - r2))
        hash = hash * m + n
    }

    if (i < key.length) {
        k2 = 0
        for (j in 0..<key.length - i) {
            k2 = k2 or (key[i + j].code shl (j * 8))
        }

        k2 *= c1
        k2 = (k2 shl r1) or (k2 ushr (32 - r1))
        k2 *= c2

        hash = hash xor k2
    }

    hash = hash xor key.length

    hash = hash xor (hash ushr 16)
    hash *= 0x85ebca6b.toInt()
    hash = hash xor (hash ushr 13)
    hash *= 0xc2b2ae35.toInt()
    hash = hash xor (hash ushr 16)

    return hash.toUInt()
}