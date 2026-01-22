package com.example.languagepracticev3.ui.screens.poetrylab

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
import com.example.languagepracticev3.data.model.PlProject
import com.example.languagepracticev3.viewmodel.PoetryLabScreen
import com.example.languagepracticev3.viewmodel.PoetryLabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoetryLabScreen(
    viewModel: PoetryLabViewModel = hiltViewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val assets by viewModel.currentProjectAssets.collectAsState()
    val issues by viewModel.currentProjectIssues.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            PoetryLabScreen.HOME -> "🎭 PoetryLab"
                            PoetryLabScreen.PROJECT -> selectedProject?.title ?: "プロジェクト"
                            PoetryLabScreen.RUN -> "Run実行"
                            PoetryLabScreen.COMPARE -> "比較"
                        }
                    )
                },
                navigationIcon = {
                    if (currentScreen != PoetryLabScreen.HOME) {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Default.ArrowBack, "戻る")
                        }
                    }
                },
                actions = {
                    if (currentScreen == PoetryLabScreen.HOME) {
                        IconButton(onClick = { showNewProjectDialog = true }) {
                            Icon(Icons.Default.Add, "新規プロジェクト")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    ) { padding ->
        when (currentScreen) {
            PoetryLabScreen.HOME -> {
                PoetryLabHomeContent(
                    projects = projects,
                    onSelectProject = { viewModel.selectProject(it) },
                    onDeleteProject = { viewModel.deleteProject(it) },
                    modifier = Modifier.padding(padding)
                )
            }
            PoetryLabScreen.PROJECT -> {
                PoetryLabProjectContent(
                    project = selectedProject!!,
                    assets = assets,
                    issues = issues,
                    onAddAsset = { type, text -> viewModel.createAsset(type, text) },
                    onAddIssue = { level, symptom -> viewModel.createIssue(level, symptom) },
                    modifier = Modifier.padding(padding)
                )
            }
            PoetryLabScreen.RUN -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Run実行画面 - AI連携が必要です")
                }
            }
            PoetryLabScreen.COMPARE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("比較画面")
                }
            }
        }
    }

    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onConfirm = { title, styleType ->
                viewModel.createProject(title, styleType)
                showNewProjectDialog = false
            }
        )
    }
}

@Composable
private fun PoetryLabHomeContent(
    projects: List<PlProject>,
    onSelectProject: (PlProject) -> Unit,
    onDeleteProject: (PlProject) -> Unit,
    modifier: Modifier = Modifier
) {
    if (projects.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.TheaterComedy,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text("プロジェクトがありません")
                Text(
                    "詩作プロジェクトを作成して始めましょう",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projects) { project ->
                Card(
                    onClick = { onSelectProject(project) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(project.title) },
                        supportingContent = { 
                            Text("スタイル: ${project.styleType} | ${project.createdAt}")
                        },
                        leadingContent = {
                            Icon(Icons.Default.Book, null)
                        },
                        trailingContent = {
                            IconButton(onClick = { onDeleteProject(project) }) {
                                Icon(Icons.Default.Delete, "削除")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PoetryLabProjectContent(
    project: PlProject,
    assets: List<com.example.languagepracticev3.data.model.PlTextAsset>,
    issues: List<com.example.languagepracticev3.data.model.PlIssue>,
    onAddAsset: (String, String) -> Unit,
    onAddIssue: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddAssetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // プロジェクト情報
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(project.title, style = MaterialTheme.typography.headlineSmall)
                Text("スタイル: ${project.styleType}")
                Text("作成: ${project.createdAt}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // テキスト成果物
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("テキスト成果物", style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { showAddAssetDialog = true }) {
                            Icon(Icons.Default.Add, "追加")
                        }
                    }
                    LazyColumn {
                        items(assets) { asset ->
                            ListItem(
                                headlineContent = { Text(asset.assetType) },
                                supportingContent = { 
                                    Text(
                                        asset.bodyText.take(50),
                                        maxLines = 2
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Issue一覧
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Issues", style = MaterialTheme.typography.titleSmall)
                    LazyColumn {
                        items(issues) { issue ->
                            ListItem(
                                headlineContent = { Text(issue.symptom) },
                                supportingContent = { Text("${issue.level} / ${issue.severity}") },
                                leadingContent = {
                                    Icon(
                                        when (issue.status) {
                                            "DONE" -> Icons.Default.CheckCircle
                                            "PLANNED" -> Icons.Default.Schedule
                                            else -> Icons.Default.Error
                                        },
                                        null,
                                        tint = when (issue.severity) {
                                            "S" -> MaterialTheme.colorScheme.error
                                            "A" -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddAssetDialog) {
        AddAssetDialog(
            onDismiss = { showAddAssetDialog = false },
            onConfirm = { type, text ->
                onAddAsset(type, text)
                showAddAssetDialog = false
            }
        )
    }
}

@Composable
private fun NewProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var styleType by remember { mutableStateOf("KOU") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新規プロジェクト") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タイトル") },
                    singleLine = true
                )
                
                Text("スタイル", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("KOU" to "口語", "BU" to "文語", "MIX" to "混合").forEach { (type, label) ->
                        FilterChip(
                            selected = styleType == type,
                            onClick = { styleType = type },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, styleType) },
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
private fun AddAssetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var assetType by remember { mutableStateOf("DRAFT") }
    var bodyText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("テキスト追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("タイプ", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("TOPIC", "DRAFT", "CORE", "REV_A").forEach { type ->
                        FilterChip(
                            selected = assetType == type,
                            onClick = { assetType = type },
                            label = { Text(type) }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    label = { Text("テキスト") },
                    minLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(assetType, bodyText) },
                enabled = bodyText.isNotEmpty()
            ) {
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
