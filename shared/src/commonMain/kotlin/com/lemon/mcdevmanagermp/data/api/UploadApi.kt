package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.consts.NETEASE_FILE_SIGN_MARKER
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
import kotlin.io.encoding.Base64

/**
 * 文件上传 API
 */
object UploadApi {

    private val client = ApiFactory.provideUploadHttpClient()

    // 默认上传地址：token 解析失败时的回退（兼容历史 image/video 流程）
    private const val DEFAULT_UPLOAD_URL = "${NETEASE_UPLOAD_LINK}x19/file/new/"

    /**
     * 上传文件到网易 FP 服务
     * @param auth 上传 token（形如 "Policy <sig>:<base64-policy>"，policy 内 url 为实际上传地址）
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

        // 不同 file_type 路由到不同子域（zip_package → pfp，image → fp），
        // 上传地址以 token policy 内签发的 url 为准，硬编码 host 会导致跨子域 token 校验失败
        val uploadUrl = resolveUploadUrl(auth)

        val response = client.submitFormWithBinaryData(
            url = uploadUrl,
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
        val sign = response.headers[NETEASE_FILE_SIGN_MARKER]
        return UploadFileResponseVO(
            body = response.bodyAsText(),
            sign = sign
        )
    }

    /**
     * 从 token policy 解析实际上传地址：token 形如 "Policy <sig>:<base64-json>"，
     * base64 解码后取 "url" 字段。解析失败回退默认地址（不破坏现有 image/video 流程）。
     */
    private fun resolveUploadUrl(auth: String): String {
        val payload = auth.substringAfter(":", "").trim()
        if (payload.isEmpty()) return DEFAULT_UPLOAD_URL
        return try {
            val json = Base64.decode(payload.encodeToByteArray()).decodeToString()
            Regex(""""url"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
                ?: DEFAULT_UPLOAD_URL
        } catch (_: Exception) {
            DEFAULT_UPLOAD_URL
        }
    }
}
