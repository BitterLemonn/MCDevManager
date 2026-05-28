package com.lemon.mcdevmanagermp.utils.encrpy

import com.lemon.mcdevmanagermp.data.dto.netease.login.PVResultStrDTO
import com.lemon.mcdevmanagermp.data.vo.netease.login.PVInfoVO
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.Clock

// VDF (Verifiable Delay Function) 验证计算
internal suspend fun computeVDF(data: PVInfoVO): PVResultStrDTO =
    withContext(Dispatchers.IO) {
        val puzzle = data.args.puzzle
        val modulus = BigInteger.parseString(data.args.mod, 16)
        var x = BigInteger.parseString(data.args.x, 16)
        val t = data.args.t
        val startTime = Clock.System.now().toEpochMilliseconds()
        var count = 0

        while (count < t || Clock.System.now().toEpochMilliseconds() - startTime < data.minTime) {
            x = (x * x) mod modulus
            count++
            val nowTime = Clock.System.now().toEpochMilliseconds()
            if (nowTime - startTime > data.maxTime) {
                break
            }
        }

        val time = Clock.System.now().toEpochMilliseconds() - startTime
        val signObj = mapOf(
            "runTimes" to count.toUInt(),
            "spendTime" to time.toUInt(),
            "t" to count.toUInt(),
            "x" to x.toString(16)
        )

        val sortedParams = listOf("runTimes", "spendTime", "t", "x")
        val encodedParams = sortedParams.joinToString("&") { key ->
            val value = signObj[key].toString()
            "${encodeUrl(key)}=${encodeUrl(value)}"
        }

        val sign = murmurHash3(encodedParams, count.toUInt())

        PVResultStrDTO(
            maxTime = data.maxTime,
            puzzle = puzzle,
            spendTime = time.toInt(),
            runTimes = count,
            sid = data.sid,
            args = """{"x":"${x.toString(16)}","t":$count,"sign":"$sign"}"""
        )
    }

private fun encodeUrl(s: String): String = buildString {
    for (ch in s) {
        if (ch in '0'..'9' || ch in 'a'..'z' || ch in 'A'..'Z'
            || ch == '-' || ch == '_' || ch == '.' || ch == '~'
        ) {
            append(ch)
        } else {
            append('%')
            append(ch.code.toString(16).uppercase().padStart(2, '0'))
        }
    }
}
