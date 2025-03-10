package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.HeaderHeight
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.theme.TitleFontSize

@Composable
fun HeaderWidget(
    title: String,
    leftAction: @Composable ((Modifier) -> Unit)? = null,
    rightAction: @Composable ((Modifier) -> Unit)? = null
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = AppTheme.colors.primaryColor)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .height(HeaderHeight)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = TitleFontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    fontFamily = Font(R.font.minecraft_ae).toFontFamily(),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (leftAction != null) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .padding(5.dp)
                ) {
                    leftAction(Modifier.align(Alignment.Center))
                }
            }
            if (rightAction != null) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(5.dp)
                ) {
                    rightAction(Modifier.align(Alignment.Center))
                }
            }
        }
        Box(
            modifier = Modifier
                .offset(y = (-4).dp)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.background)
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HeaderWidgetPreview() {
    MCDevManagerTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            HeaderWidget(
                title = "Title",
                leftAction = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .then(it),
                        contentScale = ContentScale.Crop
                    )
                }
            )
        }
    }
}