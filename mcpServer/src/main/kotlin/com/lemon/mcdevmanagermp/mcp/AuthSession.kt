package com.lemon.mcdevmanagermp.mcp

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.consts.LoginException
import com.lemon.mcdevmanagermp.data.repository.CookieRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.LoginRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.data.vo.netease.user.UserInfoVO
import com.lemon.mcdevmanagermp.domain.login.LoginUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class McpCredentials(
    val email: String?,
    val password: String?,
    val cookies: String?,
) {
    val canRelogin: Boolean get() = !email.isNullOrBlank() && !password.isNullOrBlank()
    val hasCookies: Boolean get() = !cookies.isNullOrBlank()

    companion object {
        fun fromEnvironment(env: Map<String, String> = System.getenv()): McpCredentials {
            val email = env["MCDEV_EMAIL"]?.takeIf { it.isNotBlank() }
            val password = env["MCDEV_PASSWORD"]?.takeIf { it.isNotBlank() }
            require((email == null) == (password == null)) {
                "MCDEV_EMAIL 与 MCDEV_PASSWORD 必须同时配置"
            }
            val credentials = McpCredentials(
                email = email,
                password = password,
                cookies = env["NTES_SESS"]?.takeIf { it.isNotBlank() },
            )
            require(credentials.hasCookies || credentials.canRelogin) {
                "请配置 NTES_SESS 或 MCDEV_EMAIL + MCDEV_PASSWORD"
            }
            return credentials
        }
    }
}

internal class AuthSession(
    private val credentials: McpCredentials,
    private val loginWithCookies: suspend (String) -> Unit,
    private val loginWithPassword: suspend (String, String) -> Unit,
    private val verifyUser: suspend () -> NetworkState<UserInfoVO>,
    private val clearCookies: () -> Unit,
    private val onAuthenticated: (UserInfoVO) -> Unit,
) {
    private val reloginMutex = Mutex()

    @Volatile
    private var authGeneration = 0L

    suspend fun initialize(): Result<Unit> = safely {
        if (credentials.hasCookies) {
            val cookieResult = safely {
                loginWithCookies(credentials.cookies!!)
                when (val verified = verifyUser()) {
                    is NetworkState.Success -> onAuthenticated(
                        verified.data ?: error("Cookie 有效但未返回用户信息")
                    )

                    is NetworkState.Error -> error("Cookie 验证失败")
                }
            }
            if (cookieResult.isSuccess) return@safely
            if (!credentials.canRelogin) throw cookieResult.exceptionOrNull()!!
        }
        loginAndVerify()
        authGeneration++
    }

    suspend fun <T> withReauthRetry(
        block: suspend () -> NetworkState<T>,
    ): NetworkState<T> {
        val generation = authGeneration
        val first = block()
        if (first !is NetworkState.Error || !first.e.isAuthenticationExpired()) return first
        if (!credentials.canRelogin) {
            return NetworkState.Error("Cookie 已过期，请更新 NTES_SESS 后重启 MCP", first.e)
        }

        val relogged = reloginMutex.withLock {
            if (authGeneration != generation) return@withLock true
            safely { loginAndVerify() }
                .onSuccess { authGeneration++ }
                .getOrElse { false }
        }
        if (!relogged) return NetworkState.Error("自动重新登录失败，请检查账号配置", first.e)
        return block()
    }

    private suspend fun loginAndVerify(): Boolean {
        val email = requireNotNull(credentials.email)
        val password = requireNotNull(credentials.password)
        clearCookies()
        loginWithPassword(email, password)
        return when (val result = verifyUser()) {
            is NetworkState.Success -> {
                val user = result.data ?: error("登录成功但未返回用户信息")
                onAuthenticated(user)
                true
            }

            is NetworkState.Error -> error("登录验证失败")
        }
    }

    companion object {
        fun create(credentials: McpCredentials): AuthSession {
            val loginUseCase = LoginUseCase(
                loginRepository = LoginRepositoryImpl.INSTANCE,
                userRepository = UserRepositoryImpl.INSTANCE,
                cookieRepository = CookieRepositoryImpl.INSTANCE,
            )
            return AuthSession(
                credentials = credentials,
                loginWithCookies = { loginUseCase(cookies = it) },
                loginWithPassword = { email, password ->
                    loginUseCase(email = email, password = password)
                },
                verifyUser = { UserRepositoryImpl.INSTANCE.getUserInfo() },
                clearCookies = { CookieRepositoryImpl.INSTANCE.clearCookies() },
                onAuthenticated = { AppContext.userInfo = it },
            )
        }
    }
}

private fun Throwable?.isAuthenticationExpired(): Boolean {
    var current = this
    while (current != null) {
        if (current is CookiesExpiredException || current is LoginException) return true
        current = current.cause
    }
    return false
}

internal suspend inline fun <T> safely(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
