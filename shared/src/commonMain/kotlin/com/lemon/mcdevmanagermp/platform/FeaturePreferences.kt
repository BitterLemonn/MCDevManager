package com.lemon.mcdevmanagermp.platform

/**
 * 功能开关持久化（隐藏功能解锁等彩蛋状态）。
 * 各平台 actual 实现：
 *  - Android: SharedPreferences
 *  - JVM:    java.util.prefs.Preferences
 *  - iOS:    NSUserDefaults
 */
expect class FeaturePreferences() {
    fun isPromotionUnlocked(): Boolean
    fun setPromotionUnlocked(unlocked: Boolean)
}
