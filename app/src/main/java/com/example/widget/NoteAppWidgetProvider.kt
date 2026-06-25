package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.ui.AppLanguage
import com.example.ui.Localization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch latest notes from Room
                val db = AppDatabase.getDatabase(context)
                val notes = db.noteDao().getLatestNotes(3)

                // Read opacity and theme colors
                val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
                val opacity = sp.getInt("widget_opacity", 90) // 0 to 100, default 90

                val savedLangCode = sp.getString("selected_language", AppLanguage.ZH.code) ?: AppLanguage.ZH.code
                val lang = AppLanguage.values().find { it.code == savedLangCode } ?: AppLanguage.ZH

                val widgetTitleText = Localization.getString("app_title", lang)
                val widgetEmptyTextMsg = Localization.getString("widget_empty_text", lang)
                val untitledText = Localization.getString("untitled", lang)

                val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

                // Build background color ARGB
                val baseBgColor: Int
                val textColorHeader: Int
                val textColorTitle: Int
                val textColorContent: Int
                val dividerColor: Int
                val plusIconColor: Int

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    // Follow system-wide dynamic Material You color scheme on Android 12+
                    baseBgColor = if (isDarkMode) {
                        context.getColor(android.R.color.system_neutral1_900)
                    } else {
                        context.getColor(android.R.color.system_neutral1_100)
                    }
                    textColorHeader = if (isDarkMode) {
                        context.getColor(android.R.color.system_neutral1_100)
                    } else {
                        context.getColor(android.R.color.system_neutral1_900)
                    }
                    textColorTitle = textColorHeader
                    textColorContent = if (isDarkMode) {
                        context.getColor(android.R.color.system_neutral1_300)
                    } else {
                        context.getColor(android.R.color.system_neutral1_700)
                    }
                    dividerColor = if (isDarkMode) {
                        context.getColor(android.R.color.system_accent1_800)
                    } else {
                        context.getColor(android.R.color.system_accent1_100)
                    }
                    plusIconColor = if (isDarkMode) {
                        context.getColor(android.R.color.system_accent1_200)
                    } else {
                        context.getColor(android.R.color.system_accent1_600)
                    }
                } else {
                    // Fallback custom palette matching light/dark theme perfectly
                    baseBgColor = if (isDarkMode) 0xFF1F1B1A.toInt() else 0xFFF5DED8.toInt()
                    textColorHeader = if (isDarkMode) 0xFFECE0DF.toInt() else 0xFF1F1B1A.toInt()
                    textColorTitle = textColorHeader
                    textColorContent = if (isDarkMode) 0xFFD8C2BE.toInt() else 0xFF4E4442.toInt()
                    dividerColor = if (isDarkMode) 0x20FFB5A4 else 0x208F4C38
                    plusIconColor = if (isDarkMode) 0xFFFFB5A4.toInt() else 0xFF8F4C38.toInt()
                }

                val argbBgColor = (0xFF shl 24) or (baseBgColor and 0x00FFFFFF)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.note_widget_layout)

                    // 1. Background style using setAlpha (universally supported Float RemoteView method)
                    views.setInt(R.id.widget_background_view, "setColorFilter", argbBgColor)
                    views.setFloat(R.id.widget_background_view, "setAlpha", opacity / 100f)

                    // 2. Text colors
                    views.setTextViewText(R.id.widget_title, widgetTitleText)
                    views.setTextColor(R.id.widget_title, textColorHeader)
                    views.setInt(R.id.widget_divider, "setBackgroundColor", dividerColor)

                    // Plus icon tint
                    views.setInt(R.id.btn_add_note, "setColorFilter", plusIconColor)

                    // 3. Click intent for the add button
                    val addIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("action", "ADD_NOTE")
                    }
                    val addPendingIntent = PendingIntent.getActivity(
                        context,
                        100,
                        addIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.btn_add_note, addPendingIntent)

                    // 4. Bind notes to slots
                    if (notes.isEmpty()) {
                        views.setViewVisibility(R.id.widget_empty_text, View.VISIBLE)
                        views.setTextViewText(R.id.widget_empty_text, widgetEmptyTextMsg)
                        views.setViewVisibility(R.id.notes_container, View.GONE)
                        views.setTextColor(R.id.widget_empty_text, textColorContent)
                    } else {
                        views.setViewVisibility(R.id.widget_empty_text, View.GONE)
                        views.setViewVisibility(R.id.notes_container, View.VISIBLE)

                        class SlotViews(val container: Int, val title: Int, val content: Int, val colorBar: Int)
                        val slots = listOf(
                            SlotViews(R.id.note_slot_1, R.id.note_title_1, R.id.note_content_1, R.id.note_color_bar_1),
                            SlotViews(R.id.note_slot_2, R.id.note_title_2, R.id.note_content_2, R.id.note_color_bar_2),
                            SlotViews(R.id.note_slot_3, R.id.note_title_3, R.id.note_content_3, R.id.note_color_bar_3)
                        )
                        val dividers = listOf(R.id.note_divider_1, R.id.note_divider_2)

                        for (i in 0..2) {
                            val slot = slots[i]
                            if (i < notes.size) {
                                val note = notes[i]
                                views.setViewVisibility(slot.container, View.VISIBLE)
                                
                                // Pinned status indication
                                val titlePrefix = if (note.isPinned) "📌 " else ""
                                views.setTextViewText(slot.title, titlePrefix + note.title.ifBlank { untitledText })
                                views.setTextViewText(slot.content, cleanMarkdownForWidget(note.content))

                                views.setTextColor(slot.title, textColorTitle)
                                views.setTextColor(slot.content, textColorContent)

                                // Tint the rounded left-aligned color indicator bar dynamically
                                val indicatorColor = getIndicatorColor(note.colorHex, isDarkMode, plusIconColor)
                                views.setInt(slot.colorBar, "setColorFilter", indicatorColor)

                                // Setup divider if needed
                                if (i < 2) {
                                    if (i < notes.size - 1) {
                                        views.setViewVisibility(dividers[i], View.VISIBLE)
                                        views.setInt(dividers[i], "setBackgroundColor", dividerColor)
                                    } else {
                                        views.setViewVisibility(dividers[i], View.GONE)
                                    }
                                }

                                // Setup click intent for this note
                                val noteIntent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    putExtra("action", "EDIT_NOTE")
                                    putExtra("note_id", note.id)
                                }
                                val notePendingIntent = PendingIntent.getActivity(
                                    context,
                                    note.id, // unique request code
                                    noteIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                views.setOnClickPendingIntent(slot.container, notePendingIntent)
                            } else {
                                views.setViewVisibility(slot.container, View.GONE)
                                if (i < 2) {
                                    views.setViewVisibility(dividers[i], View.GONE)
                                }
                            }
                        }
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    // Returns a beautiful, vibrant Material-style indicator color for note items in the widget
    private fun getIndicatorColor(colorName: String?, isDarkMode: Boolean, defaultColor: Int): Int {
        return when (colorName) {
            "sage" -> if (isDarkMode) 0xFF81C784.toInt() else 0xFF4CAF50.toInt()
            "sky" -> if (isDarkMode) 0xFF64B5F6.toInt() else 0xFF2196F3.toInt()
            "lavender" -> if (isDarkMode) 0xFFBA68C8.toInt() else 0xFF9C27B0.toInt()
            "rose" -> if (isDarkMode) 0xFFF06292.toInt() else 0xFFE91E63.toInt()
            "peach" -> if (isDarkMode) 0xFFFFB74D.toInt() else 0xFFFF9800.toInt()
            "slate" -> if (isDarkMode) 0xFF90A4AE.toInt() else 0xFF607D8B.toInt()
            else -> defaultColor
        }
    }

    // Helper to strip markdown and provide clean text for the widget body preview
    private fun cleanMarkdownForWidget(markdown: String): String {
        var clean = markdown
            .replace(Regex("(?m)^#+\\s+"), "") // Headings
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // Bold
            .replace(Regex("\\*(.*?)\\*"), "$1") // Italic
            .replace(Regex("`(.*?)`"), "$1") // Code
            .replace(Regex("(?m)^>\\s+"), "") // Blockquotes
            .replace(Regex("(?m)^-\\s+"), "") // List items
            .replace(Regex("(?m)^\\*\\s+"), "") // List items
            .trim()
        if (clean.length > 80) {
            clean = clean.substring(0, 77) + "..."
        }
        return clean.ifBlank { "无具体内容" }
    }
}
