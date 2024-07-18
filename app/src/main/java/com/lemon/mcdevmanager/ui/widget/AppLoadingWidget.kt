package com.lemon.mcdevmanager.ui.widget

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun AppLoadingWidget(showBackground: Boolean = true) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    var imageLoaded by remember { mutableStateOf(false) }

    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val height = configuration.screenHeightDp.dp
    val width = configuration.screenWidthDp.dp
    val minSize = if (height > width) width else height

    Box(modifier = Modifier.fillMaxSize()) {
        if (showBackground)Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .size((minSize * 0.45f))
                .align(Alignment.Center)
        ) {
            AsyncImage(
                model = R.drawable.loading,
                contentDescription = "loading",
                imageLoader = imageLoader,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.Center),
                colorFilter = ColorFilter.lighting(
                    multiply = AppTheme.colors.imgTintColor,
                    add = Color.Transparent
                ),
                onSuccess = {
                    imageLoaded = true
                }
            )
            if (imageLoaded)
                Text(
                    text = "Loading...",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.minecraft_ae)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    fontSize = 16.sp,
                )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppLoadingWidgetPreview() {
    AppLoadingWidget()
}