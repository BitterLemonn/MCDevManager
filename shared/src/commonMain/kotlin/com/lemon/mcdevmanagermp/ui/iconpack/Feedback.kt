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

val IconPack.Feedback: ImageVector
    get() {
        if (_feedback != null) {
            return _feedback!!
        }
        _feedback = Builder(
            name = "Feedback", defaultWidth = 200.0.dp, defaultHeight =
                200.0.dp, viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(896.0f, 117.3f)
                arcTo(32.0f, 32.0f, 0.0f, false, true, 928.0f, 149.3f)
                verticalLineToRelative(618.7f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -32.0f, 32.0f)
                lineTo(654.3f, 800.0f)
                lineToRelative(-117.6f, 118.5f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -42.8f, 2.4f)
                lineToRelative(-2.5f, -2.2f)
                lineToRelative(-119.8f, -118.7f)
                lineTo(128.0f, 800.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -31.9f, -28.9f)
                lineTo(96.0f, 768.0f)
                lineTo(96.0f, 149.3f)
                arcTo(32.0f, 32.0f, 0.0f, false, true, 128.0f, 117.3f)
                close()
                moveTo(544.0f, 597.3f)
                horizontalLineToRelative(-64.0f)
                verticalLineToRelative(64.0f)
                horizontalLineToRelative(64.0f)
                verticalLineToRelative(-64.0f)
                close()
                moveTo(544.0f, 266.7f)
                horizontalLineToRelative(-64.0f)
                verticalLineToRelative(256.0f)
                horizontalLineToRelative(64.0f)
                verticalLineToRelative(-256.0f)
                close()
            }
        }
            .build()
        return _feedback!!
    }

private var _feedback: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Feedback, contentDescription = "")
    }
}
