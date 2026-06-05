package com.lemon.mcdevmanagermp.ui.pages.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.components.LoginOutlineTextField
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_back
import mcdevmanagermpr.shared.generated.resources.ic_mc
import mcdevmanagermpr.shared.generated.resources.ic_setting
import mcdevmanagermpr.shared.generated.resources.ic_no_show
import mcdevmanagermpr.shared.generated.resources.ic_show
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginPage(
    onNavigateToMain: () -> Unit,
    onBack: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val viewModel = remember { LoginViewModel() }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    AppScaffold(
        viewEffect = viewModel.effect,
        onEffect = { effect ->
            when (effect) {
                is LoginEffect.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }

                is LoginEffect.NavigateTo -> onNavigateToMain()
            }
        }
    ) { innerPadding ->
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    val colors = LocalAppColors.current
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(8.dp),
                        containerColor = colors.surface,
                        contentColor = colors.onSurface
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { scaffoldPadding ->
            LoginContent(
                state = state,
                onAction = viewModel::dispatch,
                onBack = onBack,
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.padding(innerPadding).padding(scaffoldPadding)
            )
        }
    }
}

@Composable
private fun LoginContent(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        // Back button
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_back),
                    contentDescription = "返回",
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Settings button
        if (onNavigateToSettings != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToSettings
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_setting),
                    contentDescription = "设置",
                    tint = colors.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(560.dp)
                .padding(horizontal = 24.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_mc),
                contentDescription = "Logo",
                modifier = Modifier.size(140.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedContent(
                    targetState = state.isUsingCookies,
                    label = "login_mode"
                ) { useCookies ->
                    if (useCookies) {
                        CookiesForm(state = state, onAction = onAction)
                    } else {
                        EmailPasswordForm(state = state, onAction = onAction)
                    }
                }

                Button(
                    onClick = { onAction(LoginAction.Login) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp,
                        hoveredElevation = 8.dp
                    ),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = colors.onPrimary
                        )
                    } else {
                        Text(
                            text = "登录",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = colors.divider,
                        thickness = 1.dp
                    )
                    Text(
                        text = "或",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = colors.onSurface.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = colors.divider,
                        thickness = 1.dp
                    )
                }

                Button(
                    onClick = { onAction(LoginAction.ToggleCookies) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.danger,
                        contentColor = colors.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp,
                        hoveredElevation = 6.dp
                    )
                ) {
                    Text(
                        text = if (state.isUsingCookies) "使用邮箱登录" else "使用Cookies登录",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    append("MCDevManager ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Light)) {
                        append("for NetEase")
                    }
                },
                color = colors.onSurface.copy(alpha = 0.35f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EmailPasswordForm(
    state: LoginState,
    onAction: (LoginAction) -> Unit
) {
    val colors = LocalAppColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LoginOutlineTextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.UpdateEmail(it)) },
            label = {
                Text(
                    "邮箱",
                    color = colors.textColor
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        LoginOutlineTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.UpdatePassword(it)) },
            label = {
                Text(
                    "密码",
                    color = colors.textColor
                )
            },
            trailingIcon = {
                IconButton(onClick = { onAction(LoginAction.TogglePasswordVisibility) }) {
                    Icon(
                        painter = painterResource(
                            if (state.isPasswordVisible) Res.drawable.ic_no_show else Res.drawable.ic_show
                        ),
                        contentDescription = if (state.isPasswordVisible) "隐藏密码" else "显示密码",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onAction(LoginAction.Login) }
            )
        )
    }
}

@Composable
private fun CookiesForm(
    state: LoginState,
    onAction: (LoginAction) -> Unit
) {
    val colors = LocalAppColors.current

    LoginOutlineTextField(
        value = state.cookies,
        onValueChange = { onAction(LoginAction.UpdateCookies(it)) },
        label = {
            Text(
                "Cookies",
                color = colors.textColor
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onAction(LoginAction.Login) }
        )
    )
}
