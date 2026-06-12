package com.lemon.mcdevmanagermp.platform

import android.content.Context
import androidx.core.content.edit
import java.lang.ref.WeakReference

actual class UpdatePreferences actual constructor() {

    private val prefs by lazy {
        val context = prefsContext
            ?: throw IllegalStateException("请先调用 UpdatePreferences.init(context) 初始化")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun getIgnoredVersion(): String? {
        return prefs.getString(KEY_IGNORED_VERSION, null)
    }

    actual fun setIgnoredVersion(version: String) {
        prefs.edit { putString(KEY_IGNORED_VERSION, version) }
    }

    companion object {
        private const val PREFS_NAME = "mc_dev_update"
        private const val KEY_IGNORED_VERSION = "ignored_version"

        private var contextRef: WeakReference<Context>? = null

        private val prefsContext: Context?
            get() = contextRef?.get()

        fun init(context: Context) {
            contextRef = WeakReference(context.applicationContext)
        }
    }
}
