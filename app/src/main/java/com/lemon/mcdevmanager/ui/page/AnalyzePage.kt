package com.lemon.mcdevmanager.ui.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.Alignment
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
import com.lemon.mcdevmanager.ui.theme.HeaderHeight
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.FABPositionWidget
import com.lemon.mcdevmanager.ui.widget.FlowTabWidget
import com.lemon.mcdevmanager.ui.widget.FromToDatePickerWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.ui.widget.SelectCard
import com.lemon.mcdevmanager.utils.getScreenHeight
import com.lemon.mcdevmanager.utils.getScreenWidth
import kotlin.math.roundToInt

@Composable
fun AnalyzePage(
    navController: NavController, showToast: (String, String) -> Unit
) {

    var isShowFilter by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()){
        Column(Modifier.fillMaxWidth()) {
            HeaderWidget(title = "数据分析", leftAction = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable(indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { navController.navigateUp() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_back), contentDescription = "back"
                    )
                }
            })
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SelectCard(leftName = "PE", rightName = "PC") {

                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape)
                        .background(AppTheme.colors.primaryColor)
                        .clickable(
                            indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isShowFilter = !isShowFilter
                        }
                        .padding(8.dp)
                ) {
                    Image(
                        modifier = Modifier.padding(4.dp),
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "filter",
                        colorFilter = ColorFilter.tint(TextWhite)
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = isShowFilter,
            enter = expandVertically(animationSpec = tween(400)),
            exit = shrinkVertically(animationSpec = tween(400)),
            modifier = Modifier.padding(top = HeaderHeight + 40.dp + 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .wrapContentHeight(),
                backgroundColor = AppTheme.colors.card,
                shape = RoundedCornerShape(8.dp)
            ) {
                FromToDatePickerWidget(modifier = Modifier.fillMaxWidth())
            }
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