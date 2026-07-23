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

val IconPack.Analyze: ImageVector
    get() {
        if (_analyze != null) {
            return _analyze!!
        }
        _analyze = Builder(
            name = "Analyze", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(192.0f, 128.0f)
                horizontalLineToRelative(640.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, 64.0f, 64.0f)
                verticalLineToRelative(640.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, -64.0f, 64.0f)
                lineTo(192.0f, 896.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, -64.0f, -64.0f)
                lineTo(128.0f, 192.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, 64.0f, -64.0f)
                close()
                moveTo(256.0f, 448.0f)
                verticalLineToRelative(320.0f)
                horizontalLineToRelative(128.0f)
                lineTo(384.0f, 448.0f)
                lineTo(256.0f, 448.0f)
                close()
                moveTo(448.0f, 320.0f)
                verticalLineToRelative(448.0f)
                horizontalLineToRelative(128.0f)
                lineTo(576.0f, 320.0f)
                lineTo(448.0f, 320.0f)
                close()
                moveTo(640.0f, 512.0f)
                verticalLineToRelative(256.0f)
                horizontalLineToRelative(128.0f)
                lineTo(768.0f, 512.0f)
                horizontalLineToRelative(-128.0f)
                close()
            }
        }
            .build()
        return _analyze!!
    }

private var _analyze: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Analyze, contentDescription = "")
    }
}
