package com.orhanobut.logger

import android.os.Handler
import android.os.HandlerThread
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV formatted file logging for Android.
 * Writes to CSV the following data:
 * epoch timestamp, ISO8601 timestamp (human-readable), log level, tag, log message.
 */
class CsvFormatStrategy private constructor(builder: Builder) : FormatStrategy {
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
        var messageLog = message
        Utils.checkNotNull(messageLog)

        val tag = formatTag(onceOnlyTag)

        date.time = System.currentTimeMillis()

        val builder = StringBuilder()

        // machine-readable date/time
        builder.append(date.time.toString())

        // human-readable date/time
        builder.append(SEPARATOR)
        builder.append(dateFormat.format(date))

        // level
        builder.append(SEPARATOR)
        builder.append(Utils.logLevel(priority))

        // tag
        builder.append(SEPARATOR)
        builder.append(tag)

        // message
        if (messageLog.contains(NEW_LINE)) {
            // a new line would break the CSV format, so we replace it here
            messageLog = messageLog.replace(NEW_LINE.toRegex(), NEW_LINE_REPLACEMENT)
        }
        builder.append(SEPARATOR)
        builder.append(messageLog)

        // new line
        builder.append(NEW_LINE)

        logStrategy.log(priority, tag, builder.toString())
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

        fun build(fileName: String?, diskPath: String): CsvFormatStrategy {
            if (date == null) {
                date = Date()
            }
            if (dateFormat == null) {
                dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.CHINA)
            }
            if (logStrategy == null) {
                val ht = HandlerThread("AndroidFileLogger.$diskPath")
                ht.start()
                val handler: Handler =
                    DiskLogStrategy.WriteHandler(ht.looper, diskPath, fileName!!, MAX_BYTES)
                logStrategy = DiskLogStrategy(handler)
            }
            return CsvFormatStrategy(this)
        }

        companion object {
            private const val MAX_BYTES = 5000 * 1024 // 500K averages to a 4000 lines per file
        }
    }

    companion object {
        private val NEW_LINE: String = System.lineSeparator()
        private val NEW_LINE_REPLACEMENT: String = System.lineSeparator()
        private const val SEPARATOR = ","

        fun newBuilder(): Builder {
            return Builder()
        }
    }
}
