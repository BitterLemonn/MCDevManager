package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun MainUserCard(
    username: String,
    avatarUrl: String,
    mainLevel: Int = 0,
    subLevel: Int = 0,
    levelText: String = ""
) {
    val context = LocalContext.current

    var levelImg by remember { mutableIntStateOf(0) }
    val isLoadingAvatar by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = mainLevel) {
        var levelImgStr = when (mainLevel) {
            1 -> "ic_dev1"
            2 -> "ic_dev2"
            3 -> "ic_dev3"
            4 -> "ic_dev4"
            5 -> "ic_dev5"
            else -> ""
        }
        levelImgStr += when (subLevel) {
            1 -> "_1"
            2 -> "_2"
            3 -> "_3"
            4 -> "_4"
            5 -> "_5"
            6 -> "_6"
            7 -> "_7"
            8 -> "_8"
            9 -> "_9"
            10 -> "_10"
            else -> ""
        }
        val resId = context.resources.getIdentifier(levelImgStr, "drawable", context.packageName)
        if (resId != 0) levelImg = resId
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "avatar image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterVertically),
                    placeholder = painterResource(id = R.drawable.img_avatar)
                )
                Text(
                    text = username,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor
                )
            }
            if (mainLevel != 0 && levelImg != 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = levelText,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 16.sp,
                        color = AppTheme.colors.textColor
                    )
                    Image(
                        painter = painterResource(id = levelImg),
                        contentDescription = "level image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun MainUserCardPreview() {
    MainUserCard(
        "苦柠",
        "https://x19.fp.ps.netease.com/file/65967c76a72130654744d818gJc5iJKi05",
        3,
        2,
        "杰出精英 LV.2"
    )
}