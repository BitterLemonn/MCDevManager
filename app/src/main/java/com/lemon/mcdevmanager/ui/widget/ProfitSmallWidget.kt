package com.lemon.mcdevmanager.ui.widget

import android.support.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.data.common.DOWNImage
import com.lemon.mcdevmanager.data.common.NORMALImage
import com.lemon.mcdevmanager.data.common.UPImage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.IconDark
import com.lemon.mcdevmanager.ui.theme.IconLight
import com.lemon.mcdevmanager.utils.pxToDp
import com.orhanobut.logger.Logger
import java.util.Base64

@Composable
fun ProfitSmallWidget(
    @DrawableRes icon: Int,
    mainText: String,
    mainNum: Int,
    subText: String,
    subNum: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val upImageBytes by remember { mutableStateOf(Base64.getDecoder().decode(UPImage)) }
    val normalImageBytes by remember { mutableStateOf(Base64.getDecoder().decode(NORMALImage)) }
    val downImageBytes by remember { mutableStateOf(Base64.getDecoder().decode(DOWNImage)) }

    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels

    var largeTextSize by remember { mutableStateOf(24.sp) }
    var mediumTextSize by remember { mutableStateOf(18.sp) }
    var smallTextSize by remember { mutableStateOf(14.sp) }
    var smallestTextSize by remember { mutableStateOf(12.sp) }

    LaunchedEffect(key1 = Unit) {
        var offset = (400 - screenWidth) / 50
        if (offset < 0) offset = 0
        largeTextSize = (24 - 2 * offset).sp
        mediumTextSize = (18 - 2 * offset).sp
        smallTextSize = (14 - 2 * offset).sp
        smallestTextSize = (12 - 2 * offset).sp
    }

    Box(
        modifier = Modifier.then(modifier)
    ) {
        Box(modifier = Modifier.height(24.dp)) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "icon",
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
                colorFilter = ColorFilter.lighting(
                    multiply = if (isSystemInDarkTheme()) IconDark else IconLight,
                    add = Color.Transparent
                )
            )
        }
        Column(modifier = Modifier.padding(start = 20.dp)) {
            Text(
                text = mainText,
                fontSize = mediumTextSize,
                color = AppTheme.colors.textColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Box(modifier = Modifier.heightIn(min = 24.dp)) {
                    Text(
                        text = mainNum.toString(),
                        fontFamily = Font(R.font.minecraft_ae).toFontFamily(),
                        fontSize = if (mainNum.toString().length < 6) largeTextSize else mediumTextSize,
                        color = AppTheme.colors.textColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context).data(
                        when (if (mainNum > subNum) 1 else if (mainNum < subNum) -1 else 0) {
                            1 -> upImageBytes
                            -1 -> downImageBytes
                            else -> normalImageBytes
                        }
                    ).build(),
                    contentDescription = "indicator",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterVertically),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.lighting(
                        multiply = if (isSystemInDarkTheme()) IconDark else IconLight,
                        add = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .heightIn(min = 18.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                if (subNum.toString().length + subText.length < 12)
                    Row(Modifier.align(Alignment.Center)) {
                        Text(
                            text = subText,
                            fontSize = smallTextSize,
                            color = AppTheme.colors.textColor,
                            letterSpacing = 2.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = subNum.toString(),
                            fontSize = smallTextSize,
                            color = AppTheme.colors.textColor,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                else
                    Column(Modifier.align(Alignment.Center)) {
                        Text(
                            text = subText,
                            fontSize = smallTextSize,
                            color = AppTheme.colors.textColor,
                            letterSpacing = 1.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = subNum.toString(),
                            fontSize = smallTextSize,
                            color = AppTheme.colors.textColor,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfitSmallWidgetPreview() {
    ProfitSmallWidget(
        icon = R.drawable.ic_diamond,
        mainText = "本月钻石收益",
        mainNum = 3000,
        subText = "上月钻石收益",
        subNum = 15000000
    )
}