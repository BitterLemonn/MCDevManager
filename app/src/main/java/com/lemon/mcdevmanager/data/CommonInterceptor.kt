package com.lemon.mcdevmanager.data

//import com.orhanobut.logger.Logger
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
//        Logger.d(
//            String.format(
//                "Interceptor: Sending request %s on %s%n%s",
//                request.url, chain.connection(), request.headers
//            )
//        )
        val response: Response = chain.proceed(request)
        val t2 = System.nanoTime()
//        Logger.d(
//            String.format(
//                "Interceptor: Received response for %s in %.1fms%n%s",
//                response.request.url, (t2 - t1) / 1e6, response.headers
//            )
//        )
        //拆包 正式运行须注释起来
//        Logger.e("拆包！！: header:${response.headers}\nbody:${(response.body as ResponseBody).string()}")
        return response
    }
}