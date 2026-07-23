package com.lemon.mcdevmanagermp.ui.iconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import kotlin.Unit

val IconPack.Calendar: ImageVector
    get() {
        if (_calendar != null) {
            return _calendar!!
        }
        _calendar = Builder(
            name = "Calendar", defaultWidth = 200.0.dp, defaultHeight =
                200.0.dp, viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(64.0f, 409.6f)
                verticalLineToRelative(467.2f)
                curveToRelative(0.0f, 19.2f, 6.4f, 38.4f, 19.2f, 57.6f)
                curveToRelative(12.8f, 19.2f, 38.4f, 32.0f, 64.0f, 32.0f)
                horizontalLineToRelative(729.6f)
                curveToRelative(32.0f, 0.0f, 57.6f, -19.2f, 76.8f, -44.8f)
                curveToRelative(6.4f, -12.8f, 6.4f, -25.6f, 6.4f, -38.4f)
                lineTo(960.0f, 409.6f)
                lineTo(64.0f, 409.6f)
                close()
                moveTo(672.0f, 569.6f)
                curveToRelative(-6.4f, 0.0f, -12.8f, 6.4f, -19.2f, 6.4f)
                lineTo(364.8f, 576.0f)
                curveToRelative(-12.8f, 0.0f, -19.2f, -12.8f, -25.6f, -25.6f)
                curveToRelative(0.0f, -6.4f, 0.0f, -12.8f, 6.4f, -19.2f)
                curveToRelative(6.4f, -6.4f, 12.8f, -6.4f, 19.2f, -6.4f)
                horizontalLineToRelative(288.0f)
                curveToRelative(12.8f, 0.0f, 25.6f, 12.8f, 25.6f, 25.6f)
                curveToRelative(0.0f, 6.4f, 0.0f, 12.8f, -6.4f, 19.2f)
                close()
                moveTo(947.2f, 172.8f)
                curveToRelative(-19.2f, -19.2f, -38.4f, -38.4f, -70.4f, -38.4f)
                horizontalLineToRelative(-121.6f)
                lineTo(755.2f, 256.0f)
                curveToRelative(0.0f, 25.6f, -25.6f, 51.2f, -51.2f, 51.2f)
                reflectiveCurveToRelative(-51.2f, -25.6f, -51.2f, -51.2f)
                lineTo(652.8f, 140.8f)
                lineTo(371.2f, 140.8f)
                lineTo(371.2f, 256.0f)
                curveToRelative(0.0f, 25.6f, -25.6f, 51.2f, -51.2f, 51.2f)
                curveToRelative(-32.0f, 0.0f, -51.2f, -25.6f, -51.2f, -51.2f)
                lineTo(268.8f, 140.8f)
                lineTo(153.6f, 140.8f)
                horizontalLineToRelative(-6.4f)
                curveToRelative(-32.0f, 0.0f, -64.0f, 19.2f, -76.8f, 51.2f)
                curveToRelative(-6.4f, 12.8f, -6.4f, 32.0f, -6.4f, 44.8f)
                verticalLineToRelative(121.6f)
                horizontalLineToRelative(896.0f)
                lineTo(960.0f, 224.0f)
                curveToRelative(0.0f, -19.2f, -6.4f, -32.0f, -12.8f, -51.2f)
                close()
                moveTo(947.2f, 172.8f)
            }
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(294.4f, 89.6f)
                lineTo(294.4f, 262.4f)
                curveToRelative(0.0f, 6.4f, 0.0f, 12.8f, 6.4f, 19.2f)
                lineTo(320.0f, 281.6f)
                curveToRelative(12.8f, 0.0f, 25.6f, -12.8f, 25.6f, -25.6f)
                lineTo(345.6f, 108.8f)
                verticalLineToRelative(-19.2f)
                curveToRelative(0.0f, -6.4f, 0.0f, -12.8f, -6.4f, -19.2f)
                curveTo(332.8f, 64.0f, 326.4f, 64.0f, 320.0f, 64.0f)
                curveToRelative(-12.8f, 0.0f, -25.6f, 12.8f, -25.6f, 25.6f)
                close()
                moveTo(678.4f, 89.6f)
                lineTo(678.4f, 262.4f)
                curveToRelative(0.0f, 6.4f, 0.0f, 12.8f, 6.4f, 19.2f)
                curveToRelative(6.4f, 6.4f, 12.8f, 6.4f, 19.2f, 6.4f)
                curveToRelative(12.8f, 0.0f, 25.6f, -12.8f, 25.6f, -25.6f)
                lineTo(729.6f, 108.8f)
                verticalLineToRelative(-19.2f)
                curveToRelative(0.0f, -6.4f, 0.0f, -12.8f, -6.4f, -19.2f)
                curveToRelative(-6.4f, -6.4f, -12.8f, -6.4f, -19.2f, -6.4f)
                curveToRelative(-12.8f, 0.0f, -25.6f, 12.8f, -25.6f, 25.6f)
                close()
                moveTo(678.4f, 89.6f)
            }
        }
            .build()
        return _calendar!!
    }

private var _calendar: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Calendar, contentDescription = "")
    }
}
