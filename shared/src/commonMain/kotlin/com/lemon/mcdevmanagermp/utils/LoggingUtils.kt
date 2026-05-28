package com.lemon.mcdevmanagermp.utils

import com.lemon.mcdevmanagermp.platform.getLogDirectory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import kotlin.time.Clock

object Logger {
    const val LOG_LEVEL = "DEBUG"
    private val logLevel = when (LOG_LEVEL) {
        "DEBUG" -> 0
        "INFO" -> 1
        "WARN" -> 2
        "ERROR" -> 3
        else -> 0
    }

    private val logger = KotlinLogging.logger {}
    private val logDir by lazy { getLogDirectory().toPath() }

    private const val MAX_LOG_FILE_SIZE = 5 * 1024 * 1024L // 5MB
    private var currentLogDate: LocalDate? = null
    private var currentLogIndex: Int = 0
    private var currentLogFile: Path? = null

    fun i(message: String) {
        info(message)
    }

    fun w(message: String) {
        warn(message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        error(message, throwable)
    }

    fun d(message: String) {
        debug(message)
    }

    fun info(message: String) {
        if (logLevel > 1) return
        logger.info { message }
        writeLog("INFO", message)
    }

    fun warn(message: String) {
        logger.warn { message }
        writeLog("WARN", message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        logger.error(throwable) { message }
        writeLog("ERROR", "$message ${throwable?.stackTraceToString() ?: ""}")
    }

    fun debug(message: String) {
        if (logLevel > 0) return
        logger.debug { message }
        writeLog("DEBUG", message)
    }

    private fun getLogFile(): Path {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        // 如果日期变更，重置索引和文件
        if (currentLogDate != today) {
            currentLogDate = today
            currentLogIndex = 0
            currentLogFile = null
        }

        // 检查当前文件是否已满
        var file = currentLogFile
        if (file != null) {
            if (FileSystem.SYSTEM.exists(file)) {
                val size = try {
                    FileSystem.SYSTEM.metadata(file).size ?: 0L
                } catch (e: Exception) {
                    0L
                }
                if (size >= MAX_LOG_FILE_SIZE) {
                    currentLogIndex++
                    currentLogFile = null
                }
            } else {
                currentLogFile = null
            }
        }

        // 查找可用的日志文件
        if (currentLogFile == null) {
            while (true) {
                val fileName = if (currentLogIndex == 0) {
                    "app-$today.log"
                } else {
                    "app-$today-$currentLogIndex.log"
                }
                val candidate = logDir.resolve(fileName)

                if (!FileSystem.SYSTEM.exists(candidate)) {
                    currentLogFile = candidate
                    break
                }

                val size = try {
                    FileSystem.SYSTEM.metadata(candidate).size ?: 0L
                } catch (e: Exception) {
                    0L
                }

                if (size < MAX_LOG_FILE_SIZE) {
                    currentLogFile = candidate
                    break
                }

                currentLogIndex++
            }
        }
        return currentLogFile!!
    }

    private fun writeLog(level: String, message: String) {
        try {
            val currentMoment = Clock.System.now()
            val datetime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
            val logEntry = "$datetime [$level] $message\n"

            val file = getLogFile()
            FileSystem.SYSTEM.appendingSink(file).buffer().use { sink ->
                sink.writeUtf8(logEntry)
            }
        } catch (e: Exception) {
            println("无法写入日志文件: ${e.message}")
        }
    }

    fun autoDeleteOldLogs(days: Int = 3) {
        try {
            val cutoffDate = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .minus(days, DateTimeUnit.DAY)

            FileSystem.SYSTEM.list(logDir).forEach { path ->
                val fileName = path.name
                val regex = Regex("""app-(\d{4}-\d{2}-\d{2})(-\d+)?\.log""")
                val matchResult = regex.find(fileName)
                if (matchResult != null) {
                    val dateString = matchResult.groupValues[1]
                    val fileDate = LocalDate.parse(dateString)
                    if (fileDate < cutoffDate) {
                        FileSystem.SYSTEM.delete(path)
                    }
                }
            }
        } catch (e: Exception) {
            println("无法删除旧日志文件: ${e.message}")
        }
    }
}
