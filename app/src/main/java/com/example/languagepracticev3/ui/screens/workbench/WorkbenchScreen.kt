package com.example.languagepracticev3.ui.screens.workbench

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.Topic
import com.example.languagepracticev3.viewmodel.WorkbenchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkbenchScreen(
    viewModel: WorkbenchViewModel = hiltViewModel()
) {
    val topics by viewModel.topics.collectAsState()
    val works by viewModel.works.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val outputText by viewModel.outputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showNewTopicDialog by remember { mutableStateOf(false) }
    var showSaveWorkDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作業台 (Workbench)") },
                actions = {
                    IconButton(onClick = { showNewTopicDialog = true }) {
                        Icon(Icons.Default.Add, "新規トピック")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左パネル: トピック一覧
            Card(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "トピック",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                    LazyColumn {
                        items(topics) { topic ->
                            TopicItem(
                                topic = topic,
                                isSelected = selectedTopic?.id == topic.id,
                                onClick = { viewModel.selectTopic(topic) },
                                onDelete = { viewModel.deleteTopic(topic) }
                            )
                        }
                    }
                }
            }

            // 中央パネル: 入力
            Card(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        "入力",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    selectedTopic?.let { topic ->
                        Text(
                            "選択中: ${topic.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = viewModel::updateInputText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = { Text("テキスト入力") },
                        placeholder = { Text("ここにテーマや内容を入力...") }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = viewModel::generateText,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("生成")
                        }
                    }
                }
            }

            // 右パネル: 出力
            Card(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "出力",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row {
                            IconButton(
                                onClick = { showSaveWorkDialog = true },
                                enabled = outputText.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Save, "保存")
                            }
                            IconButton(onClick = viewModel::clearOutput) {
                                Icon(Icons.Default.Clear, "クリア")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = outputText,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        readOnly = true,
                        label = { Text("生成結果") }
                    )
                }
            }
        }
    }

    if (showNewTopicDialog) {
        NewTopicDialog(
            onDismiss = { showNewTopicDialog = false },
            onConfirm = { title, emotion, scene, tags ->
                viewModel.createNewTopic(title, emotion, scene, tags)
                showNewTopicDialog = false
            }
        )
    }

    if (showSaveWorkDialog) {
        SaveWorkDialog(
            onDismiss = { showSaveWorkDialog = false },
            onConfirm = { title ->
                viewModel.saveWork(title, outputText)
                showSaveWorkDialog = false
            }
        )
    }
}

@Composable
private fun TopicItem(
    topic: Topic,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(topic.title) },
        supportingContent = topic.emotion.takeIf { it.isNotEmpty() }?.let { { Text(it) } },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "削除", tint = MaterialTheme.colorScheme.error)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = if (isSelected) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            ListItemDefaults.colors()
        },
        tonalElevation = if (isSelected) 4.dp else 0.dp
    )
}

@Composable
private fun NewTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf("") }
    var scene by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新規トピック") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = emotion,
                    onValueChange = { emotion = it },
                    label = { Text("感情") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = scene,
                    onValueChange = { scene = it },
                    label = { Text("場面") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("タグ (カンマ区切り)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, emotion, scene, tags) },
                enabled = title.isNotEmpty()
            ) {
                Text("作成")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun SaveWorkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("作品を保存") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("タイトル") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.isNotEmpty()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
