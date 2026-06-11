package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.FilesApi
import com.lemon.mcdevmanagermp.data.api.UploadApi
import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.vo.netease.upload.UploadFileVO
import com.lemon.mcdevmanagermp.domain.upload.FileUploadRepository
import com.lemon.mcdevmanagermp.utils.Logger
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CancellationException

class FileUploadRepositoryImpl : FileUploadRepository {
    companion object {
        val INSTANCE by lazy { FileUploadRepositoryImpl() }
        private const val TAG = "FileUpload"
    }

    private val filesApi = FilesApi.INSTANCE

    override suspend fun uploadFile(
        fileType: String,
        fileName: String,
        file: PlatformFile,
        mimeType: String
    ): NetworkState<String> {
        return try {
            val tokenResult = UnifiedExceptionHandler.handleRequest {
                filesApi.getFileToken(fileType = fileType)
            }
            val token = when (tokenResult) {
                is NetworkState.Success -> tokenResult.data?.token
                    ?: return NetworkState.Error("获取上传 token 失败")

                is NetworkState.Error -> return NetworkState.Error(tokenResult.msg, tokenResult.e)
            }
            Logger.d("$TAG: 获取 token 成功, 准备上传文件: $fileName")

            // 直接通过 Ktor HttpClient 上传，不经过 Ktorfit
            val responseText = UploadApi.uploadFile(
                auth = token,
                fileName = fileName,
                file = file,
                mimeType = mimeType
            )
            Logger.d("$TAG: 上传响应: $responseText")

            val jsonText = Regex("<textarea>(.*?)</textarea>")
                .find(responseText)?.groupValues?.get(1)?.trim()
                ?: return NetworkState.Error("解析上传响应失败")

            val uploadResult = JSONConverter.decodeFromString<UploadFileVO>(jsonText)
            val url = uploadResult.url
            if (url.isBlank()) {
                Logger.e("$TAG: 上传文件返回空 URL")
                return NetworkState.Error("上传文件返回空 URL")
            }

            Logger.d("$TAG: 文件上传成功: $url")
            NetworkState.Success(url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e("$TAG: 文件上传失败", e)
            NetworkState.Error("文件上传失败: ${e.message}", e)
        }
    }
}
