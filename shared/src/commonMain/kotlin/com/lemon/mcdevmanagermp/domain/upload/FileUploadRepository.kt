package com.lemon.mcdevmanagermp.domain.upload

import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
import io.github.vinceglb.filekit.PlatformFile

interface FileUploadRepository {
    /**
     * 上传文件到网易 FP 服务
     * @param fileType 文件类型："image" 或 "video"
     * @param fileName 文件名
     * @param file 文件引用（延迟读取，避免 OOM）
     * @param mimeType MIME 类型，如 "image/jpeg"、"video/mp4"
     * @return 上传成功返回文件 URL，失败返回错误信息
     */
    suspend fun uploadFile(
        fileType: String,
        fileName: String,
        file: PlatformFile,
        mimeType: String
    ): NetworkState<FileInfoDTO>
}
