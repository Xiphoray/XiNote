package com.example.ui

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin

import androidx.compose.foundation.isSystemInDarkTheme
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.core.MarkwonTheme

fun stripMarkdown(markdown: String): String {
    var text = markdown
    text = text.replace(Regex("(?m)^#{1,6}\\s+"), "") // Remove headers
    text = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Remove bold
    text = text.replace(Regex("\\*(.*?)\\*"), "$1") // Remove italic
    text = text.replace(Regex("`(.*?)`"), "$1") // Remove code
    text = text.replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1") // Remove links
    text = text.replace(Regex("(?m)^\\s*[-*+]\\s+"), "") // Remove list items
    text = text.replace(Regex("(?m)^>\\s+"), "") // Remove blockquotes
    return text.trim()
}

@Composable
fun MarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()
    val isDark = isSystemInDarkTheme()
    
    val markwon = remember(context, isDark) {
        Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    val codeBg = if (isDark) 0xFF2B2B2B.toInt() else 0xFFF5F5F5.toInt()
                    val codeText = if (isDark) 0xFFE0E0E0.toInt() else 0xFF333333.toInt()
                    builder
                        .codeBackgroundColor(codeBg)
                        .codeBlockBackgroundColor(codeBg)
                        .codeTextColor(codeText)
                        .codeBlockTextColor(codeText)
                        .linkColor(linkColor)
                }
            })
            .build()
    }
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setTextColor(color)
                setLinkTextColor(linkColor)
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f)
                setLineSpacing(0f, 1.2f)
            }
        },
        update = { textView ->
            textView.setTextColor(color)
            textView.setLinkTextColor(linkColor)
            markwon.setMarkdown(textView, markdown)
        }
    )
}
