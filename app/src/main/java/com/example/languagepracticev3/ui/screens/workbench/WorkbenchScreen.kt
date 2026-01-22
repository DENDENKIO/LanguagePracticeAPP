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
import com.example.languagepracticev3.data.model.OperationKind
import com.example.languagepracticev3.viewmodel.SaveResult
import com.example.languagepracticev3.viewmodel.WorkbenchViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "作業台",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 操作種別選択
        OperationSelector(
            selected = uiState.selectedOperation,
            onSelect = viewModel::updateOperation
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 入力フィールド（簡易版）
        OutlinedTextField(
            value = uiState.inputTopic,
            onValueChange = viewModel::updateInputTopic,
            label = { Text("トピック / お題") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.inputSourceText,
            onValueChange = viewModel::updateInputSourceText,
            label = { Text("原文 / ソーステキスト") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // アクション
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
            Icon(Icons.Default.Create, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("プロンプト生成")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showOutputDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentPaste, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI結果を貼り付け")
        }

        // 保存結果の表示
        uiState.lastSaveResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            val color = if (result is SaveResult.Success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Text(
                text = when (result) {
                    is SaveResult.Success -> "保存成功: ${result.count}件"
                    is SaveResult.Error -> "エラー: ${result.message}"
                },
                color = color,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // ダイアログ類
    if (showPromptDialog) {
        AlertDialog(
            onDismissRequest = { showPromptDialog = false },
            title = { Text("生成されたプロンプト") },
            text = { Text(uiState.generatedPrompt) },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("prompt", uiState.generatedPrompt))
                    showPromptDialog = false
                }) { Text("コピー") }
            }
        )
    }

    if (showOutputDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showOutputDialog = false },
            title = { Text("AI出力を解析") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateAiOutput(text)
                    viewModel.parseAndSaveOutput()
                    showOutputDialog = false
                }) { Text("解析して保存") }
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
            label = { Text("操作") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
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
