package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.NovaCyan

@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    val sections = parseMarkdownSections(content)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (section in sections) {
            when (section) {
                is MarkdownSection.CodeBlock -> {
                    CodeBlockView(
                        language = section.language,
                        code = section.code,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", section.code))
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                is MarkdownSection.Paragraph -> {
                    val annotated = buildAnnotatedParagraph(section.text, textColor)
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    )
                }
                is MarkdownSection.Header -> {
                    val headerStyle = when (section.level) {
                        1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = NovaCyan)
                        2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NovaCyan)
                        else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = NovaCyan)
                    }
                    Text(
                        text = section.text,
                        style = headerStyle,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .background(CyberBlack, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" }.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NovaCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = code,
                color = Color(0xFFE2E8F0),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

sealed class MarkdownSection {
    data class Paragraph(val text: String) : MarkdownSection()
    data class CodeBlock(val language: String, val code: String) : MarkdownSection()
    data class Header(val level: Int, val text: String) : MarkdownSection()
}

fun parseMarkdownSections(markdown: String): List<MarkdownSection> {
    val sections = mutableListOf<MarkdownSection>()
    val lines = markdown.lines()
    var inCodeBlock = false
    var codeLang = ""
    val codeBuilder = StringBuilder()
    val paraBuilder = StringBuilder()

    fun flushPara() {
        if (paraBuilder.isNotBlank()) {
            sections.add(MarkdownSection.Paragraph(paraBuilder.toString().trim()))
            paraBuilder.clear()
        }
    }

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                // End code block
                sections.add(MarkdownSection.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                inCodeBlock = false
            } else {
                flushPara()
                inCodeBlock = true
                codeLang = line.trim().removePrefix("```").trim()
            }
            continue
        }

        if (inCodeBlock) {
            codeBuilder.append(line).append("\n")
        } else {
            if (line.startsWith("# ")) {
                flushPara()
                sections.add(MarkdownSection.Header(1, line.removePrefix("# ").trim()))
            } else if (line.startsWith("## ")) {
                flushPara()
                sections.add(MarkdownSection.Header(2, line.removePrefix("## ").trim()))
            } else if (line.startsWith("### ")) {
                flushPara()
                sections.add(MarkdownSection.Header(3, line.removePrefix("### ").trim()))
            } else {
                paraBuilder.append(line).append("\n")
            }
        }
    }
    if (inCodeBlock) {
        sections.add(MarkdownSection.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
    } else {
        flushPara()
    }

    return if (sections.isEmpty()) listOf(MarkdownSection.Paragraph(markdown)) else sections
}

private fun buildAnnotatedParagraph(text: String, defaultColor: Color) = buildAnnotatedString {
    var cursor = 0
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    val codeRegex = Regex("`([^`]+)`")

    // Simple parser for bold and inline code
    val combinedMatches = (boldRegex.findAll(text).map { it to "BOLD" } +
            codeRegex.findAll(text).map { it to "CODE" })
        .sortedBy { it.first.range.first }

    var lastIdx = 0
    for ((match, type) in combinedMatches) {
        if (match.range.first < lastIdx) continue
        if (match.range.first > lastIdx) {
            withStyle(SpanStyle(color = defaultColor)) {
                append(text.substring(lastIdx, match.range.first))
            }
        }
        val innerText = match.groupValues[1]
        if (type == "BOLD") {
            withStyle(SpanStyle(color = defaultColor, fontWeight = FontWeight.Bold)) {
                append(innerText)
            }
        } else {
            withStyle(
                SpanStyle(
                    color = NovaCyan,
                    fontFamily = FontFamily.Monospace,
                    background = CyberBlack
                )
            ) {
                append(" $innerText ")
            }
        }
        lastIdx = match.range.last + 1
    }

    if (lastIdx < text.length) {
        withStyle(SpanStyle(color = defaultColor)) {
            append(text.substring(lastIdx))
        }
    }
}
