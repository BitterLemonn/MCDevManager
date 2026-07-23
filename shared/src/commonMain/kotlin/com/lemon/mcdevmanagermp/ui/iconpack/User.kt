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

val IconPack.User: ImageVector
    get() {
        if (_user != null) {
            return _user!!
        }
        _user = Builder(
            name = "User", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(502.5f, 63.1f)
                curveToRelative(125.9f, 0.0f, 227.9f, 100.4f, 227.9f, 224.2f)
                curveToRelative(0.0f, 123.8f, -102.0f, 224.2f, -227.9f, 224.2f)
                curveToRelative(-125.9f, 0.0f, -227.9f, -100.4f, -227.9f, -224.2f)
                curveTo(274.6f, 163.5f, 376.6f, 63.1f, 502.5f, 63.1f)
                lineTo(502.5f, 63.1f)
                close()
                moveTo(502.5f, 63.1f)
                curveToRelative(125.9f, 0.0f, 227.9f, 100.4f, 227.9f, 224.2f)
                curveToRelative(0.0f, 123.8f, -102.0f, 224.2f, -227.9f, 224.2f)
                curveToRelative(-125.9f, 0.0f, -227.9f, -100.4f, -227.9f, -224.2f)
                curveTo(274.6f, 163.5f, 376.6f, 63.1f, 502.5f, 63.1f)
                lineTo(502.5f, 63.1f)
                close()
                moveTo(417.0f, 586.3f)
                lineToRelative(190.0f, 0.0f)
                curveToRelative(162.6f, 0.0f, 294.4f, 129.6f, 294.4f, 289.6f)
                lineToRelative(0.0f, 18.7f)
                curveToRelative(0.0f, 63.0f, -131.8f, 65.4f, -294.4f, 65.4f)
                lineToRelative(-190.0f, 0.0f)
                curveToRelative(-162.6f, 0.0f, -294.4f, -0.1f, -294.4f, -65.4f)
                lineToRelative(0.0f, -18.7f)
                curveTo(122.6f, 715.9f, 254.4f, 586.3f, 417.0f, 586.3f)
                lineTo(417.0f, 586.3f)
                close()
                moveTo(417.0f, 586.3f)
            }
        }
            .build()
        return _user!!
    }

private var _user: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.User, contentDescription = "")
    }
}
