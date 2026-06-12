package com.lemon.mcdevmanagermp.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.lemon.mcdevmanagermp.utils.Logger
import java.io.File

actual class DownloadNotifier actual constructor() {

    private val context = AndroidLogContext.getContext()
    private val notificationManager =
        context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val CHANNEL_NAME = "下载通知"
        private const val NOTIFICATION_ID = 1
    }

    actual fun startNotification(title: String) {
        if (context == null || notificationManager == null) return
        createChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("正在准备下载...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, 0, false)
            .setOngoing(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
        Logger.d("下载通知: 开始")
    }

    actual fun updateProgress(progress: Int, downloadedBytes: Long, totalBytes: Long) {
        if (context == null || notificationManager == null) return
        val downloadedText = formatFileSize(downloadedBytes)
        val totalText = if (totalBytes > 0) formatFileSize(totalBytes) else "未知"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("正在下载更新")
            .setContentText("$downloadedText / $totalText ($progress%)")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    actual fun finishNotification(success: Boolean, filePath: String?) {
        if (context == null || notificationManager == null) return
        if (success && filePath != null) {
            val file = File(filePath)
            val installIntent = createInstallIntent(file)
            val pendingIntent = if (installIntent != null) {
                PendingIntent.getActivity(
                    context, 0, installIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else null

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("下载完成")
                .setContentText("点击安装更新")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setOngoing(false)

            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent)
            }

            notificationManager.notify(NOTIFICATION_ID, builder.build())
            Logger.d("下载通知: 完成")
        } else {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("下载失败")
                .setContentText("请重试")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
            notificationManager.notify(NOTIFICATION_ID, notification)
            Logger.d("下载通知: 失败")
        }
    }

    actual fun cancelNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "下载进度通知"
            setShowBadge(false)
        }
        notificationManager?.createNotificationChannel(channel)
    }

    private fun createInstallIntent(file: File): Intent? {
        if (!file.exists() || !file.name.endsWith(".apk")) return null
        return try {
            val uri = FileProvider.getUriForFile(
                context!!,
                "${context.packageName}.fileprovider",
                file
            )
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            Logger.e("创建安装 Intent 失败: ${e.message}", e)
            null
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
            bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}
