package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import android.widget.Toast
import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val note by viewModel.currentEditingNote.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current

    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(Localization.getString("loading", currentLanguage), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val noteId = note?.id
    var title by rememberSaveable(noteId) { mutableStateOf(note?.title ?: "") }
    var contentValue by rememberSaveable(noteId, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(note?.content ?: ""))
    }
    
    val undoStack = remember(noteId) { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember(noteId) { mutableStateListOf<TextFieldValue>() }
    var lastSaveTime by remember(noteId) { mutableLongStateOf(System.currentTimeMillis()) }

    fun updateContent(newValue: TextFieldValue) {
        if (contentValue.text != newValue.text) {
            val currentTime = System.currentTimeMillis()
            val textDiff = Math.abs(contentValue.text.length - newValue.text.length)
            val isWhitespace = newValue.text.length > contentValue.text.length && newValue.text.lastOrNull()?.isWhitespace() == true
            
            if (textDiff > 1 || (currentTime - lastSaveTime > 1000) || isWhitespace) {
                if (undoStack.isEmpty() || undoStack.last().text != contentValue.text) {
                    undoStack.add(contentValue)
                    if (undoStack.size > 100) undoStack.removeAt(0)
                }
                lastSaveTime = currentTime
                redoStack.clear()
            }
        }
        contentValue = newValue
    }

    fun performUndo() {
        if (undoStack.isNotEmpty()) {
            if (redoStack.isEmpty() || redoStack.last().text != contentValue.text) {
                redoStack.add(contentValue)
            }
            contentValue = undoStack.removeLast()
            lastSaveTime = System.currentTimeMillis()
        }
    }

    fun performRedo() {
        if (redoStack.isNotEmpty()) {
            if (undoStack.isEmpty() || undoStack.last().text != contentValue.text) {
                undoStack.add(contentValue)
            }
            contentValue = redoStack.removeLast()
            lastSaveTime = System.currentTimeMillis()
        }
    }

    var colorHex by rememberSaveable(noteId) { mutableStateOf(note?.colorHex ?: "default") }
    var topic by rememberSaveable(noteId) { mutableStateOf(note?.topic ?: "默认") }
    var isPinned by rememberSaveable(noteId) { mutableStateOf(note?.isPinned ?: false) }
    var showInWidget by rememberSaveable(noteId) { mutableStateOf(note?.showInWidget ?: true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(Localization.getString("delete", currentLanguage) ?: "删除记事") },
            text = { Text(Localization.getString("delete_note_desc", currentLanguage)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteNote()
                    }
                ) {
                    Text(Localization.getString("delete", currentLanguage) ?: "删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text(Localization.getString("cancel", currentLanguage) ?: "取消")
                }
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageFile = generateNoteImage(context, title, contentValue.text, colorHex, currentLanguage)
            if (imageFile != null && imageFile.exists()) {
                saveImageToGallery(context, imageFile, currentLanguage)
            } else {
                Toast.makeText(context, Localization.getString("edit_note_image_fail", currentLanguage) ?: "生成图片失败", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, Localization.getString("storage_permission_denied", currentLanguage) ?: "需要存储权限以保存图片", Toast.LENGTH_SHORT).show()
        }
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(title, contentValue.text, colorHex, topic, isPinned, showInWidget, note) {
        val originalNote = note
        if (originalNote == null || originalNote.id == 0) {
            title.isNotEmpty() || contentValue.text.isNotEmpty()
        } else {
            title != originalNote.title ||
            contentValue.text != originalNote.content ||
            colorHex != originalNote.colorHex ||
            topic != originalNote.topic ||
            isPinned != originalNote.isPinned ||
            showInWidget != originalNote.showInWidget
        }
    }

    val handleBack = {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            viewModel.navigateToHome()
        }
    }

    BackHandler {
        handleBack()
    }

    if (showUnsavedDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(Localization.getString("unsaved_changes_title", currentLanguage)) },
            text = { Text(Localization.getString("unsaved_changes_desc", currentLanguage)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showUnsavedDialog = false
                    viewModel.navigateToHome()
                }) {
                    Text(Localization.getString("leave", currentLanguage), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showUnsavedDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage))
                }
            }
        )
    }

    val isDark = isSystemInDarkTheme()
    val noteCardColor = getNoteCardColor(colorHex, isDark)

    // Layout adaptation based on device screen orientation/width
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Edit, 1 = Preview (For mobile)

    // Inserts Markdown syntax tags at current cursor selection
    fun insertMarkdown(tagType: String) {
        val text = contentValue.text
        val selection = contentValue.selection
        val start = selection.start
        val end = selection.end

        val newText = StringBuilder(text)
        val selectionText = text.substring(start, end)

        val tagOutput = when (tagType) {
            "bold" -> "**$selectionText**"
            "italic" -> "*$selectionText*"
            "code" -> "`$selectionText`"
            "heading" -> if (start == 0 || text[start - 1] == '\n') "# $selectionText" else "\n# $selectionText"
            "quote" -> if (start == 0 || text[start - 1] == '\n') "> $selectionText" else "\n> $selectionText"
            "rule" -> if (start == 0 || text[start - 1] == '\n') "---\n$selectionText" else "\n---\n$selectionText"
            "codeblock" -> "\n```\n$selectionText\n```\n"
            else -> ""
        }

        if (undoStack.isEmpty() || undoStack.last().text != contentValue.text) {
            undoStack.add(contentValue)
            if (undoStack.size > 100) undoStack.removeAt(0)
        }
        redoStack.clear()

        newText.replace(start, end, tagOutput)
        val nextSelectionCursor = start + tagOutput.length
        contentValue = TextFieldValue(
            text = newText.toString(),
            selection = androidx.compose.ui.text.TextRange(nextSelectionCursor)
        )
        lastSaveTime = System.currentTimeMillis()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            // Header Action Command Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = Localization.getString("back", currentLanguage))
                    }
                    Text(
                        text = if (note?.id == 0) Localization.getString("new_note", currentLanguage) else Localization.getString("edit_note", currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Undo / Redo buttons
                    IconButton(onClick = { performUndo() }, enabled = undoStack.isNotEmpty()) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = { performRedo() }, enabled = redoStack.isNotEmpty()) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    if (!isWideScreen) {
                        androidx.compose.material3.FilledTonalIconButton(onClick = { selectedTab = if (selectedTab == 0) 1 else 0 }, shape = RoundedCornerShape(12.dp)) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Preview else Icons.Default.Edit,
                                contentDescription = if (selectedTab == 0) "Preview" else "Edit",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Save note
                    val canSave = hasUnsavedChanges && (title.isNotBlank() || contentValue.text.isNotBlank())
                    androidx.compose.material3.FilledTonalIconButton(
                        onClick = {
                            viewModel.saveNote(title, contentValue.text, colorHex, topic, isPinned, showInWidget, navigateBack = false)
                            Toast.makeText(context, Localization.getString("save", currentLanguage), Toast.LENGTH_SHORT).show()
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector = if (canSave) Icons.Default.Save else Icons.Default.Check,
                            contentDescription = Localization.getString("save", currentLanguage)
                        )
                    }

                    // Settings/More button
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Adaptive Multi-Pane Content Layout
            if (isWideScreen) {
                // Side-by-side Dual Split Screen for Tablets / Landscape Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Editor Panel
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        EditorCardLayout(
                            title = title,
                            onTitleChange = { title = it },
                            contentValue = contentValue,
                            onContentChange = { updateContent(it) },
                            noteCardColor = noteCardColor,
                            onInsertMarkdown = { insertMarkdown(it) },
                            currentLanguage = currentLanguage
                        )
                    }

                    // Right Preview Panel
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = noteCardColor.copy(alpha = 0.8f)),
                        border = borderFromColor(noteCardColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = Localization.getString("preview", currentLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            if (title.isNotBlank()) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                            }
                            if (contentValue.text.isBlank()) {
                                Text(
                                    Localization.getString("markdown_preview_empty", currentLanguage),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            } else {
                                MarkdownContent(markdown = contentValue.text)
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    if (selectedTab == 0) {
                        // Editor Panel
                        EditorCardLayout(
                            title = title,
                            onTitleChange = { title = it },
                            contentValue = contentValue,
                            onContentChange = { updateContent(it) },
                            noteCardColor = noteCardColor,
                            onInsertMarkdown = { insertMarkdown(it) },
                            currentLanguage = currentLanguage
                        )
                    } else {
                        // Preview Panel
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = noteCardColor.copy(alpha = 0.8f)),
                            border = borderFromColor(noteCardColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                if (title.isNotBlank()) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(bottom = 12.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    )
                                }
                                if (contentValue.text.isBlank()) {
                                    Text(
                                        Localization.getString("no_content_preview", currentLanguage),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                } else {
                                    MarkdownContent(markdown = contentValue.text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(Localization.getString("edit_note_settings_title", currentLanguage) ?: "记事设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Color Selection
                Text(Localization.getString("edit_note_color_title", currentLanguage) ?: "记事颜色", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NOTE_COLORS.forEach { (colorKey, _) ->
                        val circleColor = getNoteCardColor(colorKey, isDark)
                        val isSelected = colorHex == colorKey

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(circleColor, CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .clickable { colorHex = colorKey },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
                
                // Topic Selection
                Text(Localization.getString("note_topic", currentLanguage), style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val predefinedTopics = listOf(
                        Localization.getString("topic_work", currentLanguage) ?: "工作/代码",
                        Localization.getString("topic_plan", currentLanguage) ?: "计划/待办",
                        Localization.getString("topic_shopping", currentLanguage) ?: "购物",
                        Localization.getString("topic_idea", currentLanguage) ?: "灵感/点子",
                        Localization.getString("topic_love", currentLanguage) ?: "情感/纪念日",
                        Localization.getString("topic_finance", currentLanguage) ?: "财务/理财",
                        Localization.getString("topic_study", currentLanguage) ?: "学习",
                        Localization.getString("topic_default", currentLanguage) ?: "默认"
                    )
                    items(predefinedTopics) { t ->
                        androidx.compose.material3.FilterChip(
                            selected = topic == t,
                            onClick = { topic = t },
                            label = { Text(Localization.getTopicName(t, currentLanguage)) }
                        )
                    }
                }

                HorizontalDivider()

                // Pin Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPinned = !isPinned }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(Localization.getString("edit_note_pin_title", currentLanguage) ?: "置顶记事")
                    }
                    androidx.compose.material3.Switch(checked = isPinned, onCheckedChange = { isPinned = it })
                }

                // Show in Widget
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInWidget = !showInWidget }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Preview, contentDescription = null, tint = if (showInWidget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(Localization.getString("edit_note_widget_title", currentLanguage) ?: "在小组件中显示")
                    }
                    androidx.compose.material3.Switch(checked = showInWidget, onCheckedChange = { showInWidget = it })
                }

                HorizontalDivider()

                // Share options
                Text(Localization.getString("edit_note_share_title", currentLanguage) ?: "分享", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "${title}\n\n${contentValue.text}")
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                            showSettingsSheet = false
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = Localization.getString("edit_note_share_text", currentLanguage))
                        }
                        Text(Localization.getString("edit_note_share_text", currentLanguage) ?: "分享文本", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Share as Image (generates a beautiful styled PNG image)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            try {
                                val imageFile = generateNoteImage(context, title, contentValue.text, colorHex, currentLanguage)
                                if (imageFile != null && imageFile.exists()) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(sendIntent, Localization.getString("edit_note_share_image", currentLanguage)))
                                } else {
                                    android.widget.Toast.makeText(context, Localization.getString("edit_note_image_fail", currentLanguage) ?: "生成图片失败", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, Localization.getString("edit_note_share_fail", currentLanguage) ?: "分享失败", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            showSettingsSheet = false
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Image, contentDescription = Localization.getString("edit_note_share_image", currentLanguage))
                        }
                        Text(Localization.getString("edit_note_share_image", currentLanguage) ?: "分享图片", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Save Image
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                val imageFile = generateNoteImage(context, title, contentValue.text, colorHex, currentLanguage)
                                if (imageFile != null && imageFile.exists()) {
                                    saveImageToGallery(context, imageFile, currentLanguage)
                                } else {
                                    android.widget.Toast.makeText(context, Localization.getString("edit_note_image_fail", currentLanguage) ?: "生成图片失败", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    val imageFile = generateNoteImage(context, title, contentValue.text, colorHex, currentLanguage)
                                    if (imageFile != null && imageFile.exists()) {
                                        saveImageToGallery(context, imageFile, currentLanguage)
                                    } else {
                                        android.widget.Toast.makeText(context, Localization.getString("edit_note_image_fail", currentLanguage) ?: "生成图片失败", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            }
                            showSettingsSheet = false
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Default.SaveAlt, contentDescription = Localization.getString("edit_note_save_image", currentLanguage))
                        }
                        Text(Localization.getString("edit_note_save_image", currentLanguage) ?: "保存图片", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Share as Word
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            try {
                                val file = java.io.File(context.cacheDir, "shared_files").apply { mkdirs() }
                                val docFile = java.io.File(file, "note_${System.currentTimeMillis()}.doc")
                                // A simple HTML file saved with .doc extension opens in Word perfectly!
                                docFile.writeText("<html><head><meta charset='UTF-8'></head><body><h1>${title}</h1><p>${contentValue.text.replace("\n", "<br>")}</p></body></html>")
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", docFile)
                                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/msword"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, Localization.getString("edit_note_export_word", currentLanguage)))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, Localization.getString("edit_note_share_fail", currentLanguage) ?: "分享失败", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            showSettingsSheet = false
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Description, contentDescription = Localization.getString("edit_note_export_word", currentLanguage))
                        }
                        Text(Localization.getString("edit_note_export_word", currentLanguage) ?: "导出Word", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                // Delete Note
                if (note?.id != 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSettingsSheet = false
                                showDeleteConfirm = true
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(Localization.getString("delete", currentLanguage) ?: "删除记事", color = MaterialTheme.colorScheme.error)
                    }
                }
                
                Spacer(modifier = Modifier.navigationBarsPadding().height(16.dp))
            }
        }
    }
}

@Composable
fun EditorCardLayout(
    title: String,
    onTitleChange: (String) -> Unit,
    contentValue: TextFieldValue,
    onContentChange: (TextFieldValue) -> Unit,
    noteCardColor: Color,
    onInsertMarkdown: (String) -> Unit,
    currentLanguage: AppLanguage
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = noteCardColor),
        border = borderFromColor(noteCardColor)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize()
        ) {
            // Note Title Input
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text(Localization.getString("note_title_placeholder", currentLanguage), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Markdown Quick Action Formatting Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Heading Format
                IconButton(onClick = { onInsertMarkdown("heading") }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Title, contentDescription = Localization.getString("format_heading", currentLanguage), modifier = Modifier.size(18.dp))
                }
                // Bold Format
                IconButton(onClick = { onInsertMarkdown("bold") }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.FormatBold, contentDescription = Localization.getString("format_bold", currentLanguage), modifier = Modifier.size(18.dp))
                }
                // Italic Format
                IconButton(onClick = { onInsertMarkdown("italic") }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.FormatItalic, contentDescription = Localization.getString("format_italic", currentLanguage), modifier = Modifier.size(18.dp))
                }
                // Code Span Format
                IconButton(onClick = { onInsertMarkdown("code") }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Code, contentDescription = Localization.getString("format_code", currentLanguage), modifier = Modifier.size(18.dp))
                }
                // Blockquote Format
                IconButton(onClick = { onInsertMarkdown("quote") }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.FormatQuote, contentDescription = Localization.getString("format_quote", currentLanguage), modifier = Modifier.size(18.dp))
                }
                // Rule Format
                IconButton(onClick = { onInsertMarkdown("rule") }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.HorizontalRule, contentDescription = Localization.getString("format_rule", currentLanguage), modifier = Modifier.size(18.dp))
                }
                // Code Block Format
                IconButton(onClick = { onInsertMarkdown("codeblock") }, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = Localization.getString("format_codeblock", currentLanguage),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable Note Markdown content input
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                OutlinedTextField(
                    value = contentValue,
                    onValueChange = onContentChange,
                    placeholder = { Text(Localization.getString("note_content_placeholder", currentLanguage), fontSize = 14.sp) },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.fillMaxSize(),
                    visualTransformation = MarkdownVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )


            }
        }
    }
}

@Composable
fun borderFromColor(bg: Color): androidx.compose.foundation.BorderStroke? {
    val isDark = isSystemInDarkTheme()
    return if (isDark) null else androidx.compose.foundation.BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

private fun saveImageToGallery(context: android.content.Context, imageFile: java.io.File, currentLanguage: AppLanguage) {
    try {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "Note_${System.currentTimeMillis()}.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Notes")
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri).use { out ->
                if (out != null) {
                    java.io.FileInputStream(imageFile).copyTo(out)
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.clear()
                values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
            android.widget.Toast.makeText(context, Localization.getString("edit_note_image_saved", currentLanguage) ?: "图片已保存至相册", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, Localization.getString("edit_note_save_fail", currentLanguage) ?: "保存失败", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        val errorMsg = Localization.getString("edit_note_save_error", currentLanguage) ?: "保存出错: %s"
        android.widget.Toast.makeText(context, String.format(errorMsg, e.message), android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun generateNoteImage(context: android.content.Context, title: String, content: String, colorHex: String, currentLanguage: AppLanguage): java.io.File? {
    try {
        val width = 1080
        
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 48f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
        }
        
        val contentPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#333333")
            textSize = 36f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.NORMAL)
        }
        
        val cleanContent = content
            .replace(Regex("(?m)^#+\\s+"), "")
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("`([^`]+)`"), "$1")
            .trim()
            
        val padding = 80
        val textWidth = width - (padding * 2)
        
        fun wrapText(text: String, paint: android.graphics.Paint, maxWidth: Int): List<String> {
            val lines = mutableListOf<String>()
            text.split("\n").forEach { paragraph ->
                if (paragraph.isEmpty()) {
                    lines.add("")
                    return@forEach
                }
                var start = 0
                while (start < paragraph.length) {
                    val count = paint.breakText(paragraph, start, paragraph.length, true, maxWidth.toFloat(), null)
                    lines.add(paragraph.substring(start, start + count))
                    start += count
                }
            }
            return lines
        }
        
        val titleLines = wrapText(title.ifBlank { Localization.getString("untitled", currentLanguage) ?: "无标题" }, titlePaint, textWidth)
        val contentLines = wrapText(cleanContent, contentPaint, textWidth)
        
        val lineSpacing = 16f
        val titleLineHeight = titlePaint.fontMetrics.descent - titlePaint.fontMetrics.ascent + lineSpacing
        val contentLineHeight = contentPaint.fontMetrics.descent - contentPaint.fontMetrics.ascent + lineSpacing
        
        var totalHeight = padding * 2
        totalHeight += (titleLines.size * titleLineHeight).toInt()
        totalHeight += 40 
        totalHeight += (contentLines.size * contentLineHeight).toInt()
        
        if (totalHeight < 600) {
            totalHeight = 600
        }
        
        val bitmap = android.graphics.Bitmap.createBitmap(width, totalHeight, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        val bgColorInt = when (colorHex) {
            "sage" -> android.graphics.Color.parseColor("#F1F8E9")
            "sky" -> android.graphics.Color.parseColor("#E1F5FE")
            "lavender" -> android.graphics.Color.parseColor("#F3E5F5")
            "rose" -> android.graphics.Color.parseColor("#FCE4EC")
            "peach" -> android.graphics.Color.parseColor("#FFF3E0")
            "slate" -> android.graphics.Color.parseColor("#ECEFF1")
            else -> android.graphics.Color.parseColor("#FAFAF9")
        }
        
        canvas.drawColor(bgColorInt)
        
        val borderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#A1887F")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRect(20f, 20f, width - 20f, totalHeight - 20f, borderPaint)
        
        borderPaint.strokeWidth = 2f
        canvas.drawRect(28f, 28f, width - 28f, totalHeight - 28f, borderPaint)
        
        var currentY = padding.toFloat() - titlePaint.fontMetrics.ascent
        
        titleLines.forEach { line ->
            canvas.drawText(line, padding.toFloat(), currentY, titlePaint)
            currentY += titleLineHeight
        }
        
        val dividerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#D7CCC8")
            strokeWidth = 3f
        }
        currentY += 10
        canvas.drawLine(padding.toFloat(), currentY, width - padding.toFloat(), currentY, dividerPaint)
        currentY += 40 - contentPaint.fontMetrics.ascent
        
        contentLines.forEach { line ->
            canvas.drawText(line, padding.toFloat(), currentY, contentPaint)
            currentY += contentLineHeight
        }
        
        val cacheFolder = java.io.File(context.cacheDir, "shared_files").apply { mkdirs() }
        val imageFile = java.io.File(cacheFolder, "note_image_${System.currentTimeMillis()}.png")
        java.io.FileOutputStream(imageFile).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
        return imageFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

class MarkdownVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
            append(text.text)
            
            // Highlight Headings
            val headingRegex = Regex("(?m)^#{1,6}\\s+.*$")
            headingRegex.findAll(text.text).forEach { match ->
                addStyle(
                    style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF6200EA), fontSize = 18.sp),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
            
            // Highlight Bold
            val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
            boldRegex.findAll(text.text).forEach { match ->
                addStyle(
                    style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
            
            // Highlight Italic
            val italicRegex = Regex("\\*(.*?)\\*")
            italicRegex.findAll(text.text).forEach { match ->
                if (!match.value.startsWith("**")) {
                    addStyle(
                        style = androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                }
            }
            
            // Highlight Code Span
            val codeRegex = Regex("`(.*?)`")
            codeRegex.findAll(text.text).forEach { match ->
                addStyle(
                    style = androidx.compose.ui.text.SpanStyle(background = Color.Gray.copy(alpha = 0.2f), fontFamily = FontFamily.Monospace),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
            
            // Highlight Blockquote
            val quoteRegex = Regex("(?m)^>.*$")
            quoteRegex.findAll(text.text).forEach { match ->
                addStyle(
                    style = androidx.compose.ui.text.SpanStyle(color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
        return androidx.compose.ui.text.input.TransformedText(annotatedString, androidx.compose.ui.text.input.OffsetMapping.Identity)
    }
}
