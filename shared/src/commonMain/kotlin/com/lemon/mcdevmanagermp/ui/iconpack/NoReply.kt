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

val IconPack.NoReply: ImageVector
    get() {
        if (_noReply != null) {
            return _noReply!!
        }
        _noReply = Builder(
            name = "NoReply", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(510.1f, 928.0f)
                horizontalLineToRelative(5.5f)
                curveToRelative(52.6f, -0.7f, 96.7f, -38.4f, 103.3f, -88.5f)
                horizontalLineTo(406.8f)
                curveToRelative(6.6f, 50.1f, 50.7f, 87.9f, 103.3f, 88.5f)
                close()
                moveTo(771.7f, 598.5f)
                verticalLineTo(410.9f)
                curveToRelative(0.6f, -105.3f, -70.9f, -197.0f, -172.2f, -220.8f)
                verticalLineToRelative(-4.5f)
                curveToRelative(0.8f, -31.7f, -15.5f, -61.4f, -42.5f, -77.6f)
                curveToRelative(-27.1f, -16.1f, -60.6f, -16.1f, -87.7f, 0.0f)
                reflectiveCurveToRelative(-43.3f, 45.8f, -42.5f, 77.6f)
                verticalLineToRelative(4.5f)
                curveTo(325.2f, 213.7f, 253.4f, 305.5f, 254.0f, 410.9f)
                verticalLineToRelative(187.6f)
                curveToRelative(-51.9f, 41.3f, -83.2f, 103.5f, -85.9f, 170.2f)
                horizontalLineToRelative(689.5f)
                curveToRelative(-2.6f, -66.7f, -34.0f, -128.9f, -85.9f, -170.2f)
                close()
            }
        }
            .build()
        return _noReply!!
    }

private var _noReply: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.NoReply, contentDescription = "")
    }
}
