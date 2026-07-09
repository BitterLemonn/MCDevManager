package com.lemon.mcdevmanagermp.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual fun copyTextToClipboard(text: String) {
    val context = AndroidLogContext.getContext() ?: return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("", text))
}
