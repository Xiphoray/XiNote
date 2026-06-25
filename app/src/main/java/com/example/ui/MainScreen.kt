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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Translate
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

// Beautiful modern pastel colors matching dark and light themes (High Density Palette)
fun getNoteCardColor(colorHex: String?, isDark: Boolean): Color {
    if (colorHex == null || colorHex == "default") {
        return if (isDark) Color(0xFF241E1D) else Color(0xFFFFF8F6)
    }
    return when (colorHex) {
        "sage" -> if (isDark) Color(0xFF222D24) else Color(0xFFF4F9F4)
        "sky" -> if (isDark) Color(0xFF1F2B35) else Color(0xFFF4F8FA)
        "lavender" -> if (isDark) Color(0xFF2E2235) else Color(0xFFFAF5FC)
        "rose" -> if (isDark) Color(0xFF371E23) else Color(0xFFFCF5F6)
        "peach" -> if (isDark) Color(0xFF34221E) else Color(0xFFFFF7F4)
        "slate" -> if (isDark) Color(0xFF232324) else Color(0xFFF7F7F8)
        else -> if (isDark) Color(0xFF241E1D) else Color(0xFFFFF8F6)
    }
}

val NOTE_COLORS = listOf(
    "default" to "默认",
    "sage" to "薄荷绿",
    "sky" to "天空蓝",
    "lavender" to "薰衣草",
    "rose" to "蔷薇粉",
    "peach" to "温暖橙",
    "slate" to "石板灰"
)

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
    val isDark = isSystemInDarkTheme()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateToEditNote(null) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            ) {
                Icon(Icons.Default.Add, contentDescription = Localization.getString("add_note", currentLanguage))
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

                // Language Selector Button with Dropdown Menu
                Box {
                    IconButton(
                        onClick = { showLanguageMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = Localization.getString("language", currentLanguage),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(text = "${lang.displayName} (${lang.nativeName})") },
                                onClick = {
                                    viewModel.changeLanguage(context, lang)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Widget Customization Dialog Trigger
                IconButton(
                    onClick = {
                        viewModel.loadWidgetOpacity(context)
                        showWidgetSettings = true
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = Localization.getString("widget_settings", currentLanguage),
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
                val pinnedNotes = notes.filter { it.isPinned }
                val otherNotes = notes.filter { !it.isPinned }

                val configuration = LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp
                val gridColumns = when {
                    screenWidthDp >= 900 -> GridCells.Fixed(4)
                    screenWidthDp >= 600 -> GridCells.Fixed(3)
                    else -> GridCells.Fixed(2)
                }

                LazyVerticalGrid(
                    columns = gridColumns,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
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
                                onDelete = { viewModel.deleteNoteDirectly(note) },
                                currentLanguage = currentLanguage
                            )
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = Localization.getString("others", currentLanguage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                        }
                        items(otherNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                isDark = isDark,
                                onClick = { viewModel.navigateToEditNote(note.id) },
                                onTogglePin = { viewModel.togglePin(note) },
                                onDelete = { viewModel.deleteNoteDirectly(note) },
                                currentLanguage = currentLanguage
                            )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    isDark: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val cardColor = getNoteCardColor(note.colorHex, isDark)
    val dateFormat = remember { SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onTogglePin
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifBlank { Localization.getString("untitled", currentLanguage) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = Localization.getString(if (note.isPinned) "unpin" else "pin", currentLanguage),
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body preview: strip heading symbols and display a clean snippet
            val previewText = remember(note.content) {
                note.content
                    .replace(Regex("(?m)^#+\\s+"), "")
                    .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                    .replace(Regex("\\*(.*?)\\*"), "$1")
                    .trim()
            }

            Text(
                text = previewText.ifBlank { Localization.getString("empty_content", currentLanguage) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(Date(note.updatedAt)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = Localization.getString("delete", currentLanguage),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
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
