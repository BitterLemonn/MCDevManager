package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.FilesApi
import com.lemon.mcdevmanagermp.data.api.UploadApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.dto.netease.activity.FileInfoDTO
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
        mimeType: String,
        secure: String
    ): NetworkState<FileInfoDTO> {
        return try {
            val tokenResult = UnifiedExceptionHandler.handleRequest {
                filesApi.getFileToken(fileType = fileType, secure = secure)
            }
            val token = when (tokenResult) {
                is NetworkState.Success -> tokenResult.data?.token
                    ?: return NetworkState.Error("获取上传 token 失败")

                is NetworkState.Error -> return NetworkState.Error(tokenResult.msg, tokenResult.e)
            }
            Logger.d("$TAG: 获取 token 成功, 准备上传文件: $fileName")

            // 直接通过 Ktor HttpClient 上传，不经过 Ktorfit
            val uploadResponse = UploadApi.uploadFile(
                auth = token,
                fileName = fileName,
                file = file,
                mimeType = mimeType
            )
            val responseText = uploadResponse.body
            val jsonText = Regex("<textarea>(.*?)</textarea>")
                .find(responseText)?.groupValues?.get(1)?.trim()
                ?: run {
                    Logger.e("$TAG: 解析上传响应失败: $uploadResponse")
                    return NetworkState.Error("解析上传响应失败")
                }
            val sign = uploadResponse.sign ?: return NetworkState.Error("上传失败，文件签名为空")
            Logger.d("$TAG: 上传响应: $responseText, sign: $sign")

            NetworkState.Success(
                FileInfoDTO(
                    body = jsonText,
                    fileType = fileType,
                    sign = sign
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e("$TAG: 文件上传失败", e)
            NetworkState.Error("文件上传失败: ${e.message}", e)
        }
    }
}
