package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SelectCard

@Composable
fun LicensePage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    var isSelectEN by remember { mutableStateOf(true) }
    val context = LocalContext.current
    Column {
        HeaderWidget(title = "开源协议", leftAction = {
            Box(modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }) { navController.navigateUp() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "back"
                )
            }
        })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_lisence),
                contentDescription = "open source",
                modifier = Modifier
                    .height(120.dp)
                    .aspectRatio(1f),
                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    color = AppTheme.colors.dividerColor
                )
                SelectCard(
                    modifier = Modifier.width(120.dp),
                    leftContain = {
                        Text(
                            text = "EN",
                            color = if (isSelectEN) TextWhite else AppTheme.colors.textColor,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    },
                    rightContain = {
                        Text(
                            text = "中文",
                            color = if (!isSelectEN) TextWhite else AppTheme.colors.textColor,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    },
                    initSelectLeft = isSelectEN,
                    nowSelectLeft = { isSelectEN = it }
                )
            }
            if (isSelectEN)
                Text(
                    text = context.assets.open("LICENSE").bufferedReader().use { it.readText() },
                    color = AppTheme.colors.textColor,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp
                )
            else
                Text(
                    text = context.assets.open("LICENSE_ZH").bufferedReader().use { it.readText() },
                    color = AppTheme.colors.textColor,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp
                )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun LicensePagePreview() {
    MCDevManagerTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)) {
            LicensePage()
        }
    }
}