package com.lemon.mcdevmanager.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.repository.UpdateRepository
import com.lemon.mcdevmanager.utils.copyFileToDownloadFolder
import com.orhanobut.logger.Logger
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


class DownloadService : Service() {
    private val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
    private val DOWNLOAD_NOTIFICATION_ID = 1

    private val TIPS_NOTIFICATION_CHANNEL_ID = "tips_channel"
    private val TIPS_NOTIFICATION_ID = 2

    private val repository = UpdateRepository.getInstance()
    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadUrl = intent?.getStringExtra("url")
        val targetPath = intent?.getStringExtra("targetPath")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createDownloadNotificationChannel()
        createTipsNotificationChannel()
        startForeground(DOWNLOAD_NOTIFICATION_ID, createDownloadNotification(0, 0))
        if (downloadUrl != null && targetPath != null) {
            Thread { downloadFile(downloadUrl, targetPath) }.start()
        } else {
            sendNotification("下载失败, 无法获取下载链接或目标路径")
            Logger.d("下载地址或目标路径为空")
        }
        return START_NOT_STICKY
    }

    private fun downloadFile(downloadLink: String, targetPath: String) {
        try {
            val url = URL(downloadLink)
            val baseUrl = "${url.protocol}://${url.host}"
            val filePath = url.path
            val responseBody = repository.downloadAsset(baseUrl, filePath)
            responseBody?.let {
                Logger.d("开始下载文件: $downloadLink")
                val contentLength = it.contentLength().toDouble()
                val inputStream = BufferedInputStream(it.byteStream())
                val outputStream = FileOutputStream(targetPath)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var totalBytesRead = 0.0
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = (totalBytesRead / contentLength * 100).toInt()
                    updateNotification(progress)
                }
                outputStream.close()
                inputStream.close()
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            cancelDownloadNotification()

            if (!packageManager.canRequestPackageInstalls()) {
                // 复制文件到下载目录
                val downloadFolderPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                copyFileToDownloadFolder(context = this,
                    sourcePath = targetPath.substringBeforeLast("/"),
                    targetPath = downloadFolderPath,
                    fileName = downloadLink.substringAfterLast("/"),
                    onSuccess = {
                        Logger.d("移动安装包至下载目录成功")
                        sendNotification(
                            "下载完成, 安装包路径: $downloadFolderPath/${
                                downloadLink.substringAfterLast("/")
                            }"
                        )
                        File(targetPath).delete()
                    },
                    onFail = {
                        Logger.d("移动安装包至下载目录失败")
                        sendNotification("下载完成, 安装包路径: $targetPath")
                    })
            } else {
                installApk(targetPath)
            }
        } catch (e: Exception) {
            sendNotification("下载失败, 请重试")
            stopForeground(STOP_FOREGROUND_REMOVE)
            notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID)
            Logger.d("下载失败: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun installApk(apkPath: String) {
        val intent = getInstallIntent(apkPath)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        sendNotificationWithIntent("下载完成, 正在进行安装", pendingIntent)
        startActivity(intent)
    }

    private fun getInstallIntent(apkPath: String): Intent {
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            Logger.e("安装包不存在")
            Toast.makeText(this, "安装包不存在", Toast.LENGTH_SHORT).show()
        }
        apkFile.setReadable(true, false)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val apkUri: Uri =
            FileProvider.getUriForFile(baseContext, "${baseContext.packageName}.provider", apkFile)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        return intent
    }

    private fun createDownloadNotification(progress: Int, maxProgress: Int): Notification {
        val builder = NotificationCompat.Builder(this, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("MC开发者管理器").setContentText("下载中... $progress%")
            .setSmallIcon(R.drawable.ic_mc).setProgress(maxProgress, progress, false)
        return builder.build()
    }

    private fun createTipsNotification(tips: String): Notification {
        val builder = NotificationCompat.Builder(this, TIPS_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("MC开发者管理器").setContentText(tips).setSmallIcon(R.drawable.ic_mc)
        return builder.build()
    }

    private fun createTipsWithIntentNotification(
        tips: String, intent: PendingIntent
    ): Notification {
        val builder = NotificationCompat.Builder(this, TIPS_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("MC开发者管理器").setContentText(tips).setSmallIcon(R.drawable.ic_mc)
            .setContentIntent(intent)
        return builder.build()
    }

    private fun cancelDownloadNotification() {
        notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID)
    }

    private fun updateNotification(progress: Int) {
        notificationManager.notify(
            DOWNLOAD_NOTIFICATION_ID, createDownloadNotification(progress, 100)
        )
    }

    private fun sendNotification(tips: String) {
        notificationManager.notify(
            TIPS_NOTIFICATION_ID, createTipsNotification(tips)
        )
    }

    private fun sendNotificationWithIntent(tips: String, intent: PendingIntent) {
        notificationManager.notify(
            TIPS_NOTIFICATION_ID, createTipsWithIntentNotification(tips, intent)
        )
    }

    private fun createDownloadNotificationChannel() {
        val name = "下载通知"
        val descriptionText = "下载进度"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(DOWNLOAD_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createTipsNotificationChannel() {
        val name = "提示通知"
        val descriptionText = "提示信息"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(TIPS_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }
}