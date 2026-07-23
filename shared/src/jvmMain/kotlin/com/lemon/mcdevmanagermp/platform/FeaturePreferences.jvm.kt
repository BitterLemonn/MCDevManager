package com.lemon.mcdevmanagermp.platform

import java.util.prefs.Preferences

actual class FeaturePreferences actual constructor() {

    private val prefs: Preferences by lazy {
        Preferences.userNodeForPackage(FeaturePreferences::class.java)
    }

    actual fun isPromotionUnlocked(): Boolean =
        prefs.getBoolean(KEY_PROMOTION_UNLOCKED, false)

    actual fun setPromotionUnlocked(unlocked: Boolean) {
        prefs.putBoolean(KEY_PROMOTION_UNLOCKED, unlocked)
        prefs.flush()
    }

    companion object {
        private const val KEY_PROMOTION_UNLOCKED = "promotion_unlocked"
    }
}
