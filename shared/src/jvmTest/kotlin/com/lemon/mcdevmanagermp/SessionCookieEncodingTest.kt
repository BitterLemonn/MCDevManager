package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.data.api.SessionCookieStorage
import com.lemon.mcdevmanagermp.utils.CookiesStore
import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Cookie
import io.ktor.http.CookieEncoding
import io.ktor.http.Url
import io.ktor.http.encodeCookieValue
import io.ktor.http.parseClientCookiesHeader
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderCookieHeader
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionCookieEncodingTest {
    private val storage = SessionCookieStorage()
    private val url = Url("http://127.0.0.1/")

    @BeforeTest
    fun setUp() = CookiesStore.clearCookies()

    @AfterTest
    fun tearDown() = CookiesStore.clearCookies()

    @Test
    fun storedValuesAreReturnedWithRawEncoding() = runBlocking {
        CookiesStore.addCookie("session", "example")
        assertEquals(CookieEncoding.RAW, storage.get(url).single().encoding)
    }

    @Test
    fun ordinaryCookieKeepsItsValue() = runBlocking {
        storage.addCookie(url, parseServerSetCookieHeader("session=example; Path=/"))
        assertEquals("example", CookiesStore.getCookie("session"))
        assertEquals("session=example", renderCookieHeader(storage.get(url).single()))
    }

    @Test
    fun rawSymbolsAndExistingPercentEscapesAreNotEncodedAgain() = runBlocking {
        val value = "fake%257C|+/%3D=="
        storage.addCookie(url, parseServerSetCookieHeader("session=$value; Path=/; HttpOnly"))
        assertEquals(value, CookiesStore.getCookie("session"))
        assertEquals("session=$value", renderCookieHeader(storage.get(url).single()))
    }

    @Test
    fun uriEncodedCookieIsConvertedToWireFormatOnce() = runBlocking {
        val value = "fake|value+/%7C=="
        val expected = encodeCookieValue(value, CookieEncoding.URI_ENCODING)
        storage.addCookie(url, Cookie("session", value, encoding = CookieEncoding.URI_ENCODING))
        assertEquals(expected, CookiesStore.getCookie("session"))
        assertEquals("session=$expected", renderCookieHeader(storage.get(url).single()))
        storage.addCookie(url, Cookie("session", expected, encoding = CookieEncoding.RAW))
        assertEquals("session=$expected", renderCookieHeader(storage.get(url).single()))
    }

    @Test
    fun base64EncodedCookieIsConvertedToWireFormatOnce() = runBlocking {
        val value = "fake|value+/%7C=="
        val expected = encodeCookieValue(value, CookieEncoding.BASE64_ENCODING)
        storage.addCookie(url, Cookie("session", value, encoding = CookieEncoding.BASE64_ENCODING))
        assertEquals(expected, CookiesStore.getCookie("session"))
        assertEquals("session=$expected", renderCookieHeader(storage.get(url).single()))
        storage.addCookie(url, Cookie("session", expected, encoding = CookieEncoding.RAW))
        assertEquals("session=$expected", renderCookieHeader(storage.get(url).single()))
    }

    @Test
    fun threeHundredCaptureAndResponseCyclesKeepHeadersAndStoreStable() = runBlocking {
        val expected = linkedMapOf(
            "S_INFO" to "fake|session|value",
            "P_INFO" to "literal%257C|+/%3D==",
            "NTES_SESS" to "fake=="
        )
        expected.forEach { (name, value) -> CookiesStore.addCookie(name, value) }
        val expectedHeader = expected.entries.joinToString("; ") { "${it.key}=${it.value}" }
        repeat(300) {
            val header = storage.get(url).joinToString("; ", transform = ::renderCookieHeader)
            assertEquals(expectedHeader, header)
            // HttpCookies 捕获请求头时按 RAW 回存；Set-Cookie 默认也按 RAW 解析。
            parseClientCookiesHeader(header).forEach { (name, value) ->
                storage.addCookie(url, Cookie(name, value, encoding = CookieEncoding.RAW))
                storage.addCookie(url, parseServerSetCookieHeader("$name=$value; Path=/"))
            }
            assertEquals(expected, CookiesStore.getAllCookiesMap())
        }
    }

    @Test
    fun historicalNestedEscapesAreNotRecursivelyDecoded() = runBlocking {
        val value = "fake%" + "25".repeat(267) + "7Cvalue"
        CookiesStore.addCookie("session", value)
        assertEquals("session=$value", renderCookieHeader(storage.get(url).single()))
        assertEquals(value, CookiesStore.getCookie("session"))
    }

    @Test
    fun emptyCookieStillRemovesExistingValue() = runBlocking {
        CookiesStore.addCookie("session", "old")
        storage.addCookie(url, Cookie("session", "", encoding = CookieEncoding.RAW))
        assertNull(CookiesStore.getCookie("session"))
        assertEquals(emptyList(), storage.get(url))
    }

    @Test
    fun realHttpRoundTripsPreserveExactWireValues() = runBlocking {
        val expected = linkedMapOf(
            "S_INFO" to "fake|session|value",
            "P_INFO" to "literal%257C|+/%3D==",
            "NTES_SESS" to "fake=="
        )
        expected.forEach { (name, value) -> CookiesStore.addCookie(name, value) }
        val observed = CopyOnWriteArrayList<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val header = exchange.requestHeaders.getFirst("Cookie").orEmpty()
            observed.add(header)
            parseClientCookiesHeader(header).forEach { (name, value) ->
                exchange.responseHeaders.add("Set-Cookie", "$name=$value; Path=/; HttpOnly")
            }
            val body = "ok".toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
            exchange.close()
        }
        val client = HttpClient {
            expectSuccess = true
            install(HttpTimeout) {
                socketTimeoutMillis = 2_000
                requestTimeoutMillis = 5_000
            }
            install(HttpCookies) { storage = this@SessionCookieEncodingTest.storage }
        }
        try {
            server.start()
            repeat(8) {
                assertEquals("ok", client.get("http://127.0.0.1:${server.address.port}/").bodyAsText())
                assertEquals(expected, CookiesStore.getAllCookiesMap())
            }
            val header = expected.entries.joinToString("; ") { "${it.key}=${it.value}" }
            assertEquals(List(8) { header }, observed.toList())
        } finally {
            client.close()
            server.stop(0)
        }
    }
}
