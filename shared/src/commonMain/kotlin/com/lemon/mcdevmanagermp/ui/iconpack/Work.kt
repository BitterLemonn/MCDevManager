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

val IconPack.Work: ImageVector
    get() {
        if (_work != null) {
            return _work!!
        }
        _work = Builder(
            name = "Work", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(760.3f, 524.8f)
                horizontalLineToRelative(-307.2f)
                curveToRelative(-25.6f, 0.0f, -46.1f, -20.5f, -46.1f, -46.1f)
                reflectiveCurveToRelative(20.5f, -46.1f, 46.1f, -46.1f)
                horizontalLineToRelative(307.2f)
                curveToRelative(25.6f, 0.0f, 46.1f, 20.5f, 46.1f, 46.1f)
                reflectiveCurveToRelative(-20.5f, 46.1f, -46.1f, 46.1f)
                close()
                moveTo(235.5f, 660.5f)
                curveToRelative(0.0f, 28.2f, 20.5f, 48.6f, 48.6f, 48.6f)
                reflectiveCurveToRelative(48.6f, -23.0f, 48.6f, -48.6f)
                curveToRelative(0.0f, -25.6f, -20.5f, -48.6f, -48.6f, -48.6f)
                reflectiveCurveToRelative(-48.6f, 20.5f, -48.6f, 48.6f)
                close()
                moveTo(235.5f, 478.7f)
                curveToRelative(0.0f, 28.2f, 20.5f, 48.6f, 48.6f, 48.6f)
                reflectiveCurveToRelative(48.6f, -23.0f, 48.6f, -48.6f)
                curveToRelative(0.0f, -25.6f, -20.5f, -48.6f, -48.6f, -48.6f)
                reflectiveCurveToRelative(-48.6f, 23.0f, -48.6f, 48.6f)
                close()
                moveTo(760.3f, 706.6f)
                horizontalLineToRelative(-307.2f)
                curveToRelative(-25.6f, 0.0f, -46.1f, -20.5f, -46.1f, -46.1f)
                reflectiveCurveToRelative(20.5f, -46.1f, 46.1f, -46.1f)
                horizontalLineToRelative(307.2f)
                curveToRelative(25.6f, 0.0f, 46.1f, 20.5f, 46.1f, 46.1f)
                reflectiveCurveToRelative(-20.5f, 46.1f, -46.1f, 46.1f)
                close()
                moveTo(970.2f, 855.0f)
                lineTo(970.2f, 279.0f)
                curveToRelative(0.0f, -69.1f, -64.0f, -64.0f, -64.0f, -64.0f)
                lineTo(545.3f, 215.0f)
                curveTo(524.8f, 215.0f, 512.0f, 204.8f, 512.0f, 204.8f)
                reflectiveCurveToRelative(-15.4f, -25.6f, -43.5f, -66.6f)
                curveToRelative(-25.6f, -46.1f, -58.9f, -38.4f, -58.9f, -38.4f)
                lineTo(130.6f, 99.8f)
                curveTo(51.2f, 99.8f, 51.2f, 174.1f, 51.2f, 174.1f)
                verticalLineToRelative(675.8f)
                curveToRelative(0.0f, 84.5f, 64.0f, 74.2f, 64.0f, 74.2f)
                horizontalLineToRelative(798.7f)
                curveToRelative(66.6f, 0.0f, 56.3f, -69.1f, 56.3f, -69.1f)
                close()
            }
        }
            .build()
        return _work!!
    }

private var _work: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.Work, contentDescription = "")
    }
}
