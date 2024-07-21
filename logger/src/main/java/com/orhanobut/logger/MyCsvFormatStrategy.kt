package com.orhanobut.logger

import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

/**
 * CSV formatted file logging for Android.
 * Writes to CSV the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log level, tag, log message.
 */
class MyCsvFormatStrategy private constructor(builder: Builder) : FormatStrategy {
    private val methodCount = 4

    private val methodOffset = 0

    private val showThreadInfo = true

    private val date: Date
    private val dateFormat: SimpleDateFormat
    private val logStrategy: LogStrategy
    private val tag: String?

    init {
        Utils.checkNotNull(builder)

        date = builder.date!!
        dateFormat = builder.dateFormat!!
        logStrategy = builder.logStrategy!!
        tag = builder.tag
    }

    override fun log(priority: Int, onceOnlyTag: String?, message: String) {
        Utils.checkNotNull(message)

        val tag = formatTag(onceOnlyTag)

        logTopBorder(priority, tag)
        logHeaderContent(priority, tag, methodCount)

        //get bytes of message with system's default charset (which is UTF-8 for Android)
        val bytes = message.toByteArray()
        val length = bytes.size
        if (length <= CHUNK_SIZE) {
            if (methodCount > 0) {
                logDivider(priority, tag)
            }
            logContent(priority, tag, message)
            logBottomBorder(priority, tag)
            return
        }
        if (methodCount > 0) {
            logDivider(priority, tag)
        }
        var i = 0
        while (i < length) {
            val count = min((length - i).toDouble(), CHUNK_SIZE.toDouble())
                .toInt()
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(priority, tag, String(bytes, i, count))
            i += CHUNK_SIZE
        }
        logBottomBorder(priority, tag)
    }


    private fun logBottomBorder(logType: Int, tag: String?) {
        logChunk(logType, tag, BOTTOM_BORDER)
    }


    private fun logContent(logType: Int, tag: String?, chunk: String) {
        Utils.checkNotNull(chunk)

        val lines = chunk.split(System.getProperty("line.separator").toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (line in lines) {
            logChunk(logType, tag, HORIZONTAL_LINE + " " + line)
        }
    }

    private fun logTopBorder(logType: Int, tag: String?) {
        logChunk(logType, tag, TOP_BORDER)
    }

    private fun logChunk(priority: Int, tag: String?, chunk: String) {
        Utils.checkNotNull(chunk)

        logStrategy.log(priority, tag, chunk)
    }

    private fun logDivider(logType: Int, tag: String?) {
        logChunk(logType, tag, MIDDLE_BORDER)
    }


    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private fun getStackOffset(trace: Array<StackTraceElement>): Int {
        Utils.checkNotNull(trace)

        var i = MIN_STACK_OFFSET
        while (i < trace.size) {
            val e = trace[i]
            val name = e.className
            if (name != LoggerPrinter::class.java.name && name != Logger::class.java.name) {
                return --i
            }
            i++
        }
        return -1
    }

    private fun logHeaderContent(logType: Int, tag: String?, methodCount: Int) {
        var methodCount = methodCount
        val trace = Thread.currentThread().stackTrace
        if (showThreadInfo) {
            logChunk(logType, tag, HORIZONTAL_LINE + " Thread: " + Thread.currentThread().name)
            logDivider(logType, tag)
        }
        var level = ""

        val stackOffset = getStackOffset(trace) + methodOffset

        //corresponding method count with the current stack may exceeds the stack trace. Trims the count
        if (methodCount + stackOffset > trace.size) {
            methodCount = trace.size - stackOffset - 1
        }

        for (i in methodCount downTo 1) {
            val stackIndex = i + stackOffset
            if (stackIndex >= trace.size) {
                continue
            }
            val builder = StringBuilder()
            builder.append(HORIZONTAL_LINE)
                .append(' ')
                .append(level)
                .append(getSimpleClassName(trace[stackIndex].className))
                .append(".")
                .append(trace[stackIndex].methodName)
                .append(" ")
                .append(" (")
                .append(trace[stackIndex].fileName)
                .append(":")
                .append(trace[stackIndex].lineNumber)
                .append(")")
            level += "   "
            logChunk(logType, tag, builder.toString())
        }
    }

    private fun getSimpleClassName(name: String): String {
        Utils.checkNotNull(name)

        val lastIndex = name.lastIndexOf(".")
        return name.substring(lastIndex + 1)
    }


    private fun formatTag(tag: String?): String? {
        if (!Utils.isEmpty(tag) && !Utils.equals(this.tag, tag)) {
            return this.tag + "-" + tag
        }
        return this.tag
    }

    class Builder {
        var date: Date? = null
        var dateFormat: SimpleDateFormat? = null
        var logStrategy: LogStrategy? = null
        var tag: String? = "PRETTY_LOGGER"

        fun date(`val`: Date?): Builder {
            date = `val`
            return this
        }

        fun dateFormat(`val`: SimpleDateFormat?): Builder {
            dateFormat = `val`
            return this
        }

        fun logStrategy(`val`: LogStrategy?): Builder {
            logStrategy = `val`
            return this
        }

        fun tag(tag: String?): Builder {
            this.tag = tag
            return this
        }

        fun build(fileName: String?): MyCsvFormatStrategy {
            if (date == null) {
                date = Date()
            }
            if (dateFormat == null) {
                dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)
            }
            if (logStrategy == null) {
                val diskPath = Environment.getExternalStorageDirectory().absolutePath
                val folder = diskPath + File.separatorChar + "logger"

                val ht = HandlerThread("AndroidFileLogger.$folder")
                ht.start()
                val handler: Handler =
                    DiskLogStrategy.WriteHandler(ht.looper, folder, fileName!!, MAX_BYTES)
                logStrategy = DiskLogStrategy(handler)
            }
            return MyCsvFormatStrategy(this)
        }

        companion object {
            private const val MAX_BYTES = 5000 * 1024 // 500K averages to a 4000 lines per file
        }
    }

    companion object {
        private val NEW_LINE: String = System.getProperty("line.separator")
        private const val NEW_LINE_REPLACEMENT = " <br> "
        private const val SEPARATOR = ","

        private const val TOP_LEFT_CORNER = '┌'
        private const val BOTTOM_LEFT_CORNER = "\n└"
        private const val MIDDLE_CORNER = '├'
        private const val HORIZONTAL_LINE = "│\n"
        private const val DOUBLE_DIVIDER = "──────────────────────────\n"
        private const val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n"
        private const val TOP_BORDER = TOP_LEFT_CORNER.toString() + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER
        private const val MIDDLE_BORDER = MIDDLE_CORNER.toString() + SINGLE_DIVIDER + SINGLE_DIVIDER

        private const val CHUNK_SIZE = 4000

        /**
         * The minimum stack trace index, starts at this class after two native calls.
         */
        private const val MIN_STACK_OFFSET = 5

        fun newBuilder(): Builder {
            return Builder()
        }
    }
}
