package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.FABPositionWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.utils.getScreenHeight
import com.lemon.mcdevmanager.utils.getScreenWidth
import kotlin.math.roundToInt

@Composable
fun AnalyzePage(
    navController: NavController, showToast: (String, String) -> Unit
) {
    val context = LocalContext.current

    var isChangingDate by remember { mutableStateOf(false) }

    var tapOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var isShowFilter by remember { mutableStateOf(false) }

    val animateOffset by animateOffsetAsState(
        targetValue = if (isShowFilter) Offset(0f, 0f) else tapOffset,
        animationSpec = tween(800)
    )
    val animateBound by animateDpAsState(
        targetValue = if (isShowFilter) 0.dp else Int.MAX_VALUE.dp,
        animationSpec = tween(800)
    )
    val animateSize by animateSizeAsState(
        targetValue = if (isShowFilter) Size(
            getScreenWidth(context).toFloat(),
            getScreenHeight(context).toFloat()
        ) else Size(0f, 0f),
    )

    Column(Modifier.fillMaxWidth()) {
        HeaderWidget(title = "数据分析", leftAction = {
            Box(modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }) { navController.navigateUp() }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back), contentDescription = "back"
                )
            }
        })
        Text(text = "tapOffset: $tapOffset", fontSize = 16.sp)
    }
    Box(
        modifier = Modifier
            .size(animateSize.width.dp, animateSize.height.dp)
            .offset { IntOffset(animateOffset.x.roundToInt(), animateOffset.y.roundToInt()) }
            .clip(RoundedCornerShape(animateBound))
            .background(AppTheme.colors.primaryColor)
    )
    FABPositionWidget {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        tapOffset = offset
                        isShowFilter = !isShowFilter
                    }
                },
            shape = CircleShape,
            backgroundColor = AppTheme.colors.primaryColor,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_filter),
                contentDescription = "filter",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(TextWhite)
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun AnalyzePagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            AnalyzePage(navController = rememberNavController(), showToast = { _, _ -> })
        }
    }
}