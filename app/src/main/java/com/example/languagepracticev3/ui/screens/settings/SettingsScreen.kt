// app/src/main/java/com/example/languagepracticev3/ui/screens/settings/SettingsScreen.kt
package com.example.languagepracticev3.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.viewmodel.SettingsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val writerName by viewModel.writerName.collectAsState()
    val readerNote by viewModel.readerNote.collectAsState()
    val selectedAiSiteId by viewModel.selectedAiSiteId.collectAsState()
    val customAiSiteUrl by viewModel.customAiSiteUrl.collectAsState()
    val isAutoMode by viewModel.isAutoMode.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var aiSiteDropdownExpanded by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    // 選択中のサイトプロファイル
    val selectedProfile = viewModel.aiSitePresets.find { it.id == selectedAiSiteId }
        ?: viewModel.aiSitePresets.first()

    // バックアップ用ファイル選択
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.backupDatabase(it) }
    }

    // 復元用ファイル選択
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreDatabase(it) }
    }

    LaunchedEffect(statusMessage) {
        if (statusMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(statusMessage)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定 (Settings)") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 基本設定カード
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "基本設定",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = writerName,
                        onValueChange = viewModel::updateWriterName,
                        label = { Text("作者名 (Writer Name)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = readerNote,
                        onValueChange = viewModel::updateReaderNote,
                        label = { Text("読者像メモ (Reader Note)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            // AI設定カード
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "AI設定",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // AIサイト選択ドロップダウン
                    ExposedDropdownMenuBox(
                        expanded = aiSiteDropdownExpanded,
                        onExpandedChange = { aiSiteDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProfile.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("AIサイト") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = aiSiteDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = aiSiteDropdownExpanded,
                            onDismissRequest = { aiSiteDropdownExpanded = false }
                        ) {
                            viewModel.aiSitePresets.forEach { profile ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(profile.name)
                                            Text(
                                                text = if (profile.supportsAuto) "自動送信対応" else "手動操作推奨",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (profile.supportsAuto)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateSelectedAiSiteId(profile.id)
                                        aiSiteDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // カスタムURL入力
                    OutlinedTextField(
                        value = customAiSiteUrl,
                        onValueChange = viewModel::updateCustomAiSiteUrl,
                        label = { Text("AIサイトURL（カスタム）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(selectedProfile.url) }
                    )

                    // 自動モード切り替え
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("自動送信モード")
                            Text(
                                text = "WebViewでプロンプトを自動入力・送信します",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAutoMode,
                            onCheckedChange = viewModel::updateAutoMode
                        )
                    }

                    // 注意事項
                    if (!selectedProfile.supportsAuto && isAutoMode) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    viewModel.autoModeHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // データ管理カード
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "データ管理",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // データベース情報
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "データベースサイズ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            viewModel.getDatabaseSize(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider()

                    // バックアップボタン
                    OutlinedButton(
                        onClick = {
                            val timestamp = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                            backupLauncher.launch("LanguagePractice_Backup_$timestamp.db")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("バックアップを作成")
                    }

                    // 復元ボタン
                    OutlinedButton(
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("バックアップから復元")
                    }

                    // 処理中インジケーター
                    if (isProcessing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("処理中...")
                        }
                    }

                    // 注意書き
                    Text(
                        "※復元後はアプリを再起動してください",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存ボタン
            Button(
                onClick = viewModel::saveSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("設定を保存")
            }
        }
    }
}
