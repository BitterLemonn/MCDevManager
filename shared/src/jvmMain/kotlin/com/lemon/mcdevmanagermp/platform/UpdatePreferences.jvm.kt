package com.lemon.mcdevmanagermp.platform

import java.util.prefs.Preferences

actual class UpdatePreferences actual constructor() {

    private val prefs: Preferences by lazy {
        Preferences.userNodeForPackage(UpdatePreferences::class.java)
    }

    actual fun getIgnoredVersion(): String? {
        return prefs.get(KEY_IGNORED_VERSION, null)
    }

    actual fun setIgnoredVersion(version: String) {
        prefs.put(KEY_IGNORED_VERSION, version)
        prefs.flush()
    }

    companion object {
        private const val KEY_IGNORED_VERSION = "ignored_version"
    }
}
