package com.lemon.mcdevmanagermp.utils.extension

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.Instant

fun Typography.applyDefaultFont(fontFamily: FontFamily): Typography {
    return this.copy(
        displayLarge = this.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = this.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = this.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = this.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = this.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = this.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = this.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = this.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = this.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = this.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = this.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = this.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = this.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = this.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = this.labelSmall.copy(fontFamily = fontFamily)
    )
}

fun String.dumpAndGetCookiesValue(key: String): String? {
    val cookies = this.split(";")
    for (cookie in cookies) {
        val separator = cookie.indexOf('=')
        if (separator > 0 && cookie.substring(0, separator).trim() == key) {
            return cookie.substring(separator + 1).trim()
        }
    }
    return null
}

fun String.isValidCookiesStr(): Boolean {
    return this.isNotEmpty() && this.contains("=") && this.contains(";")
}

fun Double.formatDecimal(digits: Int = 1): String {
    if (digits < 0) return this.toString()
    if (digits == 0) return this.roundToLong().toString()

    // 1. 计算倍率，例如保留2位就是乘100
    // 使用 Long 避免大数精度丢失
    val factor = 10.0.pow(digits)

    // 2. 四舍五入
    val scaled = (this * factor).roundToLong()

    // 3. 分离整数和小数部分
    val intPart = scaled / factor.toLong()
    val fractionPart = abs(scaled % factor.toLong())

    // 4. 拼接字符串，小数部分需要补零 (例如 1.5 -> 1.50)
    // padStart 是 Kotlin 标准库函数，跨平台通用
    return "$intPart.${fractionPart.toString().padStart(digits, '0')}"
}

fun Float.formatDecimal(digits: Int = 1): String = this.toDouble().formatDecimal(digits)

private val timeZoneCN = TimeZone.of("Asia/Shanghai")

/**
 * 将后端时间字符串格式化为可读形式。
 *
 * 解析顺序：ISO8601 带时区（Instant） → 本地日期时间（yyyy-MM-ddTHH:mm:ss） →
 * 纯日期（yyyy-MM-dd） → 毫秒时间戳。解析失败时原样返回。
 *
 * 输出规则（基于 Asia/Shanghai 时区）：
 * - 1 分钟内：刚刚
 * - 60 分钟内：N 分钟前
 * - 当天内：N 小时前
 * - 昨天：昨天 HH:mm
 * - 本年内：MM-dd HH:mm
 * - 跨年：yyyy-MM-dd
 */
fun String.toReadableTime(): String {
    if (isBlank()) return this
    val (targetEpochMillis, isTimestamp) = parseToEpochMillisOrNull(this) ?: return this

    val zone = timeZoneCN
    val nowEpochMillis = Clock.System.now().toEpochMilliseconds()
    val targetInstant = Instant.fromEpochMilliseconds(targetEpochMillis)
    val targetLocal = targetInstant.toLocalDateTime(zone)
    val nowLocal = Instant.fromEpochMilliseconds(nowEpochMillis).toLocalDateTime(zone)

    // 时间戳（秒/毫秒）不显示相对时间，直接给绝对时间，避免与系统时钟错位
    if (isTimestamp) {
        return formatDateRelative(targetLocal, nowLocal)
    }

    val diffMillis = nowEpochMillis - targetEpochMillis
    // 未来时间直接显示绝对时间
    if (diffMillis < 0) return formatDateRelative(targetLocal, nowLocal)

    val diffMinutes = diffMillis / 60_000
    val diffHours = diffMinutes / 60

    val isSameDay = targetLocal.date == nowLocal.date
    val isYesterday = targetLocal.date == nowLocal.date.minus(1, DateTimeUnit.DAY)

    return when {
        isSameDay && diffMinutes < 1 -> "刚刚"
        isSameDay && diffMinutes < 60 -> "$diffMinutes 分钟前"
        isSameDay -> "$diffHours 小时前"
        isYesterday -> "昨天 ${pad2(targetLocal.hour)}:${pad2(targetLocal.minute)}"
        else -> formatDateRelative(targetLocal, nowLocal)
    }
}

/** 绝对时间：本年内 MM-dd HH:mm，跨年 yyyy-MM-dd */
private fun formatDateRelative(target: LocalDateTime, now: LocalDateTime): String {
    val month = target.month.number
    val day = target.day
    return if (target.year == now.year) {
        "${pad2(month)}-${pad2(day)} ${pad2(target.hour)}:${pad2(target.minute)}"
    } else {
        "${target.year}-${pad2(month)}-${pad2(day)}"
    }
}

private fun pad2(v: Int): String = v.toString().padStart(2, '0')

/** Unix 秒/毫秒 → "yyyy-MM-dd"（Asia/Shanghai）；0 或解析失败返回 ""。 */
fun Long.toDateString(): String {
    if (this == 0L) return ""
    return try {
        val millis = if (this < 1_000_000_000_000) this * 1000 else this
        val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(timeZoneCN)
        "${dt.year}-${pad2(dt.month.number)}-${pad2(dt.day)}"
    } catch (e: Exception) {
        Logger.e("解析DateString失败: $e")
        ""
    }
}

/** Unix 秒 → "yyyy/MM/dd HH:mm"（系统时区）；解析失败返回 ""。 */
fun Long.toDateTimeString(): String {
    return try {
        val dt = Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.year}/${pad2(dt.month.number)}/${pad2(dt.day)} ${pad2(dt.hour)}:${pad2(dt.minute)}"
    } catch (e: Exception) {
        Logger.e("解析DateTimeString失败: $e")
        ""
    }
}

/**
 * 尝试把字符串解析为 epoch 毫秒。返回 (epochMillis, isTimestamp) 或 null。
 * isTimestamp=true 表示源是数字时间戳（已按系统时区还原）。
 */
private fun parseToEpochMillisOrNull(raw: String): Pair<Long, Boolean>? {
    val s = raw.trim()
    // 1. ISO8601（Instant.parse 接受 2024-01-15T10:30:00Z / 带偏移）
    runCatching { return Instant.parse(s).toEpochMilliseconds() to false }
    // 2. 本地日期时间 2024-01-15T10:30:00[.sss]（无时区，按 Asia/Shanghai 还原）
    runCatching {
        return LocalDateTime.parse(s).toInstant(timeZoneCN).toEpochMilliseconds() to false
    }
    // 3. 纯日期 yyyy-MM-dd（按当天 00:00:00 Asia/Shanghai）
    runCatching {
        return LocalDate.parse(s).atStartOfDayIn(timeZoneCN).toEpochMilliseconds() to false
    }
    // 4. 数字时间戳（秒 10 位 / 毫秒 13 位）
    s.toLongOrNull()?.let { ts ->
        val millis = when {
            s.length == 10 -> ts * 1000
            s.length == 13 -> ts
            else -> return null
        }
        runCatching { return millis to true }
    }
    return null
}