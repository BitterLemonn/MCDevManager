package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
private fun LoadingShimmer(
    modifier: Modifier = Modifier,
    xShimmer: Float,
    yShimmer: Float,
    gradientWidth: Float,
    content: @Composable () -> Unit
) {
    val colors = if (!isSystemInDarkTheme()) listOf(
        Color.LightGray.copy(alpha = 0.4f),
        Color.LightGray.copy(alpha = 0.1f),
        Color.LightGray.copy(alpha = 0.4f)
    ) else listOf(
        Color.DarkGray.copy(alpha = 0.9f),
        Color.DarkGray.copy(alpha = 0.2f),
        Color.DarkGray.copy(alpha = 0.9f)
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(xShimmer - gradientWidth, yShimmer - gradientWidth),
        end = Offset(xShimmer, yShimmer)
    )

    Box(
        modifier = Modifier
            .then(modifier)
            .background(brush = brush)
    ) {
        content()
    }
}

@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier,
    animationDuration: Int = 3000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val diagonalLength = sqrt(
        LocalConfiguration.current.screenWidthDp.toDouble()
            .pow(2.0) + LocalConfiguration.current.screenHeightDp.toDouble().pow(2.0)
    ).toFloat()
    val gradientWidth = diagonalLength / 2  // 动态计算gradientWidth
    val xShimmer = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = diagonalLength,
        animationSpec = infiniteRepeatable(
            tween(animationDuration, easing = LinearEasing),
            RepeatMode.Restart
        ), label = ""
    )

    val yShimmer = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = diagonalLength,
        animationSpec = infiniteRepeatable(
            tween(animationDuration, easing = LinearEasing),
            RepeatMode.Restart
        ), label = ""
    )

    LoadingShimmer(
        modifier = modifier,
        xShimmer = xShimmer.value,
        yShimmer = yShimmer.value,
        gradientWidth = gradientWidth,
        content = content
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoadingShimmerPreview() {
    ShimmerAnimation {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .background(Color.Gray)
        )
    }
}