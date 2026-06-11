package com.lemon.mcdevmanagermp.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * 简单的 HTML 解析器，将 HTML 字符串转换为 Compose AnnotatedString
 * 支持标签：<p>, <br>, <b>, <strong>, <i>, <em>, <u>, &nbsp;
 */
object HtmlParser {

    fun parse(html: String): AnnotatedString {
        return buildAnnotatedString {
            val cleaned = html
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")

            parseInternal(cleaned, this)
        }
    }

    private fun parseInternal(html: String, builder: AnnotatedString.Builder) {
        var i = 0
        val len = html.length

        while (i < len) {
            // 检查标签开始
            if (html[i] == '<') {
                val tagEnd = html.indexOf('>', i)
                if (tagEnd == -1) {
                    builder.append(html.substring(i))
                    break
                }

                val tagContent = html.substring(i + 1, tagEnd).trim()

                when {
                    // 自闭合标签
                    tagContent == "br" || tagContent == "br/" -> {
                        builder.append('\n')
                        i = tagEnd + 1
                    }

                    // 段落开始
                    tagContent == "p" -> {
                        i = tagEnd + 1
                        val closeTag = findCloseTag(html, i, "p")
                        if (closeTag != null) {
                            parseInternal(html.substring(i, closeTag.start), builder)
                            builder.append('\n')
                            i = closeTag.end
                        } else {
                            parseInternal(html.substring(i), builder)
                            break
                        }
                    }

                    // 粗体
                    tagContent == "b" || tagContent == "strong" -> {
                        i = tagEnd + 1
                        val closeTag = findCloseTag(html, i, tagContent)
                        if (closeTag != null) {
                            builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            parseInternal(html.substring(i, closeTag.start), builder)
                            builder.pop()
                            i = closeTag.end
                        } else {
                            i = tagEnd + 1
                        }
                    }

                    // 斜体
                    tagContent == "i" || tagContent == "em" -> {
                        i = tagEnd + 1
                        val closeTag = findCloseTag(html, i, tagContent)
                        if (closeTag != null) {
                            builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            parseInternal(html.substring(i, closeTag.start), builder)
                            builder.pop()
                            i = closeTag.end
                        } else {
                            i = tagEnd + 1
                        }
                    }

                    // 下划线
                    tagContent == "u" -> {
                        i = tagEnd + 1
                        val closeTag = findCloseTag(html, i, "u")
                        if (closeTag != null) {
                            builder.pushStyle(SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline))
                            parseInternal(html.substring(i, closeTag.start), builder)
                            builder.pop()
                            i = closeTag.end
                        } else {
                            i = tagEnd + 1
                        }
                    }

                    // 闭合标签或未知标签 — 跳过
                    tagContent.startsWith("/") -> {
                        i = tagEnd + 1
                    }

                    // 其他未知标签（如 span, div 等），提取内部内容
                    else -> {
                        val tagName = tagContent.split(" ").first()
                        i = tagEnd + 1
                        val closeTag = findCloseTag(html, i, tagName)
                        if (closeTag != null) {
                            parseInternal(html.substring(i, closeTag.start), builder)
                            i = closeTag.end
                        } else {
                            // 没有闭合标签，跳过
                        }
                    }
                }
            } else {
                // 普通文本
                val nextTag = html.indexOf('<', i)
                if (nextTag == -1) {
                    builder.append(html.substring(i))
                    break
                } else {
                    builder.append(html.substring(i, nextTag))
                    i = nextTag
                }
            }
        }
    }

    /**
     * 查找闭合标签的位置
     * @return CloseTagResult(start: 内容结束位置, end: 闭合标签结束位置) 或 null
     */
    private fun findCloseTag(html: String, fromIndex: Int, tagName: String): CloseTagResult? {
        val closeTag = "</$tagName>"
        val index = html.indexOf(closeTag, fromIndex)
        return if (index != -1) {
            CloseTagResult(index, index + closeTag.length)
        } else null
    }

    private data class CloseTagResult(val start: Int, val end: Int)
}
