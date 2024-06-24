package com.lemon.mcdevmanager.utils


import com.google.common.hash.Hashing
import com.lemon.mcdevmanager.data.common.JSONConverter
import com.lemon.mcdevmanager.data.netease.login.PVInfo
import com.lemon.mcdevmanager.data.netease.login.PVResultArgs
import com.lemon.mcdevmanager.data.netease.login.PVResultBean
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import com.orhanobut.logger.Logger
import kotlinx.serialization.encodeToString
import org.bouncycastle.crypto.engines.SM4Engine
import org.bouncycastle.crypto.paddings.PKCS7Padding
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.util.encoders.Hex
import java.net.URLEncoder
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Timer
import java.util.TimerTask
import javax.crypto.Cipher
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

fun vdfCompute(data: PVInfo, onCompleted: (PVResultStrBean) -> Unit) {
    val args = data.args
    val puzzle = args.puzzle
    val x = args.x
    val mod = args.mod
    val t = args.t
    val sid = data.sid

    val maxTime = data.maxTime
    val minTime = data.minTime

    var bigX = x.toBigInteger(16)
    val bigMod = mod.toBigInteger(16)

    val startTime = System.currentTimeMillis()
    var count = 0

    while (true) {
        if (System.currentTimeMillis() - startTime > maxTime) break
        if (System.currentTimeMillis() - startTime > minTime && count > t) break
        bigX = bigX.multiply(bigX).mod(bigMod)
        count++
    }
    val time = (System.currentTimeMillis() - startTime).toInt()
    val spendTime = if (time > maxTime) maxTime else time
    val prePvInfo = PVResultBean(maxTime, puzzle, spendTime, count, sid, PVResultArgs(bigX.toString(16), count))
    val pvInfo = vdfCb(startTime, count, bigX.toString(16), prePvInfo)
    onCompleted(pvInfo)
}

private fun vdfCb(
    startTime: Long,
    count: Int,
    bigX: String,
    data: PVResultBean
): PVResultStrBean {
    val time = System.currentTimeMillis() - startTime
    val signObj = hashMapOf(
        "runTimes" to count,
        "spendTime" to time,
        "t" to count,
        "x" to bigX
    )
    val sortedParams = arrayOf("runTimes", "spendTime", "t", "x")
    val encodedParams = ArrayList<String>()
    for (j in sortedParams.indices) {
        val key = sortedParams[j]
        val value = signObj[key]
        encodedParams.add(
            URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value.toString(), "UTF-8")
        )
    }
    val encodedParamsStr = encodedParams.joinToString("&")
    val sign = powSign(encodedParamsStr, count)
    data.args.sign = sign
    return PVResultStrBean(
        data.maxTime,
        data.puzzle,
        data.spendTime,
        data.runTimes,
        data.sid,
        JSONConverter.encodeToString(data.args)
    )
}

@Suppress("UnstableApiUsage")
private fun powSign(key: String, seed: Int): Int {
    val hasher = Hashing.murmur3_32(seed).newHasher()
    hasher.putString(key, Charsets.UTF_8)
    return hasher.hash().asInt()
}

inline fun <reified T> dataJsonToString(data: T): String {
    return JSONConverter.encodeToString(data).trim()
}

fun getRandomTid(): String {
    val charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    return List(32) { charSet.random(Random) }.joinToString("")
}