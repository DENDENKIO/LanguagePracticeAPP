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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
            TrainingHeader(
                title = when {
                    uiState.selectedCourse == null -> "物質-抽象変換"
                    uiState.currentSession == null -> uiState.selectedCourse!!.displayName
                    else -> uiState.selectedCourse!!.displayName
                },
                onBack = {
                    when {
                        uiState.currentSession != null -> viewModel.showExitConfirmation()
                        uiState.selectedCourse != null -> viewModel.clearCourse()
                        else -> onExitTraining()
                    }
                },
                onShowSessions = { viewModel.showSessionPicker() },
                showSessionsButton = uiState.currentSession != null
            )

            // メインコンテンツ
            when {
                uiState.selectedCourse == null -> {
                    // コース選択画面
                    CourseSelectionScreen(
                        sessions = uiState.sessions,
                        onSelectCourse = { viewModel.selectCourse(it) },
                        onLoadSession = { viewModel.loadSession(it) },
                        onDeleteSession = { viewModel.deleteSession(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                uiState.currentSession == null -> {
                    // セッション開始画面
                    SessionStartScreen(
                        course = uiState.selectedCourse!!,
                        sessions = uiState.sessions.filter { it.courseType == uiState.selectedCourse!!.ordinal },
                        onNewSession = { viewModel.startNewSession() },
                        onLoadSession = { viewModel.loadSession(it) },
                        onDeleteSession = { viewModel.deleteSession(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    // トレーニングフロー
                    when (uiState.selectedCourse) {
                        MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT -> {
                            MaterialToAbstractFlow(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        MaterialAbstractionCourse.ABSTRACT_TO_MATERIAL -> {
                            AbstractToMaterialFlow(
                                uiState = uiState,
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        null -> {}
                    }
                }
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
        SessionPickerDialog(
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
private fun TrainingHeader(
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
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
// コース選択画面
// ====================
@Composable
private fun CourseSelectionScreen(
    sessions: List<MaterialAbstractionSession>,
    onSelectCourse: (MaterialAbstractionCourse) -> Unit,
    onLoadSession: (MaterialAbstractionSession) -> Unit,
    onDeleteSession: (MaterialAbstractionSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "物質-抽象変換トレーニング",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "コースを選択してください",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // コース選択カード
        MaterialAbstractionCourse.entries.forEach { course ->
            CourseCard(
                course = course,
                onClick = { onSelectCourse(course) }
            )
        }

        // 過去のセッション
        if (sessions.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                "過去のセッション",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            sessions.take(5).forEach { session ->
                SessionCard(
                    session = session,
                    onClick = { onLoadSession(session) },
                    onDelete = { onDeleteSession(session) }
                )
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: MaterialAbstractionCourse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (course) {
                MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT ->
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                MaterialAbstractionCourse.ABSTRACT_TO_MATERIAL ->
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                course.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    course.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

// ====================
// セッション開始画面
// ====================
@Composable
private fun SessionStartScreen(
    course: MaterialAbstractionCourse,
    sessions: List<MaterialAbstractionSession>,
    onNewSession: () -> Unit,
    onLoadSession: (MaterialAbstractionSession) -> Unit,
    onDeleteSession: (MaterialAbstractionSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            course.displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // コースの説明とステップ
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    course.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "ステップ:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                when (course) {
                    MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT -> {
                        MaterialToAbstractStep.entries.dropLast(1).forEach { step ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(step.emoji, modifier = Modifier.width(28.dp))
                                Text(
                                    "${step.ordinal + 1}. ${step.displayName}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    MaterialAbstractionCourse.ABSTRACT_TO_MATERIAL -> {
                        AbstractToMaterialStep.entries.dropLast(1).forEach { step ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(step.emoji, modifier = Modifier.width(28.dp))
                                Text(
                                    "${step.ordinal + 1}. ${step.displayName}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
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
                "このコースの過去セッション",
                style = MaterialTheme.typography.titleSmall
            )

            sessions.forEach { session ->
                SessionCard(
                    session = session,
                    onClick = { onLoadSession(session) },
                    onDelete = { onDeleteSession(session) }
                )
            }
        }
    }
}

@Composable
private fun SessionCard(
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
                Text(session.sessionTitle.ifBlank { "無題のセッション #${session.id}" })
            },
            supportingContent = {
                Column {
                    val course = MaterialAbstractionCourse.entries.getOrElse(session.courseType) {
                        MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT
                    }
                    Text(
                        course.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
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
private fun SessionPickerDialog(
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
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions) { session ->
                            SessionCard(
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
// 物質→抽象コース フロー
// ====================
@Composable
private fun MaterialToAbstractFlow(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ステップインジケーター
        M2AStepIndicator(
            currentStep = uiState.m2aCurrentStep,
            onStepClick = { viewModel.goToM2AStep(it) }
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
                when (uiState.m2aCurrentStep) {
                    MaterialToAbstractStep.MATERIAL_SELECTION -> M2A_Step1_MaterialSelection(uiState, viewModel)
                    MaterialToAbstractStep.OBSERVATION -> M2A_Step2_Observation(uiState, viewModel)
                    MaterialToAbstractStep.FEATURE_EXTRACTION -> M2A_Step3_FeatureExtraction(uiState, viewModel)
                    MaterialToAbstractStep.ASSOCIATION -> M2A_Step4_Association(uiState, viewModel)
                    MaterialToAbstractStep.CONCEPTUALIZATION -> M2A_Step5_Conceptualization(uiState, viewModel)
                    MaterialToAbstractStep.EXPRESSION_GENERATION -> M2A_Step6_ExpressionGeneration(uiState, viewModel)
                    MaterialToAbstractStep.RESULT_DISPLAY -> M2A_Step7_ResultDisplay(uiState, viewModel)
                }
            }
        }

        // ナビゲーションボタン
        M2ANavigationButtons(
            currentStep = uiState.m2aCurrentStep,
            onPrevious = { viewModel.previousM2AStep() },
            onNext = { viewModel.nextM2AStep() },
            onSave = { viewModel.saveSession() },
            onFinish = { viewModel.finishTraining() }
        )
    }
}

@Composable
private fun M2AStepIndicator(
    currentStep: MaterialToAbstractStep,
    onStepClick: (MaterialToAbstractStep) -> Unit
) {
    val steps = MaterialToAbstractStep.entries.dropLast(1) // 結果表示を除く
    val currentIndex = currentStep.ordinal

    Column {
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / (steps.size + 1) },
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
private fun M2ANavigationButtons(
    currentStep: MaterialToAbstractStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    onFinish: () -> Unit
) {
    val isFirstStep = currentStep == MaterialToAbstractStep.MATERIAL_SELECTION
    val isResultStep = currentStep == MaterialToAbstractStep.RESULT_DISPLAY

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = !isFirstStep && !isResultStep
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            Spacer(Modifier.width(8.dp))
            Text("戻る")
        }

        if (!isResultStep) {
            Button(onClick = onSave) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("保存")
            }
        }

        if (isResultStep) {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Done, null)
                Spacer(Modifier.width(8.dp))
                Text("終了")
            }
        } else {
            Button(onClick = onNext) {
                Text("次へ")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}

// ====================
// M2A Step 1: 物質選択
// ====================
@Composable
private fun M2A_Step1_MaterialSelection(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = MaterialToAbstractStep.MATERIAL_SELECTION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "${step.displayName} - 思考の起点を決める",
            tips = step.tips
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("✓ 適切な例:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                    "りんご、スプーン、古い本、枯れた花、手紙、ボタン、靴、カップ、石ころ、鍵",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("✗ 避けるべき例:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text(
                    "愛、幸せ、時間、希望（抽象概念）、星（遠すぎる）、光子（物理学の概念）",
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

        InstructionCard(
            text = "💡 あなたの経験や直感で選んだ物質だからこそ、以降の観察や思考がより深く、より個人的なものになります。"
        )
    }
}

// ====================
// M2A Step 2: 観察フェーズ（7項目に細分化）
// ====================
@Composable
private fun M2A_Step2_Observation(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = MaterialToAbstractStep.OBSERVATION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "「${uiState.inputMaterial}」を観察（意味づけ禁止）",
            tips = step.tips
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
                            "• 写真のように：他人がその場にいなくても想像できるほど具体的に\n" +
                            "• 最低5項目は記入してください",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 形
        ObservationInputField(
            label = "形",
            emoji = "📐",
            value = uiState.inputObservationShape,
            onValueChange = { viewModel.updateObservationShape(it) },
            placeholder = "例: 手のひら大、上が少し細く、片側だけ浅くへこんでいる"
        )

        // 色
        ObservationInputField(
            label = "色",
            emoji = "🎨",
            value = uiState.inputObservationColor,
            onValueChange = { viewModel.updateObservationColor(it) },
            placeholder = "例: 赤が強いが、肩に黄緑のグラデーション。表面に小さな白い点が散る"
        )

        // 光
        ObservationInputField(
            label = "光",
            emoji = "✨",
            value = uiState.inputObservationLight,
            onValueChange = { viewModel.updateObservationLight(it) },
            placeholder = "例: 窓際の光が当たり、反射する帯が一本だけ走っている"
        )

        // 触感
        ObservationInputField(
            label = "触感",
            emoji = "✋",
            value = uiState.inputObservationTouch,
            onValueChange = { viewModel.updateObservationTouch(it) },
            placeholder = "例: 指先がすべるほど硬い部分と、へこみの周りだけわずかに柔らかい部分"
        )

        // におい
        ObservationInputField(
            label = "におい",
            emoji = "👃",
            value = uiState.inputObservationSmell,
            onValueChange = { viewModel.updateObservationSmell(it) },
            placeholder = "例: 近づくと、甘い匂いに混じって青い草のような匂いが少し残る"
        )

        // 音
        ObservationInputField(
            label = "音",
            emoji = "👂",
            value = uiState.inputObservationSound,
            onValueChange = { viewModel.updateObservationSound(it) },
            placeholder = "例: 指で軽く弾くと、乾いた高い音が返る"
        )

        // 状況
        ObservationInputField(
            label = "状況",
            emoji = "📍",
            value = uiState.inputObservationContext,
            onValueChange = { viewModel.updateObservationContext(it) },
            placeholder = "例: まな板の端。包丁は横に置かれ、刃先が少しだけりんごの方を向いている"
        )

        // 進捗サマリー
        ObservationProgressCard(uiState)
    }
}

@Composable
private fun ObservationInputField(
    label: String,
    emoji: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                emoji,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (value.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Check,
                    contentDescription = "入力済み",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 1,
            maxLines = 3,
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        )
    }
}

@Composable
private fun ObservationProgressCard(uiState: MaterialAbstractionUiState) {
    val fields = listOf(
        "形" to uiState.inputObservationShape,
        "色" to uiState.inputObservationColor,
        "光" to uiState.inputObservationLight,
        "触感" to uiState.inputObservationTouch,
        "におい" to uiState.inputObservationSmell,
        "音" to uiState.inputObservationSound,
        "状況" to uiState.inputObservationContext
    )
    val filledCount = fields.count { it.second.isNotBlank() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (filledCount >= 5) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "入力状況: $filledCount/7 （最低5項目必要）",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (filledCount >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                fields.forEach { (name, content) ->
                    val hasContent = content.isNotBlank()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            name,
                            style = MaterialTheme.typography.labelSmall
                        )
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
// M2A Step 3: 特徴抽出
// ====================
@Composable
private fun M2A_Step3_FeatureExtraction(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = MaterialToAbstractStep.FEATURE_EXTRACTION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "「${uiState.inputMaterial}」の特徴を抽出",
            tips = step.tips
        )

        // 観点タブ
        ScrollableTabRow(
            selectedTabIndex = uiState.currentFeatureAspect.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureAspect.entries.forEach { aspect ->
                val hasContent = when (aspect) {
                    FeatureAspect.FORM_AND_STATE -> uiState.inputFeatureFormState.isNotBlank()
                    FeatureAspect.TIME_PASSAGE -> uiState.inputFeatureTimePassage.isNotBlank()
                    FeatureAspect.POSITION_AND_PLACEMENT -> uiState.inputFeaturePositionPlacement.isNotBlank()
                    FeatureAspect.CUSTOM_FEATURE -> uiState.inputFeatureCustom.isNotBlank()
                }
                Tab(
                    selected = uiState.currentFeatureAspect == aspect,
                    onClick = { viewModel.selectFeatureAspect(aspect) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(aspect.displayName, maxLines = 1)
                            if (hasContent) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "回答例: ${currentAspect.exampleAnswers.joinToString(" / ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
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
            minLines = 5,
            placeholder = { Text("感情語を使わず、物質的事実として記述してください...") }
        )

        // 進捗
        FeatureExtractionProgressCard(uiState)

        // 注意
        WarningCard(
            text = "⚠️ ここでは感情語（「寂しい」「悲しい」など）を使わず、純粋に物質的事実として特徴を列挙してください。"
        )
    }
}

@Composable
private fun FeatureExtractionProgressCard(uiState: MaterialAbstractionUiState) {
    val filledCount = listOf(
        uiState.inputFeatureFormState,
        uiState.inputFeatureTimePassage,
        uiState.inputFeaturePositionPlacement,
        uiState.inputFeatureCustom
    ).count { it.isNotBlank() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (filledCount >= 2) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "抽出状況: $filledCount/4 （最低2つ必要）",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (filledCount >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    "形と状態" to uiState.inputFeatureFormState.isNotBlank(),
                    "時間経過" to uiState.inputFeatureTimePassage.isNotBlank(),
                    "位置配置" to uiState.inputFeaturePositionPlacement.isNotBlank(),
                    "その他" to uiState.inputFeatureCustom.isNotBlank()
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
// M2A Step 4: 連想フェーズ
// ====================
@Composable
private fun M2A_Step4_Association(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = MaterialToAbstractStep.ASSOCIATION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "特徴から感情・概念を連想",
            tips = step.tips
        )

        // 各特徴からの連想入力
        if (uiState.inputFeatureFormState.isNotBlank()) {
            AssociationInputSection(
                title = "「形と状態」からの連想",
                feature = uiState.inputFeatureFormState,
                value = uiState.inputAssociationFromFormState,
                onValueChange = { viewModel.updateAssociationFromFormState(it) },
                placeholder = "例: 傷がある → 過去の痕跡、完璧さの欠如、歴史を抱えている..."
            )
        }

        if (uiState.inputFeatureTimePassage.isNotBlank()) {
            AssociationInputSection(
                title = "「時間経過」からの連想",
                feature = uiState.inputFeatureTimePassage,
                value = uiState.inputAssociationFromTimePassage,
                onValueChange = { viewModel.updateAssociationFromTimePassage(it) },
                placeholder = "例: 新鮮さを失う → 価値の低下、衰退の始まり、もう二度と戻らない..."
            )
        }

        if (uiState.inputFeaturePositionPlacement.isNotBlank()) {
            AssociationInputSection(
                title = "「位置と配置」からの連想",
                feature = uiState.inputFeaturePositionPlacement,
                value = uiState.inputAssociationFromPositionPlacement,
                onValueChange = { viewModel.updateAssociationFromPositionPlacement(it) },
                placeholder = "例: かごの奥に置かれている → 見落とされている、選ばれない、忘れられている..."
            )
        }

        if (uiState.inputFeatureCustom.isNotBlank()) {
            AssociationInputSection(
                title = "「その他の特徴」からの連想",
                feature = uiState.inputFeatureCustom,
                value = uiState.inputAssociationFromCustom,
                onValueChange = { viewModel.updateAssociationFromCustom(it) },
                placeholder = "例: 誰にも手を伸ばされない → 望まれていない、必要とされていない、孤立..."
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "すべての連想の中から、最も深く、最も強く自分に響くものは何ですか？\nこれが次のステップで「テーマ」になります。",
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

        // サジェスト
        if (uiState.suggestedThemes.isNotEmpty()) {
            Text("関連しそうなテーマ:", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.suggestedThemes) { theme ->
                    SuggestionChip(
                        onClick = { viewModel.updateStrongestAssociation(theme) },
                        label = { Text(theme) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssociationInputSection(
    title: String,
    feature: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                "特徴: ${feature.take(80)}${if (feature.length > 80) "..." else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("連想される感情・概念（3〜5個）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text(placeholder) }
            )
        }
    }
}

// ====================
// M2A Step 5: 概念化フェーズ
// ====================
@Composable
private fun M2A_Step5_Conceptualization(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = MaterialToAbstractStep.CONCEPTUALIZATION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "テーマの確定",
            tips = step.tips
        )

        // 最強の連想の表示
        if (uiState.inputStrongestAssociation.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("最強の連想", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "「${uiState.inputStrongestAssociation}」",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 既存テーマ一覧
        Text("既存のテーマから選択:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EmotionThemeDatabase.getAllThemeNames()) { theme ->
                FilterChip(
                    selected = uiState.selectedTheme == theme && !uiState.isCustomTheme,
                    onClick = { viewModel.selectTheme(theme, false) },
                    label = { Text(theme) }
                )
            }
        }

        HorizontalDivider()

        // カスタムテーマ
        Text("または、カスタムテーマを作成:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = if (uiState.isCustomTheme) uiState.selectedTheme else "",
            onValueChange = { viewModel.selectTheme(it, true) },
            label = { Text("カスタムテーマ名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 心の空白、忘れられた約束...") }
        )

        // 選択されたテーマの情報
        if (uiState.selectedTheme.isNotBlank()) {
            val themeInfo = EmotionThemeDatabase.getTheme(uiState.selectedTheme)

            if (themeInfo != null && !uiState.isCustomTheme) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
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
                            "反対の概念:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            themeInfo.opposites.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "⚠️ 次のステップで避けるべき禁止ワード:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            themeInfo.forbiddenWords.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "💡 参考表現例:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            themeInfo.exampleExpression,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            } else if (uiState.isCustomTheme) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "カスタムテーマ「${uiState.selectedTheme}」",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ 自動生成される禁止ワード:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            uiState.currentForbiddenWords.joinToString(", "),
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
// M2A Step 6: 表現生成
// ====================
@Composable
private fun M2A_Step6_ExpressionGeneration(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = MaterialToAbstractStep.EXPRESSION_GENERATION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "禁止ワードを避けて表現",
            tips = step.tips
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
                        uiState.selectedTheme.ifBlank { "未定" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "⚠️ 禁止ワード: ${uiState.currentForbiddenWords.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // 禁止ワード違反の警告
        if (uiState.forbiddenWordWarnings.isNotEmpty()) {
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
                        "禁止ワードが含まれています: ${uiState.forbiddenWordWarnings.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.inputGeneratedExpression,
            onValueChange = { viewModel.updateGeneratedExpression(it) },
            label = { Text("表現（3〜5行、抽象語禁止）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            isError = uiState.forbiddenWordWarnings.isNotEmpty(),
            placeholder = {
                Text(
                    "例（テーマ: 孤独）:\n\n" +
                            "果物かごの奥底で、他の果物の根元に埋もれたりんご。\n" +
                            "赤と黄色の皮は、もう誰の目にも止まらない。\n" +
                            "かごを空けるたび、新しい果物が上に重ねられていった。"
                )
            }
        )

        // 統計情報
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "📊 統計情報",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("文字数: ${uiState.charCount}文字", style = MaterialTheme.typography.bodySmall)
                Text("行数: ${uiState.lineCount}行", style = MaterialTheme.typography.bodySmall)

                if (uiState.sensoryWordCounts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("感覚語カウント:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        uiState.sensoryWordCounts.forEach { (type, count) ->
                            val label = when (type) {
                                "visual" -> "視覚"
                                "tactile" -> "触覚"
                                "auditory" -> "聴覚"
                                "olfactory" -> "嗅覚"
                                "gustatory" -> "味覚"
                                "metaphor" -> "比喩"
                                else -> type
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "$count",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // チェックリスト
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "✅ 最終確認チェックリスト",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExpressionChecklist.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateChecklistItem(item.id, !uiState.checklistStates[item.id]!!) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.checklistStates[item.id] ?: false,
                            onCheckedChange = { viewModel.updateChecklistItem(item.id, it) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// ====================
// M2A Step 7: 結果表示
// ====================
@Composable
private fun M2A_Step7_ResultDisplay(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "トレーニング完了！",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // サマリー
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📋 サマリー",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("物質", uiState.inputMaterial)
                SummaryRow("テーマ", uiState.selectedTheme)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "完成した表現:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        uiState.inputGeneratedExpression.ifBlank { "（表現なし）" },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // フィードバック
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📊 フィードバック",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("感覚語の使用:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    uiState.sensoryWordCounts.forEach { (type, count) ->
                        val label = when (type) {
                            "visual" -> "👁️視覚"
                            "tactile" -> "✋触覚"
                            "auditory" -> "👂聴覚"
                            "olfactory" -> "👃嗅覚"
                            "gustatory" -> "👅味覚"
                            "metaphor" -> "🔄比喩"
                            else -> type
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                            Text(
                                "$count",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.forbiddenWordWarnings.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("禁止ワードなし！素晴らしい！", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "禁止ワードが含まれています: ${uiState.forbiddenWordWarnings.joinToString(", ")}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        InstructionCard(
            text = "💡 同じテーマに他の物質からもアプローチしてみましょう。\n「物質は異なるが、本質的な感情は同じ」という発見があります。"
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            value.ifBlank { "（未入力）" },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ====================
// 抽象→物質コース フロー
// ====================
@Composable
private fun AbstractToMaterialFlow(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ステップインジケーター
        A2MStepIndicator(
            currentStep = uiState.a2mCurrentStep,
            onStepClick = { viewModel.goToA2MStep(it) }
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
                when (uiState.a2mCurrentStep) {
                    AbstractToMaterialStep.THEME_SELECTION -> A2M_Step1_ThemeSelection(uiState, viewModel)
                    AbstractToMaterialStep.THEME_UNDERSTANDING -> A2M_Step2_ThemeUnderstanding(uiState, viewModel)
                    AbstractToMaterialStep.MATERIAL_CANDIDATES -> A2M_Step3_MaterialCandidates(uiState, viewModel)
                    AbstractToMaterialStep.MATERIAL_DECISION -> A2M_Step4_MaterialDecision(uiState, viewModel)
                    AbstractToMaterialStep.MATERIAL_SPECIFICATION -> A2M_Step5_MaterialSpecification(uiState, viewModel)
                    AbstractToMaterialStep.DESCRIPTION -> A2M_Step6_Description(uiState, viewModel)
                    AbstractToMaterialStep.RESULT_DISPLAY -> A2M_Step7_ResultDisplay(uiState, viewModel)
                }
            }
        }

        // ナビゲーションボタン
        A2MNavigationButtons(
            currentStep = uiState.a2mCurrentStep,
            onPrevious = { viewModel.previousA2MStep() },
            onNext = { viewModel.nextA2MStep() },
            onSave = { viewModel.saveSession() },
            onFinish = { viewModel.finishTraining() }
        )
    }
}

@Composable
private fun A2MStepIndicator(
    currentStep: AbstractToMaterialStep,
    onStepClick: (AbstractToMaterialStep) -> Unit
) {
    val steps = AbstractToMaterialStep.entries.dropLast(1)
    val currentIndex = currentStep.ordinal

    Column {
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / (steps.size + 1) },
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
private fun A2MNavigationButtons(
    currentStep: AbstractToMaterialStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    onFinish: () -> Unit
) {
    val isFirstStep = currentStep == AbstractToMaterialStep.THEME_SELECTION
    val isResultStep = currentStep == AbstractToMaterialStep.RESULT_DISPLAY

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = !isFirstStep && !isResultStep
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            Spacer(Modifier.width(8.dp))
            Text("戻る")
        }

        if (!isResultStep) {
            Button(onClick = onSave) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("保存")
            }
        }

        if (isResultStep) {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Done, null)
                Spacer(Modifier.width(8.dp))
                Text("終了")
            }
        } else {
            Button(onClick = onNext) {
                Text("次へ")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}

// ====================
// A2M Step 1: テーマ選択
// ====================
@Composable
private fun A2M_Step1_ThemeSelection(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = AbstractToMaterialStep.THEME_SELECTION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = step.displayName,
            tips = step.tips
        )

        // ランダムボタン
        Button(
            onClick = { viewModel.selectRandomTheme() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Shuffle, null)
            Spacer(Modifier.width(8.dp))
            Text("ランダムにテーマを選ぶ")
        }

        HorizontalDivider()

        // テーマ一覧
        Text("テーマを選択:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EmotionThemeDatabase.getAllThemeNames()) { theme ->
                FilterChip(
                    selected = uiState.selectedTheme == theme && !uiState.isCustomTheme,
                    onClick = { viewModel.selectTheme(theme, false) },
                    label = { Text(theme) }
                )
            }
        }

        HorizontalDivider()

        // カスタムテーマ
        Text("または、カスタムテーマを入力:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = if (uiState.isCustomTheme) uiState.selectedTheme else "",
            onValueChange = { viewModel.selectTheme(it, true) },
            label = { Text("カスタムテーマ名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 心の空白、忘れられた約束...") }
        )

        // 選択されたテーマの表示
        if (uiState.selectedTheme.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("選択されたテーマ", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "「${uiState.selectedTheme}」",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ====================
// A2M Step 2: テーマ理解
// ====================
@Composable
private fun A2M_Step2_ThemeUnderstanding(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = AbstractToMaterialStep.THEME_UNDERSTANDING

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "「${uiState.selectedTheme}」を理解する",
            tips = step.tips
        )

        OutlinedTextField(
            value = uiState.inputThemeDefinition,
            onValueChange = { viewModel.updateThemeDefinition(it) },
            label = { Text("テーマの定義（あなたの言葉で）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 4,
            placeholder = { Text("このテーマは私にとって...") }
        )

        OutlinedTextField(
            value = uiState.inputThemeOrigin,
            onValueChange = { viewModel.updateThemeOrigin(it) },
            label = { Text("テーマの由来・きっかけ") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3,
            placeholder = { Text("いつ、どんな時にこの感情を感じましたか？") }
        )

        OutlinedTextField(
            value = uiState.inputThemeOpposites,
            onValueChange = { viewModel.updateThemeOpposites(it) },
            label = { Text("反対の概念") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 孤独の反対 → 繋がり、愛、承認...") }
        )

        OutlinedTextField(
            value = uiState.inputThemeCharacteristics,
            onValueChange = { viewModel.updateThemeCharacteristics(it) },
            label = { Text("テーマの特徴・性質") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3,
            placeholder = { Text("このテーマにはどんな側面がありますか？") }
        )
    }
}

// ====================
// A2M Step 3: 物質候補
// ====================
@Composable
private fun A2M_Step3_MaterialCandidates(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = AbstractToMaterialStep.MATERIAL_CANDIDATES

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "「${uiState.selectedTheme}」を象徴する物質候補",
            tips = step.tips
        )

        // 5つの候補入力
        (0 until 5).forEach { index ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "候補 ${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = when (index) {
                            0 -> uiState.inputMaterialCandidate1
                            1 -> uiState.inputMaterialCandidate2
                            2 -> uiState.inputMaterialCandidate3
                            3 -> uiState.inputMaterialCandidate4
                            4 -> uiState.inputMaterialCandidate5
                            else -> ""
                        },
                        onValueChange = { viewModel.updateMaterialCandidate(index, it) },
                        label = { Text("物質名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("例: 未開封の封筒、発芽前の種子...") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = when (index) {
                            0 -> uiState.inputCandidateReason1
                            1 -> uiState.inputCandidateReason2
                            2 -> uiState.inputCandidateReason3
                            3 -> uiState.inputCandidateReason4
                            4 -> uiState.inputCandidateReason5
                            else -> ""
                        },
                        onValueChange = { viewModel.updateCandidateReason(index, it) },
                        label = { Text("選んだ理由") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("この物質がテーマを象徴する理由...") }
                    )
                }
            }
        }

        // 進捗
        val filledCount = listOf(
            uiState.inputMaterialCandidate1,
            uiState.inputMaterialCandidate2,
            uiState.inputMaterialCandidate3,
            uiState.inputMaterialCandidate4,
            uiState.inputMaterialCandidate5
        ).count { it.isNotBlank() }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (filledCount >= 3) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                "候補数: $filledCount/5 （最低3つ必要）",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp),
                color = if (filledCount >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ====================
// A2M Step 4: 物質決定
// ====================
@Composable
private fun A2M_Step4_MaterialDecision(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = AbstractToMaterialStep.MATERIAL_DECISION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "最適な物質を1つ選択",
            tips = step.tips
        )

        // 候補一覧
        val candidates = listOf(
            uiState.inputMaterialCandidate1,
            uiState.inputMaterialCandidate2,
            uiState.inputMaterialCandidate3,
            uiState.inputMaterialCandidate4,
            uiState.inputMaterialCandidate5
        )

        candidates.forEachIndexed { index, candidate ->
            if (candidate.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectChosenMaterial(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.chosenMaterialIndex == index)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (uiState.chosenMaterialIndex == index)
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.chosenMaterialIndex == index,
                            onClick = { viewModel.selectChosenMaterial(index) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                candidate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val reason = when (index) {
                                0 -> uiState.inputCandidateReason1
                                1 -> uiState.inputCandidateReason2
                                2 -> uiState.inputCandidateReason3
                                3 -> uiState.inputCandidateReason4
                                4 -> uiState.inputCandidateReason5
                                else -> ""
                            }
                            if (reason.isNotBlank()) {
                                Text(
                                    reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.chosenMaterialIndex >= 0) {
            OutlinedTextField(
                value = uiState.inputChosenMaterialReason,
                onValueChange = { viewModel.updateChosenMaterialReason(it) },
                label = { Text("この物質を選んだ決め手") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                minLines = 3,
                placeholder = { Text("他の候補と比べて、なぜこれが最適なのか...") }
            )
        }
    }
}

// ====================
// A2M Step 5: 物質の具体化
// ====================
@Composable
private fun A2M_Step5_MaterialSpecification(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = AbstractToMaterialStep.MATERIAL_SPECIFICATION
    val chosenMaterial = when (uiState.chosenMaterialIndex) {
        0 -> uiState.inputMaterialCandidate1
        1 -> uiState.inputMaterialCandidate2
        2 -> uiState.inputMaterialCandidate3
        3 -> uiState.inputMaterialCandidate4
        4 -> uiState.inputMaterialCandidate5
        else -> ""
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "「$chosenMaterial」を具体化",
            tips = step.tips
        )

        OutlinedTextField(
            value = uiState.inputMaterialState,
            onValueChange = { viewModel.updateMaterialState(it) },
            label = { Text("物質の状態（テーマを最もよく表す状態）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 4,
            placeholder = { Text("例: 卵なら「割られる直前」「殻にヒビが入っている」など") }
        )

        OutlinedTextField(
            value = uiState.inputMaterialContext,
            onValueChange = { viewModel.updateMaterialContext(it) },
            label = { Text("状況・文脈（いつ、どこで）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3,
            placeholder = { Text("例: 朝の台所、誰かの手の中、店頭に陳列...") }
        )

        OutlinedTextField(
            value = uiState.inputMaterialCondition,
            onValueChange = { viewModel.updateMaterialCondition(it) },
            label = { Text("条件・制約（時間軸、天候、周囲の物など）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3,
            placeholder = { Text("例: 朝日が差し込む、周りには他の卵がない...") }
        )
    }
}

// ====================
// A2M Step 6: 描写
// ====================
@Composable
private fun A2M_Step6_Description(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val step = AbstractToMaterialStep.DESCRIPTION

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GuidanceCard(
            emoji = step.emoji,
            title = "物質を描写（感情語禁止）",
            tips = step.tips
        )

        // テーマと禁止ワード
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("テーマ: ${uiState.selectedTheme}", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "⚠️ 禁止ワード: ${uiState.currentForbiddenWords.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // 禁止ワード警告
        if (uiState.forbiddenWordWarnings.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "禁止ワードが含まれています: ${uiState.forbiddenWordWarnings.joinToString(", ")}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.inputGeneratedExpression,
            onValueChange = { viewModel.updateGeneratedExpression(it) },
            label = { Text("描写（3〜5行）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            isError = uiState.forbiddenWordWarnings.isNotEmpty(),
            placeholder = { Text("5つの感覚を使い、物質の具体的な状態を描写してください...") }
        )

        // 統計
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("文字数: ${uiState.charCount} / 行数: ${uiState.lineCount}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ====================
// A2M Step 7: 結果表示
// ====================
@Composable
private fun A2M_Step7_ResultDisplay(
    uiState: MaterialAbstractionUiState,
    viewModel: MaterialAbstractionViewModel
) {
    val chosenMaterial = when (uiState.chosenMaterialIndex) {
        0 -> uiState.inputMaterialCandidate1
        1 -> uiState.inputMaterialCandidate2
        2 -> uiState.inputMaterialCandidate3
        3 -> uiState.inputMaterialCandidate4
        4 -> uiState.inputMaterialCandidate5
        else -> ""
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "トレーニング完了！",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // サマリー
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📋 サマリー",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("テーマ", uiState.selectedTheme)
                SummaryRow("選んだ物質", chosenMaterial)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "完成した描写:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        uiState.inputGeneratedExpression.ifBlank { "（描写なし）" },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        InstructionCard(
            text = "💡 逆方向のトレーニング（物質→抽象）も試してみましょう。\n思考の柔軟性がさらに高まります。"
        )
    }
}

// ====================
// 共通コンポーネント
// ====================
@Composable
private fun GuidanceCard(
    emoji: String,
    title: String,
    tips: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            tips.forEach { tip ->
                Text(
                    "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InstructionCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun WarningCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
