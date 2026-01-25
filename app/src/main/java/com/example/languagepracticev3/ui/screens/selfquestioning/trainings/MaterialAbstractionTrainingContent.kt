// app/src/main/java/com/example/languagepracticev3/ui/screens/selfquestioning/trainings/MaterialAbstractionTrainingContent.kt
package com.example.languagepracticev3.ui.screens.selfquestioning.trainings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.*
import com.example.languagepracticev3.viewmodel.MaterialAbstractionUiState
import com.example.languagepracticev3.viewmodel.MaterialAbstractionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialAbstractionTrainingContent(
    viewModel: MaterialAbstractionViewModel = hiltViewModel(),
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
                title = "物質-抽象変換",
                onBack = {
                    if (uiState.currentSession != null) {
                        viewModel.showExitConfirmation()
                    } else {
                        onExitTraining()
                    }
                },
                onShowSessions = {
                    if (uiState.currentSession != null) {
                        viewModel.showSessionPicker()
                    }
                },
                showSessionsButton = uiState.currentSession != null
            )

            // メインコンテンツ
            if (uiState.currentSession == null) {
                MaterialAbstractionStartScreen(
                    sessions = uiState.sessions,
                    onNewSession = { viewModel.startNewSession() },
                    onLoadSession = { viewModel.loadSession(it) },
                    onDeleteSession = { viewModel.deleteSession(it) },
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
            onDismiss = { viewModel.hideSessionPicker() }
        )
    }
}

// ====================
// ヘッダー
// ====================
@Composable
private fun MaterialAbstractionHeader(
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
// 開始画面
// ====================
@Composable
private fun MaterialAbstractionStartScreen(
    sessions: List<MaterialAbstractionSession>,
    onNewSession: () -> Unit,
    onLoadSession: (MaterialAbstractionSession) -> Unit,
    onDeleteSession: (MaterialAbstractionSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "物質-抽象変換プロセス",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "日常の身近な物質を観察し、そこから感情を引き出し、言葉で表現するプロセスです。",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 7ステップの概要
                MaterialAbstractionStep.entries.forEach { step ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(step.emoji, modifier = Modifier.width(24.dp))
                        Text(
                            "${step.ordinal + 1}. ${step.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
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
            Text("過去のセッション", style = MaterialTheme.typography.titleSmall)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    MaterialAbstractionSessionCard(
                        session = session,
                        onClick = { onLoadSession(session) },
                        onDelete = { onDeleteSession(session) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialAbstractionSessionCard(
    session: MaterialAbstractionSession,
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
                Text(session.selectedMaterial.ifEmpty { "無題のセッション #${session.id}" })
            },
            supportingContent = {
                Column {
                    Text(
                        "ステップ: ${MaterialAbstractionStep.entries.getOrElse(session.currentStep) { MaterialAbstractionStep.MATERIAL_SELECTION }.displayName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (session.selectedTheme.isNotBlank()) {
                        Text(
                            "テーマ: ${session.selectedTheme}",
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
// トレーニングフロー
// ====================
@Composable
private fun MaterialAbstractionTrainingFlow(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel,
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
                    MaterialAbstractionStep.MATERIAL_SELECTION -> Step1MaterialSelection(uiState, viewModel)
                    MaterialAbstractionStep.OBSERVATION -> Step2Observation(uiState, viewModel)
                    MaterialAbstractionStep.FEATURE_EXTRACTION -> Step3FeatureExtraction(uiState, viewModel)
                    MaterialAbstractionStep.ASSOCIATION -> Step4Association(uiState, viewModel)
                    MaterialAbstractionStep.CONCEPTUALIZATION -> Step5Conceptualization(uiState, viewModel)
                    MaterialAbstractionStep.EXPRESSION_GENERATION -> Step6ExpressionGeneration(uiState, viewModel)
                    MaterialAbstractionStep.RESULT_DISPLAY -> Step7ResultDisplay(uiState, viewModel)
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
            modifier = Modifier.fillMaxWidth(),
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

// ====================
// ステップ1: 物質選択
// ====================
@Composable
private fun Step1MaterialSelection(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = "🔴",
            title = "物質選択 - 思考の起点を決める",
            content = "日常の身近な物質を選んでください。\n触れることができる、物理的に存在するものが対象です。"
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("適切な例:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(
                    "りんご、スプーン、古い本、枯れた花、手紙、ボタン、靴、カップ、石ころ、鍵",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("避けるべき例:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(
                    "愛、幸せ、時間、星（遠すぎる）、光子（物理学の概念）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputMaterial,
            onValueChange = { viewModel.updateInputMaterial(it) },
            label = { Text("観察する物質を入力") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.materialValidationError.isNotBlank(),
            supportingText = if (uiState.materialValidationError.isNotBlank()) {
                { Text(uiState.materialValidationError, color = MaterialTheme.colorScheme.error) }
            } else null,
            placeholder = { Text("例: りんご、古い手紙、空のボトル...") }
        )

        // 選択のヒント
        Text(
            "💡 ヒント: あなたの経験や直感で選んだ物質だからこそ、以降の観察や思考がより深く、より個人的なものになります。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ====================
// ステップ2: 観察フェーズ
// ====================
@Composable
private fun Step2Observation(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = "🟠",
            title = "観察フェーズ - 感覚の目覚め",
            content = "「${uiState.inputMaterial}」を5つの感覚で詳細に観察してください。\n実物がなくても、想像で記述して構いません。"
        )

        // 感覚タブ
        ScrollableTabRow(
            selectedTabIndex = uiState.currentSenseTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            SenseType.entries.forEach { sense ->
                Tab(
                    selected = uiState.currentSenseTab == sense,
                    onClick = { viewModel.selectSenseTab(sense) },
                    text = { Text("${sense.emoji} ${sense.displayName}") }
                )
            }
        }

        // 選択された感覚の入力エリア
        val currentSense = uiState.currentSenseTab

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    currentSense.guidingQuestion,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "例: ${currentSense.examples.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = when (currentSense) {
                SenseType.VISUAL -> uiState.inputObservationVisual
                SenseType.TACTILE -> uiState.inputObservationTactile
                SenseType.AUDITORY -> uiState.inputObservationAuditory
                SenseType.OLFACTORY -> uiState.inputObservationOlfactory
                SenseType.GUSTATORY -> uiState.inputObservationGustatory
            },
            onValueChange = {
                when (currentSense) {
                    SenseType.VISUAL -> viewModel.updateObservationVisual(it)
                    SenseType.TACTILE -> viewModel.updateObservationTactile(it)
                    SenseType.AUDITORY -> viewModel.updateObservationAuditory(it)
                    SenseType.OLFACTORY -> viewModel.updateObservationOlfactory(it)
                    SenseType.GUSTATORY -> viewModel.updateObservationGustatory(it)
                }
            },
            label = { Text("${currentSense.displayName}的観察") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            minLines = 5
        )

        // 入力状況サマリー
        ObservationProgressSummary(uiState)
    }
}

@Composable
private fun ObservationProgressSummary(uiState: MaterialAbstractionUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SenseType.entries.forEach { sense ->
                val hasContent = when (sense) {
                    SenseType.VISUAL -> uiState.inputObservationVisual.isNotBlank()
                    SenseType.TACTILE -> uiState.inputObservationTactile.isNotBlank()
                    SenseType.AUDITORY -> uiState.inputObservationAuditory.isNotBlank()
                    SenseType.OLFACTORY -> uiState.inputObservationOlfactory.isNotBlank()
                    SenseType.GUSTATORY -> uiState.inputObservationGustatory.isNotBlank()
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(sense.emoji)
                    Icon(
                        if (hasContent) Icons.Default.Check else Icons.Default.Remove,
                        null,
                        tint = if (hasContent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ====================
// ステップ3: 特徴抽出
// ====================
@Composable
private fun Step3FeatureExtraction(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = "🟡",
            title = "特徴抽出 - 本質への接近",
            content = "観察した情報から、「${uiState.inputMaterial}」の本質的な特徴を4つの観点で抽出します。"
        )

        // 観点タブ
        ScrollableTabRow(
            selectedTabIndex = uiState.currentFeatureAspect.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureAspect.entries.forEach { aspect ->
                Tab(
                    selected = uiState.currentFeatureAspect == aspect,
                    onClick = { viewModel.selectFeatureAspect(aspect) },
                    text = { Text(aspect.displayName, maxLines = 1) }
                )
            }
        }

        val currentAspect = uiState.currentFeatureAspect

        // 誘導質問カード
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "💭 ${currentAspect.guidingQuestion}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                currentAspect.followUpQuestions.forEach { question ->
                    Text(
                        "• $question",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedTextField(
            value = when (currentAspect) {
                FeatureAspect.FORM_AND_STATE -> uiState.inputFeatureFormState
                FeatureAspect.TIME_PASSAGE -> uiState.inputFeatureTimePassage
                FeatureAspect.POSITION_AND_PLACEMENT -> uiState.inputFeaturePositionPlacement
                FeatureAspect.CUSTOM_FEATURE -> uiState.inputFeatureCustom
            },
            onValueChange = {
                when (currentAspect) {
                    FeatureAspect.FORM_AND_STATE -> viewModel.updateFeatureFormState(it)
                    FeatureAspect.TIME_PASSAGE -> viewModel.updateFeatureTimePassage(it)
                    FeatureAspect.POSITION_AND_PLACEMENT -> viewModel.updateFeaturePositionPlacement(it)
                    FeatureAspect.CUSTOM_FEATURE -> viewModel.updateFeatureCustom(it)
                }
            },
            label = { Text("${currentAspect.displayName}についての回答") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            minLines = 5
        )

        // 入力状況サマリー
        FeatureExtractionProgressSummary(uiState)
    }
}

@Composable
private fun FeatureExtractionProgressSummary(uiState: MaterialAbstractionUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("抽出状況:", style = MaterialTheme.typography.labelSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "形と状態" to uiState.inputFeatureFormState.isNotBlank(),
                    "時間経過" to uiState.inputFeatureTimePassage.isNotBlank(),
                    "位置配置" to uiState.inputFeaturePositionPlacement.isNotBlank(),
                    "カスタム" to uiState.inputFeatureCustom.isNotBlank()
                ).forEach { (name, hasContent) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(name, style = MaterialTheme.typography.labelSmall)
                        Icon(
                            if (hasContent) Icons.Default.Check else Icons.Default.Remove,
                            null,
                            tint = if (hasContent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ====================
// ステップ4: 連想フェーズ
// ====================
@Composable
private fun Step4Association(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = "🟢",
            title = "連想フェーズ - 感情の浮上",
            content = "抽出した特徴から、連想される「感情」や「概念」を記述してください。\nここから具体→抽象への飛躍が起こります。"
        )

        // 各特徴からの連想
        if (uiState.inputFeatureFormState.isNotBlank()) {
            AssociationInputCard(
                title = "形と状態から",
                feature = uiState.inputFeatureFormState.take(50) + "...",
                value = uiState.inputAssociationFromFormState,
                onValueChange = { viewModel.updateAssociationFromFormState(it) }
            )
        }

        if (uiState.inputFeatureTimePassage.isNotBlank()) {
            AssociationInputCard(
                title = "時間経過から",
                feature = uiState.inputFeatureTimePassage.take(50) + "...",
                value = uiState.inputAssociationFromTimePassage,
                onValueChange = { viewModel.updateAssociationFromTimePassage(it) }
            )
        }

        if (uiState.inputFeaturePositionPlacement.isNotBlank()) {
            AssociationInputCard(
                title = "位置と配置から",
                feature = uiState.inputFeaturePositionPlacement.take(50) + "...",
                value = uiState.inputAssociationFromPositionPlacement,
                onValueChange = { viewModel.updateAssociationFromPositionPlacement(it) }
            )
        }

        if (uiState.inputFeatureCustom.isNotBlank()) {
            AssociationInputCard(
                title = "カスタム特徴から",
                feature = uiState.inputFeatureCustom.take(50) + "...",
                value = uiState.inputAssociationFromCustom,
                onValueChange = { viewModel.updateAssociationFromCustom(it) }
            )
        }

        HorizontalDivider()

        // 最強の連想
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🎯 最も強く響く連想を一言で",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "すべての連想の中から、最も深く、最も強く響くものは何ですか？",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputStrongestAssociation,
            onValueChange = { viewModel.updateStrongestAssociation(it) },
            label = { Text("最強の連想（一言で）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 孤独、喪失、期待、儚さ...") }
        )
    }
}

@Composable
private fun AssociationInputCard(
    title: String,
    feature: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                "特徴: $feature",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("連想される感情・概念") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("複数あれば列挙してください") }
            )
        }
    }
}

// ====================
// ステップ5: 概念化フェーズ
// ====================
@Composable
private fun Step5Conceptualization(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = "🔵",
            title = "概念化フェーズ - テーマの確定",
            content = "最強の連想「${uiState.inputStrongestAssociation}」をテーマとして確定します。\n既存のテーマを選ぶか、カスタムテーマを作成してください。"
        )

        // 提案されたテーマ
        if (uiState.suggestedThemes.isNotEmpty()) {
            Text("おすすめのテーマ:", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.suggestedThemes) { theme ->
                    FilterChip(
                        selected = uiState.selectedTheme == theme && !uiState.isCustomTheme,
                        onClick = { viewModel.selectTheme(theme, false) },
                        label = { Text(theme) }
                    )
                }
            }
        }

        // 既存テーマ一覧
        Text("既存のテーマ:", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EmotionThemeDatabase.themes.keys.toList()) { theme ->
                FilterChip(
                    selected = uiState.selectedTheme == theme && !uiState.isCustomTheme,
                    onClick = { viewModel.selectTheme(theme, false) },
                    label = { Text(theme) }
                )
            }
        }

        HorizontalDivider()

        // カスタムテーマ
        Text("または、カスタムテーマを作成:", style = MaterialTheme.typography.labelMedium)

        OutlinedTextField(
            value = if (uiState.isCustomTheme) uiState.selectedTheme else "",
            onValueChange = { viewModel.selectTheme(it, true) },
            label = { Text("カスタムテーマ名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 心の空白、忘れられた約束...") }
        )

        if (uiState.isCustomTheme && uiState.selectedTheme.isNotBlank()) {
            OutlinedTextField(
                value = uiState.inputCustomThemeDefinition,
                onValueChange = { viewModel.updateCustomThemeDefinition(it) },
                label = { Text("テーマの定義（任意）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("このテーマが意味することを簡単に説明") }
            )
        }

        // 選択されたテーマの情報
        if (uiState.selectedTheme.isNotBlank() && !uiState.isCustomTheme) {
            EmotionThemeDatabase.getTheme(uiState.selectedTheme)?.let { themeInfo ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "「${themeInfo.name}」の定義:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(themeInfo.definition, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "次のステップで避けるべき禁止ワード:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            themeInfo.forbiddenWords.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// ====================
// ステップ6: 表現生成
// ====================
@Composable
private fun Step6ExpressionGeneration(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = "🟣",
            title = "表現生成 - 言語化と創作",
            content = "テーマ「${uiState.selectedTheme}」を、禁止ワードを避けながら、物質の具体的な状態を通じて表現してください。（3〜5行、150〜300字推奨）"
        )

        // 禁止ワード警告
        if (uiState.currentForbiddenWords.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "禁止ワード（使用しないでください）:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        uiState.currentForbiddenWords.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // 禁止ワード検出警告
        if (uiState.forbiddenWordWarnings.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "⚠️ 禁止ワード検出: ${uiState.forbiddenWordWarnings.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        // 表現入力エリア
        OutlinedTextField(
            value = uiState.inputGeneratedExpression,
            onValueChange = { viewModel.updateGeneratedExpression(it) },
            label = { Text("あなたの表現") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            placeholder = {
                Text(
                    "「${uiState.inputMaterial}」の具体的な状態を描写することで、\n" +
                            "「${uiState.selectedTheme}」という感情が読者に伝わる文章を書いてください。"
                )
            }
        )

        // リアルタイムフィードバック
        ExpressionFeedbackPanel(uiState)
    }
}

@Composable
private fun ExpressionFeedbackPanel(uiState: MaterialAbstractionUiState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("📊 リアルタイムフィードバック", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // 行数・文字数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("行数: ${uiState.lineCount} (推奨: 3〜5)", style = MaterialTheme.typography.bodySmall)
                Text("文字数: ${uiState.charCount} (推奨: 150〜300)", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 感覚語カウント
            Text("感覚語の使用:", style = MaterialTheme.typography.labelSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "視覚" to (uiState.sensoryWordCounts["visual"] ?: 0),
                    "触覚" to (uiState.sensoryWordCounts["tactile"] ?: 0),
                    "聴覚" to (uiState.sensoryWordCounts["auditory"] ?: 0),
                    "嗅覚" to (uiState.sensoryWordCounts["olfactory"] ?: 0),
                    "味覚" to (uiState.sensoryWordCounts["gustatory"] ?: 0),
                    "比喩" to (uiState.sensoryWordCounts["metaphor"] ?: 0)
                ).forEach { (name, count) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(name, style = MaterialTheme.typography.labelSmall)
                        Text(
                            "$count",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (count > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 禁止ワード状態
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (uiState.forbiddenWordWarnings.isEmpty()) Icons.Default.Check else Icons.Default.Close,
                    null,
                    tint = if (uiState.forbiddenWordWarnings.isEmpty()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (uiState.forbiddenWordWarnings.isEmpty()) "✓ 禁止ワード使用なし"
                    else "⚠️ 禁止ワード検出",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.forbiddenWordWarnings.isEmpty()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ====================
// ステップ7: 結果表示
// ====================
@Composable
private fun Step7ResultDisplay(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = "🟣",
            title = "結果表示 - プロセスの完成",
            content = "おめでとうございます！物質-抽象変換プロセスが完了しました。"
        )

        // 変換サマリー
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "「${uiState.inputMaterial}」",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("↓", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "「${uiState.selectedTheme}」",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 最終表現
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("最終表現:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    uiState.inputGeneratedExpression,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // フィードバック情報
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("フィードバック:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // 禁止ワード状態
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (uiState.forbiddenWordWarnings.isEmpty()) Icons.Default.Check else Icons.Default.Warning,
                        null,
                        tint = if (uiState.forbiddenWordWarnings.isEmpty()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (uiState.forbiddenWordWarnings.isEmpty()) "禁止ワード使用なし"
                        else "禁止ワード検出: ${uiState.forbiddenWordWarnings.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 感覚語の分布
                Text("感覚語の使用分布:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    uiState.sensoryWordCounts.forEach { (sense, count) ->
                        val displayName = when (sense) {
                            "visual" -> "視覚"
                            "tactile" -> "触覚"
                            "auditory" -> "聴覚"
                            "olfactory" -> "嗅覚"
                            "gustatory" -> "味覚"
                            "metaphor" -> "比喩"
                            else -> sense
                        }
                        Text(
                            "$displayName: $count",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // プロセス振り返り
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("プロセスの振り返り:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "このトレーニングを通じて、あなたは「${uiState.inputMaterial}」という具体的な物質から、" +
                            "「${uiState.selectedTheme}」という普遍的な感情を引き出しました。\n\n" +
                            "同じテーマは、他の物質からも到達可能です。" +
                            "これが「物質は異なるが、本質的な感情は同じ」という発見です。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ====================
// 共通コンポーネント
// ====================
@Composable
private fun GuidanceCard(
    emoji: String,
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, style = MaterialTheme.typography.bodySmall)
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
    val isResultStep = currentStep == MaterialAbstractionStep.RESULT_DISPLAY

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = !isFirstStep && !isResultStep
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

        if (isResultStep) {
            Button(
                onClick = { /* セッション終了処理 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.Done, null)
                Spacer(Modifier.width(8.dp))
                Text("終了")
            }
        } else if (currentStep == MaterialAbstractionStep.EXPRESSION_GENERATION) {
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
                                    Text(session.selectedMaterial.ifEmpty { "無題 #${session.id}" })
                                },
                                supportingContent = {
                                    Column {
                                        Text(session.selectedTheme.ifEmpty { "テーマ未設定" })
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
                                    IconButton(onClick = { onDelete(session) }) {
                                        Icon(Icons.Default.Delete, "削除")
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
