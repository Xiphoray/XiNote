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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

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

                    // Get widget height dynamically to determine how many items fit
                    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    val minHeight = options?.getInt("appWidgetMinHeight", 180) ?: 180
                    val maxSlots = when {
                        minHeight < 110 -> 1
                        minHeight < 190 -> 2
                        else -> 3
                    }

                    // 1. Background style using setAlpha (universally supported Float RemoteView method)
                    views.setInt(R.id.widget_background_view, "setColorFilter", argbBgColor)
                    views.setFloat(R.id.widget_background_view, "setAlpha", opacity / 100f)

                    // 2. Text colors
                    views.setTextViewText(R.id.widget_title, widgetTitleText)
                    views.setTextColor(R.id.widget_title, textColorHeader)
                    views.setInt(R.id.widget_divider, "setBackgroundColor", dividerColor)

                    // Click intent for the widget title to open home
                    val homeIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val homePendingIntent = PendingIntent.getActivity(
                        context,
                        101, // Use a different request code
                        homeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_title, homePendingIntent)

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

                    // 4. Bind notes to ListView using RemoteViewsService
                    if (notes.isEmpty()) {
                        views.setViewVisibility(R.id.widget_empty_text, View.VISIBLE)
                        views.setTextViewText(R.id.widget_empty_text, widgetEmptyTextMsg)
                        views.setViewVisibility(R.id.notes_list, View.GONE)
                        views.setTextColor(R.id.widget_empty_text, textColorContent)
                    } else {
                        views.setViewVisibility(R.id.widget_empty_text, View.GONE)
                        views.setViewVisibility(R.id.notes_list, View.VISIBLE)

                        val serviceIntent = Intent(context, NoteWidgetService::class.java)
                        views.setRemoteAdapter(R.id.notes_list, serviceIntent)
                        
                        // Handle clicks in the ListView
                        val clickIntentTemplate = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        val pendingIntentTemplate = PendingIntent.getActivity(
                            context,
                            0,
                            clickIntentTemplate,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                        )
                        views.setPendingIntentTemplate(R.id.notes_list, pendingIntentTemplate)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.notes_list)
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
        if (clean.length > 250) {
            clean = clean.substring(0, 247) + "..."
        }
        return clean.ifBlank { "无具体内容" }
    }
}
