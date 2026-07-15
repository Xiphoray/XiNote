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
    object ManageTemplates : Screen
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
    val collapsedTopics = MutableStateFlow<Set<String>>(emptySet())

    fun loadPreferences(context: Context) {
        loadWidgetOpacity(context)
        loadLanguage(context)
        loadTheme(context)
        loadTemplates(context)
        
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        listLayout.value = sp.getInt("list_layout", 1)
        val savedCollapsed = sp.getStringSet("collapsed_topics", emptySet())
        collapsedTopics.value = savedCollapsed ?: emptySet()
    }

    fun setListLayout(context: Context, layout: Int) {
        listLayout.value = layout
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putInt("list_layout", layout).apply()
    }

    fun toggleCollapsedTopic(context: Context, topic: String) {
        val current = collapsedTopics.value.toMutableSet()
        if (current.contains(topic)) {
            current.remove(topic)
        } else {
            current.add(topic)
        }
        collapsedTopics.value = current
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        sp.edit().putStringSet("collapsed_topics", current).apply()
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
            val toastMessage = String.format(Localization.getString("auto_assign_topic_success", currentLanguage.value), count)
            android.widget.Toast.makeText(context, toastMessage, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAutoTopic(title: String, content: String): String {
        val text = (title + " " + content).lowercase(java.util.Locale.getDefault())
        return when {
            text.contains("code") || text.contains("bug") || text.contains("develop") || text.contains("编程") || text.contains("代码") -> Localization.getString("topic_work", currentLanguage.value) ?: "工作/代码"
            text.contains("plan") || text.contains("todo") || text.contains("schedule") || text.contains("meeting") || text.contains("计划") || text.contains("会议") -> Localization.getString("topic_plan", currentLanguage.value) ?: "计划/待办"
            text.contains("shopping") || text.contains("buy") || text.contains("cart") || text.contains("购物") || text.contains("买") -> Localization.getString("topic_shopping", currentLanguage.value) ?: "购物"
            text.contains("idea") || text.contains("creative") || text.contains("点子") || text.contains("想法") || text.contains("灵感") -> Localization.getString("topic_idea", currentLanguage.value) ?: "灵感/点子"
            text.contains("love") || text.contains("heart") || text.contains("纪念日") || text.contains("情侣") -> Localization.getString("topic_love", currentLanguage.value) ?: "情感/纪念日"
            text.contains("finance") || text.contains("money") || text.contains("cost") || text.contains("钱") || text.contains("账单") || text.contains("工资") || text.contains("理财") -> Localization.getString("topic_finance", currentLanguage.value) ?: "财务/理财"
            text.contains("study") || text.contains("learn") || text.contains("read") || text.contains("学习") || text.contains("看书") -> Localization.getString("topic_study", currentLanguage.value) ?: "学习"
            else -> Localization.getString("topic_default", currentLanguage.value) ?: "默认"
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

    data class NoteTemplate(val name: String, val content: String)
    
    private val _templates = MutableStateFlow<List<NoteTemplate>>(emptyList())
    val templates: StateFlow<List<NoteTemplate>> = _templates

    fun loadTemplates(context: Context) {
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        val defaultTemplates = listOf(
            NoteTemplate("日记", "# 日记\n\n日期: \n\n天气: \n\n## 今日感悟\n\n"),
            NoteTemplate("会议记录", "# 会议记录\n\n主题: \n\n参会人: \n\n## 会议纪要\n\n- \n\n## 待办事项\n\n- [ ] "),
            NoteTemplate("购物清单", "# 购物清单\n\n## 超市\n- [ ] \n- [ ] \n\n## 网购\n- [ ] "),
            NoteTemplate("待办清单", "# 待办清单\n\n## 今日任务\n- [ ] \n- [ ] \n\n## 明日计划\n- [ ] "),
            NoteTemplate("读书笔记", "# 读书笔记\n\n书名: \n\n## 摘抄\n>\n\n## 读后感\n"),
            NoteTemplate("周报汇报", "# 周报\n\n## 本周工作完成情况\n- \n\n## 下周工作计划\n- \n\n## 需要协调的资源\n- "),
            NoteTemplate("旅行计划", "# 旅行计划\n\n目的地: \n\n## 行程安排\n- Day 1: \n- Day 2: \n\n## 必带物品\n- [ ] 身份证\n- [ ] 充电宝\n")
        )
        val saved = sp.getString("note_templates", null)
        if (saved != null) {
            val list = mutableListOf<NoteTemplate>()
            saved.split("|||").forEach {
                val parts = it.split("===")
                if (parts.size == 2) {
                    list.add(NoteTemplate(parts[0], parts[1]))
                }
            }
            if (list.isNotEmpty()) {
                _templates.value = list
                return
            }
        }
        _templates.value = defaultTemplates
    }

    fun saveTemplates(context: Context, newTemplates: List<NoteTemplate>) {
        _templates.value = newTemplates
        val sp = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        val serialized = newTemplates.joinToString("|||") { "${it.name}===${it.content}" }
        sp.edit().putString("note_templates", serialized).apply()
    }


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
    
    fun navigateToManageTemplates() {
        currentScreen.value = Screen.ManageTemplates
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
                    content = ""
                )
                currentScreen.value = Screen.EditNote(null)
            }
        }
    }

    fun navigateToEditNoteWithSharedText(text: String, context: Context? = null) {
        viewModelScope.launch {
            currentEditingNote.value = Note(
                title = "",
                content = text
            )
            currentScreen.value = Screen.EditNote(null)
        }
    }

    // Database updates
    fun saveNote(title: String, content: String, colorHex: String?, topic: String, isPinned: Boolean, showInWidget: Boolean, navigateBack: Boolean = true) {
        val note = currentEditingNote.value ?: return
        viewModelScope.launch {
            val updatedNote = note.copy(
                title = title,
                content = content,
                colorHex = colorHex,
                topic = topic,
                isPinned = isPinned,
                showInWidget = showInWidget,
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.insert(updatedNote).toInt()
            
            // Update currentEditingNote so we know it's saved
            val finalId = if (note.id == 0 && newId > 0) newId else note.id
            currentEditingNote.value = updatedNote.copy(id = finalId)
            
            // Trigger automatic background backup to WebDAV if credentials are configured
            launch {
                val config = repository.backupManager.getWebDavConfig()
                if (config.url.isNotBlank() && config.username.isNotBlank() && config.password.isNotBlank()) {
                    repository.backupManager.backupToCloud()
                }
            }
            
            if (navigateBack) {
                navigateToHome()
            }
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
