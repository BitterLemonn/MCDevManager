package com.lemon.mcdevmanagermp.domain.upload

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import com.lemon.mcdevmanagermp.utils.Logger
import io.github.vinceglb.filekit.PlatformFile

/**
 * 待上传文件信息
 * @param fileName 文件名
 * @param file 文件引用（上传时才读取，避免 OOM）
 * @param mimeType MIME 类型，如 "image/jpeg"、"video/mp4"
 */
data class UploadFileEntry(
    val fileName: String,
    val file: PlatformFile,
    val mimeType: String
)

/**
 * 批量上传图片结果
 * @param files 上传成功的文件信息列表
 * @param errors 上传失败的错误信息列表
 */
data class BatchUploadResult(
    val files: List<FileInfoDTO>,
    val errors: List<String>
)

/**
 * 单文件上传结果
 * @param fileInfo 上传成功的文件信息，失败时为 null
 * @param error 上传失败的错误信息，成功时为 null
 */
data class SingleUploadResult(
    val fileInfo: FileInfoDTO?,
    val error: String?
)

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
     * @return NetworkState<FileInfoDTO>
     */
    suspend fun uploadImage(
        fileName: String,
        file: PlatformFile,
        mimeType: String
    ): NetworkState<FileInfoDTO> {
        return fileUploadRepository.uploadFile("image", fileName, file, mimeType)
    }

    /**
     * 批量上传图片（逐个上传，同一时间只有一个文件在内存中）
     * @param files 待上传文件列表
     * @return BatchUploadResult
     */
    suspend fun uploadImages(files: List<UploadFileEntry>): BatchUploadResult {
        val results = mutableListOf<FileInfoDTO>()
        val errors = mutableListOf<String>()

        for ((index, entry) in files.withIndex()) {
            Logger.d("$TAG: 上传图片 ${index + 1}/${files.size}: ${entry.fileName}")
            when (val result = uploadImage(entry.fileName, entry.file, entry.mimeType)) {
                is NetworkState.Success -> {
                    result.data?.let { results.add(it) }
                }

                is NetworkState.Error -> {
                    errors.add("图片 ${index + 1} 上传失败: ${result.msg}")
                }
            }
        }

        return BatchUploadResult(files = results, errors = errors)
    }

    /**
     * 上传视频
     * @param fileName 文件名
     * @param file 文件引用（上传时才读取，避免 OOM）
     * @param mimeType MIME 类型，如 "video/mp4"
     * @param fileSize 文件大小（用于校验，不读取文件内容）
     * @return SingleUploadResult
     */
    suspend fun uploadVideo(
        fileName: String,
        file: PlatformFile,
        mimeType: String,
        fileSize: Long
    ): SingleUploadResult {
        // 校验文件大小（使用元数据，不读取文件内容）
        if (fileSize > MAX_VIDEO_SIZE) {
            return SingleUploadResult(fileInfo = null, error = "视频文件大小不能超过 50MB")
        }

        return when (val result =
            fileUploadRepository.uploadFile("video", fileName, file, mimeType)) {
            is NetworkState.Success -> SingleUploadResult(fileInfo = result.data, error = null)
            is NetworkState.Error -> SingleUploadResult(fileInfo = null, error = result.msg)
        }
    }
}
