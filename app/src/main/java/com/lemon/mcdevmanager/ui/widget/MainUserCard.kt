package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun MainUserCard(
    username: String, avatarUrl: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        )
    ) {
        Row(Modifier.fillMaxWidth()) {
            AsyncImage(model = avatarUrl,
                contentDescription = "avatar image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically),
                onLoading = {

                })
            Text(
                text = username,
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                fontSize = 16.sp,
                color = AppTheme.colors.textColor
            )
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun MainUserCardPreview() {
    MainUserCard(
        "苦柠", "https://x19.fp.ps.netease.com/file/65967c76a72130654744d818gJc5iJKi05"
    )
}