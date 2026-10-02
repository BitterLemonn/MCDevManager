package com.lemon.mcdevmanagermp.ui.components

/**
 * 平台 PE 详情（`info` / `sync_item_info.info`）实际接受的标签。
 * 取值依据：抓取线上已上架作品的详情，标签集合仅为 p / em / span / img / br / strong。
 */
private val ALLOWED_DETAIL_TAGS = setOf("p", "span", "img", "br", "strong", "em")

/** 连同内容整体丢弃的标签（编辑或粘贴可能带入）。 */
private val DROPPED_DETAIL_TAGS = listOf("script", "style")

/** 标签 token：属性值（含 base64 data URI）不含 `>`，故 `[^>]*` 边界安全。 */
private val TAG_TOKEN = Regex("<\\s*(/?)\\s*([a-zA-Z][a-zA-Z0-9]*)([^>]*)>")

/** 属性 token（仅匹配带引号的属性）。 */
private val ATTR_TOKEN = Regex("([a-zA-Z_:][-a-zA-Z0-9_:.]*)\\s*=\\s*(\"([^\"]*)\"|'([^']*)')")

/**
 * 详情 HTML 清洗：把编辑器输出收敛到网易 PE 详情接口实际接受的白名单。
 *
 * rich-editor 的 `toHtml()` 会把粗体/斜体输出为 `<b>` / `<i>`，并保留 `<u>` / `<s>` 与
 * `<s>` 系列的删除线；而平台只接受 `<strong>` / `<em>` 及 `text-decoration` 样式
 * （提交 `<b>` 会得到「提交内容中含有非法字符: bad tag: b」）。同时清洗掉图片的
 * width/height（接口要求纯净 `<img>`，缺省值为 0）、以及编辑器/粘贴可能带入的其它标签。
 *
 * 处理规则：
 * - `b`/`strong` → `strong`；`i`/`em` → `em`（语义等价，平台接受）
 * - `u`/`ins` → `span style="text-decoration: underline;"`；`s`/`strike`/`del` → `line-through`
 * - `p`/`span`/`strong`/`em` 保留 `style`（颜色与底色依赖它），`img` 仅保留 `src`
 * - 其它标签一律**去壳保留内容**（如 `<h1>`、`<ul>`、`<code>`、`<a>`），不丢用户文本
 * - `script`/`style` 连同内容整体丢弃
 *
 * 已知限制（不做处理）：纯白背景色（`white` / `#FFFFFF` / `rgb(255,255,255)`）会被 rich-editor
 * 自身在 `toHtml()` 中丢弃（实测各种写法均输出无样式文本），本函数无法挽回；白色**文字**不受影响。
 */
internal fun sanitizeDetailHtml(html: String): String {
    if (html.isEmpty()) return html
    var result = html
    DROPPED_DETAIL_TAGS.forEach { tag ->
        result = result
            .replace(Regex("(?is)<$tag\\b[^>]*>.*?</$tag\\s*>"), "")
            .replace(Regex("(?is)<$tag\\b[^>]*/?>"), "")
    }
    return TAG_TOKEN.replace(result) { m ->
        val closing = m.groupValues[1] == "/"
        val name = m.groupValues[2].lowercase()
        val attrs = m.groupValues[3]
        if (name !in ALLOWED_DETAIL_TAGS && name !in REWRITTEN_DETAIL_TAGS) return@replace ""
        if (closing) closeTag(name) else openTag(name, attrs)
    }
}

/** 需要改写标签名（或替换为样式 span）的标签。 */
private val REWRITTEN_DETAIL_TAGS = setOf("b", "i", "u", "ins", "s", "strike", "del")

private fun openTag(name: String, attrs: String): String = when (name) {
    "b", "strong" -> "<strong${styleAttr(attrs)}>"
    "i", "em" -> "<em${styleAttr(attrs)}>"
    "u", "ins" -> "<span style=\"text-decoration: underline;\">"
    "s", "strike", "del" -> "<span style=\"text-decoration: line-through;\">"
    "p" -> "<p${styleAttr(attrs)}>"
    "span" -> "<span${styleAttr(attrs)}>"
    "img" -> "<img${srcAttr(attrs)}>"
    "br" -> "<br>"
    else -> ""
}

private fun closeTag(name: String): String = when (name) {
    "b", "strong" -> "</strong>"
    "i", "em" -> "</em>"
    "u", "ins", "s", "strike", "del" -> "</span>"
    "p" -> "</p>"
    "span" -> "</span>"
    "img", "br" -> ""
    else -> ""
}

private fun styleAttr(raw: String): String = keepAttrs(raw, "style", ::normalizeStyleCss)

private fun srcAttr(raw: String): String = keepAttrs(raw, "src")

/** 仅保留 [allowed] 中的属性，统一为双引号并转义值内的 `"`；[transform] 可选地归一化属性值。 */
private fun keepAttrs(
    raw: String,
    allowed: String,
    transform: (String) -> String = { it }
): String =
    ATTR_TOKEN.findAll(raw)
        .filter { it.groupValues[1].lowercase() == allowed }
        .joinToString("") { m ->
            val value = m.groupValues[3].ifEmpty { m.groupValues[4] }
            " ${m.groupValues[1]}=\"${transform(value).replace("\"", "&quot;")}\""
        }

/** `background` 简写 → `background-color`（平台侧实测只用后者）；已带 `-` 后缀的不重复改写。 */
private val BACKGROUND_SHORTHAND = Regex("(?i)(?<![-\\w])background(?![\\w-])")

/**
 * `rgb(r,g,b)` / `rgba(r,g,b,a)` 的逗号分隔。
 * rich-editor 的解码器要求逗号后有空白，否则 `rgb(255,255,255)` 会被解析成 `#225555`
 * 这类错误颜色（实测）；补一层空白即可，不改变平台已接受的颜色写法。
 */
private val RGB_COMMAS = Regex("(?i)\\b(rgba?)\\(([^)]*)\\)")

/**
 * 归一化内联样式，使编辑器输出可被编辑器自身再次准确还原、并与平台约定一致。
 *
 * rich-editor 的 `toHtml()` 会把 `background-color` 简写成 `background`，而平台线上数据统一
 * 使用 `background-color`；其解码器又要求 `rgb()` / `rgba()` 的逗号后带空白，否则解析出错色。
 * 故在输出边界统一键名并规范颜色函数的空格，取值本身保持平台已接受的 rgb/rgba/命名色写法。
 */
internal fun normalizeStyleCss(css: String): String {
    if (css.isEmpty()) return css
    return RGB_COMMAS.replace(BACKGROUND_SHORTHAND.replace(css, "background-color")) { m ->
        val fn = m.groupValues[1]
        val args = m.groupValues[2].split(",").joinToString(", ") { it.trim() }
        "$fn($args)"
    }
}
