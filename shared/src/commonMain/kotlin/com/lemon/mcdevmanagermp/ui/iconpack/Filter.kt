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

val IconPack.Filter: ImageVector
    get() {
        if (_filter != null) {
            return _filter!!
        }
        _filter = Builder(
            name = "Filter", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(436.8f, 514.7f)
                lineToRelative(-213.4f, -249.0f)
                arcTo(64.0f, 64.0f, 0.0f, false, true, 272.0f, 160.0f)
                horizontalLineToRelative(489.7f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, 48.6f, 105.6f)
                lineToRelative(-213.4f, 249.0f)
                verticalLineToRelative(191.8f)
                arcToRelative(128.0f, 128.0f, 0.0f, false, true, -48.0f, 100.0f)
                lineTo(488.8f, 854.4f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, -52.0f, -25.0f)
                verticalLineTo(514.7f)
                close()
            }
        }
            .build()
        return _filter!!
    }

private var _filter: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Filter, contentDescription = "")
    }
}
