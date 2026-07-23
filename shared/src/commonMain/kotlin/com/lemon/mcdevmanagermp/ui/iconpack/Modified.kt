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

val IconPack.Modified: ImageVector
    get() {
        if (_modified != null) {
            return _modified!!
        }
        _modified = Builder(
            name = "Modified", defaultWidth = 200.0.dp, defaultHeight =
                200.0.dp, viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(823.8f, 53.2f)
                curveToRelative(-111.6f, -51.2f, -247.8f, -31.2f, -339.5f, 60.4f)
                curveToRelative(-83.5f, 83.5f, -107.5f, 203.8f, -72.2f, 308.7f)
                lineToRelative(-354.3f, 353.3f)
                curveToRelative(-52.2f, 52.2f, -52.2f, 136.7f, 0.0f, 188.9f)
                reflectiveCurveToRelative(137.2f, 52.2f, 189.4f, 0.0f)
                lineToRelative(354.3f, -353.3f)
                curveToRelative(105.0f, 35.3f, 225.8f, 11.3f, 309.2f, -72.2f)
                curveToRelative(91.6f, -91.6f, 110.6f, -226.8f, 59.4f, -337.9f)
                curveToRelative(-16.4f, 18.4f, -44.0f, 42.0f, -83.5f, 77.8f)
                curveToRelative(-41.5f, 41.5f, -100.4f, 41.5f, -142.3f, 0.0f)
                curveToRelative(-41.5f, -41.5f, -41.5f, -100.4f, 0.0f, -141.8f)
                curveToRelative(38.9f, -38.4f, 80.4f, -84.0f, 79.4f, -84.0f)
                close()
            }
        }
            .build()
        return _modified!!
    }

private var _modified: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Modified, contentDescription = "")
    }
}
