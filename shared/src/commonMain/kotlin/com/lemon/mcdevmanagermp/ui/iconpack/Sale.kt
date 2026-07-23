package com.lemon.mcdevmanagermp.ui.iconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import kotlin.Unit

val IconPack.Sale: ImageVector
    get() {
        if (_sale != null) {
            return _sale!!
        }
        _sale = Builder(
            name = "Sale", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(303.4f, 96.5f)
                curveToRelative(22.4f, -18.2f, 51.0f, -28.4f, 79.8f, -28.6f)
                curveToRelative(28.1f, -0.5f, 56.3f, 8.5f, 79.1f, 25.0f)
                curveToRelative(9.3f, 6.7f, 17.5f, 14.7f, 25.2f, 23.1f)
                curveToRelative(13.6f, 15.1f, 24.6f, 32.5f, 32.3f, 51.3f)
                curveToRelative(7.7f, -18.9f, 18.9f, -36.5f, 32.6f, -51.6f)
                curveToRelative(8.3f, -9.0f, 17.1f, -17.6f, 27.3f, -24.5f)
                curveToRelative(14.1f, -9.7f, 30.1f, -16.7f, 46.9f, -20.3f)
                curveToRelative(30.7f, -6.8f, 63.9f, -2.1f, 91.3f, 13.3f)
                curveToRelative(25.3f, 14.0f, 45.6f, 36.7f, 56.5f, 63.5f)
                curveToRelative(13.6f, 32.7f, 12.9f, 70.8f, -1.5f, 103.1f)
                curveToRelative(24.9f, 0.0f, 49.7f, -0.1f, 74.6f, 0.1f)
                curveToRelative(25.6f, 0.4f, 51.0f, 9.5f, 70.9f, 25.7f)
                curveToRelative(19.5f, 15.7f, 33.8f, 37.8f, 39.8f, 62.1f)
                curveToRelative(3.0f, 11.6f, 3.8f, 23.6f, 3.6f, 35.5f)
                verticalLineToRelative(87.2f)
                curveToRelative(-0.1f, 12.9f, -2.3f, 25.7f, -6.6f, 37.8f)
                curveToRelative(-8.5f, 24.2f, -25.3f, 45.4f, -46.9f, 59.2f)
                curveToRelative(-18.5f, 12.0f, -40.4f, 18.3f, -62.4f, 18.5f)
                horizontalLineTo(542.8f)
                verticalLineTo(297.7f)
                curveToRelative(-15.5f, -0.2f, -30.9f, -0.2f, -46.4f, 0.0f)
                curveToRelative(-0.1f, 93.0f, 0.0f, 186.1f, 0.0f, 279.1f)
                horizontalLineTo(193.9f)
                curveToRelative(-12.2f, -0.1f, -24.5f, -1.9f, -36.1f, -5.7f)
                curveToRelative(-27.1f, -8.7f, -50.6f, -27.9f, -64.8f, -52.7f)
                curveToRelative(-10.0f, -17.2f, -15.2f, -37.0f, -15.5f, -56.9f)
                verticalLineToRelative(-94.6f)
                curveToRelative(0.3f, -10.1f, 1.4f, -20.3f, 4.1f, -30.1f)
                curveToRelative(6.9f, -25.7f, 23.0f, -48.7f, 44.6f, -64.1f)
                curveToRelative(19.1f, -13.8f, 42.5f, -21.4f, 66.1f, -21.7f)
                curveToRelative(24.7f, -0.1f, 49.4f, 0.0f, 74.1f, 0.0f)
                curveToRelative(-12.6f, -28.6f, -14.8f, -61.7f, -5.5f, -91.6f)
                curveToRelative(7.6f, -24.8f, 22.6f, -46.9f, 42.5f, -62.9f)
                moveToRelative(66.0f, 19.5f)
                curveToRelative(-20.0f, 3.6f, -38.4f, 15.1f, -50.6f, 31.3f)
                curveToRelative(-11.8f, 15.3f, -17.6f, 34.9f, -16.7f, 54.1f)
                curveToRelative(0.7f, 17.8f, 7.6f, 35.1f, 18.3f, 49.2f)
                horizontalLineToRelative(164.8f)
                curveToRelative(4.9f, -35.5f, -6.4f, -72.8f, -29.7f, -100.0f)
                curveToRelative(-8.4f, -9.7f, -17.8f, -19.0f, -29.2f, -25.2f)
                curveToRelative(-17.1f, -9.7f, -37.6f, -13.2f, -56.9f, -9.4f)
                moveToRelative(264.7f, 1.0f)
                curveToRelative(-16.0f, 3.9f, -30.7f, 12.7f, -41.9f, 24.8f)
                curveToRelative(-28.8f, 27.6f, -43.2f, 69.2f, -38.0f, 108.7f)
                curveToRelative(54.9f, 0.0f, 109.9f, 0.1f, 164.8f, 0.0f)
                curveToRelative(13.1f, -16.6f, 20.0f, -38.3f, 18.3f, -59.5f)
                curveToRelative(-1.2f, -15.4f, -6.7f, -30.5f, -15.9f, -42.9f)
                curveToRelative(-9.4f, -12.7f, -22.5f, -22.7f, -37.3f, -28.2f)
                curveToRelative(-15.8f, -6.0f, -33.5f, -7.0f, -50.0f, -2.9f)
                close()
                moveTo(147.6f, 622.8f)
                horizontalLineToRelative(349.0f)
                verticalLineToRelative(333.3f)
                horizontalLineTo(284.9f)
                curveToRelative(-9.8f, -0.1f, -19.6f, 0.3f, -29.4f, -0.3f)
                curveToRelative(-30.0f, -2.0f, -58.8f, -16.2f, -78.6f, -38.7f)
                curveToRelative(-16.4f, -18.3f, -26.7f, -41.9f, -28.8f, -66.4f)
                curveToRelative(-0.7f, -6.1f, -0.4f, -12.2f, -0.5f, -18.3f)
                curveToRelative(0.0f, -69.8f, -0.1f, -139.7f, 0.0f, -209.6f)
                close()
                moveTo(543.0f, 622.8f)
                horizontalLineToRelative(349.2f)
                verticalLineToRelative(215.7f)
                curveToRelative(0.0f, 16.6f, -3.2f, 33.1f, -10.0f, 48.3f)
                curveToRelative(-12.9f, 29.4f, -38.6f, 53.0f, -69.1f, 63.1f)
                curveToRelative(-11.1f, 3.8f, -22.7f, 5.8f, -34.4f, 6.1f)
                horizontalLineTo(543.0f)
                verticalLineTo(622.8f)
                close()
            }
        }
            .build()
        return _sale!!
    }

private var _sale: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Sale, contentDescription = "")
    }
}
