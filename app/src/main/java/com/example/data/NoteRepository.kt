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
        val intent = Intent().apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            component = ComponentName(context, "com.example.widget.NoteAppWidgetProvider")
        }
        context.sendBroadcast(intent)
    }
}
