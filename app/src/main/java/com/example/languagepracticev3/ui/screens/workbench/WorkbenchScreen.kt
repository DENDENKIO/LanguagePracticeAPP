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
import com.example.languagepracticev3.data.models.LengthProfile
import com.example.languagepracticev3.data.models.OperationKind
import com.example.languagepracticev3.viewmodel.SaveResult
import com.example.languagepracticev3.viewmodel.WorkbenchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkbenchScreen(
    viewModel: WorkbenchViewModel = hiltViewModel(),
    onOpenAiBrowser: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPromptDialog by remember { mutableStateOf(false) }
    var showOutputDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // ヘッダー
        Text(
            text = "作業台",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

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

        // 操作に応じた入力フィールド（全分岐対応）
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Create, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("プロンプト生成")
            }

            // AIに送信
            Button(
                onClick = {
                    if (uiState.generatedPrompt.isNotBlank()) {
                        // クリップボードにコピー
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("prompt", uiState.generatedPrompt))
                        Toast.makeText(context, "コピーしました", Toast.LENGTH_SHORT).show()

                        // ブラウザを開く（または内蔵WebView）
                        onOpenAiBrowser(uiState.selectedAiSite.url, uiState.generatedPrompt)
                    } else {
                        Toast.makeText(context, "先にプロンプトを生成してください", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = uiState.generatedPrompt.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("AIに送信")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 結果入力ボタン
        OutlinedButton(
            onClick = { showOutputDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("AI出力を貼り付け")
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

// ==========================================
// 各操作種別の入力コンポーネント
// ==========================================

@Composable
private fun ReaderAutoGenInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    OutlinedTextField(
        value = uiState.contextKindInput,
        onValueChange = viewModel::updateContextKind,
        label = { Text("作品種別（例：随筆、詩、日記）") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TextGenInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.writerInput,
            onValueChange = viewModel::updateWriter,
            label = { Text("書き手（ペルソナ名）") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.topicInput,
            onValueChange = viewModel::updateTopic,
            label = { Text("お題（空欄でAI自動生成）") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.readerInput,
            onValueChange = viewModel::updateReader,
            label = { Text("想定読者") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.toneLabel,
            onValueChange = viewModel::updateToneLabel,
            label = { Text("文調（任意）") },
            modifier = Modifier.fillMaxWidth()
        )

        // 文字数選択
        LengthSelector(
            selected = uiState.selectedLength,
            onSelect = viewModel::updateLength
        )
    }
}

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
            value = "${selected.displayName} (${selected.minChars}〜${selected.maxChars}字)",
            onValueChange = {},
            readOnly = true,
            label = { Text("文字数") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LengthProfile.entries.forEach { length ->
                DropdownMenuItem(
                    text = { Text("${length.displayName} (${length.minChars}〜${length.maxChars}字)") },
                    onClick = {
                        onSelect(length)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StudyCardInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.readerInput,
            onValueChange = viewModel::updateReader,
            label = { Text("想定学習者") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.toneLabel,
            onValueChange = viewModel::updateToneLabel,
            label = { Text("文調（任意）") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.sourceTextInput,
            onValueChange = viewModel::updateSourceText,
            label = { Text("対象本文 *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 10
        )
    }
}

@Composable
private fun PersonaGenInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    OutlinedTextField(
        value = uiState.genreInput,
        onValueChange = viewModel::updateGenre,
        label = { Text("ジャンル（空欄で多様なジャンル）") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TopicGenInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    OutlinedTextField(
        value = uiState.imageUrlInput,
        onValueChange = viewModel::updateImageUrl,
        label = { Text("参考画像URL（任意）") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ObserveImageInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    OutlinedTextField(
        value = uiState.imageUrlInput,
        onValueChange = viewModel::updateImageUrl,
        label = { Text("画像URL *") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CoreExtractInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.readerInput,
            onValueChange = viewModel::updateReader,
            label = { Text("想定読者") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.sourceTextInput,
            onValueChange = viewModel::updateSourceText,
            label = { Text("対象本文 *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 10
        )
    }
}

@Composable
private fun RevisionInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.sourceTextInput,
            onValueChange = viewModel::updateSourceText,
            label = { Text("元の文章 *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 8
        )

        OutlinedTextField(
            value = uiState.coreTheme,
            onValueChange = viewModel::updateCoreTheme,
            label = { Text("テーマ") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.coreEmotion,
            onValueChange = viewModel::updateCoreEmotion,
            label = { Text("感情") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.coreTakeaway,
            onValueChange = viewModel::updateCoreTakeaway,
            label = { Text("持ち帰り") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.coreSentence,
            onValueChange = viewModel::updateCoreSentence,
            label = { Text("核の一文 *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.readerInput,
            onValueChange = viewModel::updateReader,
            label = { Text("想定読者") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GikoInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.sourceTextInput,
            onValueChange = viewModel::updateSourceText,
            label = { Text("元の現代文 *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 8
        )

        OutlinedTextField(
            value = uiState.toneLabel,
            onValueChange = viewModel::updateToneLabel,
            label = { Text("文調ラベル（例: 平安風、漢文調）") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.toneRuleText,
            onValueChange = viewModel::updateToneRule,
            label = { Text("文調のルール") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            maxLines = 4
        )

        OutlinedTextField(
            value = uiState.topicInput,
            onValueChange = viewModel::updateTopic,
            label = { Text("お題") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.readerInput,
            onValueChange = viewModel::updateReader,
            label = { Text("想定読者") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PersonaVerifyInputs(
    uiState: com.example.languagepracticev3.viewmodel.WorkbenchUiState,
    viewModel: WorkbenchViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = uiState.personaNameInput,
            onValueChange = viewModel::updatePersonaName,
            label = { Text("対象ペルソナ名 *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.personaBioInput,
            onValueChange = viewModel::updatePersonaBio,
            label = { Text("現在のBIO *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 5
        )

        OutlinedTextField(
            value = uiState.evidence1Input,
            onValueChange = viewModel::updateEvidence1,
            label = { Text("根拠テキスト E1") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            maxLines = 4
        )

        OutlinedTextField(
            value = uiState.evidence2Input,
            onValueChange = viewModel::updateEvidence2,
            label = { Text("根拠テキスト E2") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            maxLines = 4
        )

        OutlinedTextField(
            value = uiState.evidence3Input,
            onValueChange = viewModel::updateEvidence3,
            label = { Text("根拠テキスト E3") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            maxLines = 4
        )
    }
}
