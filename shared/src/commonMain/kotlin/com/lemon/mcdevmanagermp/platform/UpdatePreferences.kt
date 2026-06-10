package com.lemon.mcdevmanagermp.platform

/**
 * 更新偏好设置持久化接口。
 * 各平台需通过 actual 提供实现：
 *  - Android: SharedPreferences
 *  - JVM:    java.util.prefs.Preferences
 *  - iOS:    NSUserDefaults
 */
expect class UpdatePreferences() {
    fun getIgnoredVersion(): String?
    fun setIgnoredVersion(version: String)
}
