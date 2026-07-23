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

val IconPack.Comment: ImageVector
    get() {
        if (_comment != null) {
            return _comment!!
        }
        _comment = Builder(
            name = "Comment", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(867.3f, 784.6f)
                lineTo(681.2f, 784.6f)
                curveToRelative(-62.6f, 60.7f, -158.6f, 169.6f, -158.6f, 169.6f)
                curveToRelative(-5.8f, 6.1f, -15.3f, 6.1f, -21.1f, 0.0f)
                curveToRelative(0.0f, 0.0f, -131.2f, -135.8f, -162.9f, -169.6f)
                lineTo(156.8f, 784.6f)
                curveToRelative(-67.4f, 0.0f, -122.0f, -63.0f, -122.0f, -131.4f)
                lineTo(34.7f, 189.1f)
                curveToRelative(0.0f, -68.4f, 53.4f, -123.8f, 119.3f, -123.8f)
                lineToRelative(715.9f, 0.0f)
                curveToRelative(65.9f, 0.0f, 119.3f, 55.4f, 119.3f, 123.8f)
                lineToRelative(0.0f, 464.2f)
                curveTo(989.3f, 721.6f, 934.7f, 784.6f, 867.3f, 784.6f)
                lineTo(867.3f, 784.6f)
                close()
                moveTo(273.4f, 373.6f)
                curveToRelative(-32.9f, 0.0f, -59.7f, 27.7f, -59.7f, 61.9f)
                curveToRelative(0.0f, 34.2f, 26.7f, 61.9f, 59.7f, 61.9f)
                reflectiveCurveToRelative(59.7f, -27.7f, 59.7f, -61.9f)
                curveTo(333.0f, 401.4f, 306.3f, 373.6f, 273.4f, 373.6f)
                lineTo(273.4f, 373.6f)
                close()
                moveTo(512.0f, 373.6f)
                curveToRelative(-32.9f, 0.0f, -59.7f, 27.7f, -59.7f, 61.9f)
                curveToRelative(0.0f, 34.2f, 26.7f, 61.9f, 59.7f, 61.9f)
                curveToRelative(32.9f, 0.0f, 59.7f, -27.7f, 59.7f, -61.9f)
                curveTo(571.7f, 401.4f, 544.9f, 373.6f, 512.0f, 373.6f)
                lineTo(512.0f, 373.6f)
                close()
                moveTo(750.6f, 373.6f)
                curveToRelative(-32.9f, 0.0f, -59.7f, 27.7f, -59.7f, 61.9f)
                curveToRelative(0.0f, 34.2f, 26.7f, 61.9f, 59.7f, 61.9f)
                curveToRelative(33.0f, 0.0f, 59.7f, -27.7f, 59.7f, -61.9f)
                curveTo(810.3f, 401.4f, 783.6f, 373.6f, 750.6f, 373.6f)
                lineTo(750.6f, 373.6f)
                close()
                moveTo(750.6f, 373.6f)
            }
        }
            .build()
        return _comment!!
    }

private var _comment: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Comment, contentDescription = "")
    }
}
