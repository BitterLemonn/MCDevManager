package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanager.utils.pxToDp
import kotlin.math.roundToInt

@Composable
fun FABPositionWidget(
    content: @Composable (ColumnScope.() -> Unit) = { }
) {
    var fullHeight by remember { mutableIntStateOf(0) }
    var fabHeight by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { fullHeight = it.size.height }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
                .width(60.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { fabHeight = it.size.height }
                    .offset {
                        IntOffset(
                            x = 0,
                            y = pxToDp(context, fullHeight.toFloat()) - pxToDp(
                                context, fabHeight.toFloat())
                        )
                    }
            ) { content() }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun FABPositionWidgetPreview() {
    FABPositionWidget {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Black)
                .padding(16.dp)
        ) {
            // Your content here
        }
    }
}