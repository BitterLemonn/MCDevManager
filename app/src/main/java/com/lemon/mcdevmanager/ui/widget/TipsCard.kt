package com.lemon.mcdevmanager.ui.widget

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun TipsCard(
    modifier: Modifier = Modifier,
    @DrawableRes headerIcon: Int? = null,
    content: String,
    dismissText: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (headerIcon != null) {
                Image(
                    painter = painterResource(id = headerIcon),
                    contentDescription = "header icon",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = content,
                color = AppTheme.colors.textColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
                    .padding(end = 16.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = dismissText,
                color = AppTheme.colors.info,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun TipsCardPreview() {
    TipsCard(
        headerIcon = R.drawable.ic_notice,
        content = "这是一个提示卡片",
        dismissText = "知道了",
        onDismiss = {}
    )
}