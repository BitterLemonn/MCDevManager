package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashPage(
    navController: NavController
) {
    var waitingLast = 0
    LaunchedEffect(key1 = Unit) {
        this.launch {
            while (waitingLast < 5) {
                waitingLast++
                delay(1000)
            }
            // TODO 判断是否获取到缓存
            navController.navigate(LOGIN_PAGE)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isSystemInDarkTheme()) Color(0xFF417C54)
                else Color(0xFF50C878)
            )
    ) {
        Column(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mc),
                contentDescription = "icon",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(200.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "开发者管理器",
                color = TextWhite,
                fontSize = 26.sp,
                fontFamily = FontFamily(Font(R.font.minecraft_ae)),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun SplashPagePreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    SplashPage(navController)
}