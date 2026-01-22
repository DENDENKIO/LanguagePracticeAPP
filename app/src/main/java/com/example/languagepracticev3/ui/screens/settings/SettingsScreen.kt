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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.viewmodel.SettingsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToLibrary: () -> Unit = {}  // ★追加: ライブラリへのナビゲーション
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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

            // ★データ管理カード（拡充）
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "データ管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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

                    // ★保存データ閲覧・編集ボタン（参考ソフトのLibrary相当）
                    Button(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("保存データを閲覧・編集")
                    }

                    Text(
                        "※保存した作品、ペルソナ、トピック等の検索・編集・削除ができます",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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

                    // ★全データ削除ボタン（参考ソフト相当）
                    OutlinedButton(
                        onClick = { showClearDataDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("全データを削除")
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
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("設定を保存")
            }
        }
    }

    // ★全データ削除確認ダイアログ
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("全データ削除") },
            text = {
                Column {
                    Text("以下のデータがすべて削除されます：")
                    Spacer(Modifier.height(8.dp))
                    Text("• 作品 (Works)")
                    Text("• 学習カード (StudyCards)")
                    Text("• ペルソナ (Personas)")
                    Text("• トピック (Topics)")
                    Text("• 観察記録 (Observations)")
                    Text("• 実行ログ (RunLogs)")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "この操作は元に戻せません。",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("削除する")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
