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

val IconPack.Star: ImageVector
    get() {
        if (_star != null) {
            return _star!!
        }
        _star = Builder(
            name = "Star", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(314.0f, 914.3f)
                curveToRelative(-20.4f, 0.0f, -40.2f, -6.3f, -56.9f, -18.8f)
                curveToRelative(-30.3f, -21.9f, -44.9f, -58.5f, -38.7f, -95.1f)
                lineToRelative(24.0f, -141.1f)
                curveToRelative(3.1f, -18.3f, -3.1f, -36.6f, -16.2f, -49.1f)
                lineTo(123.3f, 509.9f)
                curveToRelative(-26.6f, -26.1f, -36.0f, -64.3f, -24.6f, -99.8f)
                curveToRelative(11.5f, -35.5f, 41.8f, -61.1f, 78.9f, -66.4f)
                lineToRelative(141.6f, -20.4f)
                curveToRelative(18.3f, -2.6f, 34.0f, -14.1f, 41.8f, -30.3f)
                lineToRelative(63.2f, -128.5f)
                curveTo(440.9f, 130.6f, 474.4f, 109.7f, 512.0f, 109.7f)
                reflectiveCurveToRelative(71.1f, 20.9f, 87.2f, 54.3f)
                lineTo(663.0f, 292.6f)
                curveToRelative(8.4f, 16.2f, 24.0f, 27.7f, 41.8f, 30.3f)
                lineToRelative(141.6f, 20.4f)
                curveToRelative(37.1f, 5.2f, 67.4f, 30.8f, 78.9f, 66.4f)
                curveToRelative(11.5f, 35.5f, 2.1f, 73.7f, -24.6f, 99.8f)
                lineToRelative(-102.4f, 99.8f)
                curveToRelative(-13.1f, 12.5f, -19.3f, 31.3f, -16.2f, 49.1f)
                lineToRelative(24.0f, 141.1f)
                curveToRelative(6.3f, 37.1f, -8.4f, 73.1f, -38.7f, 95.1f)
                curveToRelative(-30.3f, 21.9f, -69.5f, 24.6f, -102.4f, 7.3f)
                lineTo(538.1f, 836.4f)
                curveToRelative(-16.2f, -8.4f, -35.5f, -8.4f, -51.7f, 0.0f)
                lineToRelative(-127.0f, 66.9f)
                curveToRelative(-14.6f, 7.3f, -30.3f, 11.0f, -45.5f, 11.0f)
                close()
                moveTo(476.5f, 817.6f)
                close()
            }
        }
            .build()
        return _star!!
    }

private var _star: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Star, contentDescription = "")
    }
}
