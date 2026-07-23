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

val IconPack.Profit: ImageVector
    get() {
        if (_profit != null) {
            return _profit!!
        }
        _profit = Builder(
            name = "Profit", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = EvenOdd
            ) {
                moveTo(512.0f, 61.4f)
                arcToRelative(163.4f, 163.4f, 0.0f, false, true, 123.0f, 55.6f)
                lineToRelative(97.4f, -4.6f)
                arcToRelative(81.9f, 81.9f, 0.0f, false, true, 62.2f, 139.4f)
                lineToRelative(-70.6f, 71.4f)
                curveToRelative(90.2f, 51.9f, 162.4f, 135.8f, 203.5f, 237.8f)
                curveToRelative(36.0f, 89.2f, 44.2f, 189.2f, 24.7f, 300.0f)
                arcToRelative(122.9f, 122.9f, 0.0f, false, true, -121.0f, 101.6f)
                horizontalLineTo(192.8f)
                arcToRelative(122.9f, 122.9f, 0.0f, false, true, -121.0f, -101.6f)
                curveToRelative(-19.5f, -110.8f, -11.2f, -210.7f, 24.7f, -300.0f)
                curveToRelative(41.2f, -102.1f, 113.4f, -186.0f, 203.7f, -237.9f)
                lineToRelative(-70.5f, -71.3f)
                arcToRelative(81.9f, 81.9f, 0.0f, false, true, 62.2f, -139.4f)
                lineToRelative(97.3f, 4.6f)
                arcTo(163.4f, 163.4f, 0.0f, false, true, 512.0f, 61.4f)
                close()
                moveTo(572.4f, 436.2f)
                arcToRelative(30.7f, 30.7f, 0.0f, false, true, 53.2f, 30.7f)
                lineToRelative(-49.7f, 86.0f)
                lineTo(624.6f, 553.0f)
                arcToRelative(30.7f, 30.7f, 0.0f, false, true, 0.0f, 61.4f)
                horizontalLineToRelative(-81.9f)
                verticalLineToRelative(61.4f)
                horizontalLineToRelative(81.9f)
                arcToRelative(30.7f, 30.7f, 0.0f, false, true, 0.0f, 61.4f)
                horizontalLineToRelative(-81.9f)
                verticalLineToRelative(92.2f)
                arcToRelative(30.7f, 30.7f, 0.0f, false, true, -61.4f, 0.0f)
                verticalLineTo(737.3f)
                horizontalLineToRelative(-81.9f)
                arcToRelative(30.7f, 30.7f, 0.0f, false, true, 0.0f, -61.4f)
                horizontalLineToRelative(81.9f)
                verticalLineToRelative(-61.4f)
                horizontalLineToRelative(-81.9f)
                arcToRelative(30.7f, 30.7f, 0.0f, false, true, 0.0f, -61.4f)
                horizontalLineToRelative(48.7f)
                lineToRelative(-49.7f, -86.0f)
                arcToRelative(30.7f, 30.7f, 0.0f, false, true, 53.2f, -30.7f)
                lineTo(512.0f, 540.9f)
                close()
            }
        }
            .build()
        return _profit!!
    }

private var _profit: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Profit, contentDescription = "")
    }
}
