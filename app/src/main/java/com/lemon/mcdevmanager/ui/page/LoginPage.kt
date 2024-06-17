package com.lemon.mcdevmanager.ui.page

import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

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
                        settings.javaScriptEnabled = true
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 7.0; Nexus 5X Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.85 Mobile Safari/537.36"
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                // 获取登录成功后的cookie
                                if (url?.contains("/#/checkin") == true) {
                                    isLoginSuccess = true
                                    return false
                                }
                                return super.shouldOverrideUrlLoading(view, request)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                getCookies(url)
                            }
                        }
                        loadUrl("https://mcdev.webapp.163.com/#/login")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center)
            )
        }

    }
}

private fun getCookies(url: String?) {
    val cookieManager = CookieManager.getInstance()
    val cookies = cookieManager.getCookie(url)
    println("cookies: $cookies")
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun LoginPagePreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    LoginPage(navController) { _, _ -> }
}