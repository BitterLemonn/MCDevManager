package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 富文本内容分段：普通 HTML 文本段，或从 HTML 中解析出的表格段。
 */
internal sealed interface HtmlSegment {
    data class Text(val html: String) : HtmlSegment
    data class Table(val data: HtmlTableData) : HtmlSegment
}

/**
 * 从 HTML `<table>` 中解析出的表格数据。
 *
 * @param header 表头单元格文本；表格无表头行（首行非 `th`）时为 null
 * @param rows 数据行，每行为单元格文本列表
 */
internal data class HtmlTableData(
    val header: List<String>?,
    val rows: List<List<String>>
)

private val TABLE_OPEN_TAG = Regex("(?is)<table\\b")
private val TABLE_TAG_PAIR = Regex("(?is)</?table\\b[^>]*>")
private val TR_BLOCK = Regex("(?is)<tr\\b[^>]*>(.*?)</tr\\s*>")
private val CELL_BLOCK = Regex("(?is)<t([hd])\\b[^>]*>(.*?)</t\\s*\\1\\s*>")
private val ANY_TAG = Regex("(?is)<[^>]+>")

/**
 * 将 HTML 按顶层 `<table>` 切分为文本段与表格段的交替序列。
 *
 * `<table>` 的匹配按开闭标签计数处理嵌套；无法解析出行数据的表格降级为原始
 * HTML 文本段（交由富文本渲染剥壳显示，保证内容不丢失）。
 */
internal fun parseHtmlWithTables(html: String): List<HtmlSegment> {
    val segments = mutableListOf<HtmlSegment>()
    var cursor = 0
    while (true) {
        val open = TABLE_OPEN_TAG.find(html, cursor) ?: break
        var depth = 0
        var tableEnd = -1
        for (tag in TABLE_TAG_PAIR.findAll(html, open.range.first)) {
            depth += if (tag.value.startsWith("</")) -1 else 1
            if (depth == 0) {
                tableEnd = tag.range.last + 1
                break
            }
        }
        if (tableEnd < 0) break // 未闭合的表格：剩余内容整体按文本处理
        if (open.range.first > cursor) {
            segments += HtmlSegment.Text(html.substring(cursor, open.range.first))
        }
        val tableHtml = html.substring(open.range.first, tableEnd)
        segments += parseHtmlTable(tableHtml)?.let { HtmlSegment.Table(it) }
            ?: HtmlSegment.Text(tableHtml)
        cursor = tableEnd
    }
    if (cursor < html.length) segments += HtmlSegment.Text(html.substring(cursor))
    return segments.filterNot { it is HtmlSegment.Text && it.html.isBlank() }
}

/**
 * 解析单个 `<table>` 的结构：提取所有 `<tr>` 行与行内 `<th>` / `<td>` 单元格，
 * 首个全 `th` 的行作为表头；单元格内容剥去内联标签并解码常见 HTML 实体。
 * 无有效行时返回 null。
 */
private fun parseHtmlTable(tableHtml: String): HtmlTableData? {
    val structured = TR_BLOCK.findAll(tableHtml).mapNotNull { rowMatch ->
        val cells = CELL_BLOCK.findAll(rowMatch.groupValues[1]).map { cell ->
            val isHeader = cell.groupValues[1].equals("h", ignoreCase = true)
            cleanCellText(cell.groupValues[2]) to isHeader
        }.toList()
        cells.takeIf { it.isNotEmpty() }
    }.toList()
    if (structured.isEmpty()) return null

    val headerRowIndex = structured.indexOfFirst { row -> row.all { it.second } }
    val header = headerRowIndex.takeIf { it >= 0 }?.let { structured[it].map { it.first } }
    val dataRows = structured.withIndex()
        .filter { (index, _) -> index != headerRowIndex }
        .map { (_, row) -> row.map { it.first } }
    return HtmlTableData(header = header, rows = dataRows)
}

private val BR_TAG = Regex("(?i)<br\\b[^>]*>")

/** 单元格文本清洗：`br` 转空白、其余标签直接剥除，实体解码后合并连续空白（含不间断空格）。 */
private fun cleanCellText(raw: String): String =
    decodeHtmlEntities(BR_TAG.replace(ANY_TAG.replace(raw, ""), " "))
        .replace(Regex("[ \\t\\r\\n\\u00A0]+"), " ")
        .trim()

private val HTML_ENTITY = Regex("&#(x[0-9a-fA-F]+|\\d+);|&(nbsp|amp|lt|gt|quot|apos);")

/** 解码常见 HTML 实体：命名实体与十进制/十六进制数字实体。 */
internal fun decodeHtmlEntities(text: String): String = HTML_ENTITY.replace(text) { m ->
    m.groupValues[1].takeIf { it.isNotEmpty() }?.let { code ->
        val value = if (code.startsWith("x", ignoreCase = true)) {
            code.substring(1).toIntOrNull(16)
        } else {
            code.toIntOrNull()
        }
        value?.let { codePointToString(it) }
    } ?: when (m.groupValues[2].lowercase()) {
        "nbsp" -> " "
        "amp" -> "&"
        "lt" -> "<"
        "gt" -> ">"
        "quot" -> "\""
        "apos" -> "'"
        else -> m.value
    }
}

/**
 * Unicode 码点转字符串，跨平台实现（commonMain 不能使用 `java.lang.Character`）：
 * BMP 内直接转 [Char]，增补平面手工构造 UTF-16 代理对；非法码点返回 null。
 */
private fun codePointToString(code: Int): String? {
    if (code < 0 || code > 0x10FFFF) return null
    if (code < 0x10000) return Char(code).toString()
    val v = code - 0x10000
    return Char(0xD800 + (v shr 10)).toString() + Char(0xDC00 + (v and 0x3FF)).toString()
}

/**
 * 渲染可包含 `<table>` 的 HTML 富文本：普通片段走 [RichHtmlText]，
 * 表格片段以带边框网格展示（支持横向滚动），供消息中心等内容不可控的场景使用。
 */
@Composable
fun RichHtmlWithTablesText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val segments = remember(html) { parseHtmlWithTables(html) }
    Column(modifier = modifier) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is HtmlSegment.Text -> RichHtmlText(
                    html = segment.html,
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = color
                )

                is HtmlSegment.Table -> {
                    if (index > 0) Spacer(Modifier.height(8.dp))
                    HtmlTable(data = segment.data)
                }
            }
        }
    }
}

/**
 * 表格网格：外层圆角边框，表头以主色淡底区分；列宽按该列全部单元格文本的最大
 * 实测宽度统一取值（跨行对齐），整体可横向滚动以容纳多列数据。
 */
@Composable
private fun HtmlTable(
    data: HtmlTableData,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val cellStyle = MaterialTheme.typography.bodySmall

    val columnCount = (data.header?.size ?: 0).coerceAtLeast(
        data.rows.maxOfOrNull { it.size } ?: 0
    )
    // 每列宽度 = 该列所有单元格（含表头，表头按半粗体样式）文本的最大测量宽 + 左右内边距
    val columnWidths = remember(data, textMeasurer, cellStyle, density) {
        (0 until columnCount).map { col ->
            val headerWidth = data.header?.getOrNull(col)?.takeIf { it.isNotEmpty() }
                ?.let { textMeasurer.measure(it, cellStyle.copy(fontWeight = FontWeight.SemiBold)).size.width }
                ?: 0
            val bodyWidth = data.rows.maxOfOrNull { row ->
                textMeasurer.measure(row.getOrNull(col).orEmpty(), cellStyle).size.width
            } ?: 0
            with(density) { maxOf(headerWidth, bodyWidth).toDp() + 20.dp }
        }
    }

    val allRows = remember(data) {
        buildList {
            data.header?.let { add(it) }
            addAll(data.rows)
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
    ) {
        allRows.forEachIndexed { rowIndex, cells ->
            val isHeader = rowIndex == 0 && data.header != null
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                (0 until columnCount).forEach { col ->
                    Box(
                        modifier = Modifier
                            .width(columnWidths[col].coerceAtLeast(40.dp))
                            .fillMaxHeight()
                            .border(0.5.dp, colors.outlineVariant)
                            .background(
                                if (isHeader) colors.primary.copy(alpha = 0.08f) else Color.Transparent
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = cells.getOrNull(col).orEmpty(),
                            style = cellStyle,
                            fontWeight = if (isHeader) FontWeight.SemiBold else null,
                            color = colors.textColor
                        )
                    }
                }
            }
        }
    }
}
