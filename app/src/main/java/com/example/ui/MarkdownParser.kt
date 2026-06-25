package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletList(val items: List<String>) : MarkdownBlock
    data class Blockquote(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    object HorizontalRule : MarkdownBlock
}

fun parseMarkdown(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.split("\n")
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        when {
            trimmed.isEmpty() -> {
                i++
            }
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length
                val text = trimmed.substring(level).trim()
                blocks.add(MarkdownBlock.Heading(level.coerceAtMost(6), text))
                i++
            }
            trimmed.startsWith(">") -> {
                val text = trimmed.substring(1).trim()
                blocks.add(MarkdownBlock.Blockquote(text))
                i++
            }
            trimmed.startsWith("---") || trimmed.startsWith("***") -> {
                blocks.add(MarkdownBlock.HorizontalRule)
                i++
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                val items = mutableListOf<String>()
                while (i < lines.size && (lines[i].trim().startsWith("- ") || lines[i].trim().startsWith("* "))) {
                    val currLine = lines[i].trim()
                    items.add(currLine.substring(2))
                    i++
                }
                blocks.add(MarkdownBlock.BulletList(items))
            }
            trimmed.startsWith("```") -> {
                val language = trimmed.substring(3).trim()
                val codeBuilder = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeBuilder.append(lines[i]).append("\n")
                    i++
                }
                if (i < lines.size) i++ // Consume closing ```
                blocks.add(MarkdownBlock.CodeBlock(language, codeBuilder.toString().trimEnd()))
            }
            else -> {
                val paraBuilder = StringBuilder()
                paraBuilder.append(line)
                i++
                while (i < lines.size && 
                       lines[i].trim().isNotEmpty() && 
                       !lines[i].trim().startsWith("#") && 
                       !lines[i].trim().startsWith(">") && 
                       !lines[i].trim().startsWith("- ") && 
                       !lines[i].trim().startsWith("* ") && 
                       !lines[i].trim().startsWith("---") && 
                       !lines[i].trim().startsWith("```")) {
                    paraBuilder.append("\n").append(lines[i])
                    i++
                }
                blocks.add(MarkdownBlock.Paragraph(paraBuilder.toString()))
            }
        }
    }
    return blocks
}

@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val parsedBlocks = parseMarkdown(markdown)
    Column(modifier = modifier) {
        parsedBlocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val scale = when (block.level) {
                        1 -> 24.sp to FontWeight.Bold
                        2 -> 20.sp to FontWeight.Bold
                        3 -> 18.sp to FontWeight.Bold
                        else -> 16.sp to FontWeight.SemiBold
                    }
                    Text(
                        text = block.text,
                        fontSize = scale.first,
                        fontWeight = scale.second,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    if (block.level == 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = renderInlineMarkdown(block.text),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                is MarkdownBlock.BulletList -> {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        block.items.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = renderInlineMarkdown(item),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                is MarkdownBlock.Blockquote -> {
                    val quoteBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .drawBehind {
                                drawLine(
                                    color = quoteBorderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                            )
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = renderInlineMarkdown(block.text),
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        if (block.language.isNotEmpty()) {
                            Text(
                                text = block.language.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Text(
                            text = block.code,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MarkdownBlock.HorizontalRule -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun renderInlineMarkdown(text: String) = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        val char = text[index]
        when {
            text.startsWith("**", index) && text.indexOf("**", index + 2) != -1 -> {
                val end = text.indexOf("**", index + 2)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(index + 2, end))
                }
                index = end + 2
            }
            char == '*' && text.indexOf('*', index + 1) != -1 -> {
                val end = text.indexOf('*', index + 1)
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(index + 1, end))
                }
                index = end + 1
            }
            char == '`' && text.indexOf('`', index + 1) != -1 -> {
                val end = text.indexOf('`', index + 1)
                withStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    append(text.substring(index + 1, end))
                }
                index = end + 1
            }
            else -> {
                append(char)
                index++
            }
        }
    }
}
