package com.lemon.mcdevmanagermp.domain.upload

import com.lemon.mcdevmanagermp.data.common.NetworkState

interface FileUploadRepository {
    /**
     * 上传文件到网易 FP 服务
     * @param fileType 文件类型："image" 或 "video"
     * @param fileName 文件名
     * @param fileBytes 文件字节数据
     * @param mimeType MIME 类型，如 "image/jpeg"、"video/mp4"
     * @return 上传成功返回文件 URL，失败返回错误信息
     */
    suspend fun uploadFile(
        fileType: String,
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String
    ): NetworkState<String>
}
