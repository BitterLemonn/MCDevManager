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

val IconPack.Dashboard: ImageVector
    get() {
        if (_dashboard != null) {
            return _dashboard!!
        }
        _dashboard = Builder(
            name = "Dashboard", defaultWidth = 200.0.dp, defaultHeight =
                200.0.dp, viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(512.0f, 34.1f)
                curveTo(249.2f, 34.1f, 34.1f, 249.2f, 34.1f, 512.0f)
                reflectiveCurveToRelative(215.0f, 477.9f, 477.9f, 477.9f)
                reflectiveCurveToRelative(477.9f, -215.0f, 477.9f, -477.9f)
                reflectiveCurveTo(774.8f, 34.1f, 512.0f, 34.1f)
                close()
                moveTo(522.2f, 624.6f)
                curveToRelative(-41.0f, 0.0f, -78.5f, -22.2f, -99.0f, -58.0f)
                curveToRelative(0.0f, 0.0f, 0.0f, -1.7f, -1.7f, -1.7f)
                lineTo(290.1f, 327.7f)
                curveToRelative(-5.1f, -10.2f, -3.4f, -22.2f, 3.4f, -30.7f)
                curveToRelative(8.5f, -8.5f, 20.5f, -10.2f, 30.7f, -5.1f)
                lineTo(559.8f, 409.6f)
                lineToRelative(20.5f, 10.2f)
                horizontalLineToRelative(1.7f)
                curveToRelative(32.4f, 20.5f, 51.2f, 56.3f, 51.2f, 93.9f)
                curveToRelative(0.0f, 59.7f, -49.5f, 110.9f, -110.9f, 110.9f)
                close()
            }
        }
            .build()
        return _dashboard!!
    }

private var _dashboard: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Dashboard, contentDescription = "")
    }
}
