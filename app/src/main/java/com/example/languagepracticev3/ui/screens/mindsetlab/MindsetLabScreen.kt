package com.example.languagepracticev3.ui.screens.mindsetlab

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
import com.example.languagepracticev3.data.model.MsDay
import com.example.languagepracticev3.data.model.MsEntry
import com.example.languagepracticev3.data.model.MindsetDefinitions
import com.example.languagepracticev3.viewmodel.MindsetLabScreen
import com.example.languagepracticev3.viewmodel.MindsetLabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindsetLabScreen(
    viewModel: MindsetLabViewModel = hiltViewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val days by viewModel.days.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val entries by viewModel.currentDayEntries.collectAsState()
    val selectedMindsets by viewModel.selectedMindsets.collectAsState()
    val scene by viewModel.scene.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            MindsetLabScreen.HOME -> "🧠 MindsetLab"
                            MindsetLabScreen.SESSION -> selectedDay?.dateKey ?: "セッション"
                            MindsetLabScreen.REVIEW -> "レビュー"
                            MindsetLabScreen.HISTORY -> "履歴"
                        }
                    )
                },
                navigationIcon = {
                    if (currentScreen != MindsetLabScreen.HOME) {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Default.ArrowBack, "戻る")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        when (currentScreen) {
            MindsetLabScreen.HOME -> {
                MindsetLabHomeContent(
                    days = days,
                    selectedMindsets = selectedMindsets,
                    scene = scene,
                    availableMindsets = viewModel.availableMindsets,
                    onToggleMindset = { viewModel.toggleMindset(it) },
                    onSceneChange = { viewModel.updateScene(it) },
                    onStartSession = { viewModel.createOrUpdateToday() },
                    onSelectDay = { viewModel.selectDay(it) },
                    onDeleteDay = { viewModel.deleteDay(it) },
                    modifier = Modifier.padding(padding)
                )
            }
            MindsetLabScreen.SESSION -> {
                MindsetLabSessionContent(
                    day = selectedDay!!,
                    entries = entries,
                    drills = viewModel.getDrillsForSelectedMindsets(),
                    onSaveEntry = { type, text -> viewModel.saveEntry(type, text) },
                    onUpdateEntry = { entry, text -> viewModel.updateEntry(entry, text) },
                    onDeleteEntry = { viewModel.deleteEntry(it) },
                    modifier = Modifier.padding(padding)
                )
            }
            MindsetLabScreen.REVIEW -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("レビュー機能 - AI連携が必要です")
                }
            }
            MindsetLabScreen.HISTORY -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("履歴一覧")
                }
            }
        }
    }
}

@Composable
private fun MindsetLabHomeContent(
    days: List<MsDay>,
    selectedMindsets: List<Int>,
    scene: String,
    availableMindsets: List<com.example.languagepracticev3.data.model.MindsetInfo>,
    onToggleMindset: (Int) -> Unit,
    onSceneChange: (String) -> Unit,
    onStartSession: () -> Unit,
    onSelectDay: (MsDay) -> Unit,
    onDeleteDay: (MsDay) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // マインドセット選択
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("今日のマインドセット (最大3つ)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                
                availableMindsets.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { mindset ->
                            FilterChip(
                                selected = selectedMindsets.contains(mindset.id),
                                onClick = { onToggleMindset(mindset.id) },
                                label = { Text("${mindset.id}. ${mindset.shortName}") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // シーン入力
        OutlinedTextField(
            value = scene,
            onValueChange = onSceneChange,
            label = { Text("今日のシーン") },
            placeholder = { Text("例: カフェで執筆、朝の散歩中...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 開始ボタン
        Button(
            onClick = onStartSession,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedMindsets.isNotEmpty()
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(8.dp))
            Text("セッション開始")
        }

        Divider()

        // 過去のセッション
        Text("過去のセッション", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { day ->
                Card(
                    onClick = { onSelectDay(day) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(day.dateKey) },
                        supportingContent = {
                            val mindsetNames = day.getFocusMindsetList()
                                .mapNotNull { MindsetDefinitions.all[it]?.shortName }
                                .joinToString(", ")
                            Text(mindsetNames.ifEmpty { "マインドセット未設定" })
                        },
                        leadingContent = {
                            Icon(Icons.Default.CalendarToday, null)
                        },
                        trailingContent = {
                            IconButton(onClick = { onDeleteDay(day) }) {
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
private fun MindsetLabSessionContent(
    day: MsDay,
    entries: List<MsEntry>,
    drills: List<com.example.languagepracticev3.data.model.DrillDef>,
    onSaveEntry: (String, String) -> Unit,
    onUpdateEntry: (MsEntry, String) -> Unit,
    onDeleteEntry: (MsEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDrillIndex by remember { mutableIntStateOf(0) }
    var inputText by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左パネル: ドリル一覧
        Card(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                item {
                    Text(
                        "ドリル一覧",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                items(drills.size) { index ->
                    val drill = drills[index]
                    val existingEntry = entries.find { it.entryType == drill.entryType }
                    
                    ListItem(
                        headlineContent = { Text(drill.title) },
                        supportingContent = { Text(drill.hint) },
                        leadingContent = {
                            if (existingEntry != null) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, null)
                            }
                        },
                        colors = if (currentDrillIndex == index) {
                            ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        } else {
                            ListItemDefaults.colors()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 右パネル: 入力エリア
        Card(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
        ) {
            if (drills.isNotEmpty() && currentDrillIndex < drills.size) {
                val currentDrill = drills[currentDrillIndex]
                val existingEntry = entries.find { it.entryType == currentDrill.entryType }

                LaunchedEffect(currentDrillIndex, existingEntry) {
                    inputText = existingEntry?.bodyText ?: ""
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(currentDrill.title, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        currentDrill.hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = { Text("入力") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { if (currentDrillIndex > 0) currentDrillIndex-- },
                            enabled = currentDrillIndex > 0
                        ) {
                            Icon(Icons.Default.ArrowBack, null)
                            Text("前へ")
                        }

                        Button(
                            onClick = {
                                if (existingEntry != null) {
                                    onUpdateEntry(existingEntry, inputText)
                                } else {
                                    onSaveEntry(currentDrill.entryType, inputText)
                                }
                            },
                            enabled = inputText.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Save, null)
                            Text("保存")
                        }

                        Button(
                            onClick = { if (currentDrillIndex < drills.size - 1) currentDrillIndex++ },
                            enabled = currentDrillIndex < drills.size - 1
                        ) {
                            Text("次へ")
                            Icon(Icons.Default.ArrowForward, null)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("マインドセットを選択してドリルを表示してください")
                }
            }
        }
    }
}
