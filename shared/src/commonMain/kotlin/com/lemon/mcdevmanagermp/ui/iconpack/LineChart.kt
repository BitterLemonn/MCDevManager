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

val IconPack.LineChart: ImageVector
    get() {
        if (_lineChart != null) {
            return _lineChart!!
        }
        _lineChart = Builder(
            name = "LineChart", defaultWidth = 200.0.dp, defaultHeight =
                200.0.dp, viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(896.0f, 896.0f)
                horizontalLineTo(96.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -32.0f, -32.0f)
                verticalLineTo(224.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 64.0f, 0.0f)
                verticalLineToRelative(608.0f)
                horizontalLineToRelative(768.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, true, 0.0f, 64.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(247.0f, 640.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -21.0f, -56.2f)
                lineToRelative(201.0f, -174.2f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 42.3f, 0.3f)
                lineToRelative(172.1f, 153.4f)
                lineToRelative(229.1f, -246.3f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 46.9f, 43.6f)
                lineToRelative(-250.4f, 269.2f)
                arcToRelative(31.9f, 31.9f, 0.0f, false, true, -44.7f, 2.1f)
                lineToRelative(-174.6f, -155.5f)
                lineToRelative(-179.7f, 155.8f)
                arcToRelative(31.9f, 31.9f, 0.0f, false, true, -20.9f, 7.8f)
                close()
            }
        }
            .build()
        return _lineChart!!
    }

private var _lineChart: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.LineChart, contentDescription = "")
    }
}
