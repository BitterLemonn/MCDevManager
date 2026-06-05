package com.lemon.mcdevmanagermp.ui.pages.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownBody(markdown: String, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val lines = markdown.split('\n')
        var inParagraph = false

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                if (inParagraph) {
                    Spacer(Modifier.height(4.dp))
                    inParagraph = false
                }
                continue
            }

            inParagraph = true

            when {
                trimmed.startsWith("### ") -> {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("### ")),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                }

                trimmed.startsWith("## ") -> {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = parseInlineMarkdown(trimmed.removePrefix("## ")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                }

                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val content = trimmed.removePrefix("- ").removePrefix("* ")
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                    ) {
                        Text(
                            text = "  •",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = parseInlineMarkdown(content),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                else -> {
                    Text(
                        text = parseInlineMarkdown(trimmed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text.startsWith("[", i) -> {
                    val closeBracket = text.indexOf("](", i + 1)
                    val closeParen =
                        if (closeBracket != -1) text.indexOf(")", closeBracket + 2) else -1
                    if (closeBracket != -1 && closeParen != -1) {
                        append(text.substring(i + 1, closeBracket))
                        i = closeParen + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
