package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Beautiful modern vibrant pastel colors matching dark and light themes (High Density Palette)
fun getNoteCardColor(colorHex: String?, isDark: Boolean): Color {
    if (colorHex == null || colorHex == "default") {
        return if (isDark) Color(0xFF261D1C) else Color(0xFFFFFBFA)
    }
    return when (colorHex) {
        "sage" -> if (isDark) Color(0xFF1D3B23) else Color(0xFFE2F4E4)
        "sky" -> if (isDark) Color(0xFF1B364A) else Color(0xFFDCEEFA)
        "lavender" -> if (isDark) Color(0xFF33203D) else Color(0xFFF0E5F9)
        "rose" -> if (isDark) Color(0xFF431F25) else Color(0xFFFCE4E8)
        "peach" -> if (isDark) Color(0xFF41281A) else Color(0xFFFCEADA)
        "slate" -> if (isDark) Color(0xFF2A2C30) else Color(0xFFE9ECEF)
        else -> if (isDark) Color(0xFF261D1C) else Color(0xFFFFFBFA)
    }
}

val NOTE_COLORS = listOf(
    "default" to "宣纸白",
    "sage" to "竹青",
    "sky" to "天青",
    "lavender" to "丁香",
    "rose" to "胭脂",
    "peach" to "秋香",
    "slate" to "黛灰"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val widgetOpacity by viewModel.widgetOpacity.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var showWidgetSettings by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<com.example.data.Note?>(null) }
    val isDark = isSystemInDarkTheme()

    if (noteToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(Localization.getString("delete", currentLanguage) ?: "删除记事") },
            text = { Text("确定要删除这条记事吗？删除后将无法恢复。") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.deleteNoteDirectly(noteToDelete!!)
                        noteToDelete = null
                    }
                ) {
                    Text(Localization.getString("delete", currentLanguage) ?: "删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { noteToDelete = null }
                ) {
                    Text(Localization.getString("cancel", currentLanguage) ?: "取消")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            var showTemplateMenu by remember { mutableStateOf(false) }
            val templates by viewModel.templates.collectAsState()
            Box(modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onClick = { viewModel.navigateToEditNote(null) },
                                onLongClick = { showTemplateMenu = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = Localization.getString("add_note", currentLanguage))
                    }
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = showTemplateMenu,
                    onDismissRequest = { showTemplateMenu = false }
                ) {
                    templates.forEach { template ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(template.name) },
                            onClick = {
                                viewModel.navigateToEditNoteWithSharedText(template.content, context)
                                showTemplateMenu = false
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 800.dp)
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
            // Search Bar & Header Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text(Localization.getString("search_placeholder", currentLanguage), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = Localization.getString("search_placeholder", currentLanguage)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = Localization.getString("cancel", currentLanguage))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                var showViewOptions by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showViewOptions = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewAgenda,
                            contentDescription = "View Options",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = showViewOptions,
                        onDismissRequest = { showViewOptions = false }
                    ) {
                        val listLayout by viewModel.listLayout.collectAsState()
                        
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(if (listLayout == 0) "当前: 一列" else "当前: 两列") },
                            onClick = {
                                viewModel.setListLayout(context, (listLayout + 1) % 2)
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("一键智能分配主题") },
                            onClick = {
                                viewModel.autoAssignTopic(context)
                                showViewOptions = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Consolidated Settings Page Button
                IconButton(
                    onClick = { viewModel.navigateToSettings() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = Localization.getString("settings", currentLanguage),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Notes List/Grid
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_high_density_empty),
                            contentDescription = Localization.getString("no_notes", currentLanguage),
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) Localization.getString("no_notes_found", currentLanguage) else Localization.getString("write_first_idea", currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) Localization.getString("try_different_keyword", currentLanguage) else Localization.getString("click_to_start", currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                val listLayout by viewModel.listLayout.collectAsState()
                val collapsedTopics by viewModel.collapsedTopics.collectAsState()
                val configuration = LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp
                
                val gridColumns = when (listLayout) {
                    0 -> StaggeredGridCells.Fixed(1)
                    1 -> StaggeredGridCells.Fixed(2)
                    else -> StaggeredGridCells.Fixed(2)
                }

                LazyVerticalStaggeredGrid(
                    columns = gridColumns,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    val pinnedNotes = notes.filter { it.isPinned }
                    val otherNotes = notes.filter { !it.isPinned }

                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = Localization.getString("pinned", currentLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(pinnedNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                isDark = isDark,
                                onClick = { viewModel.navigateToEditNote(note.id) },
                                onTogglePin = { viewModel.togglePin(note) },
                                onDelete = { noteToDelete = note },
                                onUpdateNote = { viewModel.insertNote(it) },
                                currentLanguage = currentLanguage,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        val groupedNotes = otherNotes.groupBy { it.topic }.toSortedMap()
                        groupedNotes.forEach { (topic, topicNotes) ->
                            item(span = StaggeredGridItemSpan.FullLine) {
                                val isCollapsed = collapsedTopics.contains(topic)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.toggleCollapsedTopic(context, topic)
                                        }
                                        .padding(top = 12.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = topic,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                        contentDescription = "Collapse",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            if (!collapsedTopics.contains(topic)) {
                                items(topicNotes, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        isDark = isDark,
                                        onClick = { viewModel.navigateToEditNote(note.id) },
                                        onTogglePin = { viewModel.togglePin(note) },
                                        onDelete = { noteToDelete = note },
                                        onUpdateNote = { viewModel.insertNote(it) },
                                        currentLanguage = currentLanguage,
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Widget Customization Dialog
    if (showWidgetSettings) {
        WidgetSettingsDialog(
            currentOpacity = widgetOpacity,
            isDark = isDark,
            currentLanguage = currentLanguage,
            onOpacityChange = { viewModel.updateWidgetOpacity(context, it) },
            onDismiss = { showWidgetSettings = false }
        )
    }
}

fun getCategoryEmoji(topic: String, title: String, content: String): String {
    return when (topic) {
        "工作/代码" -> "💻"
        "计划/待办" -> "📅"
        "购物" -> "🛒"
        "灵感/点子" -> "💡"
        "情感/纪念日" -> "💖"
        "财务/理财" -> "💵"
        "学习" -> "📚"
        "默认" -> {
            val text = (title + " " + content).lowercase(Locale.getDefault())
            when {
                text.contains("code") || text.contains("bug") || text.contains("develop") || text.contains("programming") || text.contains("编程") || text.contains("代码") -> "💻"
                text.contains("plan") || text.contains("todo") || text.contains("schedule") || text.contains("meeting") || text.contains("agenda") || text.contains("计划") || text.contains("会议") -> "📅"
                text.contains("shopping") || text.contains("buy") || text.contains("cart") || text.contains("store") || text.contains("购物") || text.contains("买") || text.contains("超市") -> "🛒"
                text.contains("idea") || text.contains("creative") || text.contains("thought") || text.contains("brainstorm") || text.contains("点子") || text.contains("想法") || text.contains("灵感") -> "💡"
                text.contains("love") || text.contains("heart") || text.contains("like") || text.contains("anniversary") || text.contains("爱") || text.contains("喜欢") || text.contains("纪念日") || text.contains("情侣") -> "💖"
                text.contains("finance") || text.contains("money") || text.contains("cost") || text.contains("pay") || text.contains("salary") || text.contains("钱") || text.contains("账单") || text.contains("工资") || text.contains("理财") -> "💵"
                text.contains("study") || text.contains("learn") || text.contains("read") || text.contains("book") || text.contains("学习") || text.contains("看书") || text.contains("书") || text.contains("课程") -> "📚"
                text.contains("music") || text.contains("song") || text.contains("sing") || text.contains("concert") || text.contains("音乐") || text.contains("歌") || text.contains("演唱会") -> "🎵"
                text.contains("food") || text.contains("eat") || text.contains("cook") || text.contains("recipe") || text.contains("饭") || text.contains("吃") || text.contains("菜谱") || text.contains("美味") -> "🍕"
                text.contains("sport") || text.contains("run") || text.contains("workout") || text.contains("gym") || text.contains("health") || text.contains("运动") || text.contains("跑") || text.contains("健身") || text.contains("健康") -> "🏃"
                text.contains("travel") || text.contains("trip") || text.contains("flight") || text.contains("vacation") || text.contains("旅游") || text.contains("出行") || text.contains("飞机") || text.contains("度假") -> "✈️"
                else -> "📝"
            }
        }
        else -> "📝"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    isDark: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onUpdateNote: (Note) -> Unit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val cardColor = getNoteCardColor(note.colorHex, isDark)
    val dateFormat = remember { SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()) }
    val categoryEmoji = remember(note.topic, note.title, note.content) { getCategoryEmoji(note.topic, note.title, note.content) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (isDark) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth()
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Cute dynamic category sticker badge!
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = categoryEmoji,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = note.title.ifBlank { Localization.getString("untitled", currentLanguage) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body preview: strip heading symbols and display a clean snippet
            val previewText = remember(note.content) {
                stripMarkdown(note.content)
            }

            Text(
                text = previewText.ifBlank { Localization.getString("empty_content", currentLanguage) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Dynamic task progress tracker for todo-style note content
            val todoStats = remember(note.content) {
                val hasBracket = note.content.contains("[ ]") || note.content.contains("[x]") || note.content.contains("[X]")
                if (hasBracket) {
                    val total = note.content.split(Regex("\\[[ xX]\\]")).size - 1
                    val checked = note.content.split(Regex("\\[[xX]\\]")).size - 1
                    if (total > 0) checked to total else null
                } else {
                    null
                }
            }

            if (todoStats != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val (checked, total) = todoStats
                val progress = checked.toFloat() / total
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "任务进度: $checked/$total",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = dateFormat.format(Date(note.updatedAt)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = Localization.getString(if (note.isPinned) "unpin" else "pin", currentLanguage),
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = Localization.getString("delete", currentLanguage),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            androidx.compose.material3.DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(if (note.isPinned) "取消固定" else "固定记事") },
                    onClick = {
                        onTogglePin()
                        showMenu = false
                    }
                )
            }
        }
    }
}
}

@Composable
fun WidgetSettingsDialog(
    currentOpacity: Int,
    isDark: Boolean,
    currentLanguage: AppLanguage,
    onOpacityChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var opacity by remember { mutableStateOf(currentOpacity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.getString("widget_customization", currentLanguage), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = Localization.getString("widget_customization_desc", currentLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Localization.getString("opacity", currentLanguage), fontWeight = FontWeight.SemiBold)
                    Text("$opacity%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = opacity.toFloat(),
                    onValueChange = {
                        opacity = it.toInt()
                        onOpacityChange(it.toInt())
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Canvas of the Widget
                Text(Localization.getString("desktop_preview", currentLanguage), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                val previewBgColor = if (isDark) {
                    val alpha = (opacity * 2.55).toInt()
                    Color(0xFF1F1B1A).copy(alpha = alpha / 255f)
                } else {
                    val alpha = (opacity * 2.55).toInt()
                    Color(0xFFF5DED8).copy(alpha = alpha / 255f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            // Simulate desktop background wallpaper with a nice warm gradient
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF8F4C38), Color(0xFFF5DED8))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulated Widget Body
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(previewBgColor, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Localization.getString("app_title", currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFECE0DF) else Color(0xFF1F1B1A)
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFFFB5A4) else Color(0xFF8F4C38),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = if (isDark) Color(0xFFFFB5A4).copy(0.2f) else Color(0xFF8F4C38).copy(0.2f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Localization.getString("realtime_preview_title", currentLanguage),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFECE0DF) else Color(0xFF1F1B1A)
                        )
                        Text(
                            Localization.getString("realtime_preview_desc", currentLanguage),
                            fontSize = 8.sp,
                            color = if (isDark) Color(0xFFD8C2BE) else Color(0xFF4E4442),
                            maxLines = 1
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.getString("done", currentLanguage))
            }
        }
    )
}
