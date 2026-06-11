package com.lemon.mcdevmanagermp.domain.upload

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.utils.Logger
import io.github.vinceglb.filekit.PlatformFile

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
     * @param file 文件引用（上传时才读取，避免 OOM）
     * @param mimeType MIME 类型，如 "image/jpeg"
     * @return Pair<url?, error?>
     */
    suspend fun uploadImage(fileName: String, file: PlatformFile, mimeType: String): Pair<String?, String?> {
        return when (val result =
            fileUploadRepository.uploadFile("image", fileName, file, mimeType)) {
            is NetworkState.Success -> (result.data ?: result.msg) to null
            is NetworkState.Error -> null to result.msg
        }
    }

    /**
     * 批量上传图片（逐个上传，同一时间只有一个文件在内存中）
     * @param files Triple<文件名, 文件引用, MIME类型> 列表
     * @return Pair<成功的 URL 列表, 错误信息列表>
     */
    suspend fun uploadImages(files: List<Triple<String, PlatformFile, String>>): Pair<List<String>, List<String>> {
        val urls = mutableListOf<String>()
        val errors = mutableListOf<String>()

        for ((index, file) in files.withIndex()) {
            Logger.d("$TAG: 上传图片 ${index + 1}/${files.size}: ${file.first}")
            val (url, error) = uploadImage(file.first, file.second, file.third)
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
     * @param file 文件引用（上传时才读取，避免 OOM）
     * @param mimeType MIME 类型，如 "video/mp4"
     * @param fileSize 文件大小（用于校验，不读取文件内容）
     * @return Pair<url?, error?>
     */
    suspend fun uploadVideo(
        fileName: String,
        file: PlatformFile,
        mimeType: String,
        fileSize: Long
    ): Pair<String?, String?> {
        // 校验文件大小（使用元数据，不读取文件内容）
        if (fileSize > MAX_VIDEO_SIZE) {
            return null to "视频文件大小不能超过 50MB"
        }

        return when (val result =
            fileUploadRepository.uploadFile("video", fileName, file, mimeType)) {
            is NetworkState.Success -> (result.data ?: result.msg) to null
            is NetworkState.Error -> null to result.msg
        }
    }
}
