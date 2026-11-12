package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.ui.components.HtmlSegment
import com.lemon.mcdevmanagermp.ui.components.decodeHtmlEntities
import com.lemon.mcdevmanagermp.ui.components.parseHtmlWithTables
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RichHtmlWithTablesTest {

    /** 消息中心线上报表邮件的实际样例：表头 th + 数据行 td。 */
    private val sampleMailHtml =
        "<p>以下为本周组件卡顿统计：</p>" +
            "<table border=\"1\" cellspacing=\"0\" cellpadding=\"5\">" +
            "<tr><th>组件ID</th><th>组件名称</th><th>本周卡顿次数</th><th>平均卡顿时长(秒)</th><th>模块名称</th><th>函数名称</th></tr>" +
            "<tr><td>4679556122208731470</td><td>【苦柠】自定义任务</td><td>1149</td><td>0.985</td>" +
            "<td>Script_Quest.QuModLibs.Systems.Loader.Server</td><td>_systemCallListener</td></tr>" +
            "<tr><td>4679556122208731470</td><td>【苦柠】自定义任务</td><td>1082</td><td>2.624</td>" +
            "<td>Script_Quest.QuModLibs.Systems.Loader.Client</td><td>Update</td></tr>" +
            "</table>"

    @Test
    fun `表格邮件被切分为文本段与表格段`() {
        val segments = parseHtmlWithTables(sampleMailHtml)

        assertEquals(2, segments.size)
        assertTrue(segments[0] is HtmlSegment.Text)
        assertTrue(segments[1] is HtmlSegment.Table)
    }

    @Test
    fun `表格解析出表头与全部数据行`() {
        val table = parseHtmlWithTables(sampleMailHtml)
            .filterIsInstance<HtmlSegment.Table>()
            .first()
            .data

        assertEquals(
            listOf("组件ID", "组件名称", "本周卡顿次数", "平均卡顿时长(秒)", "模块名称", "函数名称"),
            table.header
        )
        assertEquals(2, table.rows.size)
        assertEquals("4679556122208731470", table.rows[0][0])
        assertEquals("【苦柠】自定义任务", table.rows[0][1])
        assertEquals("1149", table.rows[0][2])
        assertEquals("0.985", table.rows[0][3])
        assertEquals("Script_Quest.QuModLibs.Systems.Loader.Server", table.rows[0][4])
        assertEquals("_systemCallListener", table.rows[0][5])
        assertEquals("Update", table.rows[1][5])
    }

    @Test
    fun `无表格的纯文本保持单文本段`() {
        val segments = parseHtmlWithTables("<p>普通通知内容</p>")

        assertEquals(1, segments.size)
        assertEquals("<p>普通通知内容</p>", (segments[0] as HtmlSegment.Text).html)
    }

    @Test
    fun `表格前后的文本与表格按顺序切分`() {
        val html = "<p>开头</p><table><tr><td>1</td></tr></table><p>结尾</p>"
        val segments = parseHtmlWithTables(html)

        assertEquals(3, segments.size)
        assertEquals("<p>开头</p>", (segments[0] as HtmlSegment.Text).html)
        val table = (segments[1] as HtmlSegment.Table).data
        assertEquals(null, table.header)
        assertEquals(listOf(listOf("1")), table.rows)
        assertEquals("<p>结尾</p>", (segments[2] as HtmlSegment.Text).html)
    }

    @Test
    fun `嵌套表格按外层完整截取`() {
        val html = "<table><tr><td><table><tr><td>内</td></tr></table></td></tr></table>"
        val segments = parseHtmlWithTables(html)

        assertEquals(1, segments.size)
        assertTrue(segments[0] is HtmlSegment.Table)
    }

    @Test
    fun `单元格内联标签与实体被清洗`() {
        val html = "<table><tr><th>a&nbsp;&amp;&nbsp;b</th><th>c</th></tr>" +
            "<tr><td><strong>粗体</strong>&lt;x&gt;</td><td>1</td></tr></table>"
        val table = (parseHtmlWithTables(html)[0] as HtmlSegment.Table).data

        assertEquals(listOf("a & b", "c"), table.header)
        assertEquals(listOf("粗体<x>", "1"), table.rows[0])
    }

    @Test
    fun `未闭合表格整体降级为文本段`() {
        val segments = parseHtmlWithTables("<table><tr><td>数据</td></tr>")

        assertEquals(1, segments.size)
        assertTrue(segments[0] is HtmlSegment.Text)
    }

    @Test
    fun `数字与十六进制实体解码`() {
        assertEquals("中", decodeHtmlEntities("&#20013;"))
        assertEquals("A", decodeHtmlEntities("&#x41;"))
        assertEquals("\"引号\"", decodeHtmlEntities("&quot;引号&quot;"))
        assertEquals("未知&copy;保留", decodeHtmlEntities("未知&copy;保留"))
    }
}
