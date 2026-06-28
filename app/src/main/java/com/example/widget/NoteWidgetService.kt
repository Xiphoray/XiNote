package com.example.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Note
import kotlinx.coroutines.runBlocking

class NoteWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NoteWidgetFactory(this.applicationContext)
    }
}

class NoteWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var notes: List<Note> = emptyList()
    private val db = AppDatabase.getDatabase(context)

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Fetch notes synchronously for the widget
        runBlocking {
            notes = db.noteDao().getWidgetNotes()
        }
    }

    override fun onDestroy() {
        notes = emptyList()
    }

    override fun getCount(): Int = notes.size

    override fun getViewAt(position: Int): RemoteViews {
        val note = notes[position]
        val views = RemoteViews(context.packageName, R.layout.widget_list_item)
        
        val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        
        val textColorTitle: Int
        val textColorContent: Int
        val plusIconColor: Int
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            textColorTitle = if (isDarkMode) context.getColor(android.R.color.system_neutral1_100) else context.getColor(android.R.color.system_neutral1_900)
            textColorContent = if (isDarkMode) context.getColor(android.R.color.system_neutral1_300) else context.getColor(android.R.color.system_neutral1_700)
            plusIconColor = if (isDarkMode) context.getColor(android.R.color.system_accent1_200) else context.getColor(android.R.color.system_accent1_600)
        } else {
            textColorTitle = if (isDarkMode) 0xFFECE0DF.toInt() else 0xFF1F1B1A.toInt()
            textColorContent = if (isDarkMode) 0xFFD8C2BE.toInt() else 0xFF4E4442.toInt()
            plusIconColor = if (isDarkMode) 0xFFFFB5A4.toInt() else 0xFF8F4C38.toInt()
        }

        val titlePrefix = if (note.isPinned) "📌 " else ""
        views.setTextViewText(R.id.note_title, titlePrefix + note.title.ifBlank { "无标题" })
        views.setTextViewText(R.id.note_content, cleanMarkdownForWidget(note.content))
        
        views.setTextColor(R.id.note_title, textColorTitle)
        views.setTextColor(R.id.note_content, textColorContent)
        
        val indicatorColor = getIndicatorColor(note.colorHex, isDarkMode, plusIconColor)
        views.setInt(R.id.note_color_bar, "setColorFilter", indicatorColor)
        
        val fillInIntent = Intent().apply {
            putExtra("action", "EDIT_NOTE")
            putExtra("note_id", note.id)
        }
        views.setOnClickFillInIntent(R.id.note_slot, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = notes[position].id.toLong()
    override fun hasStableIds(): Boolean = true
    
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

    private fun cleanMarkdownForWidget(markdown: String): String {
        var clean = markdown
            .replace(Regex("(?m)^#+\\\\s+"), "")
            .replace(Regex("\\\\*\\\\*(.*?)\\\\*\\\\*"), "$1")
            .replace(Regex("\\\\*(.*?)\\\\*"), "$1")
            .replace(Regex("`(.*?)`"), "$1")
            .replace(Regex("(?m)^>\\\\s+"), "")
            .replace(Regex("(?m)^-\\\\s+"), "")
            .replace(Regex("(?m)^\\\\*\\\\s+"), "")
            .trim()
        if (clean.length > 250) {
            clean = clean.substring(0, 247) + "..."
        }
        return clean.ifBlank { "无具体内容" }
    }
}
