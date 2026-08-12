package com.lemon.mcdevmanagermp.mcp

import com.lemon.mcdevmanagermp.BuiltInVersion
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.PrintStream

fun main(): Unit = runBlocking {
    val protocolInput = System.`in`
    val protocolOutput = System.out
    System.setOut(PrintStream(System.err, true, Charsets.UTF_8.name()))

    val credentials = runCatching { McpCredentials.fromEnvironment() }.getOrElse {
        System.err.println("MCP 启动失败：${it.message}")
        return@runBlocking
    }
    val auth = AuthSession.create(credentials)
    auth.initialize().getOrElse {
        System.err.println("MCP 启动失败：认证失败，请检查凭据配置")
        return@runBlocking
    }
    val service = WorkToolService(auth)

    val server = Server(
        serverInfo = Implementation(name = "mcdev-mcp", version = BuiltInVersion.VERSION),
        options = ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false)),
        ),
        instructions = "管理网易 MC 开发者平台作品。写操作必须遵守工具返回的 allowedActions；删除必须由用户在客户端确认。",
    ) {
        registerWorkTools(service)
    }
    val transport = StdioServerTransport(
        protocolInput.asSource().buffered(),
        protocolOutput.asSink().buffered(),
    ) {}
    server.createSession(transport)
    awaitCancellation()
}
