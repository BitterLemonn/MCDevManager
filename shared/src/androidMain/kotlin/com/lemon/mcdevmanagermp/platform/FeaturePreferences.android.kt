package com.lemon.mcdevmanagermp.platform

import android.content.Context
import androidx.core.content.edit

actual class FeaturePreferences actual constructor() {

    private val prefs by lazy {
        val context = AndroidLogContext.getContext()
            ?: throw IllegalStateException("请先调用 AndroidLogContext.setContext(context)")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun isPromotionUnlocked(): Boolean =
        prefs.getBoolean(KEY_PROMOTION_UNLOCKED, false)

    actual fun setPromotionUnlocked(unlocked: Boolean) {
        prefs.edit { putBoolean(KEY_PROMOTION_UNLOCKED, unlocked) }
    }

    companion object {
        private const val PREFS_NAME = "mc_dev_feature"
        private const val KEY_PROMOTION_UNLOCKED = "promotion_unlocked"
    }
}
