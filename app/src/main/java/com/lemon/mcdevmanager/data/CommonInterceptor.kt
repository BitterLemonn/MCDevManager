package com.lemon.mcdevmanager.data

//import com.orhanobut.logger.Logger
import android.text.TextUtils
import com.lemon.mcdevmanager.data.common.CookiesStore
import com.orhanobut.logger.Logger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException


class CommonInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val t1 = System.nanoTime()

        request.body()?.let {
            val buffer = okio.Buffer()
            it.writeTo(buffer)
            Logger.d("拦截器:\n发送请求至 ${request.url()}\n请求头: ${request.headers()}\n请求体: ${buffer.readUtf8()}")
        }
        val response: Response = chain.proceed(request)
        val t2 = System.nanoTime()
        Logger.d(
            "拦截器:\n收到返回 ${
                response.request().url()
            } 耗时 ${(t2 - t1) / 1e6}ms\n回复头: ${response.headers()}"
        )
        if (response.headers("Set-Cookie").isNotEmpty()) {
            val cookies = response.headers("Set-Cookie")
            CookiesStore.addCookies(cookies)
        }
        //查看返回数据
        val responseBody: ResponseBody = response.peekBody(1024 * 1024.toLong())
        Logger.d("拦截器:\n返回数据: ${responseBody.string()}")
        return response
    }
}

class AddCookiesInterceptor() : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        //添加Cookie
        val cookiesStr = CookiesStore.getAllCookiesString()
        if (!TextUtils.isEmpty(cookiesStr)) {
            builder.addHeader("Cookie", cookiesStr)
        }
        return chain.proceed(builder.build())
    }
}