package com.lemon.mcdevmanagermp.domain.upload

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.utils.Logger

class FileUploadUseCase(
    private val fileUploadRepository: FileUploadRepository
) {
    companion object {
        private const val TAG = "FileUploadUseCase"
        private const val MAX_VIDEO_SIZE = 50 * 1024 * 1024L // 50MB
    }

    /**
     * 上传单张图片
     * @param fileName 文件名
     * @param bytes 文件字节数据
     * @return Pair<url?, error?>
     */
    suspend fun uploadImage(fileName: String, bytes: ByteArray): Pair<String?, String?> {
        val mimeType = guessImageMimeType(fileName)
        return when (val result =
            fileUploadRepository.uploadFile("image", fileName, bytes, mimeType)) {
            is NetworkState.Success -> (result.data ?: result.msg) to null
            is NetworkState.Error -> null to result.msg
        }
    }

    /**
     * 批量上传图片
     * @param files 文件名和字节数据列表
     * @return Pair<成功的 URL 列表, 错误信息列表>
     */
    suspend fun uploadImages(files: List<Pair<String, ByteArray>>): Pair<List<String>, List<String>> {
        val urls = mutableListOf<String>()
        val errors = mutableListOf<String>()

        for ((index, file) in files.withIndex()) {
            Logger.d("$TAG: 上传图片 ${index + 1}/${files.size}: ${file.first}")
            val (url, error) = uploadImage(file.first, file.second)
            if (url != null) {
                urls.add(url)
            }
            if (error != null) {
                errors.add("图片 ${index + 1} 上传失败: $error")
            }
        }

        return urls to errors
    }

    /**
     * 上传视频
     * @param fileName 文件名
     * @param bytes 文件字节数据
     * @return Pair<url?, error?>
     */
    suspend fun uploadVideo(fileName: String, bytes: ByteArray): Pair<String?, String?> {
        // 校验文件大小
        if (bytes.size > MAX_VIDEO_SIZE) {
            return null to "视频文件大小不能超过 50MB"
        }

        val mimeType = guessVideoMimeType(fileName)
        return when (val result =
            fileUploadRepository.uploadFile("video", fileName, bytes, mimeType)) {
            is NetworkState.Success -> (result.data ?: result.msg) to null
            is NetworkState.Error -> null to result.msg
        }
    }

    /**
     * 根据文件名猜测图片 MIME 类型
     */
    private fun guessImageMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            else -> "image/jpeg"
        }
    }

    /**
     * 根据文件名猜测视频 MIME 类型
     */
    private fun guessVideoMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "avi" -> "video/avi"
            "mov" -> "video/quicktime"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "webm" -> "video/webm"
            else -> "video/mp4"
        }
    }
}
