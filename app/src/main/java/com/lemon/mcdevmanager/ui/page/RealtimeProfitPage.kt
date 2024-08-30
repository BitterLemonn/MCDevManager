package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lt.compose_views.value_selector.date_selector.DateSelector
import com.lt.compose_views.value_selector.date_selector.DateSelectorState
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RealtimeProfitPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val nowDate = remember { ZonedDateTime.now() }
    val hintColor = AppTheme.colors.hintColor
    val dateState = remember {
        DateSelectorState(
            defaultYear = nowDate.year,
            defaultMonth = nowDate.monthValue,
            defaultDay = nowDate.dayOfMonth,
            maxYear = nowDate.year,
        )
    }
    var isShowDateSelector by remember { mutableStateOf(false) }


    Column {
        HeaderWidget(
            title = "实时收益", leftAction = {
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
            }
        )
        Row(Modifier.fillMaxWidth()) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .clip(CircleShape)
                    .padding(8.dp)
                    .clickable(
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() }
                    ) { isShowDateSelector = true },
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "calendar",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(8.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.card
                ),
                shape = RoundedCornerShape(8.dp)
            ) {}
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun RealtimeProfitPagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            RealtimeProfitPage()
        }
    }
}