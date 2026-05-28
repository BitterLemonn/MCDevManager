package com.lemon.mcdevmanagermp.utils.encrpy

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.dto.netease.login.PVResultStrDTO
import com.lemon.mcdevmanagermp.data.vo.netease.login.PVInfoVO
import com.lemon.mcdevmanagermp.platform.rsaEncrypt
import kotlin.random.Random

fun rsaEncrypt(input: String, publicKeyStr: String): String {
    return rsaEncrypt(input, publicKeyStr)
}

fun sm4Encrypt(input: String, key: String): String {
    return SM4.encrypt(input, key)
}

suspend fun vdfAsync(data: PVInfoVO): PVResultStrDTO {
    return computeVDF(data)
}

inline fun <reified T> dataJsonToString(data: T): String {
    return JSONConverter.encodeToString(data).trim()
}

fun getRandomTid(): String {
    val charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    return List(32) { charSet.random(Random) }.joinToString("")
}

fun murmurHash3(key: String, seed: UInt): UInt {
    val c1 = 0xcc9e2d51.toInt()
    val c2 = 0x1b873593
    val r1 = 15
    val r2 = 13
    val m = 5
    val n = 0xe6546b64.toInt()

    var hash = seed.toInt()

    var i = 0
    while (i + 4 <= key.length) {
        var k1 =
            (key[i + 3].code shl 24) or (key[i + 2].code shl 16) or (key[i + 1].code shl 8) or key[i].code
        i += 4

        k1 *= c1
        k1 = (k1 shl r1) or (k1 ushr (32 - r1))
        k1 *= c2

        hash = hash xor k1
        hash = (hash shl r2) or (hash ushr (32 - r2))
        hash = hash * m + n
    }

    if (i < key.length) {
        var k2 = 0
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
