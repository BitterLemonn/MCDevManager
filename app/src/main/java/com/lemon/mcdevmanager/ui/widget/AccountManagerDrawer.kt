package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lt.compose_views.chain_scrollable_component.swipe_to_dismiss.SwipeToDismiss

@Composable
fun AccountManagerDrawer(
    accountList: List<String> = emptyList(),
    onClick: (String) -> Unit = {},
    onDismiss: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onRightClick: () -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth(0.8f)
            .background(AppTheme.colors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.card)
        ) {
            Text(
                text = "账号管理",
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                letterSpacing = 8.sp,
                color = AppTheme.colors.textColor
            )
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(50.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) { onRightClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "add",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(AppTheme.colors.textColor)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            color = AppTheme.colors.dividerColor,
            thickness = 0.5.dp
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(accountList) {
                AccountItem(
                    account = it,
                    onClick = onClick,
                    onDismiss = onDismiss
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(AppTheme.colors.error)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                ) { onLogout() }
        ) {
            Text(
                text = "退出登录",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                letterSpacing = 2.sp,
                color = TextWhite
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountItem(
    account: String,
    onDismiss: (String) -> Unit = {},
    onClick: (String) -> Unit = {}
) {
    SwipeToDismiss(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .height(60.dp),
        minScrollPosition = 0.dp,
        maxScrollPosition = if (account == AppContext.nowNickname) 0.dp else 80.dp,
        backgroundContent = {
            SwipeBackgroundContent(onDismiss = { onDismiss(account) })
        }) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (account == AppContext.nowNickname) AppTheme.colors.info else AppTheme.colors.card)
                .clickable(
                    enabled = account != AppContext.nowNickname,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                ) { onClick(account) }
        ) {
            Text(
                text = account,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterStart),
                letterSpacing = 2.sp,
                color = if (account == AppContext.nowNickname) TextWhite else AppTheme.colors.textColor
            )
        }
    }
}

@Composable
private fun SwipeBackgroundContent(
    onDismiss: () -> Unit = {}
) {
    Box(modifier = Modifier
        .height(60.dp)
        .width(80.dp)
        .background(AppTheme.colors.error)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple()
        ) {
            onDismiss()
        }) {
        Image(
            painter = painterResource(id = R.drawable.ic_del),
            contentDescription = "delete",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun AccountManagerDrawerPreview() {
    AccountManagerDrawer(
        accountList = listOf(
            "账号1",
            "账号2",
            "账号3",
            "账号4",
            "账号5",
            "账号6",
            "账号7",
            "账号8",
            "账号9",
            "账号10"
        )
    )
}

@Composable
@Preview
private fun AccountItemPreview() {
    AccountItem("账号1")
}

@Composable
@Preview
private fun SwipeBackgroundContentPreview() {
    SwipeBackgroundContent()
}