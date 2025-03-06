package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material.ripple
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.ANALYZE_PAGE
import com.lemon.mcdevmanager.data.common.MOD_DATA_DETAIL_PAGE
import com.lemon.mcdevmanager.ui.base.BasePage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lemon.mcdevmanager.viewModel.OneClickPriceFeedbackViewActions
import com.lemon.mcdevmanager.viewModel.OneClickPriceFeedbackViewEvents
import com.lemon.mcdevmanager.viewModel.OneClickPriceFeedbackViewModel

@Composable
fun ModSelectPage(
    navController: NavController = rememberNavController(),
    viewModel: OneClickPriceFeedbackViewModel = viewModel(),
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val viewStates by viewModel.viewStates.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.dispatch(OneClickPriceFeedbackViewActions.LoadResourceList)
    }
    BasePage(viewEvent = viewModel.viewEvents, onEvent = { event ->
        when (event) {
            is OneClickPriceFeedbackViewEvents.ShowToast -> {
                showToast(event.message, event.flag)
            }
        }
    }) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderWidget(
                title = "组件数据",
                leftAction = {
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable(indication = ripple(),
                            interactionSource = remember { MutableInteractionSource() }) { navController.navigateUp() }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "back"
                        )
                    }
                }
            )
            if (viewStates.resList.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewStates.resList) {
                        ModItem(modName = it.itemName, iid = it.itemId) {
                            navController.navigate("$MOD_DATA_DETAIL_PAGE/${it.itemId}") {
                                launchSingleTop = true
                                popUpTo(ANALYZE_PAGE)
                            }
                        }
                    }
                }
            } else if (!viewStates.isShowLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "暂无数据",
                        color = AppTheme.colors.hintColor,
                        fontSize = getNoScaleTextSize(LocalContext.current, 18f).sp
                    )
                }
            }
        }
        if (viewStates.isShowLoading) {
            AppLoadingWidget()
        }
    }
}

@Composable
private fun ModItem(
    modName: String,
    iid: String,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mod),
                contentDescription = "mod",
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = modName,
                    color = AppTheme.colors.textColor,
                    fontSize = getNoScaleTextSize(context, 14f).sp
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = iid,
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 12f).sp
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ModSelectPagePreview() {
    MCDevManagerTheme {
        Box(
            Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.background)
        ) {
            ModSelectPage()
        }
    }
}

@Composable
@Preview
private fun ModItemPreview() {
    MCDevManagerTheme {
        ModItem("modName", "iid")
    }
}