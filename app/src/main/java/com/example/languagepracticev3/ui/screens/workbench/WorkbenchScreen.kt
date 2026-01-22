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
import com.example.languagepracticev3.data.model.LengthProfile
import com.example.languagepracticev3.data.model.OperationKind
import com.example.languagepracticev3.ui.screens.aibrowser.AiBrowserScreen
import com.example.languagepracticev3.viewmodel.SaveResult
import com.example.languagepracticev3.viewmodel.WorkbenchViewModel
import com.example.languagepracticev3.viewmodel.WorkbenchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkbenchScreen(
    viewModel: WorkbenchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPromptDialog by remember { mutableStateOf(false) }
    var showOutputDialog by remember { mutableStateOf(false) }

    // AIブラウザ画面の表示（設定で保存されたAIサイトを使用）
    if (uiState.showAiBrowser) {
        AiBrowserScreen(
            siteProfile = uiState.aiSiteProfile,
            prompt = uiState.generatedPrompt,
            onResultReceived = { result ->
                viewModel.onAiResultReceived(result)
            },
            onDismiss = {
                viewModel.closeAiBrowser()
            }
        )
        return
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "作業台",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.clearInputs() }) {
                Icon(Icons.Default.Refresh, "クリア")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 使用するAIサイト表示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Computer, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "使用AI: ${uiState.aiSiteProfile.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (uiState.isAutoMode) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "(自動モード)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 操作説明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = getOperationDescription(uiState.selectedOperation),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 操作種別選択
        OperationSelector(
            selected = uiState.selectedOperation,
            onSelect = viewModel::updateOperation
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 動的入力フィールド
        DynamicInputFields(
            uiState = uiState,
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(24.dp))

        // アクションボタン群
        ActionButtons(
            uiState = uiState,
            viewModel = viewModel,
            context = context,
            onShowPromptDialog = { showPromptDialog = true },
            onShowOutputDialog = { showOutputDialog = true }
        )

        // 処理中インジケーター
        if (uiState.isProcessing) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("処理中...")
            }
        }

        // 保存結果の表示
        uiState.lastSaveResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            SaveResultCard(result)
        }
    }

    // ダイアログ類
    if (showPromptDialog) {
        PromptDialog(
            prompt = uiState.generatedPrompt,
            onCopy = {
                copyToClipboard(context, uiState.generatedPrompt)
                showPromptDialog = false
            },
            onDismiss = { showPromptDialog = false }
        )
    }

    if (showOutputDialog) {
        OutputDialog(
            onSubmit = { text ->
                viewModel.updateAiOutput(text)
                viewModel.parseAndSaveOutput()
                showOutputDialog = false
            },
            onDismiss = { showOutputDialog = false }
        )
    }
}

// ====================
// 操作種別セレクター
// ====================
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
            label = { Text("操作") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OperationKind.entries.forEach { op ->
                DropdownMenuItem(
                    text = { Text(op.displayName) },
                    onClick = { onSelect(op); expanded = false }
                )
            }
        }
    }
}

// ====================
// 動的入力フィールド
// ====================
@Composable
private fun DynamicInputFields(
    uiState: WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    when (uiState.selectedOperation) {
        OperationKind.TEXT_GEN -> TextGenInputs(uiState, viewModel)
        OperationKind.STUDY_CARD -> StudyCardInputs(uiState, viewModel)
        OperationKind.PERSONA_GEN -> PersonaGenInputs(uiState, viewModel)
        OperationKind.TOPIC_GEN -> TopicGenInputs(uiState, viewModel)
        OperationKind.OBSERVE_IMAGE -> ObserveImageInputs(uiState, viewModel)
        OperationKind.CORE_EXTRACT -> CoreExtractInputs(uiState, viewModel)
        OperationKind.GIKO -> GikoInputs(uiState, viewModel)
        OperationKind.REVISION_FULL -> RevisionInputs(uiState, viewModel)
        OperationKind.PERSONA_VERIFY_ASSIST -> PersonaVerifyInputs(uiState, viewModel)
        else -> GenericInputs(uiState, viewModel)
    }
}

@Composable
private fun TextGenInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.inputTopic,
            onValueChange = viewModel::updateInputTopic,
            label = { Text("トピック / お題 *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputWriter,
            onValueChange = viewModel::updateInputWriter,
            label = { Text("書き手 (Persona名)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputReader,
            onValueChange = viewModel::updateInputReader,
            label = { Text("読者像") },
            modifier = Modifier.fillMaxWidth()
        )
        LengthSelector(
            selected = uiState.selectedLength,
            onSelect = viewModel::updateSelectedLength
        )
    }
}

@Composable
private fun StudyCardInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.inputSourceText,
            onValueChange = viewModel::updateInputSourceText,
            label = { Text("対象本文 *") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            minLines = 5
        )
        OutlinedTextField(
            value = uiState.inputReader,
            onValueChange = viewModel::updateInputReader,
            label = { Text("読者像 (任意)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PersonaGenInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    OutlinedTextField(
        value = uiState.inputGenre,
        onValueChange = viewModel::updateInputGenre,
        label = { Text("ジャンル (任意: 例「古典和歌」「現代詩」)") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TopicGenInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    OutlinedTextField(
        value = uiState.inputImageUrl,
        onValueChange = viewModel::updateInputImageUrl,
        label = { Text("画像URL (任意: 画像からトピック生成)") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ObserveImageInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    OutlinedTextField(
        value = uiState.inputImageUrl,
        onValueChange = viewModel::updateInputImageUrl,
        label = { Text("画像URL *") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CoreExtractInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.inputSourceText,
            onValueChange = viewModel::updateInputSourceText,
            label = { Text("対象本文 *") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            minLines = 5
        )
        OutlinedTextField(
            value = uiState.inputReader,
            onValueChange = viewModel::updateInputReader,
            label = { Text("読者像 (任意)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GikoInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.inputSourceText,
            onValueChange = viewModel::updateInputSourceText,
            label = { Text("元の現代文 *") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            minLines = 5
        )
        OutlinedTextField(
            value = uiState.inputToneLabel,
            onValueChange = viewModel::updateInputToneLabel,
            label = { Text("文調ラベル (例: 古今調、新古今調)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputToneRule,
            onValueChange = viewModel::updateInputToneRule,
            label = { Text("文調ルール (詳細)") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3
        )
        OutlinedTextField(
            value = uiState.inputReader,
            onValueChange = viewModel::updateInputReader,
            label = { Text("読者像 (任意)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RevisionInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.inputSourceText,
            onValueChange = viewModel::updateInputSourceText,
            label = { Text("元原稿 *") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            minLines = 5
        )
        OutlinedTextField(
            value = uiState.inputCoreSentence,
            onValueChange = viewModel::updateInputCoreSentence,
            label = { Text("核の一文 *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputCoreTheme,
            onValueChange = viewModel::updateInputCoreTheme,
            label = { Text("テーマ (任意)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputCoreEmotion,
            onValueChange = viewModel::updateInputCoreEmotion,
            label = { Text("感情 (任意)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputCoreTakeaway,
            onValueChange = viewModel::updateInputCoreTakeaway,
            label = { Text("持ち帰り (任意)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputReader,
            onValueChange = viewModel::updateInputReader,
            label = { Text("読者像 (任意)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PersonaVerifyInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.inputTargetPersonaName,
            onValueChange = viewModel::updateInputTargetPersonaName,
            label = { Text("ペルソナ名 *") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputTargetPersonaBio,
            onValueChange = viewModel::updateInputTargetPersonaBio,
            label = { Text("ペルソナBio *") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 4
        )
        OutlinedTextField(
            value = uiState.inputEvidence1,
            onValueChange = viewModel::updateInputEvidence1,
            label = { Text("根拠テキスト") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 4
        )
    }
}

@Composable
private fun GenericInputs(uiState: WorkbenchUiState, viewModel: WorkbenchViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.inputTopic,
            onValueChange = viewModel::updateInputTopic,
            label = { Text("トピック / お題") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputSourceText,
            onValueChange = viewModel::updateInputSourceText,
            label = { Text("ソーステキスト") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            minLines = 4
        )
    }
}

// ====================
// 長さセレクター
// ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LengthSelector(
    selected: LengthProfile,
    onSelect: (LengthProfile) -> Unit
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
            label = { Text("長さ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LengthProfile.entries.forEach { len ->
                DropdownMenuItem(
                    text = { Text(len.displayName) },
                    onClick = { onSelect(len); expanded = false }
                )
            }
        }
    }
}

// ====================
// アクションボタン群（AI選択を削除）
// ====================
@Composable
private fun ActionButtons(
    uiState: WorkbenchUiState,
    viewModel: WorkbenchViewModel,
    context: Context,
    onShowPromptDialog: () -> Unit,
    onShowOutputDialog: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // プロンプト生成ボタン
        Button(
            onClick = {
                val error = viewModel.validateInput()
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.generatePrompt()
                    onShowPromptDialog()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isProcessing
        ) {
            Icon(Icons.Default.Create, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("プロンプト生成")
        }

        // AI画面へ送信ボタン（設定で保存されたAIを使用）
        Button(
            onClick = {
                val error = viewModel.validateInput()
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.generatePrompt()
                    viewModel.openAiBrowser()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !uiState.isProcessing
        ) {
            Icon(Icons.Default.Send, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI画面へ送信 → 自動取り込み")
        }

        // 手動貼り付けボタン
        OutlinedButton(
            onClick = onShowOutputDialog,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isProcessing
        ) {
            Icon(Icons.Default.ContentPaste, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI結果を手動貼り付け")
        }
    }
}

// ====================
// 保存結果カード
// ====================
@Composable
private fun SaveResultCard(result: SaveResult) {
    val (color, icon, text) = when (result) {
        is SaveResult.Success -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            Icons.Default.CheckCircle,
            "✓ ${result.message.ifEmpty { "${result.count}件保存しました" }}"
        )
        is SaveResult.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Default.Error,
            "✗ ${result.message}"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text)
        }
    }
}

// ====================
// ダイアログ
// ====================
@Composable
private fun PromptDialog(
    prompt: String,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("生成されたプロンプト") },
        text = {
            Column {
                Text(
                    text = if (prompt.length > 1000) prompt.take(1000) + "..." else prompt,
                    style = MaterialTheme.typography.bodySmall
                )
                if (prompt.length > 1000) {
                    Text(
                        text = "(${prompt.length}文字 - 一部のみ表示)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("コピー")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("閉じる") }
        }
    )
}

@Composable
private fun OutputDialog(
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI出力を貼り付け") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                placeholder = { Text("AIの出力をここに貼り付けてください") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(text) },
                enabled = text.isNotBlank()
            ) {
                Text("解析して保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

// ====================
// ヘルパー
// ====================
private fun getOperationDescription(op: OperationKind): String = when (op) {
    OperationKind.TEXT_GEN -> "指定したお題・読者像・書き手から本文を生成し、作品(Work)として保存します。"
    OperationKind.STUDY_CARD -> "貼り付けた本文を分解して学習カード(StudyCard)を作り、練習メニューとして保存します。"
    OperationKind.TOPIC_GEN -> "作品づくりのための「詳細お題（固定条件つき）」を複数生成してTopicとして保存します。"
    OperationKind.PERSONA_GEN -> "実在人物ベースの書き手像（Persona）を生成して、書き手候補として保存します。"
    OperationKind.OBSERVE_IMAGE -> "画像から観察ノート（五感・比喩・核候補）を作り、Observationとして保存します。"
    OperationKind.CORE_EXTRACT -> "本文の核（テーマ/感情/持ち帰り/核の一文）を抽出し、分析用Workとして保存します。"
    OperationKind.REVISION_FULL -> "核を不変条件にして全文推敲（複数案）を作り、推敲Workとして保存します。"
    OperationKind.GIKO -> "本文の意味を維持したまま指定文調へ書き換え、擬古文Workとして保存します。"
    OperationKind.PERSONA_VERIFY_ASSIST -> "人物プロフィールの根拠テキストをもとに矛盾/支持を整理し、検証ログとして保存します。"
    else -> "選択した操作を実行して、結果をライブラリに保存します。"
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("prompt", text))
    Toast.makeText(context, "コピーしました", Toast.LENGTH_SHORT).show()
}
