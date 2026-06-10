package com.lemon.mcdevmanagermp.platform

import android.content.Intent
import androidx.core.net.toUri

actual fun openUrl(url: String) {
    val context = AndroidLogContext.getContext() ?: return
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
