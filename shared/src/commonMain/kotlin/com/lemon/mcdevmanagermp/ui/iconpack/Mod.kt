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

val IconPack.Mod: ImageVector
    get() {
        if (_mod != null) {
            return _mod!!
        }
        _mod = Builder(
            name = "Mod", defaultWidth = 218.2.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1117.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(413.6f, 408.2f)
                lineTo(114.6f, 242.5f)
                arcToRelative(29.8f, 29.8f, 0.0f, false, true, 0.0f, -52.7f)
                lineToRelative(276.6f, -153.3f)
                arcToRelative(161.9f, 161.9f, 0.0f, false, true, 156.2f, 0.0f)
                lineToRelative(276.6f, 153.3f)
                arcToRelative(29.8f, 29.8f, 0.0f, false, true, 0.0f, 52.7f)
                lineToRelative(-298.9f, 165.7f)
                arcToRelative(115.7f, 115.7f, 0.0f, false, true, -111.5f, 0.0f)
            }
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(547.8f, 918.1f)
                lineToRelative(276.6f, -153.3f)
                curveToRelative(48.3f, -26.8f, 78.1f, -76.3f, 78.1f, -129.8f)
                verticalLineTo(328.3f)
                curveToRelative(0.0f, -23.4f, -26.4f, -38.0f, -47.5f, -26.3f)
                lineToRelative(-299.0f, 165.6f)
                curveToRelative(-34.4f, 19.2f, -55.8f, 54.5f, -55.8f, 92.8f)
                verticalLineToRelative(331.4f)
                curveToRelative(0.0f, 23.4f, 26.5f, 38.0f, 47.6f, 26.3f)
                moveTo(390.8f, 918.1f)
                lineToRelative(-276.6f, -153.3f)
                curveToRelative(-48.3f, -26.8f, -78.1f, -76.3f, -78.1f, -129.8f)
                verticalLineTo(328.3f)
                curveToRelative(0.0f, -23.4f, 26.4f, -38.0f, 47.5f, -26.3f)
                lineToRelative(299.0f, 165.6f)
                curveToRelative(34.4f, 19.2f, 55.8f, 54.5f, 55.8f, 92.8f)
                verticalLineToRelative(331.4f)
                curveToRelative(0.0f, 23.4f, -26.5f, 38.0f, -47.6f, 26.3f)
            }
        }
            .build()
        return _mod!!
    }

private var _mod: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Mod, contentDescription = "")
    }
}
