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
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.BottomNameInput
import com.lemon.mcdevmanager.viewModel.LoginViewAction
import com.lemon.mcdevmanager.viewModel.LoginViewModel

@Composable
fun LoginPage(
    navController: NavController,
    viewModel: LoginViewModel = viewModel(),
    showToast: (String, String) -> Unit = { _, _ -> },
) {
    val viewState = viewModel.viewState
    val states = viewState.collectAsState()

    var isLoginSuccess by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                        value = email,
                        onValueChange = { email = it },
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
                        value = password,
                        onValueChange = {
                            password = it
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
                                viewModel.dispatch(LoginViewAction.Login(email, password))
                            }
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = {
                            viewModel.dispatch(LoginViewAction.Login(email, password))
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