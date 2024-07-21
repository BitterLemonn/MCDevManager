package com.orhanobut.logger

import android.util.Log

/**
 * LogCat implementation for [LogStrategy]
 *
 * This simply prints out all logs to Logcat by using standard [Log] class.
 */
class LogcatLogStrategy : LogStrategy {
    override fun log(priority: Int, tag: String?, message: String) {
        var tags = tag
        Utils.checkNotNull(message)

        if (tags == null) {
            tags = DEFAULT_TAG
        }

        Log.println(priority, tags, message)
    }

    companion object {
        const val DEFAULT_TAG: String = "NO_TAG"
    }
}
