package com.example.languagepracticev3.ui.screens.practice

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
import com.example.languagepracticev3.data.model.PracticeSession
import com.example.languagepracticev3.viewmodel.PracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val currentSession by viewModel.currentSession.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("練習 (Practice)") },
                actions = {
                    if (currentSession == null) {
                        Button(
                            onClick = { viewModel.startNewSession() },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("新規セッション")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (currentSession != null) {
            PracticeSessionContent(
                viewModel = viewModel,
                currentStep = currentStep,
                modifier = Modifier.padding(padding)
            )
        } else {
            SessionsList(
                sessions = sessions,
                onSelect = { viewModel.loadSession(it) },
                onDelete = { viewModel.deleteSession(it) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun SessionsList(
    sessions: List<PracticeSession>,
    onSelect: (PracticeSession) -> Unit,
    onDelete: (PracticeSession) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "練習セッションがありません",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "「新規セッション」をタップして開始してください",
                    style = MaterialTheme.typography.bodyMedium,
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
            items(sessions) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { 
                            Text("セッション #${session.id}")
                        },
                        supportingContent = {
                            Column {
                                Text(session.createdAt)
                                if (session.isCompleted) {
                                    Text("完了", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        leadingContent = {
                            Icon(
                                if (session.isCompleted) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                                null,
                                tint = if (session.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { onSelect(session) }) {
                                    Icon(Icons.Default.OpenInNew, "開く")
                                }
                                IconButton(onClick = { onDelete(session) }) {
                                    Icon(Icons.Default.Delete, "削除")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeSessionContent(
    viewModel: PracticeViewModel,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val drillAMemo by viewModel.drillAMemo.collectAsState()
    val drillBMetaphors by viewModel.drillBMetaphors.collectAsState()
    val drillCDraft by viewModel.drillCDraft.collectAsState()
    val drillCCore by viewModel.drillCCore.collectAsState()
    val drillCRevision by viewModel.drillCRevision.collectAsState()
    val wrapBestOne by viewModel.wrapBestOne.collectAsState()
    val wrapTodo by viewModel.wrapTodo.collectAsState()

    val steps = listOf(
        "ドリルA: 素材集め",
        "ドリルB: 比喩づくり",
        "ドリルC: 作品作成",
        "ドリルC: 推敲",
        "まとめ"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ステップインジケーター
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            "${currentStep + 1}/${steps.size}: ${steps[currentStep]}",
            style = MaterialTheme.typography.titleMedium
        )

        // ステップ内容
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (currentStep) {
                    0 -> DrillAContent(drillAMemo, viewModel::updateDrillAMemo)
                    1 -> DrillBContent(drillBMetaphors, viewModel::updateDrillBMetaphors)
                    2 -> DrillCDraftContent(drillCDraft, viewModel::updateDrillCDraft)
                    3 -> DrillCRevisionContent(
                        drillCCore, viewModel::updateDrillCCore,
                        drillCRevision, viewModel::updateDrillCRevision
                    )
                    4 -> WrapUpContent(
                        wrapBestOne, viewModel::updateWrapBestOne,
                        wrapTodo, viewModel::updateWrapTodo
                    )
                }
            }
        }

        // ナビゲーションボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { viewModel.previousStep() },
                enabled = currentStep > 0
            ) {
                Icon(Icons.Default.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("戻る")
            }

            Button(onClick = { viewModel.saveSession() }) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("保存")
            }

            if (currentStep < steps.size - 1) {
                Button(onClick = { viewModel.nextStep() }) {
                    Text("次へ")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
            } else {
                Button(
                    onClick = { viewModel.completeSession() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("完了")
                }
            }
        }
    }
}

@Composable
private fun DrillAContent(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("素材を集めましょう", style = MaterialTheme.typography.titleSmall)
        Text(
            "日常の観察、感じたこと、気になった言葉などをメモしてください",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            label = { Text("メモ") }
        )
    }
}

@Composable
private fun DrillBContent(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("比喩を作りましょう", style = MaterialTheme.typography.titleSmall)
        Text(
            "集めた素材から比喩表現を考えてみてください",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            label = { Text("比喩") }
        )
    }
}

@Composable
private fun DrillCDraftContent(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("ドラフトを書きましょう", style = MaterialTheme.typography.titleSmall)
        Text(
            "素材と比喩を使って文章を書いてみてください",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            label = { Text("ドラフト") }
        )
    }
}

@Composable
private fun DrillCRevisionContent(
    core: String, onCoreChange: (String) -> Unit,
    revision: String, onRevisionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("推敲しましょう", style = MaterialTheme.typography.titleSmall)
        
        OutlinedTextField(
            value = core,
            onValueChange = onCoreChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f),
            label = { Text("コア（伝えたいこと）") }
        )
        
        OutlinedTextField(
            value = revision,
            onValueChange = onRevisionChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            label = { Text("推敲版") }
        )
    }
}

@Composable
private fun WrapUpContent(
    bestOne: String, onBestOneChange: (String) -> Unit,
    todo: String, onTodoChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("まとめ", style = MaterialTheme.typography.titleSmall)
        
        OutlinedTextField(
            value = bestOne,
            onValueChange = onBestOneChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f),
            label = { Text("今日のベスト表現") }
        )
        
        OutlinedTextField(
            value = todo,
            onValueChange = onTodoChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f),
            label = { Text("次回やること") }
        )
    }
}
