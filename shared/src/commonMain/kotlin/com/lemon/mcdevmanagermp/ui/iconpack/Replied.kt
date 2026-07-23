package com.lemon.mcdevmanagermp.ui.iconpack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import kotlin.Unit

val IconPack.Replied: ImageVector
    get() {
        if (_replied != null) {
            return _replied!!
        }
        _replied = Builder(
            name = "Replied", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = EvenOdd
            ) {
                moveTo(512.0f, 64.0f)
                curveTo(264.6f, 64.0f, 64.0f, 264.6f, 64.0f, 512.0f)
                reflectiveCurveToRelative(200.6f, 448.0f, 448.0f, 448.0f)
                reflectiveCurveToRelative(448.0f, -200.6f, 448.0f, -448.0f)
                reflectiveCurveTo(759.4f, 64.0f, 512.0f, 64.0f)
                close()
                moveTo(732.6f, 353.6f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, -56.6f, 0.0f)
                lineTo(444.1f, 585.5f)
                lineTo(348.0f, 489.4f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, -56.6f, 0.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, 0.0f, 56.6f)
                lineToRelative(124.4f, 124.4f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, 56.6f, 0.0f)
                lineToRelative(260.2f, -260.2f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, 0.0f, -56.6f)
                close()
            }
        }
            .build()
        return _replied!!
    }

private var _replied: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Replied, contentDescription = "")
    }
}
