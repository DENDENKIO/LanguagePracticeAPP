// app/src/main/java/com/example/languagepracticev3/ui/screens/selfquestioning/trainings/FeatureAbstractionTrainingContent.kt
package com.example.languagepracticev3.ui.screens.selfquestioning.trainings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.*
import com.example.languagepracticev3.viewmodel.FeatureAbstractionTrainingViewModel
import com.example.languagepracticev3.viewmodel.FeatureAbstractionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureAbstractionTrainingContent(
    viewModel: FeatureAbstractionTrainingViewModel = hiltViewModel(),
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
            FATrainingHeader(
                title = if (uiState.currentSession == null) "特徴-抽象変換" else "特徴-抽象変換 (7ステップ)",
                onBack = {
                    if (uiState.currentSession != null) {
                        viewModel.showExitConfirmation()
                    } else {
                        onExitTraining()
                    }
                },
                onShowSessions = { viewModel.showSessionPicker() },
                showSessionsButton = uiState.currentSession != null
            )

            // メインコンテンツ
            if (uiState.currentSession == null) {
                // セッション開始画面
                FASessionStartScreen(
                    sessions = uiState.sessions,
                    onNewSession = { viewModel.startNewSession() },
                    onLoadSession = { viewModel.loadSession(it) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // トレーニングフロー
                FATrainingFlow(
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
                    Text("中断して戻る")
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
        FASessionPickerDialog(
            sessions = uiState.sessions,
            onSelect = { viewModel.loadSession(it) },
            onNewSession = { viewModel.startNewSession() },
            onDelete = { viewModel.deleteSession(it) },
            onDismiss = { viewModel.hideSessionPicker() }
        )
    }

    // 軸セレクターダイアログ
    if (uiState.showAxisSelector) {
        FAAxisSelectorDialog(
            selectedAxes = uiState.selectedAxes,
            onToggleAxis = { viewModel.toggleAxis(it) },
            onDismiss = { viewModel.hideAxisSelector() },
            dictionary = viewModel.dictionary
        )
    }

    // タグセレクターダイアログ
    if (uiState.showTagSelector) {
        FATagSelectorDialog(
            selectedTags = uiState.selectedTags,
            recommendedTags = viewModel.getRecommendedTags(),
            onToggleTag = { viewModel.toggleTag(it) },
            onDismiss = { viewModel.hideTagSelector() },
            dictionary = viewModel.dictionary
        )
    }

    // テンプレートセレクターダイアログ
    if (uiState.showTemplateSelector) {
        FATemplateSelectorDialog(
            onSelectTemplate = { templateId, customValues ->
                viewModel.generateTagSentence(templateId, customValues)
            },
            onDismiss = { viewModel.hideTemplateSelector() },
            dictionary = viewModel.dictionary,
            targetMaterial = uiState.inputTargetMaterial
        )
    }
}

// ====================
// ヘッダー
// ====================
@Composable
private fun FATrainingHeader(
    title: String,
    onBack: () -> Unit,
    onShowSessions: () -> Unit,
    showSessionsButton: Boolean
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
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (showSessionsButton) {
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
private fun FASessionStartScreen(
    sessions: List<FeatureAbstractionSession>,
    onNewSession: () -> Unit,
    onLoadSession: (FeatureAbstractionSession) -> Unit,
    onDeleteSession: (FeatureAbstractionSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "特徴-抽象変換トレーニング",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "7つのステップで物質の特徴から感情を引き出し、抽象語を使わずに表現します。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 7ステップの説明カード
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "7ステップの流れ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FeatureAbstractionStep.entries.forEach { step ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            step.emoji,
                            modifier = Modifier.width(32.dp)
                        )
                        Column {
                            Text(
                                "${step.ordinal + 1}. ${step.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                step.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNewSession,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("新規トレーニングを開始")
        }

        if (sessions.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "過去のセッション",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            sessions.forEach { session ->
                FASessionCard(
                    session = session,
                    onClick = { onLoadSession(session) },
                    onDelete = { onDeleteSession(session) }
                )
            }
        }
    }
}

@Composable
private fun FASessionCard(
    session: FeatureAbstractionSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        ListItem(
            headlineContent = {
                Text(session.sessionTitle.ifBlank { "無題のセッション #${session.id}" })
            },
            supportingContent = {
                Column {
                    val step = FeatureAbstractionStep.entries.getOrElse(session.currentStep) {
                        FeatureAbstractionStep.OBSERVATION
                    }
                    Text(
                        "ステップ: ${step.emoji} ${step.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (session.targetMaterial.isNotBlank()) {
                        Text(
                            "対象: ${session.targetMaterial}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        session.createdAt.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingContent = {
                Icon(
                    if (session.isCompleted) Icons.Default.CheckCircle else Icons.Default.Edit,
                    null,
                    tint = if (session.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "削除")
                }
            }
        )
    }
}

// ====================
// セッションピッカーダイアログ
// ====================
@Composable
private fun FASessionPickerDialog(
    sessions: List<FeatureAbstractionSession>,
    onSelect: (FeatureAbstractionSession) -> Unit,
    onNewSession: () -> Unit,
    onDelete: (FeatureAbstractionSession) -> Unit,
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
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions) { session ->
                            FASessionCard(
                                session = session,
                                onClick = {
                                    onSelect(session)
                                    onDismiss()
                                },
                                onDelete = { onDelete(session) }
                            )
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
// トレーニングフロー
// ====================
@Composable
private fun FATrainingFlow(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ステップインジケーター
        FAStepIndicator(
            currentStep = uiState.currentStep,
            onStepClick = { viewModel.goToStep(it) }
        )

        // メインコンテンツ
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
                    FeatureAbstractionStep.OBSERVATION -> FAStep1_Observation(uiState, viewModel)
                    FeatureAbstractionStep.FEATURE_EXTRACTION -> FAStep2_FeatureExtraction(uiState, viewModel)
                    FeatureAbstractionStep.AXIS_TAG_SELECTION -> FAStep3_AxisTagSelection(uiState, viewModel)
                    FeatureAbstractionStep.CONVERGENCE -> FAStep4_Convergence(uiState, viewModel)
                    FeatureAbstractionStep.ASSOCIATION -> FAStep5_Association(uiState, viewModel)
                    FeatureAbstractionStep.THEME_DECISION -> FAStep6_ThemeDecision(uiState, viewModel)
                    FeatureAbstractionStep.FINAL_EXPRESSION -> FAStep7_FinalExpression(uiState, viewModel)
                }
            }
        }

        // ナビゲーションボタン
        FANavigationButtons(
            currentStep = uiState.currentStep,
            onPrevious = { viewModel.previousStep() },
            onNext = { viewModel.nextStep() },
            onSave = { viewModel.saveSession() },
            onComplete = { viewModel.completeSession() }
        )
    }
}

@Composable
private fun FAStepIndicator(
    currentStep: FeatureAbstractionStep,
    onStepClick: (FeatureAbstractionStep) -> Unit
) {
    val steps = FeatureAbstractionStep.entries
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
            items(steps) { step ->
                val index = step.ordinal
                FilterChip(
                    selected = index == currentIndex,
                    onClick = { onStepClick(step) },
                    label = { Text(step.emoji) },
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

@Composable
private fun FANavigationButtons(
    currentStep: FeatureAbstractionStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    onComplete: () -> Unit
) {
    val isFirstStep = currentStep == FeatureAbstractionStep.OBSERVATION
    val isLastStep = currentStep == FeatureAbstractionStep.FINAL_EXPRESSION

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
// Step 1: 観察
// ====================
@Composable
private fun FAStep1_Observation(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FAGuidanceCard(
            emoji = "👁️",
            title = "対象を5感で観察する",
            description = "身近な物質を選び、感情や意味を付けずに観察します。\n「見たまま」「触ったまま」を言葉にしてください。"
        )

        OutlinedTextField(
            value = uiState.inputTargetMaterial,
            onValueChange = { viewModel.updateTargetMaterial(it) },
            label = { Text("観察対象（物質名）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: りんご、封筒、卵、古い本...") }
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "⚠️ 観察のルール",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "• 意味づけ禁止：「寂しそう」「美しい」などの感情語は使わない\n" +
                            "• 5感を使う：視覚・触覚・聴覚・嗅覚・味覚\n" +
                            "• 写真のように：他人がその場にいなくても想像できるほど具体的に",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputObservationRaw,
            onValueChange = { viewModel.updateObservationRaw(it) },
            label = { Text("観察記録（5感で描写）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            placeholder = {
                Text(
                    "例（りんご）:\n" +
                            "【視覚】直径8cmほど。上部は濃い赤、下部に向かって黄緑のグラデーション。表面に小さな白い点が散在。軸の周りに5本の浅い溝。\n" +
                            "【触覚】表面はつるつるだが、よく触ると微細なざらつきがある。押すと硬く、指は沈まない。持つと冷たい。\n" +
                            "【嗅覚】軸に近づけると甘酸っぱい香り。皮の部分はほぼ無臭。"
                )
            }
        )
    }
}

// ====================
// Step 2: 特徴抽出
// ====================
@Composable
private fun FAStep2_FeatureExtraction(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FAGuidanceCard(
            emoji = "📝",
            title = "特徴を箇条書きで抽出",
            description = "観察から「事実」だけを短い文で列挙します。\n感情や解釈を入れず、物質的な特徴のみを書きます。"
        )

        // 観察記録の参照
        if (uiState.inputObservationRaw.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("観察記録:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        uiState.inputObservationRaw.take(200) +
                                if (uiState.inputObservationRaw.length > 200) "..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text(
            "特徴を5つ抽出してください:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        uiState.inputFeatures.forEachIndexed { index, feature ->
            OutlinedTextField(
                value = feature,
                onValueChange = { viewModel.updateFeature(index, it) },
                label = { Text("特徴 ${index + 1}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        when (index) {
                            0 -> "例: 表面に傷がある"
                            1 -> "例: 一部が変色している"
                            2 -> "例: 軸が乾燥している"
                            3 -> "例: 重さは約200g"
                            4 -> "例: 冷蔵庫から出したばかり"
                            else -> ""
                        }
                    )
                }
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "💡 良い特徴の例",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "✓ 「表面に3本の傷がある」（事実）\n" +
                            "✗ 「傷ついて痛々しい」（感情）\n" +
                            "✓ 「棚の奥に置かれている」（事実）\n" +
                            "✗ 「忘れられている」（解釈）",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ====================
// Step 3: 軸・タグ選択
// ====================
@Composable
private fun FAStep3_AxisTagSelection(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FAGuidanceCard(
            emoji = "🏷️",
            title = "軸とタグを選択してタグ文を生成",
            description = "20軸から特徴に関連する軸を選び、タグを選択して「タグ文」を作ります。\nタグ文は特徴を抽象的な言葉で言い換えたものです。"
        )

        // 選択した軸
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "選択した軸: ${uiState.selectedAxes.size}個",
                style = MaterialTheme.typography.labelMedium
            )
            Button(onClick = { viewModel.showAxisSelector() }) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("軸を選択")
            }
        }

        if (uiState.selectedAxes.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.selectedAxes.toList()) { axisId ->
                    val axis = viewModel.dictionary.getAxisById(axisId)
                    if (axis != null) {
                        AssistChip(
                            onClick = { viewModel.toggleAxis(axisId) },
                            label = { Text("${axis.id}. ${axis.label}") },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // 選択したタグ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "選択したタグ: ${uiState.selectedTags.size}個",
                style = MaterialTheme.typography.labelMedium
            )
            Button(onClick = { viewModel.showTagSelector() }) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("タグを選択")
            }
        }

        if (uiState.selectedTags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "生成したタグ文: ${uiState.generatedTagSentences.size}本",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { viewModel.showTemplateSelector() }) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("テンプレートで生成")
            }
        }

        // カスタムタグ文入力
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.inputCustomTagSentence,
                onValueChange = { viewModel.updateCustomTagSentence(it) },
                label = { Text("カスタムタグ文") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("自由にタグ文を書く...") }
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.addCustomTagSentence(uiState.inputCustomTagSentence) },
                enabled = uiState.inputCustomTagSentence.isNotBlank()
            ) {
                Icon(Icons.Default.Add, "追加")
            }
        }

        // タグ文リスト
        if (uiState.generatedTagSentences.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    uiState.generatedTagSentences.forEachIndexed { index, sentence ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}. $sentence",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.removeTagSentence(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, "削除", Modifier.size(16.dp))
                            }
                        }
                        if (index < uiState.generatedTagSentences.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// ====================
// Step 4: 収束
// ====================
@Composable
private fun FAStep4_Convergence(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FAGuidanceCard(
            emoji = "🎯",
            title = "タグ文を2〜4本に絞り込む",
            description = "生成したタグ文の中から、最も「強い」ものを選びます。\n「強い」とは、感情を引き出す力が強いものです。"
        )

        Text(
            "選択: ${uiState.strongTagSentenceIndices.size}/4本（最低2本）",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (uiState.strongTagSentenceIndices.size >= 2)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error
        )

        if (uiState.generatedTagSentences.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    "⚠️ タグ文がありません。前のステップでタグ文を生成してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            uiState.generatedTagSentences.forEachIndexed { index, sentence ->
                val isSelected = index in uiState.strongTagSentenceIndices
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleStrongTagSentence(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleStrongTagSentence(index) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "タグ文 ${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                sentence,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "💡 選び方のヒント",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "• 読んだとき「何かを感じる」もの\n" +
                            "• 具体的なのに、抽象的な意味を想起させるもの\n" +
                            "• 「不可逆」「境界」「待機」など緊張感のあるもの",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ====================
// Step 5: 連想
// ====================
@Composable
private fun FAStep5_Association(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FAGuidanceCard(
            emoji = "💭",
            title = "タグ文から連想を出す",
            description = "選んだタグ文それぞれから、3〜5個の連想を出します。\n感情・状況・概念など、自由に連想してください。"
        )

        if (uiState.strongTagSentenceIndices.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    "⚠️ 強いタグ文が選択されていません。前のステップで選択してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            uiState.strongTagSentenceIndices.toList().sorted().forEach { sentenceIndex ->
                val sentence = uiState.generatedTagSentences.getOrNull(sentenceIndex) ?: return@forEach

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "タグ文 ${sentenceIndex + 1}:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            sentence,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("連想（3〜5個）:", style = MaterialTheme.typography.labelSmall)

                        val associations = uiState.inputAssociations[sentenceIndex] ?: listOf("", "", "", "", "")
                        associations.take(5).forEachIndexed { assocIndex, assoc ->
                            OutlinedTextField(
                                value = assoc,
                                onValueChange = { viewModel.updateAssociation(sentenceIndex, assocIndex, it) },
                                label = { Text("連想 ${assocIndex + 1}") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = {
                                    Text(
                                        when (assocIndex) {
                                            0 -> "例: 取り返しがつかない"
                                            1 -> "例: 決断の瞬間"
                                            2 -> "例: 覚悟"
                                            else -> ""
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ====================
// Step 6: テーマ決定
// ====================
@Composable
private fun FAStep6_ThemeDecision(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FAGuidanceCard(
            emoji = "✨",
            title = "抽象テーマを1つ決める",
            description = "連想の中から最も強く響くものを選び、テーマを決定します。\nテーマは「期待」「孤独」「喪失」など抽象的な言葉です。"
        )

        // 連想一覧の表示
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "出した連想:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val allAssociations = uiState.inputAssociations.values.flatten().filter { it.isNotBlank() }
                if (allAssociations.isEmpty()) {
                    Text(
                        "連想がありません。前のステップで入力してください。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allAssociations) { assoc ->
                            SuggestionChip(
                                onClick = { viewModel.updateAbstractTheme(assoc) },
                                label = { Text(assoc) }
                            )
                        }
                    }
                }
            }
        }

        // よく使われるテーマ
        Text("よく使われるテーマ:", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.dictionary.commonAbstractThemes) { theme ->
                FilterChip(
                    selected = uiState.inputAbstractTheme == theme,
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
            placeholder = { Text("例: 期待、孤独、喪失、信頼...") }
        )

        // 禁止ワード
        if (uiState.inputAbstractTheme.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "⚠️ 禁止ワード（最終表現で使えない言葉）",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(uiState.inputForbiddenWords) { word ->
                            AssistChip(
                                onClick = { viewModel.removeForbiddenWord(word) },
                                label = {
                                    Text(
                                        word,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var newForbiddenWord by remember { mutableStateOf("") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newForbiddenWord,
                            onValueChange = { newForbiddenWord = it },
                            label = { Text("禁止ワード追加") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
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
                }
            }
        }
    }
}

// ====================
// Step 7: 最終表現
// ====================
@Composable
private fun FAStep7_FinalExpression(
    uiState: FeatureAbstractionUiState,
    viewModel: FeatureAbstractionTrainingViewModel
) {
    val forbiddenWordsUsed = viewModel.checkForbiddenWords()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FAGuidanceCard(
            emoji = "🖊️",
            title = "抽象語を使わずに表現する",
            description = "テーマ「${uiState.inputAbstractTheme.ifBlank { "未定" }}」を、\n禁止ワードを使わずに3〜5行で表現してください。"
        )

        // テーマと禁止ワードの表示
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("テーマ: ", style = MaterialTheme.typography.labelMedium)
                    Text(
                        uiState.inputAbstractTheme.ifBlank { "未定" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "禁止ワード: ${uiState.inputForbiddenWords.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // 禁止ワード違反の警告
        if (forbiddenWordsUsed.isNotEmpty()) {
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
                        Icons.Default.Warning,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "禁止ワードが含まれています: ${forbiddenWordsUsed.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.inputFinalExpression,
            onValueChange = { viewModel.updateFinalExpression(it) },
            label = { Text("最終表現（3〜5行）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            isError = forbiddenWordsUsed.isNotEmpty(),
            placeholder = {
                Text(
                    "例（テーマ: 孤独）:\n\n" +
                            "スーパーの棚の奥に、\n" +
                            "一つだけ残ったりんごがある。\n" +
                            "表面には小さな傷。\n" +
                            "誰の手も、もう伸びてこない。"
                )
            }
        )

        // 文字数と行数
        val lines = uiState.inputFinalExpression.split("\n").filter { it.isNotBlank() }
        Text(
            "行数: ${lines.size}行 / 文字数: ${uiState.inputFinalExpression.length}文字",
            style = MaterialTheme.typography.bodySmall,
            color = if (lines.size in 3..5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        // スコア表示
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "📊 セッションサマリー",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("対象物質: ${uiState.inputTargetMaterial}")
                Text("選択した軸: ${uiState.selectedAxes.size}個")
                Text("選択したタグ: ${uiState.selectedTags.size}個")
                Text("タグ文: ${uiState.generatedTagSentences.size}本")
                Text("強いタグ文: ${uiState.strongTagSentenceIndices.size}本")
                Text("テーマ: ${uiState.inputAbstractTheme}")
            }
        }
    }
}

// ====================
// 共通コンポーネント
// ====================
@Composable
private fun FAGuidanceCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ====================
// 軸セレクターダイアログ
// ====================
@Composable
private fun FAAxisSelectorDialog(
    selectedAxes: Set<Int>,
    onToggleAxis: (Int) -> Unit,
    onDismiss: () -> Unit,
    dictionary: MaterialAbstractionDictionary
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("軸を選択（20軸）") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dictionary.axes) { axis ->
                    val isSelected = axis.id in selectedAxes
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleAxis(axis.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleAxis(axis.id) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "${axis.id}. ${axis.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    axis.definition,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "例: ${axis.examples.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完了 (${selectedAxes.size}個選択)")
            }
        },
        dismissButton = {}
    )
}

// ====================
// タグセレクターダイアログ
// ====================
@Composable
private fun FATagSelectorDialog(
    selectedTags: Set<String>,
    recommendedTags: List<MaterialAbstractionDictionary.Tag>,
    onToggleTag: (String) -> Unit,
    onDismiss: () -> Unit,
    dictionary: MaterialAbstractionDictionary
) {
    var selectedFacet by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("タグを選択") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                // おすすめタグ
                if (recommendedTags.isNotEmpty()) {
                    Text(
                        "おすすめタグ:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(recommendedTags.take(10)) { tag ->
                            FilterChip(
                                selected = tag.id in selectedTags,
                                onClick = { onToggleTag(tag.id) },
                                label = { Text(tag.label, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // ファセット選択
                Text("ファセットで絞り込み:", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(dictionary.facets) { facet ->
                        FilterChip(
                            selected = selectedFacet == facet.key,
                            onClick = {
                                selectedFacet = if (selectedFacet == facet.key) null else facet.key
                            },
                            label = { Text(facet.label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                // タグリスト
                val displayTags = if (selectedFacet != null) {
                    dictionary.getTagsByFacet(selectedFacet!!)
                } else {
                    dictionary.tags.take(50)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(displayTags) { tag ->
                        val isSelected = tag.id in selectedTags
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTag(tag.id) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleTag(tag.id) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    tag.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (tag.aliases.isNotEmpty()) {
                                    Text(
                                        "類義: ${tag.aliases.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完了 (${selectedTags.size}個選択)")
            }
        },
        dismissButton = {}
    )
}

// ====================
// テンプレートセレクターダイアログ
// ====================
@Composable
private fun FATemplateSelectorDialog(
    onSelectTemplate: (String, Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
    dictionary: MaterialAbstractionDictionary,
    targetMaterial: String
) {
    var selectedFacet by remember { mutableStateOf("CORE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("テンプレートでタグ文を生成") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // ファセット選択
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(dictionary.facets) { facet ->
                        FilterChip(
                            selected = selectedFacet == facet.key,
                            onClick = { selectedFacet = facet.key },
                            label = { Text(facet.label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // テンプレートリスト
                val templates = dictionary.getTemplatesByFacet(selectedFacet)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates) { template ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTemplate(template.id, mapOf("対象" to targetMaterial))
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    template.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (template.vars.isNotEmpty()) {
                                    Text(
                                        "変数: ${template.vars.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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
