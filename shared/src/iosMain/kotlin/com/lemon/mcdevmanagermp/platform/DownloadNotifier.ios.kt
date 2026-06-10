package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

actual class DownloadNotifier actual constructor() {

    companion object {
        private const val DOWNLOAD_CATEGORY = "DOWNLOAD_CATEGORY"
        private const val NOTIFICATION_ID = "download_notification"
    }

    actual fun startNotification(title: String) {
        @OptIn(ExperimentalForeignApi::class)
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = platform.UserNotifications.UNAuthorizationOptionAlert or
                    platform.UserNotifications.UNAuthorizationOptionSound
        ) { granted, error ->
            if (granted) {
                Logger.d("iOS 通知权限已授权")
            } else {
                Logger.d("iOS 通知权限被拒绝: ${error?.localizedDescription}")
            }
        }
        sendNotification("正在准备下载...", "下载更新中")
        Logger.d("下载通知: 开始")
    }

    actual fun updateProgress(progress: Int, downloadedBytes: Long, totalBytes: Long) {
        val downloadedText = formatFileSize(downloadedBytes)
        val totalText = if (totalBytes > 0) formatFileSize(totalBytes) else "未知"
        sendNotification("正在下载更新 $progress%", "$downloadedText / $totalText")
    }

    actual fun finishNotification(success: Boolean, filePath: String?) {
        if (success) {
            sendNotification("下载完成", "应用将自动安装更新")
            Logger.d("下载通知: 完成")
        } else {
            sendNotification("下载失败", "请重试")
            Logger.d("下载通知: 失败")
        }
    }

    actual fun cancelNotification() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(NOTIFICATION_ID))
    }

    private fun sendNotification(title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = NOTIFICATION_ID,
            content = content,
            trigger = null
        )
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                Logger.e("iOS 通知发送失败: ${error.localizedDescription}")
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000 -> "${(bytes / 100_000) / 10.0} MB"
            bytes >= 1_000 -> "${(bytes / 100) / 10.0} KB"
            else -> "$bytes B"
        }
    }
}
