package com.orhanobut.logger

/**
 * Used to determine how messages should be printed or saved.
 *
 * @see PrettyFormatStrategy
 *
 * @see CsvFormatStrategy
 */
interface FormatStrategy {
    fun log(priority: Int, tag: String?, message: String)
}
