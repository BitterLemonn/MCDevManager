package com.lemon.mcdevmanager.ui.page

import android.util.Log
import android.webkit.CookieManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eclipsesource.v8.JavaCallback
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Function
import com.eclipsesource.v8.V8Object
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.netease.login.PVArgs
import com.lemon.mcdevmanager.data.netease.login.PVInfo
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.BottomNameInput
import com.lemon.mcdevmanager.utils.dataJsonToString
import com.lemon.mcdevmanager.viewModel.LoginViewAction
import com.lemon.mcdevmanager.viewModel.LoginViewEvent
import com.lemon.mcdevmanager.viewModel.LoginViewModel
import com.orhanobut.logger.Logger
import com.zj.mvi.core.observeEvent

@Composable
fun LoginPage(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(),
    showToast: (String, String) -> Unit = { _, _ -> },
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val viewState = viewModel.viewState.collectAsState()
    val states = viewState.value

    var isLoginSuccess by remember { mutableStateOf(false) }
    var needComputePower by remember { mutableStateOf(false) }

    var pvInfo by remember {
        mutableStateOf(
            PVInfo(
                sid = "",
                hashFunc = "",
                needCheck = false,
                args = PVArgs(
                    mod = "",
                    t = 0,
                    puzzle = "",
                    x = ""
                ),
                maxTime = 0,
                minTime = 0
            )
        )
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.viewEvent.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is LoginViewEvent.LoginFailed -> showToast("登录失败", event.message)
                is LoginViewEvent.LoginSuccess -> {
                    isLoginSuccess = true
                }

                is LoginViewEvent.ComputePower -> {
                    needComputePower = true
                    pvInfo = event.pvInfo
                }

                else -> {}
            }
        }
    }

    SideEffect {
        if (needComputePower) {
            Logger.d("开始计算power")
            val script =
                context.assets.open("powerCompute.js").bufferedReader().use { it.readText() }
            val e = """
                    var e = {
                        sid: "${pvInfo.sid}",
                        hashFunc: "${pvInfo.hashFunc}",
                        needCheck: ${pvInfo.needCheck},
                        args: ${dataJsonToString(pvInfo.args)},
                        maxTime: ${pvInfo.maxTime},
                        minTime: ${pvInfo.minTime}
                    };
                """.trimIndent()
            val runPow = """
                var e = vdfFun(e);
            """.trimIndent()
            val runtime = V8.createV8Runtime()
            runtime.executeVoidScript(script)
            runtime.executeVoidScript(e)
            runtime.executeVoidScript(runPow)
            val result = runtime.executeScript("e") as V8Object

            val maxTime = result.getInteger("maxTime")
            val args = result.getString("args")
            val puzzle = result.getString("puzzle")
            val runTimes = result.getInteger("runTimes")
            val sid = result.getString("sid")
            val spendTime = result.getInteger("spendTime")
            val pvResultStrBean = PVResultStrBean(
                maxTime = maxTime,
                args = args,
                puzzle = puzzle,
                runTimes = runTimes,
                sid = sid,
                spendTime = spendTime
            )
            viewModel.dispatch(LoginViewAction.ComputePower(pvResultStrBean))
            result.release()
            runtime.release()
            needComputePower = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLoginSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 30.dp)
                ) {
                    Text(
                        text = "登录",
                        fontSize = 40.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        color = TextWhite,
                        fontFamily = FontFamily(Font(R.font.minecraft_ae)),
                        letterSpacing = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_mc),
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(120.dp)
                            .aspectRatio(1f),
                        colorFilter = ColorFilter.lighting(
                            multiply =
                            if (isSystemInDarkTheme()) Color(0xFFAAAAAA)
                            else Color(0xFFFFFFFF),
                            add = Color.Transparent
                        )
                    )
                    OutlinedTextField(
                        value = states.username,
                        onValueChange = {
                            viewModel.dispatch(LoginViewAction.UpdateUsername(it))
                        },
                        label = {
                            Text(text = "邮箱")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.primaryColor,
                            focusedTextColor = AppTheme.colors.textColor,
                            focusedLabelColor = AppTheme.colors.primaryColor,
                            focusedContainerColor = AppTheme.colors.card,
                            unfocusedLabelColor = AppTheme.colors.secondaryColor,
                            unfocusedBorderColor = AppTheme.colors.secondaryColor,
                            unfocusedTextColor = AppTheme.colors.textColor,
                            unfocusedContainerColor = AppTheme.colors.card
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = states.password,
                        onValueChange = {
                            viewModel.dispatch(LoginViewAction.UpdatePassword(it))
                        },
                        label = {
                            Text(text = "密码", modifier = Modifier.background(Color.Transparent))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.primaryColor,
                            focusedTextColor = AppTheme.colors.textColor,
                            focusedLabelColor = AppTheme.colors.primaryColor,
                            focusedContainerColor = AppTheme.colors.card,
                            unfocusedLabelColor = AppTheme.colors.secondaryColor,
                            unfocusedBorderColor = AppTheme.colors.secondaryColor,
                            unfocusedTextColor = AppTheme.colors.textColor,
                            unfocusedContainerColor = AppTheme.colors.card
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.dispatch(LoginViewAction.Login)
                            }
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = {
                            viewModel.dispatch(LoginViewAction.Login)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.primaryColor,
                            contentColor = TextWhite
                        )
                    ) {
                        Text(text = "登录", fontSize = 16.sp, modifier = Modifier.padding(10.dp))
                    }
                }
            }
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
        BottomNameInput(
            hint = "请输入助记名称",
            label = "名称",
            isShow = isLoginSuccess,
            onConfirm = { name ->
                val token = getCookies("https://mcdev.webapp.163.com/#/square")
                if (token.isNotBlank()) {

                }
            })
    }
}

private fun getCookies(url: String?): String {
    val cookieManager = CookieManager.getInstance()
    val cookies = cookieManager.getCookie(url)
    val keyMap = mutableMapOf<String, String>()
    cookies.split(";").forEach {
        Log.e("TAG", "getCookies: $it")
        val key = it.split("=")[0].trim()
        val value = it.split("=")[1].trim()
        keyMap[key] = value
    }
    return keyMap["NTES_SESS"] ?: ""
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun LoginPagePreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    LoginPage(navController)
}