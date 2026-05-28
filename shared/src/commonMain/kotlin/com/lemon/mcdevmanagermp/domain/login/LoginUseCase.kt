package com.lemon.mcdevmanagermp.domain.login

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.consts.CookiesNotValidException
import com.lemon.mcdevmanagermp.data.consts.NETEASE_TOP_URL
import com.lemon.mcdevmanagermp.data.consts.NETEASE_USER_COOKIE
import com.lemon.mcdevmanagermp.data.vo.netease.login.PVInfoVO
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.Logger
import com.lemon.mcdevmanagermp.utils.UnifiedExceptionHandler.unwrapNetworkState
import com.lemon.mcdevmanagermp.utils.encrpy.vdfAsync
import com.lemon.mcdevmanagermp.utils.extension.dumpAndGetCookiesValue
import com.lemon.mcdevmanagermp.utils.extension.isValidCookiesStr

class LoginUseCase(
    private val loginRepository: LoginRepository,
    private val userRepository: UserRepository
) {

    @Throws
    suspend operator fun invoke(
        email: String? = null,
        password: String? = null,
        cookies: String? = null
    ) {
        when {
            email != null && password != null -> loginWithEmail(email, password)
            cookies != null -> loginByCookies(cookies)
        }
    }

    private suspend fun loginWithEmail(email: String, password: String) {
        repeat(3) { attempt ->
            try {
                unwrapNetworkState(loginRepository.init(""))
                Logger.d("初始化登录成功")

                val power = unwrapNetworkState(loginRepository.getPower(email, NETEASE_TOP_URL))!!
                Logger.d("获取权限成功")

                val pvInfo = JSONConverter.decodeFromString<PVInfoVO>(power)
                val pvInfoResult = vdfAsync(pvInfo)

                val ticket = unwrapNetworkState(loginRepository.getTicket(email, NETEASE_TOP_URL))!!
                Logger.d("获取Ticket成功")

                unwrapNetworkState(
                    loginRepository.loginWithTicket(email, password, ticket, pvInfoResult)
                )
                Logger.d("登录成功")
                return
            } catch (e: Exception) {
                if (attempt == 2) throw e
                Logger.d("登录失败, 重试中...")
            }
        }
    }

    private suspend fun loginByCookies(cookies: String) {
        if (!cookies.isValidCookiesStr()) throw CookiesNotValidException()
        val cookieValue = cookies.dumpAndGetCookiesValue(NETEASE_USER_COOKIE)
            ?: failCookiesLogin()
        AppContext.cookiesStore.addCookie(NETEASE_USER_COOKIE, cookieValue)
        try {
            unwrapNetworkState(userRepository.getUserInfo())
        } catch (_: Exception) {
            failCookiesLogin()
        }
    }

    private fun failCookiesLogin(): Nothing {
        AppContext.cookiesStore.clearCookies()
        throw CookiesNotValidException()
    }
}
