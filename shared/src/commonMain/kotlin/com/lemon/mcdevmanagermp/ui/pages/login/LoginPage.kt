package com.lemon.mcdevmanagermp.ui.pages.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.fallback
import com.github.panpf.sketch.request.placeholder
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.components.BelowAnchorPositionProvider
import com.lemon.mcdevmanagermp.ui.components.LoginOutlineTextField
import com.lemon.mcdevmanagermp.ui.components.collectUiEffect
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.iconpack.NoShow
import com.lemon.mcdevmanagermp.ui.iconpack.Setting
import com.lemon.mcdevmanagermp.ui.iconpack.Show
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_mc
import mcdevmanagermpr.shared.generated.resources.img_avatar
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginPage(
    onNavigateToMain: () -> Unit,
    onBack: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val viewModel = remember { LoginViewModel() }
    val state by viewModel.state.collectAsState()
    viewModel.effect.collectUiEffect { effect ->
        when (effect) {
            is LoginEffect.ShowToast -> showToast(effect.message)

            is LoginEffect.NavigateTo -> onNavigateToMain()
        }
    }

    AppScaffold { innerPadding ->
        Scaffold(
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
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

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
                    .padding(start = 16.dp, top = 16.dp + statusBarTop, end = 16.dp, bottom = 16.dp)
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
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
                    .padding(start = 16.dp, top = 16.dp + statusBarTop, end = 16.dp, bottom = 16.dp)
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
                    imageVector = IconPack.Setting,
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
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().onSizeChanged { fieldWidthPx = it.width }) {
            LoginOutlineTextField(
                value = state.email,
                onValueChange = { onAction(LoginAction.UpdateEmail(it)) },
                label = {
                    Text(
                        "邮箱",
                        color = colors.textColor
                    )
                },
                trailingIcon = if (state.savedAccounts.isNotEmpty()) {
                    {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = "切换账号",
                                tint = colors.primary
                            )
                        }
                    }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            if (expanded && state.savedAccounts.isNotEmpty()) {
                val menuWidth = with(density) { fieldWidthPx.toDp() }
                Popup(
                    popupPositionProvider = BelowAnchorPositionProvider,
                    onDismissRequest = {
                        expanded = false
                        focusManager.clearFocus()
                    },
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .width(menuWidth)
                            .heightIn(max = 240.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceContainerHigh,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, colors.outlineVariant)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            state.savedAccounts.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AccountAvatarItem(headImg = account.headImg)
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = account.email,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.textColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (account.nickname.isNotBlank() && account.nickname != account.email) {
                                                    Text(
                                                        text = account.nickname,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = colors.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        onAction(LoginAction.SelectSavedAccount(account))
                                        expanded = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

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
                        imageVector = if (state.isPasswordVisible) IconPack.NoShow else IconPack.Show,
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onAction(LoginAction.ToggleRememberPassword(!state.rememberPassword)) }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.rememberPassword,
                onCheckedChange = { onAction(LoginAction.ToggleRememberPassword(it)) },
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primary,
                    checkmarkColor = colors.onPrimary
                )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "记住密码",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textColor
            )
        }
    }
}

@Composable
private fun AccountAvatarItem(headImg: String?) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(colors.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            uri = headImg,
            state = rememberAsyncImageState(ComposableImageOptions {
                placeholder(Res.drawable.img_avatar)
                fallback(Res.drawable.img_avatar)
                crossfade()
                error(Res.drawable.img_avatar)
                sizeMultiplier(2.0f)
            }),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
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
