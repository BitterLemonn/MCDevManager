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

val IconPack.Total: ImageVector
    get() {
        if (_total != null) {
            return _total!!
        }
        _total = Builder(
            name = "Total", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(384.0f, 426.7f)
                horizontalLineTo(213.3f)
                curveToRelative(-46.9f, 0.0f, -85.3f, -38.4f, -85.3f, -85.3f)
                verticalLineTo(213.3f)
                curveToRelative(0.0f, -46.9f, 38.4f, -85.3f, 85.3f, -85.3f)
                horizontalLineToRelative(170.7f)
                curveToRelative(46.9f, 0.0f, 85.3f, 38.4f, 85.3f, 85.3f)
                verticalLineToRelative(128.0f)
                curveToRelative(0.0f, 46.9f, -38.4f, 85.3f, -85.3f, 85.3f)
                close()
                moveTo(384.0f, 896.0f)
                horizontalLineTo(213.3f)
                curveToRelative(-46.9f, 0.0f, -85.3f, -38.4f, -85.3f, -85.3f)
                verticalLineToRelative(-213.3f)
                curveToRelative(0.0f, -46.9f, 38.4f, -85.3f, 85.3f, -85.3f)
                horizontalLineToRelative(170.7f)
                curveToRelative(46.9f, 0.0f, 85.3f, 38.4f, 85.3f, 85.3f)
                verticalLineToRelative(213.3f)
                curveToRelative(0.0f, 46.9f, -38.4f, 85.3f, -85.3f, 85.3f)
                close()
                moveTo(810.7f, 512.0f)
                horizontalLineToRelative(-170.7f)
                curveToRelative(-46.9f, 0.0f, -85.3f, -38.4f, -85.3f, -85.3f)
                verticalLineTo(213.3f)
                curveToRelative(0.0f, -46.9f, 38.4f, -85.3f, 85.3f, -85.3f)
                horizontalLineToRelative(170.7f)
                curveToRelative(46.9f, 0.0f, 85.3f, 38.4f, 85.3f, 85.3f)
                verticalLineToRelative(213.3f)
                curveToRelative(0.0f, 46.9f, -38.4f, 85.3f, -85.3f, 85.3f)
                close()
                moveTo(810.7f, 896.0f)
                horizontalLineToRelative(-170.7f)
                curveToRelative(-46.9f, 0.0f, -85.3f, -38.4f, -85.3f, -85.3f)
                verticalLineToRelative(-128.0f)
                curveToRelative(0.0f, -46.9f, 38.4f, -85.3f, 85.3f, -85.3f)
                horizontalLineToRelative(170.7f)
                curveToRelative(46.9f, 0.0f, 85.3f, 38.4f, 85.3f, 85.3f)
                verticalLineToRelative(128.0f)
                curveToRelative(0.0f, 46.9f, -38.4f, 85.3f, -85.3f, 85.3f)
                close()
            }
        }
            .build()
        return _total!!
    }

private var _total: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Total, contentDescription = "")
    }
}
