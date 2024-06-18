package com.lemon.mcdevmanager.ui.page

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.widget.BottomDialog
import com.lemon.mcdevmanager.ui.widget.BottomNameInput

@Composable
fun LoginPage(
    navController: NavController,
    showToast: (String, String) -> Unit = { _, _ -> },
) {
    var isLoginSuccess by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 登录使用WebView
        if (!isLoginSuccess) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        clearCache(true)
                        clearFormData()
                        CookieManager.getInstance().removeAllCookies(null)

                        webViewClient = object : WebViewClient() {
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                Log.e(
                                    "TAG",
                                    "onReceivedError: ${error?.description}\n request: ${request?.requestHeaders}\nurl: ${request?.url}"
                                )
                                super.onReceivedError(view, request, error)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                return true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                Log.d("TAG", "onPageFinished: $url")
                                if (url == "https://mcdev.webapp.163.com/#/square") {
                                    getCookies(url)
                                    isLoginSuccess = true
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                Log.e("TAG", "onConsoleMessage: ${consoleMessage?.message()}")
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }

                        this.settings.javaScriptEnabled = true
                        this.settings.loadsImagesAutomatically = true
                        this.settings.domStorageEnabled = true
                        this.settings.loadWithOverviewMode = true
                        this.settings.useWideViewPort = true

                        this.settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; SM-A205U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.181 Mobile Safari/537.36"

                        this.loadUrl("https://mcdev.webapp.163.com/#/square")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.Center)
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_login_bg),
                    contentDescription = "login success background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 登录成功后弹出输入名称框
        BottomNameInput(hint = "请输入助记名称", label = "名称", isShow = isLoginSuccess, onConfirm = {
            // TODO 跳转到主页
        })
    }
}

private fun getCookies(url: String?) {
    val cookieManager = CookieManager.getInstance()
    val cookies = cookieManager.getCookie(url)
//    Log.e("TAG", "getCookies: $cookies")
    val keyMap = mutableMapOf<String, String>()
    cookies.split(";").forEach {
        Log.e("TAG", "getCookies: $it")
        val key = it.split("=")[0].trim()
        val value = it.split("=")[1].trim()
        keyMap[key] = value
    }

}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun LoginPagePreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    LoginPage(navController) { _, _ -> }
}