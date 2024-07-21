package com.orhanobut.logger

import com.orhanobut.logger.Logger.ASSERT
import com.orhanobut.logger.Logger.DEBUG
import com.orhanobut.logger.Logger.ERROR
import com.orhanobut.logger.Logger.INFO
import com.orhanobut.logger.Logger.VERBOSE
import com.orhanobut.logger.Logger.WARN
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

/**
 * Provides convenient methods to some common operations
 */
internal object Utils {
    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.length == 0
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     *
     * *Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.*
     *
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     *
     *
     * NOTE: Logic slightly change due to strict policy on CI -
     * "Inner assignments should be avoided"
     */
    fun equals(a: CharSequence?, b: CharSequence?): Boolean {
        if (a === b) return true
        if (a != null && b != null) {
            val length = a.length
            if (length == b.length) {
                if (a is String && b is String) {
                    return a == b
                } else {
                    for (i in 0 until length) {
                        if (a[i] != b[i]) return false
                    }
                    return true
                }
            }
        }
        return false
    }

    /**
     * Copied from "android.util.Log.getStackTraceString()" in order to avoid usage of Android stack
     * in unit tests.
     *
     * @return Stack trace in form of String
     */
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun logLevel(value: Int): String {
        return when (value) {
            VERBOSE -> "VERBOSE"
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            WARN -> "WARN"
            ERROR -> "ERROR"
            ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    fun toString(`object`: Any?): String {
        if (`object` == null) {
            return "null"
        }
        if (!`object`.javaClass.isArray) {
            return `object`.toString()
        }
        if (`object` is BooleanArray) {
            return `object`.contentToString()
        }
        if (`object` is ByteArray) {
            return `object`.contentToString()
        }
        if (`object` is CharArray) {
            return `object`.contentToString()
        }
        if (`object` is ShortArray) {
            return `object`.contentToString()
        }
        if (`object` is IntArray) {
            return `object`.contentToString()
        }
        if (`object` is LongArray) {
            return `object`.contentToString()
        }
        if (`object` is FloatArray) {
            return `object`.contentToString()
        }
        if (`object` is DoubleArray) {
            return `object`.contentToString()
        }
        if (`object` is Array<*> && `object`.isArrayOf<Any>()) {
            return (`object` as Array<Any?>).contentDeepToString()
        }
        return "Couldn't find a correct type for the object"
    }

    fun <T> checkNotNull(obj: T?): T {
        if (obj == null) {
            throw NullPointerException()
        }
        return obj
    }
}
