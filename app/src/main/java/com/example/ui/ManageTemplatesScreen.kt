package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTemplatesScreen(viewModel: NoteViewModel) {
    val context = LocalContext.current
    val templates by viewModel.templates.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    
    var showDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }

    BackHandler {
        viewModel.navigateToSettings()
    }

    val onSaveTemplate = {
        val newList = templates.toMutableList()
        if (editName.isNotBlank()) {
            val newTemplate = NoteViewModel.NoteTemplate(editName, editContent)
            if (editingIndex != null) {
                newList[editingIndex!!] = newTemplate
            } else {
                newList.add(newTemplate)
            }
            viewModel.saveTemplates(context, newList)
        }
        showDialog = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("manage_templates_title", currentLanguage) ?: "管理自定义模板") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateToSettings() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Localization.getString("back", currentLanguage))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingIndex = null
                editName = ""
                editContent = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = Localization.getString("manage_templates_add", currentLanguage))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            itemsIndexed(templates) { index, template ->
                ListItem(
                    headlineContent = { Text(template.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { 
                        Text(template.content, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant) 
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                editingIndex = index
                                editName = template.name
                                editContent = template.content
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = Localization.getString("edit", currentLanguage))
                            }
                            IconButton(onClick = {
                                val newList = templates.toMutableList()
                                newList.removeAt(index)
                                viewModel.saveTemplates(context, newList)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = Localization.getString("delete", currentLanguage), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    modifier = Modifier.clickable {
                        editingIndex = index
                        editName = template.name
                        editContent = template.content
                        showDialog = true
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingIndex == null) (Localization.getString("manage_templates_add", currentLanguage) ?: "添加模板") else (Localization.getString("manage_templates_edit", currentLanguage) ?: "编辑模板")) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(Localization.getString("manage_templates_name", currentLanguage) ?: "模板名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text(Localization.getString("manage_templates_content", currentLanguage) ?: "模板内容 (Markdown)") },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onSaveTemplate) {
                    Text(Localization.getString("save", currentLanguage) ?: "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(Localization.getString("cancel", currentLanguage) ?: "取消")
                }
            }
        )
    }
}

