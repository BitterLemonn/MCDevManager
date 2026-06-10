package com.lemon.mcdevmanagermp.platform

import platform.Foundation.NSUserDefaults

actual class UpdatePreferences actual constructor() {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getIgnoredVersion(): String? {
        return defaults.stringForKey(KEY_IGNORED_VERSION)
    }

    actual fun setIgnoredVersion(version: String) {
        defaults.setObject(version, forKey = KEY_IGNORED_VERSION)
        defaults.synchronize()
    }

    companion object {
        private const val KEY_IGNORED_VERSION = "ignored_version"
    }
}
