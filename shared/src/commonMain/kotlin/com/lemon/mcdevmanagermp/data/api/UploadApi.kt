package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.consts.NETEASE_UPLOAD_LINK
import com.lemon.mcdevmanagermp.data.vo.netease.upload.UploadFileResponseVO
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.io.buffered

/**
 * 文件上传 API
 */
object UploadApi {

    private val client = ApiFactory.provideUploadHttpClient()

    /**
     * 上传文件到网易 FP 服务
     * @param auth 上传 token
     * @param fileName 文件名
     * @param file 文件引用（延迟读取）
     * @param mimeType MIME 类型
     * @return UploadResponse 包含响应体和 x-ntes-signature 签名
     */
    suspend fun uploadFile(
        auth: String,
        fileName: String,
        file: PlatformFile,
        mimeType: String
    ): UploadFileResponseVO {
        val fileSize = try {
            file.size().takeIf { it > 0 }
        } catch (_: Exception) {
            null
        }

        val response = client.submitFormWithBinaryData(
            url = "${NETEASE_UPLOAD_LINK}x19/file/new/",
            formData = formData {
                append("Authorization", auth)
                append(
                    key = "fpfile",
                    value = InputProvider(fileSize) {
                        file.source().buffered()
                    },
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"$fileName\""
                        )
                        append(HttpHeaders.ContentType, mimeType)
                    }
                )
            }
        )
        val sign = response.headers["x-ntes-signature"]
        return UploadFileResponseVO(
            body = response.bodyAsText(),
            sign = sign
        )
    }
}
