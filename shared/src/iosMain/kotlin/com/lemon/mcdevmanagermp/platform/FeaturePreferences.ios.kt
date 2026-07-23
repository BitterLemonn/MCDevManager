package com.lemon.mcdevmanagermp.platform

import platform.Foundation.NSUserDefaults

actual class FeaturePreferences actual constructor() {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun isPromotionUnlocked(): Boolean =
        defaults.boolForKey(KEY_PROMOTION_UNLOCKED)

    actual fun setPromotionUnlocked(unlocked: Boolean) {
        defaults.setBool(unlocked, forKey = KEY_PROMOTION_UNLOCKED)
        defaults.synchronize()
    }

    companion object {
        private const val KEY_PROMOTION_UNLOCKED = "promotion_unlocked"
    }
}
