package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.INCENTIVE_PAGE
import com.lemon.mcdevmanager.data.common.INCOME_DETAIL_PAGE
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.utils.getNoScaleTextSize
import com.lt.compose_views.other.VerticalSpace

@Composable
fun ProfitPage(
    navController: NavController = rememberNavController()
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        // 标题
        HeaderWidget(title = "收益管理", leftAction = {
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

        VerticalSpace(dp = 8)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.card
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "收益管理",
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 12f).sp
                )
                VerticalSpace(dp = 8)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            navController.navigate(INCOME_DETAIL_PAGE) {
                                launchSingleTop = true
                            }
                        }
                ) {
                    Text(
                        text = "收益详情",
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = getNoScaleTextSize(context, 16f).sp
                    )
                }
            }
        }
        VerticalSpace(dp = 8)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.card
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "激励内容",
                    color = AppTheme.colors.hintColor,
                    fontSize = getNoScaleTextSize(context, 12f).sp
                )
                VerticalSpace(dp = 8)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            navController.navigate(INCENTIVE_PAGE) {
                                launchSingleTop = true
                            }
                        }
                ) {
                    Text(
                        text = "激励历史",
                        color = AppTheme.colors.textColor,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = getNoScaleTextSize(context, 16f).sp
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun ProfitPagePreview() {
    MCDevManagerTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            ProfitPage()
        }
    }
}