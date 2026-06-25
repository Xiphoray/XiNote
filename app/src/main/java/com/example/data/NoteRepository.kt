package com.example.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val context: Context, private val noteDao: NoteDao) {
    val allNotesFlow: Flow<List<Note>> = noteDao.getAllNotesFlow()

    suspend fun getLatestNotes(limit: Int): List<Note> {
        return noteDao.getLatestNotes(limit)
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun insert(note: Note): Long {
        val id = noteDao.insertNote(note)
        triggerWidgetUpdate()
        return id
    }

    suspend fun delete(note: Note) {
        noteDao.deleteNote(note)
        triggerWidgetUpdate()
    }

    fun triggerWidgetUpdate() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, "com.example.widget.NoteAppWidgetProvider")
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                val intent = Intent().apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                    this.component = component
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
