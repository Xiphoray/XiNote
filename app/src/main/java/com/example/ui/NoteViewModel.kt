package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    object Home : Screen
    data class EditNote(val noteId: Int?) : Screen
}

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // 1. Navigation State
    val currentScreen = MutableStateFlow<Screen>(Screen.Home)

    // 2. Search Query State
    val searchQuery = MutableStateFlow("")

    // 3. All Notes Flow from Database
    val allNotes: StateFlow<List<Note>> = repository.allNotesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 4. Filtered Notes Flow (Combines All Notes + Search Query)
    val filteredNotes: StateFlow<List<Note>> = combine(allNotes, searchQuery) { notes, query ->
        if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 5. Current Active Note for Editing
    val currentEditingNote = MutableStateFlow<Note?>(null)

    // 6. Widget Opacity Configuration
    val widgetOpacity = MutableStateFlow(90) // 0 - 100 percentage

    // 7. Language Selection Configuration (Default ZH, supports persistent switching)
    val currentLanguage = MutableStateFlow(AppLanguage.ZH)

    fun loadWidgetOpacity(context: Context) {
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        widgetOpacity.value = sp.getInt("widget_opacity", 90)
    }

    fun loadLanguage(context: Context) {
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        val savedLangCode = sp.getString("selected_language", AppLanguage.ZH.code) ?: AppLanguage.ZH.code
        val lang = AppLanguage.values().find { it.code == savedLangCode } ?: AppLanguage.ZH
        currentLanguage.value = lang
    }

    fun changeLanguage(context: Context, language: AppLanguage) {
        currentLanguage.value = language
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putString("selected_language", language.code).apply()
        // Instantly trigger widget update to sync widget text translations as well!
        repository.triggerWidgetUpdate()
    }

    fun updateWidgetOpacity(context: Context, opacity: Int) {
        widgetOpacity.value = opacity
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putInt("widget_opacity", opacity).apply()
        // Instantly sync the widget to reflect the transparent style
        repository.triggerWidgetUpdate()
    }

    // Navigation triggers
    fun navigateToHome() {
        currentScreen.value = Screen.Home
        currentEditingNote.value = null
    }

    fun navigateToEditNote(noteId: Int?, context: Context? = null) {
        viewModelScope.launch {
            if (noteId != null) {
                val note = repository.getNoteById(noteId)
                if (note != null) {
                    currentEditingNote.value = note
                    currentScreen.value = Screen.EditNote(noteId)
                } else {
                    currentScreen.value = Screen.Home
                }
            } else {
                // Initialize clean default note with an optional template or blank
                currentEditingNote.value = Note(
                    title = "",
                    content = "# "
                )
                currentScreen.value = Screen.EditNote(null)
            }
        }
    }

    // Database updates
    fun saveNote(title: String, content: String, colorHex: String?, isPinned: Boolean) {
        val note = currentEditingNote.value ?: return
        viewModelScope.launch {
            val updatedNote = note.copy(
                title = title,
                content = content,
                colorHex = colorHex,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            repository.insert(updatedNote)
            navigateToHome()
        }
    }

    fun deleteNote() {
        val note = currentEditingNote.value ?: return
        viewModelScope.launch {
            if (note.id != 0) {
                repository.delete(note)
            }
            navigateToHome()
        }
    }

    fun deleteNoteDirectly(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.insert(note.copy(isPinned = !note.isPinned))
        }
    }

    fun changeNoteColorDirectly(note: Note, colorHex: String?) {
        viewModelScope.launch {
            repository.insert(note.copy(colorHex = colorHex))
        }
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
