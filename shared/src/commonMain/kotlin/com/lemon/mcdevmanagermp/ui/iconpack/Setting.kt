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

val IconPack.Setting: ImageVector
    get() {
        if (_setting != null) {
            return _setting!!
        }
        _setting = Builder(
            name = "Setting", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(940.0f, 596.0f)
                lineToRelative(-76.0f, -57.6f)
                curveToRelative(0.8f, -8.0f, 1.6f, -16.8f, 1.6f, -26.4f)
                reflectiveCurveToRelative(-0.8f, -18.4f, -1.6f, -26.4f)
                lineToRelative(76.0f, -57.6f)
                curveToRelative(20.8f, -16.0f, 26.4f, -44.0f, 12.8f, -68.0f)
                lineTo(868.0f, 216.8f)
                curveToRelative(-9.6f, -16.8f, -28.0f, -27.2f, -47.2f, -27.2f)
                curveToRelative(-6.4f, 0.0f, -12.0f, 0.8f, -18.4f, 3.2f)
                lineTo(712.0f, 228.0f)
                curveToRelative(-15.2f, -10.4f, -31.2f, -19.2f, -47.2f, -26.4f)
                lineToRelative(-13.6f, -92.0f)
                curveToRelative(-4.0f, -26.4f, -26.4f, -45.6f, -53.6f, -45.6f)
                lineTo(426.4f, 64.0f)
                curveToRelative(-27.2f, 0.0f, -49.6f, 19.2f, -53.6f, 44.8f)
                lineTo(360.0f, 201.6f)
                curveToRelative(-16.0f, 7.2f, -31.2f, 16.0f, -47.2f, 26.4f)
                lineToRelative(-90.4f, -35.2f)
                curveToRelative(-6.4f, -2.4f, -12.8f, -3.2f, -19.2f, -3.2f)
                curveToRelative(-19.2f, 0.0f, -37.6f, 9.6f, -46.4f, 26.4f)
                lineTo(71.2f, 360.0f)
                curveToRelative(-13.6f, 22.4f, -8.0f, 52.0f, 12.8f, 68.0f)
                lineToRelative(76.0f, 57.6f)
                curveToRelative(-0.8f, 9.6f, -1.6f, 18.4f, -1.6f, 26.4f)
                reflectiveCurveToRelative(0.0f, 16.8f, 1.6f, 26.4f)
                lineTo(84.0f, 596.0f)
                curveToRelative(-20.8f, 16.0f, -26.4f, 44.0f, -12.8f, 68.0f)
                lineTo(156.0f, 807.2f)
                curveToRelative(9.6f, 16.8f, 28.0f, 27.2f, 47.2f, 27.2f)
                curveToRelative(6.4f, 0.0f, 12.0f, -0.8f, 18.4f, -3.2f)
                lineTo(312.0f, 796.0f)
                curveToRelative(15.2f, 10.4f, 31.2f, 19.2f, 47.2f, 26.4f)
                lineToRelative(13.6f, 92.0f)
                curveTo(376.0f, 940.0f, 399.2f, 960.0f, 426.4f, 960.0f)
                horizontalLineToRelative(171.2f)
                curveToRelative(27.2f, 0.0f, 49.6f, -19.2f, 53.6f, -44.8f)
                lineToRelative(13.6f, -92.8f)
                curveToRelative(16.0f, -7.2f, 31.2f, -16.0f, 47.2f, -26.4f)
                lineToRelative(90.4f, 35.2f)
                curveToRelative(6.4f, 2.4f, 12.8f, 3.2f, 19.2f, 3.2f)
                curveToRelative(19.2f, 0.0f, 37.6f, -9.6f, 46.4f, -26.4f)
                lineToRelative(85.6f, -144.8f)
                curveTo(966.4f, 640.0f, 960.8f, 612.0f, 940.0f, 596.0f)
                close()
                moveTo(704.0f, 512.0f)
                curveToRelative(0.0f, 105.6f, -86.4f, 192.0f, -192.0f, 192.0f)
                reflectiveCurveToRelative(-192.0f, -86.4f, -192.0f, -192.0f)
                reflectiveCurveToRelative(86.4f, -192.0f, 192.0f, -192.0f)
                reflectiveCurveToRelative(192.0f, 86.4f, 192.0f, 192.0f)
                close()
            }
        }
            .build()
        return _setting!!
    }

private var _setting: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Setting, contentDescription = "")
    }
}
