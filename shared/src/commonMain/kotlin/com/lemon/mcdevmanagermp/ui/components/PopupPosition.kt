package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * 强制 Popup 出现在锚点（输入框所在 Box）下方：y = anchorBounds.bottom。
 *
 * 与 [androidx.compose.material3.DropdownMenu] 的自动上下翻转不同，始终向下展开，
 * 避免遮挡锚点上方的已选项；下方空间不足时上沿对齐窗口底部，超出部分由菜单内部滚动消化。
 * 供 [TagInputField]、[ModSearchSelectField] 等"向下展开"的下拉共用。
 */
internal object BelowAnchorPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val x = anchorBounds.left
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val y = anchorBounds.bottom
            .coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))
        return IntOffset(x, y)
    }
}
