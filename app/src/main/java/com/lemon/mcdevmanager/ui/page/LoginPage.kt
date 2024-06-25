package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.common.MAIN_PAGE
import com.lemon.mcdevmanager.data.netease.login.PVArgs
import com.lemon.mcdevmanager.data.netease.login.PVInfo
import com.lemon.mcdevmanager.data.netease.login.PVResultStrBean
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.BottomNameInput
import com.lemon.mcdevmanager.ui.widget.SNACK_ERROR
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
    val keyboardController = LocalSoftwareKeyboardController.current

    val viewState = viewModel.viewState.collectAsState()
    val states = viewState.value

    var isLoginSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        val script =
            context.assets.open("powerCompute.js").bufferedReader().use { it.readText() }
        viewModel.dispatch(LoginViewAction.UpdatePowerScript(script))
        viewModel.viewEvent.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is LoginViewEvent.LoginFailed -> showToast(event.message, SNACK_ERROR)
                is LoginViewEvent.LoginSuccess -> isLoginSuccess = true
                is LoginViewEvent.RouteToNext -> navController.navigate(MAIN_PAGE) {
                    popUpTo(LOGIN_PAGE) { inclusive = true }
                }

                else -> {}
            }
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
                        color = AppTheme.colors.textColor,
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
                            multiply = if (isSystemInDarkTheme()) Color(0xFFAAAAAA)
                            else Color(0xFFFFFFFF), add = Color.Transparent
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
                            keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
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
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.dispatch(LoginViewAction.Login)
                            keyboardController?.hide()
                        }),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = {
                            viewModel.dispatch(LoginViewAction.Login)
                            keyboardController?.hide()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.primaryColor, contentColor = TextWhite
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
        BottomNameInput(hint = "请输入助记名称",
            label = "名称",
            isShow = isLoginSuccess,
            onConfirm = { name ->

            })
    }

    AnimatedVisibility(
        visible = states.isStartLogin,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AppLoadingWidget()
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun LoginPagePreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    LoginPage(navController)
}