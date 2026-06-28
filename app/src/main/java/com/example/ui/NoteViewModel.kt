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
    object Settings : Screen
    object About : Screen
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
                it.content.contains(query, ignoreCase = true) ||
                it.topic.contains(query, ignoreCase = true)
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

    // 8. Theme Selection Configuration (Default system, supports light/dark switching)
    val currentTheme = MutableStateFlow("system")

    val listLayout = MutableStateFlow(1) // 0: 1 col, 1: 2 col, 2: staggered
    val groupByTopic = MutableStateFlow(false)

    fun loadPreferences(context: Context) {
        loadWidgetOpacity(context)
        loadLanguage(context)
        loadTheme(context)
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        listLayout.value = sp.getInt("list_layout", 1)
        groupByTopic.value = sp.getBoolean("group_by_topic", false)
    }

    fun setListLayout(context: Context, layout: Int) {
        listLayout.value = layout
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putInt("list_layout", layout).apply()
    }

    fun setGroupByTopic(context: Context, group: Boolean) {
        groupByTopic.value = group
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putBoolean("group_by_topic", group).apply()
    }

    fun insertNote(note: Note) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }

    fun autoAssignTopic(context: Context) {
        viewModelScope.launch {
            val notes = repository.getLatestNotes(1000)
            var count = 0
            notes.forEach { note ->
                if (note.topic == "默认" || note.topic.isBlank()) {
                    val assignedTopic = getAutoTopic(note.title, note.content)
                    if (assignedTopic != "默认") {
                        repository.insert(note.copy(topic = assignedTopic))
                        count++
                    }
                }
            }
            android.widget.Toast.makeText(context, "一键智能分配主题完成 (更新了 $count 条记事)", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAutoTopic(title: String, content: String): String {
        val text = (title + " " + content).lowercase(java.util.Locale.getDefault())
        return when {
            text.contains("code") || text.contains("bug") || text.contains("develop") || text.contains("编程") || text.contains("代码") -> "工作/代码"
            text.contains("plan") || text.contains("todo") || text.contains("schedule") || text.contains("meeting") || text.contains("计划") || text.contains("会议") -> "计划/待办"
            text.contains("shopping") || text.contains("buy") || text.contains("cart") || text.contains("购物") || text.contains("买") -> "购物"
            text.contains("idea") || text.contains("creative") || text.contains("点子") || text.contains("想法") || text.contains("灵感") -> "灵感/点子"
            text.contains("love") || text.contains("heart") || text.contains("纪念日") || text.contains("情侣") -> "情感/纪念日"
            text.contains("finance") || text.contains("money") || text.contains("cost") || text.contains("钱") || text.contains("账单") || text.contains("工资") || text.contains("理财") -> "财务/理财"
            text.contains("study") || text.contains("learn") || text.contains("read") || text.contains("学习") || text.contains("看书") -> "学习"
            else -> "默认"
        }
    }

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

    fun loadTheme(context: Context) {
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        currentTheme.value = sp.getString("app_theme", "system") ?: "system"
    }

    fun changeTheme(context: Context, theme: String) {
        currentTheme.value = theme
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putString("app_theme", theme).apply()
    }

    fun updateWidgetOpacity(context: Context, opacity: Int) {
        widgetOpacity.value = opacity
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putInt("widget_opacity", opacity).apply()
        // Instantly sync the widget to reflect the transparent style
        repository.triggerWidgetUpdate()
    }

    fun triggerWidgetUpdate() {
        repository.triggerWidgetUpdate()
    }

    // WebDAV backup config and operations
    fun getWebDavConfig() = repository.backupManager.getWebDavConfig()

    fun saveWebDavConfig(config: com.example.data.WebDavConfig) {
        repository.backupManager.saveWebDavConfig(config)
    }

    suspend fun backupToWebDav(): Boolean {
        return repository.backupManager.backupToCloud()
    }

    suspend fun restoreFromWebDav(): Boolean {
        val result = repository.backupManager.restoreFromCloud()
        if (result) {
            // Widget should be updated after restoration
            repository.triggerWidgetUpdate()
        }
        return result
    }

    // Navigation triggers
    fun navigateToHome() {
        currentScreen.value = Screen.Home
        currentEditingNote.value = null
    }

    fun navigateToSettings() {
        currentScreen.value = Screen.Settings
    }

    fun navigateToAbout() {
        currentScreen.value = Screen.About
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
    fun saveNote(title: String, content: String, colorHex: String?, isPinned: Boolean, showInWidget: Boolean) {
        val note = currentEditingNote.value ?: return
        viewModelScope.launch {
            val updatedNote = note.copy(
                title = title,
                content = content,
                colorHex = colorHex,
                isPinned = isPinned,
                showInWidget = showInWidget,
                updatedAt = System.currentTimeMillis()
            )
            repository.insert(updatedNote)
            
            // Trigger automatic background backup to WebDAV if credentials are configured
            launch {
                val config = repository.backupManager.getWebDavConfig()
                if (config.url.isNotBlank() && config.username.isNotBlank() && config.password.isNotBlank()) {
                    repository.backupManager.backupToCloud()
                }
            }
            
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
