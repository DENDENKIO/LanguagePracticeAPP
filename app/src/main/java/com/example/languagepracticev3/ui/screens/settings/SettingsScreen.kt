// app/src/main/java/com/example/languagepracticev3/ui/screens/settings/SettingsScreen.kt
package com.example.languagepracticev3.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.viewmodel.SettingsViewModel

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

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var aiSiteDropdownExpanded by remember { mutableStateOf(false) }

    // 選択中のサイトプロファイル
    val selectedProfile = viewModel.aiSitePresets.find { it.id == selectedAiSiteId }
        ?: viewModel.aiSitePresets.first()

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
                                    "このサイトは自動送信が不安定な場合があります。手動操作をお勧めします。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // ヘルプテキスト
                    Text(
                        "※ Genspark・Perplexityは自動送信に対応しています。Google AIやChatGPTはログインが必要なため手動操作を推奨します。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::saveSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}
