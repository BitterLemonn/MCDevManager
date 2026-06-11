package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.api.FilesApi
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.NETEASE_UPLOAD_LINK
import com.lemon.mcdevmanagermp.data.vo.netease.upload.UploadFileVO
import com.lemon.mcdevmanagermp.domain.upload.FileUploadRepository
import com.lemon.mcdevmanagermp.utils.Logger
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
        fileBytes: ByteArray,
        mimeType: String
    ): NetworkState<String> {
        return try {
            // 1. 获取上传 token
            val tokenResult = filesApi.getFileToken(fileType = fileType)
            if (tokenResult.status != "200" && tokenResult.status != "ok") {
                Logger.e("$TAG: 获取上传 token 失败: ${tokenResult.msg}")
                return NetworkState.Error("获取上传 token 失败: ${tokenResult.msg}")
            }
            val token = tokenResult.data?.token ?: return NetworkState.Error("获取上传 token 失败")
            Logger.d("$TAG: 获取 token 成功, 准备上传文件: $fileName (${fileBytes.size} bytes)")

            // 2. 使用 Ktor client 直接上传文件（避免手动构造 PartData）
            val uploadClient =
                com.lemon.mcdevmanagermp.data.api.ApiFactory.provideUploadHttpClient()

            val response = uploadClient.submitFormWithBinaryData(
                url = "${NETEASE_UPLOAD_LINK}file/new",
                formData = formData {
                    append("Authorization", "UpToken $token")
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        append(HttpHeaders.ContentType, mimeType)
                    })
                }
            )

            val uploadResult = response.body<UploadFileVO>()

            // 3. 返回文件 URL
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
