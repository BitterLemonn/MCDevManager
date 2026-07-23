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

val IconPack.Show: ImageVector
    get() {
        if (_show != null) {
            return _show!!
        }
        _show = Builder(
            name = "Show", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(513.9f, 446.5f)
                arcToRelative(64.0f, 64.0f, 0.0f, true, true, -64.0f, 64.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, 64.0f, -64.0f)
                moveToRelative(0.0f, -64.0f)
                arcToRelative(128.0f, 128.0f, 0.0f, true, false, 128.0f, 128.0f)
                arcToRelative(128.0f, 128.0f, 0.0f, false, false, -128.0f, -128.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(512.0f, 289.7f)
                curveToRelative(128.0f, 0.0f, 257.7f, 67.8f, 397.2f, 207.1f)
                arcToRelative(21.3f, 21.3f, 0.0f, false, true, 0.0f, 30.1f)
                curveTo(770.1f, 666.5f, 640.0f, 734.3f, 512.0f, 734.3f)
                reflectiveCurveToRelative(-257.3f, -67.8f, -396.8f, -207.1f)
                arcToRelative(21.3f, 21.3f, 0.0f, false, true, 0.0f, -30.1f)
                curveToRelative(139.5f, -139.5f, 268.8f, -207.4f, 396.8f, -207.4f)
                moveToRelative(0.0f, -64.0f)
                curveToRelative(-145.9f, 0.0f, -291.8f, 75.3f, -442.5f, 225.9f)
                arcToRelative(85.3f, 85.3f, 0.0f, false, false, 0.0f, 120.7f)
                curveTo(220.6f, 723.0f, 366.5f, 798.3f, 512.0f, 798.3f)
                reflectiveCurveToRelative(292.3f, -75.3f, 442.7f, -225.9f)
                arcToRelative(85.3f, 85.3f, 0.0f, false, false, 0.0f, -120.7f)
                curveTo(804.3f, 301.0f, 658.3f, 225.7f, 512.0f, 225.7f)
                close()
            }
        }
            .build()
        return _show!!
    }

private var _show: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Show, contentDescription = "")
    }
}
