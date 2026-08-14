package com.lemon.mcdevmanagermp.data.api

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.consts.TRAILING_SLASH_MARKER
import com.lemon.mcdevmanagermp.utils.CookiesStore
import com.lemon.mcdevmanagermp.utils.Logger
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import io.ktor.client.plugins.logging.Logger as KtorLogger

object ApiFactory {
    private val cookiesStorage = object : CookiesStorage {
        override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
            if (cookie.value.isEmpty()) {
                CookiesStore.removeCookie(cookie.name)
                return
            }
            CookiesStore.addCookie(cookie.name, cookie.value)
        }

        override suspend fun get(requestUrl: Url): List<Cookie> {
            val cookies = CookiesStore.getAllCookiesMap()
            return cookies.map { Cookie(it.key, it.value) }
        }

        override fun close() {}
    }

    private val TrailingSlashPlugin = createClientPlugin("TrailingSlashPlugin") {
        onRequest { request, _ ->
            // 检查是否有我们自定义的 Header 标记
            if (request.headers[TRAILING_SLASH_MARKER] == "true") {
                request.headers.remove(TRAILING_SLASH_MARKER)

                val path = request.url.encodedPath
                if (!path.endsWith("/")) {
                    request.url.encodedPath = "$path/"
                }
            }
        }
    }


    private val TimeMonitorPlugin = createClientPlugin("TimeMonitorPlugin") {
        onRequest { request, _ ->
            // 在请求属性中记录开始时间
            request.attributes.put(AttributeKey("StartTime"), TimeSource.Monotonic.markNow())
        }

        onResponse { response ->
            val startTime =
                response.call.request.attributes.getOrNull(AttributeKey<TimeMark>("StartTime"))
            startTime?.let {
                val elapsed = it.elapsedNow().inWholeMilliseconds
                Logger.d("拦截器:\n请求 ${response.call.request.url} 耗时: ${elapsed}ms")
            }
            // ponytail: 兜底同步 Set-Cookie，避免 HttpCookies 在 onResponse 异常时漏存
            response.headers.getAll(HttpHeaders.SetCookie)?.let { CookiesStore.addCookies(it) }
        }
    }

    private val jsonHttpClient: HttpClient by lazy {
        HttpClient {
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(JSONConverter) }

            install(TrailingSlashPlugin)
            install(TimeMonitorPlugin)
            install(HttpTimeout) {
                connectTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
            install(HttpCookies) {
                storage = cookiesStorage
            }
        }
    }

    private val loggerHttpClient: HttpClient by lazy {
        HttpClient {
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(JSONConverter) }
            install(TimeMonitorPlugin)
            install(HttpTimeout) {
                connectTimeoutMillis = 60_000
                requestTimeoutMillis = 120_000
                socketTimeoutMillis = 60_000
            }
            install(HttpCookies) {
                storage = cookiesStorage
            }
            install(Logging) {
                logger = object : KtorLogger {
                    override fun log(message: String) {
                        // 使用你自己的 Logger 输出，Ktor 会自动格式化好 请求头/体/响应
                        Logger.d("KtorLog:\n$message")
                    }
                }
                // 打印级别：ALL (包含 Headers 和 Body)，对应你原来的 peekBody
                level = LogLevel.ALL
            }
        }
    }

    private val uploadHttpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) { json(JSONConverter) }
            install(TimeMonitorPlugin)
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 60_000  // 上传文件需要更长超时
                socketTimeoutMillis = 60_000
            }
            install(HttpCookies) {
                storage = cookiesStorage
            }
        }
    }

    private val downloadHttpClient: HttpClient by lazy {
        HttpClient {
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000 // 连接超时还是要有的
                requestTimeoutMillis = Long.MAX_VALUE  // 请求时间无限，防止下载大文件中断
                socketTimeoutMillis = Long.MAX_VALUE
            }

            install(HttpCookies) {
                storage = cookiesStorage
            }
        }
    }

    fun provideLoggerKtorfit(baseUrl: String): Ktorfit {
        return Ktorfit.Builder().baseUrl(baseUrl).httpClient(loggerHttpClient).build()
    }


    fun provideKtorfit(baseUrl: String): Ktorfit {
        return Ktorfit.Builder().baseUrl(baseUrl).httpClient(jsonHttpClient).build()
    }


    fun provideUploadHttpClient(): HttpClient = uploadHttpClient

    fun provideDownloadKtorfit(): Ktorfit {
        return Ktorfit.Builder().baseUrl("https://localhost/") // 占位符，实际会被 @Url 覆盖
            .httpClient(downloadHttpClient).build()
    }
}