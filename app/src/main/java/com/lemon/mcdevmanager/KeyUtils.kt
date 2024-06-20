@file:Suppress("UNREACHABLE_CODE")

package com.lemon.mcdevmanager

import android.util.Log
import com.lemon.mcdevmanager.data.common.base64Key
import java.math.BigInteger
import kotlin.math.pow
import kotlin.random.Random

fun base64Parse(e: String): List<Any>? {
    var isError = false
    if (e.isEmpty()) {
        isError = true
        return null
    }
    val t = mutableListOf<Any>()
    var eCopy = e
    while (eCopy.isNotEmpty()) {
        val n = eCopy[0].code
        eCopy = eCopy.substring(1)
        var i = 0
        if (5 == (31 and n))
            eCopy = eCopy.substring(1)
        else if (128 and eCopy[0].code != 0) {
            val r = 127 and eCopy[0].code
            eCopy = eCopy.substring(1)
            if (r > 0)
                i = eCopy[0].code
            if (r > 1)
                i = i shl 8 or eCopy[1].code
            if (r > 2) {
                isError = true
                return null
            }
            eCopy = eCopy.substring(r)
        } else {
            i = eCopy[0].code
            eCopy = eCopy.substring(1)
        }
        var a = ""
        if (i > 0) {
            if (i > eCopy.length) {
                isError = true
                return null
            }
            a = eCopy.substring(0, i)
            eCopy = eCopy.substring(i)
        }
        if (32 and n != 0)
            t.add(base64Parse(a) ?: return null)
        else
            t.add(base64Value((if (n and 128 != 0) 4 else n and 31), a) ?: return null)
    }
    return t
}

fun base64Value(e: Int, t: String): Any? {
    when (e) {
        1 -> return t.isNotEmpty()
        2 -> return t
        3 -> return base64Parse(t.substring(1))
        5 -> return null
        6 -> {
            val n = mutableListOf<Int>()
            val i = t[0].code
            n.add(kotlin.math.floor(i / 40.0).toInt())
            n.add(i - 40 * n[0])
            val r = mutableListOf<Int>()
            var a = 0
            for (s in 1 until t.length) {
                val o = t[s].code
                r.add(127 and o)
                if (128 and o != 0)
                    a++
                else {
                    var l = 0
                    for (c in 0 until r.size)
                        l += r[c] * 128.0.pow(a--).toInt()
                    n.add(l)
                    a = 0
                    r.clear()
                }
            }
            return n.joinToString(".")
        }

        else -> return null
    }
}


class RSA {
    fun getPublicKey(e: String): RSAPublicKey? {
        val eASN1 = ASN1Data(Base64Utils.decode(e))
        if (eASN1.error)
            return null
        val eData = eASN1.data
        return if (eData != null){
            val t1 = eData[0] as List<*>
            val t2 = t1[0] as List<*>
            val t3 = t2[0] as String
            if (t3 == "1.2.840.113549.1.1.1"){
                val t4 = t1[1] as List<*>
                val t5 = t4[0] as List<*>
                val modulus = BigInteger(HexUtils.encode(t5[0] as String), 16)
                val encryptionExponent = BigInteger(HexUtils.encode(t5[1] as String), 16)
                RSAPublicKey(modulus, encryptionExponent)
            } else null
        } else null
    }

    fun encrypt(e: String, t: RSAPublicKey?): String? {
        if (t == null)
            return null
        val n = t.modulus.bitLength() + 7 shr 3
        var eCopy = pkcs1pad2(e, n) ?: return null
        eCopy = eCopy.modPow(t.encryptionExponent, t.modulus) ?: return null
        var eStr = eCopy.toString(16)
        while (eStr.length < 2 * n) {
            eStr = "0$eStr"
        }
        return Base64Utils.encode(HexUtils.decode(eStr))
    }

    fun decrypt(e: String) {
        val t = BigInteger(e, 16)
    }

    fun pkcs1pad2(e: String, t: Int): BigInteger? {
        var tempT = t
        if (tempT < e.length + 11)
            return null
        val n = MutableList(tempT) { 0 }
        var i = e.length - 1
        while (i >= 0 && tempT > 0)
            n[--tempT] = e[i--].code

        n[--tempT] = 0
        while (tempT > 2)
            n[--tempT] = (254 * Random.nextDouble()).toInt() + 1

        n[--tempT] = 2
        n[--tempT] = 0
        return BigInteger(n.map { it.toByte() }.toByteArray())
    }
}

class RSAPublicKey(val modulus: BigInteger, val encryptionExponent: BigInteger)

class ASN1Data(val e: String) {
    var error = false
    var data: List<Any>? = parse(e)

    private fun parse(e: String): List<Any>? {
        if (e.isEmpty()) {
            error = true
            return null
        }
        val t = mutableListOf<Any>()
        var eCopy = e
        while (eCopy.isNotEmpty()) {
            val n = eCopy[0].code
            eCopy = eCopy.substring(1)
            var i = 0
            if (5 == (31 and n))
                eCopy = eCopy.substring(1)
            else if (128 and eCopy[0].code != 0) {
                val r = 127 and eCopy[0].code
                eCopy = eCopy.substring(1)
                if (r > 0)
                    i = eCopy[0].code
                if (r > 1)
                    i = i shl 8 or eCopy[1].code
                if (r > 2) {
                    error = true
                    return null
                }
                eCopy = eCopy.substring(r)
            } else {
                i = eCopy[0].code
                eCopy = eCopy.substring(1)
            }
            var a = ""
            if (i > 0) {
                if (i > eCopy.length) {
                    error = true
                    return null
                }
                a = eCopy.substring(0, i)
                eCopy = eCopy.substring(i)
            }
            if (32 and n != 0)
                t.add(parse(a) ?: return null)
            else
                t.add(value((if (n and 128 != 0) 4 else n and 31), a) ?: return null)
        }
        return t
    }

    private fun value(e: Int, t: String): Any? {
        when (e) {
            1 -> return t.isNotEmpty()
            2 -> return t
            3 -> return parse(t.substring(1))
            5 -> return null
            6 -> {
                val n = mutableListOf<Int>()
                val i = t[0].code
                n.add(kotlin.math.floor(i / 40.0).toInt())
                n.add(i - 40 * n[0])
                val r = mutableListOf<Int>()
                var a = 0
                for (s in 1 until t.length) {
                    val o = t[s].code
                    r.add(127 and o)
                    if (128 and o != 0)
                        a++
                    else {
                        var l = 0
                        for (c in 0 until r.size)
                            l += r[c] * 128.0.pow(a--).toInt()
                        n.add(l)
                        a = 0
                        r.clear()
                    }
                }
                return n.joinToString(".")
            }

            else -> return null
        }
    }

}


object Base64Utils {
    private const val base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

    fun encode(e: String): String {
        if (e.isEmpty()) return ""
        var t = ""
        var l = 0
        do {
            val n = e[l++].code
            val i = e.getOrNull(l++)?.code ?: -1
            val r = e.getOrNull(l++)?.code ?: -1
            val a = n shr 2
            val s = (3 and n) shl 4 or (i shr 4)

            val o = if (i == -1) 64 else (15 and i) shl 2 or (r shr 6)
            val c = if (r == -1 || i == -1) 64 else 63 and r

            t += "${base64[a]}${base64[s]}${base64[o]}${base64[c]}"
        } while (l < e.length)
        return t
    }

    fun decode(needDecodeKey: String): String {
        var result = ""
        var n: Int
        var i: Int
        var r: Int
        var a: Int
        var index = 0
        do {
            n = base64Key.indexOf(needDecodeKey[index++])
            i = base64Key.indexOf(needDecodeKey[index++])
            r = base64Key.indexOf(needDecodeKey[index++])
            a = base64Key.indexOf(needDecodeKey[index++])
            result += (n shl 2 or (i shr 4)).toChar()
            if (64 != r)
                result += ((15 and i) shl 4 or (r shr 2)).toChar()
            if (64 != a)
                result += ((3 and r) shl 6 or a).toChar()
        } while (index < needDecodeKey.length)
        return result
    }
}

object HexUtils {
    private const val hex = "0123456789abcdef"

    fun encode(e: String): String {
        var t = ""
        var n: Int
        var i = 0
        do {
            n = e[i++].code
            t += "${hex[n shr 4 and 15]}${hex[15 and n]}"
        } while (i < e.length)
        return t
    }

    fun decode(e: String): String {
        val eClean = e.replace(Regex("[^0-9abcdef]"), "")
        var t = ""
        var n = 0
        do {
            t += (hex.indexOf(eClean[n++]) shl 4 and 240 or 15 and hex.indexOf(eClean[n++])).toChar()
        } while (n < eClean.length)
        return t
    }
}