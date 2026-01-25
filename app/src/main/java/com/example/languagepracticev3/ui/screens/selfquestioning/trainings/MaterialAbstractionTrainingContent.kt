// app/src/main/java/com/example/languagepracticev3/ui/screens/selfquestioning/trainings/MaterialAbstractionTrainingContent.kt
package com.example.languagepracticev3.ui.screens.selfquestioning.trainings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.*
import com.example.languagepracticev3.viewmodel.MaterialAbstractionTrainingViewModel
import com.example.languagepracticev3.viewmodel.MaterialAbstractionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialAbstractionTrainingContent(
    viewModel: MaterialAbstractionTrainingViewModel = hiltViewModel(),
    onExitTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.statusMessage) {
        if (uiState.statusMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.statusMessage)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ヘッダー
            MaterialAbstractionHeader(
                hasSession = uiState.currentSession != null,
                onBack = {
                    if (uiState.currentSession != null) {
                        viewModel.showExitConfirmation()
                    } else {
                        onExitTraining()
                    }
                },
                onShowSessions = { viewModel.showSessionPicker() }
            )

            // メインコンテンツ
            if (uiState.currentSession == null) {
                MaterialAbstractionSessionStartScreen(
                    sessions = uiState.sessions,
                    onNewSession = { viewModel.startNewSession() },
                    onLoadSession = { viewModel.loadSession(it) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onShowDetail = { viewModel.showHistoryDetail(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                MaterialAbstractionTrainingFlow(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 中断確認ダイアログ
    if (uiState.showConfirmExit) {
        AlertDialog(
            onDismissRequest = { viewModel.hideExitConfirmation() },
            title = { Text("トレーニングを中断しますか？") },
            text = { Text("保存されていない変更は失われます。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmExit()
                    onExitTraining()
                }) {
                    Text("中断する")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.hideExitConfirmation() }) {
                        Text("キャンセル")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        viewModel.saveSession()
                        viewModel.hideExitConfirmation()
                    }) {
                        Text("保存して続ける")
                    }
                }
            }
        )
    }

    // セッションピッカーダイアログ
    if (uiState.showSessionPicker) {
        MaterialAbstractionSessionPickerDialog(
            sessions = uiState.sessions,
            onSelect = { viewModel.loadSession(it) },
            onNewSession = { viewModel.startNewSession() },
            onDelete = { viewModel.deleteSession(it) },
            onShowDetail = { viewModel.showHistoryDetail(it) },
            onDismiss = { viewModel.hideSessionPicker() }
        )
    }

    // 履歴詳細ダイアログ
    if (uiState.showHistoryDetail && uiState.historyDetailSession != null) {
        HistoryDetailDialog(
            session = uiState.historyDetailSession!!,
            onDismiss = { viewModel.hideHistoryDetail() }
        )
    }

    // 軸選択ダイアログ
    if (uiState.showAxisSelector) {
        AxisSelectorDialog(
            axes = viewModel.dictionary.axes,
            selectedAxes = uiState.selectedAxes,
            onToggle = { viewModel.toggleAxis(it) },
            onDismiss = { viewModel.hideAxisSelector() }
        )
    }

    // タグ選択ダイアログ
    if (uiState.showTagSelector) {
        TagSelectorDialog(
            recommendedTags = viewModel.getRecommendedTags(),
            allTags = viewModel.dictionary.tags,
            selectedTags = uiState.selectedTags,
            onToggle = { viewModel.toggleTag(it) },
            modePreference = uiState.modePreference,
            onModeChange = { viewModel.setModePreference(it) },
            onDismiss = { viewModel.hideTagSelector() }
        )
    }

    // テンプレート選択ダイアログ
    if (uiState.showTemplateSelector) {
        TemplateSelectorDialog(
            templates = viewModel.dictionary.templateFrames,
            selectedTags = uiState.selectedTags,
            targetMaterial = uiState.inputTargetMaterial,
            onSelectTemplate = { templateId, customValues ->
                viewModel.generateTagSentence(templateId, customValues)
            },
            onDismiss = { viewModel.hideTemplateSelector() }
        )
    }
}

// ====================
// ヘッダー
// ====================
@Composable
private fun MaterialAbstractionHeader(
    hasSession: Boolean,
    onBack: () -> Unit,
    onShowSessions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
        }
        Text(
            text = "物質-抽象変換",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (hasSession) {
            IconButton(onClick = onShowSessions) {
                Icon(Icons.Default.FolderOpen, contentDescription = "セッション一覧")
            }
        }
    }
    HorizontalDivider()
}

// ====================
// セッション開始画面
// ====================
@Composable
private fun MaterialAbstractionSessionStartScreen(
    sessions: List<MaterialAbstractionSession>,
    onNewSession: () -> Unit,
    onLoadSession: (MaterialAbstractionSession) -> Unit,
    onDeleteSession: (MaterialAbstractionSession) -> Unit,
    onShowDetail: (MaterialAbstractionSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "物質-抽象変換 トレーニング",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "このトレーニングでは、7つのフェーズで「具体（物質）→抽象（感情）」の変換を行います：",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                MaterialAbstractionStep.entries.forEachIndexed { index, step ->
                    Text(
                        "${index + 1}. ${step.displayName} - ${step.description}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNewSession,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("新規トレーニングを開始")
        }

        if (sessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "過去のセッション（履歴）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            sessions.forEach { session ->
                MaterialAbstractionSessionCard(
                    session = session,
                    onClick = { onLoadSession(session) },
                    onDelete = { onDeleteSession(session) },
                    onShowDetail = { onShowDetail(session) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MaterialAbstractionSessionCard(
    session: MaterialAbstractionSession,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShowDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (session.isCompleted) Icons.Default.CheckCircle else Icons.Default.Edit,
                null,
                tint = if (session.isCompleted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.sessionTitle.ifEmpty { "無題 #${session.id}" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "対象: ${session.targetMaterial.take(20)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "ステップ: ${MaterialAbstractionStep.entries.getOrNull(session.currentStep)?.displayName ?: "観察"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.isCompleted) {
                    Text(
                        "テーマ: ${session.abstractTheme}  スコア: 抽象${session.abstractScore}/描写${session.sensoryScore}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onShowDetail) {
                Icon(Icons.Default.Visibility, "詳細を見る")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "削除")
            }
        }
    }
}

// ====================
// 履歴詳細ダイアログ
// ====================
@Composable
private fun HistoryDetailDialog(
    session: MaterialAbstractionSession,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                session.sessionTitle.ifEmpty { "セッション #${session.id}" },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 各フェーズの結果を表示
                HistorySection("対象物質", session.targetMaterial)
                HistorySection("観察（生データ）", session.observationRaw)
                HistorySection("特徴リスト", session.featureList)
                HistorySection("選択した軸", session.selectedAxes)
                HistorySection("選択したタグ", session.selectedTags)
                HistorySection("生成したタグ文", session.tagSentences)
                HistorySection("強いタグ文", session.strongTagSentences)
                HistorySection("連想", session.associations.replace("|||", "\n---\n").replace(";;", "\n"))
                HistorySection("抽象テーマ", session.abstractTheme)
                HistorySection("禁止ワード", session.forbiddenWords)
                HistorySection("最終表現", session.finalExpression)

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    AssistChip(
                        onClick = {},
                        label = { Text("抽象スコア: ${session.abstractScore}/5") }
                    )
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("描写スコア: ${session.sensoryScore}/5") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "作成: ${session.createdAt.take(16)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("閉じる") }
        }
    )
}

@Composable
private fun HistorySection(title: String, content: String) {
    if (content.isBlank()) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                content,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// ====================
// トレーニングフロー
// ====================
@Composable
private fun MaterialAbstractionTrainingFlow(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ステップインジケーター
        MaterialAbstractionStepIndicator(
            currentStep = uiState.currentStep,
            onStepClick = { viewModel.goToStep(it) }
        )

        // メインコンテンツ（スクロール可能）
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (uiState.currentStep) {
                    MaterialAbstractionStep.OBSERVATION -> ObservationStepContent(uiState, viewModel)
                    MaterialAbstractionStep.FEATURE_EXTRACTION -> FeatureExtractionStepContent(uiState, viewModel)
                    MaterialAbstractionStep.AXIS_TAG_SELECTION -> AxisTagSelectionStepContent(uiState, viewModel)
                    MaterialAbstractionStep.CONVERGENCE -> ConvergenceStepContent(uiState, viewModel)
                    MaterialAbstractionStep.ASSOCIATION -> AssociationStepContent(uiState, viewModel)
                    MaterialAbstractionStep.THEME_DECISION -> ThemeDecisionStepContent(uiState, viewModel)
                    MaterialAbstractionStep.FINAL_EXPRESSION -> FinalExpressionStepContent(uiState, viewModel)
                }
            }
        }

        // ナビゲーションボタン
        MaterialAbstractionNavigationButtons(
            currentStep = uiState.currentStep,
            onPrevious = { viewModel.previousStep() },
            onNext = { viewModel.nextStep() },
            onSave = { viewModel.saveSession() },
            onComplete = { viewModel.completeSession() }
        )
    }
}

// ====================
// ステップインジケーター
// ====================
@Composable
private fun MaterialAbstractionStepIndicator(
    currentStep: MaterialAbstractionStep,
    onStepClick: (MaterialAbstractionStep) -> Unit
) {
    val steps = MaterialAbstractionStep.entries
    val currentIndex = currentStep.ordinal

    Column {
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(steps) { index, step ->
                FilterChip(
                    selected = index == currentIndex,
                    onClick = { onStepClick(step) },
                    label = { Text("${index + 1}") },
                    leadingIcon = if (index < currentIndex) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${currentIndex + 1}/${steps.size}: ${currentStep.displayName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            currentStep.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ====================
// Phase 1: 観察
// ====================
@Composable
private fun ObservationStepContent(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "💡 観察のルール",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "• 意味づけ禁止（「悲しそう」「寂しげ」などは✗）\n" +
                            "• 5感覚で描写（視覚・聴覚・触覚・嗅覚・味覚）\n" +
                            "• 事実だけを記録する",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputTargetMaterial,
            onValueChange = { viewModel.updateTargetMaterial(it) },
            label = { Text("対象物質") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: りんご、封筒、卵、スプーン...") }
        )

        OutlinedTextField(
            value = uiState.inputObservationRaw,
            onValueChange = { viewModel.updateObservationRaw(it) },
            label = { Text("具体描写（5感覚で、意味づけなし）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            placeholder = {
                Text(
                    "例:\n" +
                            "【視覚】赤と黄色が混ざった皮。表面に小さな点（果点）が散らばっている。\n" +
                            "【触覚】つるつるしているが、ヘタの周りはざらざら。\n" +
                            "【嗅覚】甘い香りが微かに。\n" +
                            "【重さ】手のひらに収まる重さ。200gくらい？"
                )
            }
        )
    }
}

// ====================
// Phase 2: 特徴抽出
// ====================
@Composable
private fun FeatureExtractionStepContent(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "📝 特徴抽出のポイント",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "観察から「事実」だけを短文で抜き出します。\n" +
                            "• 「〜している」「〜がある」の形で\n" +
                            "• 最低5つ以上",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 観察内容の参照
        if (uiState.inputObservationRaw.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("観察内容:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        uiState.inputObservationRaw.take(200) +
                                if (uiState.inputObservationRaw.length > 200) "..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text(
            "特徴リスト（事実だけを箇条書き）",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        uiState.inputFeatures.forEachIndexed { index, feature ->
            OutlinedTextField(
                value = feature,
                onValueChange = { viewModel.updateFeature(index, it) },
                label = { Text("特徴${index + 1}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        when (index) {
                            0 -> "例: まだ切られていない"
                            1 -> "例: 皮に小さな点がある"
                            2 -> "例: 甘い香りがする"
                            3 -> "例: 手のひらに収まる大きさ"
                            4 -> "例: ヘタの周りがざらざら"
                            else -> ""
                        }
                    )
                }
            )
        }
    }
}

// ====================
// Phase 3.5: 軸・タグ選択
// ====================
@Composable
private fun AxisTagSelectionStepContent(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🎯 軸・タグ選択 → タグ文生成",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "1. 特徴に関係する「軸」を選ぶ（20軸から複数可）\n" +
                            "2. 軸に紐づく「タグ」を選ぶ（各軸1〜2個推奨）\n" +
                            "3. テンプレートを使って「タグ文」を生成する",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 軸選択ボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.showAxisSelector() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ViewInAr, null)
                Spacer(Modifier.width(4.dp))
                Text("軸を選択 (${uiState.selectedAxes.size})")
            }

            OutlinedButton(
                onClick = { viewModel.showTagSelector() },
                modifier = Modifier.weight(1f),
                enabled = uiState.selectedAxes.isNotEmpty()
            ) {
                Icon(Icons.Default.Label, null)
                Spacer(Modifier.width(4.dp))
                Text("タグを選択 (${uiState.selectedTags.size})")
            }
        }

        // 選択した軸の表示
        if (uiState.selectedAxes.isNotEmpty()) {
            Text("選択した軸:", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(uiState.selectedAxes.toList()) { axisId ->
                    val axis = viewModel.dictionary.getAxisById(axisId)
                    if (axis != null) {
                        AssistChip(
                            onClick = { viewModel.toggleAxis(axisId) },
                            label = { Text("${axis.id}.${axis.label}") },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        // 選択したタグの表示
        if (uiState.selectedTags.isNotEmpty()) {
            Text("選択したタグ:", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(uiState.selectedTags.toList()) { tagId ->
                    val tag = viewModel.dictionary.getTagById(tagId)
                    if (tag != null) {
                        AssistChip(
                            onClick = { viewModel.toggleTag(tagId) },
                            label = { Text(tag.label) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // タグ文生成
        Text(
            "タグ文を生成",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.showTemplateSelector() },
                enabled = uiState.selectedTags.isNotEmpty()
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(4.dp))
                Text("テンプレートから生成")
            }
        }

        // カスタムタグ文入力
        OutlinedTextField(
            value = uiState.inputCustomTagSentence,
            onValueChange = { viewModel.updateCustomTagSentence(it) },
            label = { Text("または自由にタグ文を書く") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { viewModel.addCustomTagSentence(uiState.inputCustomTagSentence) },
                    enabled = uiState.inputCustomTagSentence.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, "追加")
                }
            }
        )

        // 生成したタグ文一覧
        if (uiState.generatedTagSentences.isNotEmpty()) {
            Text(
                "生成したタグ文 (${uiState.generatedTagSentences.size}件)",
                style = MaterialTheme.typography.labelMedium
            )
            uiState.generatedTagSentences.forEachIndexed { index, sentence ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sentence,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeTagSentence(index) }) {
                            Icon(Icons.Default.Delete, "削除", Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ====================
// Phase 3.6: 収束
// ====================
@Composable
private fun ConvergenceStepContent(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🎯 収束（上位2〜4本に絞る）",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "タグ文が増えすぎた場合、「強い」と感じるものを2〜4本選んでください。\n" +
                            "選択基準: 抽象への変換が期待できる / 印象に残る / 核心を突いている",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            "タグ文を選択（${uiState.strongTagSentenceIndices.size}/4）",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (uiState.generatedTagSentences.isEmpty()) {
            Text(
                "タグ文がありません。前のステップで生成してください。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            uiState.generatedTagSentences.forEachIndexed { index, sentence ->
                val isSelected = index in uiState.strongTagSentenceIndices
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleStrongTagSentence(index) }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleStrongTagSentence(index) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            sentence,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// ====================
// Phase 4: 連想
// ====================
@Composable
private fun AssociationStepContent(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "💭 連想を出す",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "各タグ文から連想されることを3〜5個書いてください。\n" +
                            "連想のコツ: 「これを見たときに思い浮かぶ感情・状況・物語」",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        val strongSentences = uiState.strongTagSentenceIndices.mapNotNull { idx ->
            uiState.generatedTagSentences.getOrNull(idx)?.let { idx to it }
        }

        if (strongSentences.isEmpty()) {
            Text(
                "強いタグ文が選択されていません。前のステップで選択してください。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            strongSentences.forEachIndexed { displayIndex, (originalIndex, sentence) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "タグ文${displayIndex + 1}: $sentence",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val associations = uiState.inputAssociations[displayIndex] ?: listOf("", "", "", "", "")
                        associations.take(5).forEachIndexed { assocIndex, assoc ->
                            OutlinedTextField(
                                value = assoc,
                                onValueChange = { viewModel.updateAssociation(displayIndex, assocIndex, it) },
                                label = { Text("連想${assocIndex + 1}") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = {
                                    Text(
                                        when (assocIndex) {
                                            0 -> "例: 期待"
                                            1 -> "例: 決断の瞬間"
                                            2 -> "例: 後戻りできない"
                                            else -> ""
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ====================
// Phase 5: テーマ決定
// ====================
@Composable
private fun ThemeDecisionStepContent(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "✨ テーマを決める",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "連想の中から、最も「伝えたい」抽象テーマを1つ選びます。\n" +
                            "※この語は最終表現では使いません（禁止ワードになります）",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 連想の一覧表示
        Text("出した連想:", style = MaterialTheme.typography.labelMedium)
        val allAssociations = uiState.inputAssociations.values.flatten().filter { it.isNotBlank() }
        if (allAssociations.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(allAssociations) { assoc ->
                    SuggestionChip(
                        onClick = { viewModel.updateAbstractTheme(assoc) },
                        label = { Text(assoc) }
                    )
                }
            }
        }

        // テーマ候補の提案
        Text("よく使われるテーマ:", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(viewModel.dictionary.commonAbstractThemes) { theme ->
                SuggestionChip(
                    onClick = { viewModel.updateAbstractTheme(theme) },
                    label = { Text(theme) }
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputAbstractTheme,
            onValueChange = { viewModel.updateAbstractTheme(it) },
            label = { Text("決定したテーマ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 期待、孤独、信頼...") }
        )

        HorizontalDivider()

        // 禁止ワード
        Text(
            "禁止ワード（最終表現で使えない語）",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(uiState.inputForbiddenWords) { word ->
                InputChip(
                    selected = true,
                    onClick = { viewModel.removeForbiddenWord(word) },
                    label = { Text(word) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                )
            }
        }

        var newForbiddenWord by remember { mutableStateOf("") }
        OutlinedTextField(
            value = newForbiddenWord,
            onValueChange = { newForbiddenWord = it },
            label = { Text("禁止ワードを追加") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        viewModel.addForbiddenWord(newForbiddenWord)
                        newForbiddenWord = ""
                    },
                    enabled = newForbiddenWord.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, "追加")
                }
            }
        )
    }
}

// ====================
// Phase 6: 最終表現
// ====================
@Composable
private fun FinalExpressionStepContent(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionTrainingViewModel
) {
    val forbiddenFound = viewModel.checkForbiddenWords()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🎨 抽象語禁止で表現（3〜5行）",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "テーマ「${uiState.inputAbstractTheme}」を、その言葉を使わずに表現してください。\n" +
                            "観察描写＋タグ文＋連想を材料に、150〜300字で。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 禁止ワード警告
        if (uiState.inputForbiddenWords.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "⚠️ 使ってはいけない語",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        uiState.inputForbiddenWords.joinToString("、"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // 参照情報
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("参照:", style = MaterialTheme.typography.labelSmall)
                Text("対象: ${uiState.inputTargetMaterial}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "強いタグ文: ${uiState.strongTagSentenceIndices.mapNotNull {
                        uiState.generatedTagSentences.getOrNull(it)?.take(30)
                    }.joinToString(" / ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputFinalExpression,
            onValueChange = { viewModel.updateFinalExpression(it) },
            label = { Text("最終表現（抽象語禁止、3〜5行）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            isError = forbiddenFound.isNotEmpty(),
            supportingText = {
                if (forbiddenFound.isNotEmpty()) {
                    Text(
                        "禁止ワードが含まれています: ${forbiddenFound.joinToString("、")}",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("${uiState.inputFinalExpression.length}文字")
                }
            },
            placeholder = {
                Text(
                    "例:\n" +
                            "まだ刃は触れていない。\n" +
                            "赤と黄色が混ざった皮の下に、白い果肉が閉じ込められている。\n" +
                            "切れ目が入った瞬間、甘い香りが溢れ出すだろう。\n" +
                            "その一秒前の、張り詰めた静けさ。\n" +
                            "取り返しのつかない何かが、もうすぐ始まる。"
                )
            }
        )

        // スコア表示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("抽象変換スコア", style = MaterialTheme.typography.labelSmall)
                Text(
                    "${uiState.abstractScore}/5",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("描写スコア", style = MaterialTheme.typography.labelSmall)
                Text(
                    "${uiState.sensoryScore}/5",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// ====================
// ナビゲーションボタン
// ====================
@Composable
private fun MaterialAbstractionNavigationButtons(
    currentStep: MaterialAbstractionStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    onComplete: () -> Unit
) {
    val steps = MaterialAbstractionStep.entries
    val currentIndex = currentStep.ordinal
    val isFirstStep = currentIndex == 0
    val isLastStep = currentIndex == steps.size - 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = !isFirstStep
        ) {
            Icon(Icons.Default.ArrowBack, null)
            Spacer(Modifier.width(8.dp))
            Text("戻る")
        }

        Button(onClick = onSave) {
            Icon(Icons.Default.Save, null)
            Spacer(Modifier.width(8.dp))
            Text("保存")
        }

        if (isLastStep) {
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("完了")
            }
        } else {
            Button(onClick = onNext) {
                Text("次へ")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
        }
    }
}

// ====================
// セッションピッカーダイアログ
// ====================
@Composable
private fun MaterialAbstractionSessionPickerDialog(
    sessions: List<MaterialAbstractionSession>,
    onSelect: (MaterialAbstractionSession) -> Unit,
    onNewSession: () -> Unit,
    onDelete: (MaterialAbstractionSession) -> Unit,
    onShowDetail: (MaterialAbstractionSession) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("セッションを選択") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        onNewSession()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("新規セッション")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (sessions.isEmpty()) {
                    Text(
                        "保存されたセッションはありません",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(sessions) { session ->
                            ListItem(
                                headlineContent = {
                                    Text(session.sessionTitle.ifEmpty { "無題 #${session.id}" })
                                },
                                supportingContent = {
                                    Column {
                                        Text("対象: ${session.targetMaterial.take(15)}")
                                        Text(session.createdAt.take(10))
                                    }
                                },
                                leadingContent = {
                                    Icon(
                                        if (session.isCompleted) Icons.Default.CheckCircle
                                        else Icons.Default.Edit,
                                        null
                                    )
                                },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = { onShowDetail(session) }) {
                                            Icon(Icons.Default.Visibility, "詳細")
                                        }
                                        IconButton(onClick = { onDelete(session) }) {
                                            Icon(Icons.Default.Delete, "削除")
                                        }
                                    }
                                },
                                modifier = Modifier.clickable { onSelect(session) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

// ====================
// 軸選択ダイアログ
// ====================
@Composable
private fun AxisSelectorDialog(
    axes: List<MaterialAbstractionDictionary.Axis>,
    selectedAxes: Set<Int>,
    onToggle: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("軸を選択（複数可）") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                items(axes) { axis ->
                    val isSelected = axis.id in selectedAxes
                    ListItem(
                        headlineContent = {
                            Text(
                                "${axis.id}. ${axis.label}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        supportingContent = {
                            Column {
                                Text(
                                    axis.definition,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "例: ${axis.examples.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggle(axis.id) }
                            )
                        },
                        modifier = Modifier.clickable { onToggle(axis.id) }
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完了") }
        }
    )
}

// ====================
// タグ選択ダイアログ
// ====================
@Composable
private fun TagSelectorDialog(
    recommendedTags: List<MaterialAbstractionDictionary.Tag>,
    allTags: List<MaterialAbstractionDictionary.Tag>,
    selectedTags: Set<String>,
    onToggle: (String) -> Unit,
    modePreference: String,
    onModeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAll by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val displayTags = if (showAll) {
        if (searchQuery.isBlank()) allTags
        else allTags.filter {
            it.label.contains(searchQuery) ||
                    it.aliases.any { alias -> alias.contains(searchQuery) }
        }
    } else {
        recommendedTags
    }

    // ファセットでグループ化
    val groupedTags = displayTags.groupBy { it.facet }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("タグを選択") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // モード選択
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = modePreference == "abstract",
                        onClick = { onModeChange("abstract") },
                        label = { Text("抽象変換重視") }
                    )
                    FilterChip(
                        selected = modePreference == "sensory",
                        onClick = { onModeChange("sensory") },
                        label = { Text("描写重視") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 表示切替
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (showAll) "全タグ表示中" else "おすすめタグ表示中",
                        style = MaterialTheme.typography.labelSmall
                    )
                    TextButton(onClick = { showAll = !showAll }) {
                        Text(if (showAll) "おすすめのみ" else "全タグ表示")
                    }
                }

                // 検索
                if (showAll) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("検索") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // タグ一覧
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    groupedTags.forEach { (facet, tags) ->
                        item {
                            Text(
                                facet,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(tags) { tag ->
                            val isSelected = tag.id in selectedTags
                            ListItem(
                                headlineContent = {
                                    Text(
                                        tag.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                supportingContent = if (tag.aliases.isNotEmpty()) {
                                    { Text(tag.aliases.joinToString(", "), style = MaterialTheme.typography.bodySmall) }
                                } else null,
                                leadingContent = {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onToggle(tag.id) }
                                    )
                                },
                                modifier = Modifier.clickable { onToggle(tag.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完了 (${selectedTags.size}件)") }
        }
    )
}

// ====================
// テンプレート選択ダイアログ
// ====================
@Composable
private fun TemplateSelectorDialog(
    templates: List<MaterialAbstractionDictionary.TemplateFrame>,
    selectedTags: Set<String>,
    targetMaterial: String,
    onSelectTemplate: (String, Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<MaterialAbstractionDictionary.TemplateFrame?>(null) }
    var customValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // 選択したタグのファセットに関連するテンプレートを優先表示
    val relevantFacets = selectedTags.mapNotNull { tagId ->
        MaterialAbstractionDictionary.tags.find { it.id == tagId }?.facet
    }.toSet()

    val sortedTemplates = templates.sortedByDescending { template ->
        if (template.facet in relevantFacets) 1 else 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("テンプレートを選択") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedTemplate == null) {
                    // テンプレート一覧
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        items(sortedTemplates) { template ->
                            val isRelevant = template.facet in relevantFacets
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedTemplate = template },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isRelevant)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(template.facet) }
                                        )
                                        if (isRelevant) {
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Star,
                                                "おすすめ",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        template.text,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 変数入力画面
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "テンプレート:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                selectedTemplate!!.text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedTemplate!!.vars.isNotEmpty()) {
                            Text(
                                "変数を入力:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            selectedTemplate!!.vars.forEach { varName ->
                                val defaultValue = when (varName) {
                                    "対象" -> targetMaterial
                                    else -> ""
                                }
                                OutlinedTextField(
                                    value = customValues[varName] ?: defaultValue,
                                    onValueChange = {
                                        customValues = customValues + (varName to it)
                                    },
                                    label = { Text(varName) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = {
                                selectedTemplate = null
                                customValues = emptyMap()
                            }) {
                                Text("戻る")
                            }
                            Button(onClick = {
                                onSelectTemplate(selectedTemplate!!.id, customValues)
                                onDismiss()
                            }) {
                                Text("タグ文を生成")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTemplate == null) {
                TextButton(onClick = onDismiss) { Text("キャンセル") }
            }
        }
    )
}
