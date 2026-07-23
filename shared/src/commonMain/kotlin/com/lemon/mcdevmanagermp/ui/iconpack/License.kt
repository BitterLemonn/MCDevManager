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

val IconPack.License: ImageVector
    get() {
        if (_license != null) {
            return _license!!
        }
        _license = Builder(
            name = "License", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(284.4f, 967.1f)
                curveToRelative(0.0f, 28.6f, 28.4f, 56.9f, 56.9f, 56.9f)
                curveToRelative(6.9f, 0.0f, 28.4f, 0.0f, 56.9f, -28.3f)
                lineTo(539.5f, 853.3f)
                lineTo(682.7f, 995.7f)
                curveToRelative(28.4f, 28.3f, 43.0f, 28.3f, 56.9f, 28.3f)
                curveToRelative(28.4f, 0.0f, 56.9f, -28.3f, 56.9f, -56.9f)
                verticalLineToRelative(-227.6f)
                curveToRelative(-69.5f, 47.4f, -159.7f, 85.3f, -257.0f, 85.3f)
                curveToRelative(-90.3f, 0.0f, -178.6f, -37.9f, -255.0f, -85.3f)
                verticalLineToRelative(227.6f)
                close()
                moveTo(540.4f, 739.6f)
                arcToRelative(369.8f, 369.8f, 0.0f, true, false, 0.0f, -739.6f)
                arcToRelative(369.8f, 369.8f, 0.0f, false, false, 0.0f, 739.6f)
                close()
                moveTo(540.4f, 625.8f)
                arcToRelative(256.0f, 256.0f, 0.0f, true, false, 0.0f, -512.0f)
                arcToRelative(256.0f, 256.0f, 0.0f, false, false, 0.0f, 512.0f)
                close()
            }
        }
            .build()
        return _license!!
    }

private var _license: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.License, contentDescription = "")
    }
}
