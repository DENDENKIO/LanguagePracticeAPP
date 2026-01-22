// app/src/main/java/com/example/languagepracticev3/ui/screens/workbench/WorkbenchScreen.kt
package com.example.languagepracticev3.ui.screens.workbench

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.models.AiSiteCatalog
import com.example.languagepracticev3.data.models.LengthProfile
import com.example.languagepracticev3.data.models.OperationKind
import com.example.languagepracticev3.ui.screens.aibrowser.AiBrowserScreen
import com.example.languagepracticev3.viewmodel.SaveResult
import com.example.languagepracticev3.viewmodel.SettingsViewModel
import com.example.languagepracticev3.viewmodel.WorkbenchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkbenchScreen(
    viewModel: WorkbenchViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPromptDialog by remember { mutableStateOf(false) }
    var showOutputDialog by remember { mutableStateOf(false) }
    var showAiBrowser by remember { mutableStateOf(false) }

    // 設定から読み込み
    val selectedAiSiteId by settingsViewModel.selectedAiSiteId.collectAsState()
    val isAutoMode by settingsViewModel.isAutoMode.collectAsState()
    val aiSiteProfile = remember(selectedAiSiteId) {
        settingsViewModel.selectedAiSiteProfile
    }

    // AIブラウザ画面
    if (showAiBrowser && uiState.generatedPrompt.isNotBlank()) {
        AiBrowserScreen(
            siteProfile = aiSiteProfile,
            prompt = uiState.generatedPrompt,
            onResultReceived = { result ->
                viewModel.updateAiOutput(result)
                showAiBrowser = false
                // 自動で解析を実行
                viewModel.parseAndSaveOutput()
            },
            onDismiss = {
                showAiBrowser = false
            }
        )
        return // ブラウザ表示中は他のUIを隠す
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "作業台",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // AI設定表示
            AssistChip(
                onClick = { /* 設定画面へ */ },
                label = { Text(aiSiteProfile.name) },
                leadingIcon = {
                    Icon(
                        if (isAutoMode) Icons.Default.AutoMode else Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ステータスメッセージ
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(uiState.statusMessage)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 操作種別選択
        OperationSelector(
            selected = uiState.selectedOperation,
            onSelect = viewModel::updateOperation
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 操作の説明
        Text(
            text = uiState.selectedOperation.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 操作に応じた入力フィールド
        when (uiState.selectedOperation) {
            OperationKind.READER_AUTO_GEN -> ReaderAutoGenInputs(uiState, viewModel)
            OperationKind.TOPIC_GEN -> TopicGenInputs(uiState, viewModel)
            OperationKind.PERSONA_GEN -> PersonaGenInputs(uiState, viewModel)
            OperationKind.OBSERVE_IMAGE -> ObserveImageInputs(uiState, viewModel)
            OperationKind.TEXT_GEN -> TextGenInputs(uiState, viewModel)
            OperationKind.STUDY_CARD -> StudyCardInputs(uiState, viewModel)
            OperationKind.CORE_EXTRACT -> CoreExtractInputs(uiState, viewModel)
            OperationKind.REVISION_FULL -> RevisionInputs(uiState, viewModel)
            OperationKind.GIKO -> GikoInputs(uiState, viewModel)
            OperationKind.PERSONA_VERIFY_ASSIST -> PersonaVerifyInputs(uiState, viewModel)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // アクションボタン
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // プロンプト生成
            Button(
                onClick = {
                    val error = viewModel.validateInput()
                    if (error != null) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.generatePrompt()
                        showPromptDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Create, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("プロンプト生成")
            }

            // AIに送信（自動モード）
            if (isAutoMode) {
                Button(
                    onClick = {
                        if (uiState.generatedPrompt.isNotBlank()) {
                            showAiBrowser = true
                        } else {
                            Toast.makeText(context, "先にプロンプトを生成してください", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.generatedPrompt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.AutoMode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("自動でAIに送信")
                }
            }

            // 手動モード用ボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // コピーしてブラウザを開く
                OutlinedButton(
                    onClick = {
                        if (uiState.generatedPrompt.isNotBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("prompt", uiState.generatedPrompt))
                            Toast.makeText(context, "コピーしました", Toast.LENGTH_SHORT).show()

                            // ブラウザを開く
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(aiSiteProfile.url)
                            )
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.generatedPrompt.isNotBlank()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("コピー&開く")
                }

                // 結果貼り付け
                OutlinedButton(
                    onClick = { showOutputDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("結果貼り付け")
                }
            }
        }

        // 結果表示
        uiState.lastSaveResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (result) {
                        is SaveResult.Success -> MaterialTheme.colorScheme.tertiaryContainer
                        is SaveResult.Error -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (result) {
                            is SaveResult.Success -> Icons.Default.CheckCircle
                            is SaveResult.Error -> Icons.Default.Warning
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (result) {
                            is SaveResult.Success -> "${result.count}件の${result.type}を保存しました"
                            is SaveResult.Error -> result.message
                        }
                    )
                }
            }
        }

        // AI出力プレビュー
        if (uiState.aiOutput.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        "AI出力（プレビュー）",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.aiOutput.take(500) + if (uiState.aiOutput.length > 500) "..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // プロンプト表示ダイアログ
    if (showPromptDialog) {
        AlertDialog(
            onDismissRequest = { showPromptDialog = false },
            title = { Text("生成されたプロンプト") },
            text = {
                Column {
                    Text(
                        text = uiState.generatedPrompt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("prompt", uiState.generatedPrompt))
                    Toast.makeText(context, "コピーしました", Toast.LENGTH_SHORT).show()
                    showPromptDialog = false
                }) {
                    Text("コピー")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromptDialog = false }) {
                    Text("閉じる")
                }
            }
        )
    }

    // AI出力入力ダイアログ
    if (showOutputDialog) {
        var outputText by remember { mutableStateOf(uiState.aiOutput) }

        AlertDialog(
            onDismissRequest = { showOutputDialog = false },
            title = { Text("AI出力を貼り付け") },
            text = {
                OutlinedTextField(
                    value = outputText,
                    onValueChange = { outputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("AIの出力をここに貼り付けてください") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateAiOutput(outputText)
                    viewModel.parseAndSaveOutput()
                    showOutputDialog = false
                }) {
                    Text("解析して保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOutputDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

// ==========================================
// 以下、入力コンポーネント（前回と同じ）
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OperationSelector(
    selected: OperationKind,
    onSelect: (OperationKind) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("操作種別") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            OperationKind.entries.forEach { operation ->
                DropdownMenuItem(
                    text = { Text(operation.displayName) },
                    onClick = {
                        onSelect(operation)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 以下の入力コンポーネントは前回と同じなので省略
// ReaderAutoGenInputs, TextGenInputs, StudyCardInputs, PersonaGenInputs,
// TopicGenInputs, ObserveImageInputs, CoreExtractInputs, RevisionInputs,
// GikoInputs, PersonaVerifyInputs, LengthSelector

// ... (前回のコードをそのまま使用)
