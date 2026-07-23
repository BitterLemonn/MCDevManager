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

val IconPack.NoShow: ImageVector
    get() {
        if (_noShow != null) {
            return _noShow!!
        }
        _noShow = Builder(
            name = "NoShow", defaultWidth = 200.0.dp, defaultHeight = 200.0.dp,
            viewportWidth = 1024.0f, viewportHeight = 1024.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(386.3f, 504.5f)
                lineToRelative(121.8f, -121.8f)
                arcToRelative(128.0f, 128.0f, 0.0f, false, false, -121.8f, 121.8f)
                close()
                moveTo(625.1f, 446.9f)
                lineTo(576.0f, 495.8f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, -76.8f, 76.8f)
                lineToRelative(-48.9f, 48.9f)
                arcToRelative(128.0f, 128.0f, 0.0f, false, false, 174.5f, -174.5f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(109.0f, 512.0f)
                arcToRelative(21.3f, 21.3f, 0.0f, false, true, 6.2f, -15.1f)
                curveToRelative(139.5f, -139.3f, 268.8f, -207.1f, 396.8f, -207.1f)
                arcToRelative(372.3f, 372.3f, 0.0f, false, true, 79.8f, 9.0f)
                lineToRelative(52.3f, -52.3f)
                arcToRelative(443.5f, 443.5f, 0.0f, false, false, -132.1f, -21.3f)
                curveToRelative(-145.9f, 0.0f, -291.8f, 75.3f, -442.5f, 225.9f)
                arcToRelative(85.3f, 85.3f, 0.0f, false, false, 0.0f, 120.7f)
                arcToRelative(1063.0f, 1063.0f, 0.0f, false, false, 134.2f, 115.2f)
                lineToRelative(45.9f, -45.9f)
                arcToRelative(985.8f, 985.8f, 0.0f, false, true, -134.4f, -114.1f)
                arcToRelative(21.3f, 21.3f, 0.0f, false, true, -6.2f, -14.9f)
                close()
                moveTo(954.7f, 451.6f)
                arcToRelative(966.6f, 966.6f, 0.0f, false, false, -185.8f, -149.3f)
                lineToRelative(-46.7f, 46.7f)
                arcToRelative(877.7f, 877.7f, 0.0f, false, true, 187.3f, 147.2f)
                arcToRelative(21.3f, 21.3f, 0.0f, false, true, 0.0f, 30.1f)
                curveTo(770.1f, 666.5f, 640.0f, 734.3f, 512.0f, 734.3f)
                arcToRelative(393.4f, 393.4f, 0.0f, false, true, -145.7f, -29.0f)
                lineToRelative(-48.6f, 48.6f)
                arcTo(466.1f, 466.1f, 0.0f, false, false, 512.0f, 798.3f)
                curveToRelative(145.9f, 0.0f, 291.8f, -75.3f, 442.5f, -225.9f)
                arcToRelative(85.3f, 85.3f, 0.0f, false, false, 0.2f, -120.7f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(777.3f, 204.0f)
                moveToRelative(22.6f, 22.6f)
                lineToRelative(0.0f, 0.0f)
                quadToRelative(22.6f, 22.6f, 0.0f, 45.3f)
                lineToRelative(-528.0f, 528.0f)
                quadToRelative(-22.6f, 22.6f, -45.3f, 0.0f)
                lineToRelative(0.0f, 0.0f)
                quadToRelative(-22.6f, -22.6f, 0.0f, -45.3f)
                lineToRelative(528.0f, -528.0f)
                quadToRelative(22.6f, -22.6f, 45.3f, 0.0f)
                close()
            }
        }
            .build()
        return _noShow!!
    }

private var _noShow: ImageVector? = null

@Preview
@Composable
private fun Preview(): Unit {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = IconPack.NoShow, contentDescription = "")
    }
}
