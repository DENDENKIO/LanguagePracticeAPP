// app/src/main/java/com/example/languagepracticev3/ui/screens/selfquestioning/trainings/SixHabitsTrainingContent.kt
package com.example.languagepracticev3.ui.screens.selfquestioning.trainings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.MindsetType
import com.example.languagepracticev3.data.model.SixHabitsPracticeTypes
import com.example.languagepracticev3.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SixHabitsTrainingContent(
    viewModel: SixHabitsTrainingViewModel = hiltViewModel(),
    onExitTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // ステータスメッセージ表示用スナックバー
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
            SixHabitsHeader(
                screenState = uiState.screenState,
                selectedMindsetType = uiState.selectedMindsetType,
                onBack = {
                    when (uiState.screenState) {
                        SixHabitsScreenState.HABIT_SELECTION -> onExitTraining()
                        SixHabitsScreenState.PRACTICE_SELECTION -> viewModel.backToHabitSelection()
                        SixHabitsScreenState.TRAINING -> viewModel.showExitConfirmation()
                        SixHabitsScreenState.COMPLETE -> viewModel.backToHabitSelection()
                    }
                }
            )

            // メインコンテンツ
            when (uiState.screenState) {
                SixHabitsScreenState.HABIT_SELECTION -> {
                    HabitSelectionScreen(
                        onSelectHabit = { viewModel.selectMindsetType(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                SixHabitsScreenState.PRACTICE_SELECTION -> {
                    PracticeSelectionScreen(
                        mindsetType = uiState.selectedMindsetType!!,
                        practiceTypes = viewModel.getPracticeTypes(uiState.selectedMindsetType!!),
                        onSelectPractice = { viewModel.selectPracticeType(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
                SixHabitsScreenState.TRAINING -> {
                    TrainingScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
                SixHabitsScreenState.COMPLETE -> {
                    CompleteScreen(
                        onBackToStart = { viewModel.backToHabitSelection() },
                        onExit = onExitTraining,
                        modifier = Modifier.weight(1f)
                    )
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
}

// ====================
// ヘッダー
// ====================
@Composable
private fun SixHabitsHeader(
    screenState: SixHabitsScreenState,
    selectedMindsetType: MindsetType?,
    onBack: () -> Unit
) {
    val title = when (screenState) {
        SixHabitsScreenState.HABIT_SELECTION -> "6つの思考習慣"
        SixHabitsScreenState.PRACTICE_SELECTION -> selectedMindsetType?.displayName ?: ""
        SixHabitsScreenState.TRAINING -> "トレーニング中"
        SixHabitsScreenState.COMPLETE -> "完了"
    }

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
    }
    HorizontalDivider()
}

// ====================
// 習慣選択画面
// ====================
@Composable
private fun HabitSelectionScreen(
    onSelectHabit: (MindsetType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "どの思考習慣を訓練しますか？",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "6つの習慣から1つ選んでください。各習慣には3つの練習メニューがあります。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(MindsetType.entries) { mindsetType ->
            HabitCard(
                mindsetType = mindsetType,
                onClick = { onSelectHabit(mindsetType) }
            )
        }
    }
}

@Composable
private fun HabitCard(
    mindsetType: MindsetType,
    onClick: () -> Unit
) {
    val containerColor = when (mindsetType.number) {
        1 -> MaterialTheme.colorScheme.primaryContainer
        2 -> MaterialTheme.colorScheme.secondaryContainer
        3 -> MaterialTheme.colorScheme.tertiaryContainer
        4 -> MaterialTheme.colorScheme.errorContainer
        5 -> MaterialTheme.colorScheme.surfaceVariant
        6 -> MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 番号
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "${mindsetType.number}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mindsetType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    mindsetType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ====================
// 練習タイプ選択画面
// ====================
@Composable
private fun PracticeSelectionScreen(
    mindsetType: MindsetType,
    practiceTypes: List<PracticeTypeInfo>,
    onSelectPractice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "習慣${mindsetType.number}: ${mindsetType.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        mindsetType.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "練習メニューを選択",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(practiceTypes) { practiceType ->
            PracticeTypeCard(
                practiceType = practiceType,
                onClick = { onSelectPractice(practiceType.type) }
            )
        }
    }
}

@Composable
private fun PracticeTypeCard(
    practiceType: PracticeTypeInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                practiceType.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                practiceType.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ====================
// トレーニング画面
// ====================
@Composable
private fun TrainingScreen(
    uiState: SixHabitsUiState,
    viewModel: SixHabitsTrainingViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ステップインジケーター
        TrainingStepIndicator(currentStep = uiState.currentStep)

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
                    TrainingStep.GUIDE -> GuideStepContent(uiState, viewModel)
                    TrainingStep.INPUT -> InputStepContent(uiState, viewModel)
                    TrainingStep.DEEP_QUESTION -> DeepQuestionStepContent(uiState, viewModel)
                    TrainingStep.REFLECTION -> ReflectionStepContent(uiState, viewModel)
                }
            }
        }

        // ナビゲーションボタン
        TrainingNavigationButtons(
            currentStep = uiState.currentStep,
            onPrevious = { viewModel.previousStep() },
            onNext = { viewModel.nextStep() },
            onSave = { viewModel.saveSession() },
            onComplete = { viewModel.completeSession() }
        )
    }
}

@Composable
private fun TrainingStepIndicator(currentStep: TrainingStep) {
    val steps = TrainingStep.entries
    val currentIndex = currentStep.ordinal

    Column {
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                FilterChip(
                    selected = index == currentIndex,
                    onClick = { },
                    label = { Text(step.displayName) },
                    leadingIcon = if (index < currentIndex) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
    }
}

// ====================
// Step 1: ガイド
// ====================
@Composable
private fun GuideStepContent(
    uiState: SixHabitsUiState,
    viewModel: SixHabitsTrainingViewModel
) {
    val practiceTypes = uiState.selectedMindsetType?.let { viewModel.getPracticeTypes(it) } ?: emptyList()
    val currentPractice = practiceTypes.find { it.type == uiState.selectedPracticeType }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "今回の練習",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    currentPractice?.displayName ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "やり方",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    currentPractice?.guideText ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            "準備ができたら「次へ」を押してください",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ====================
// Step 2: 入力
// ====================
@Composable
private fun InputStepContent(
    uiState: SixHabitsUiState,
    viewModel: SixHabitsTrainingViewModel
) {
    when (uiState.selectedPracticeType) {
        // 習慣①
        SixHabitsPracticeTypes.TITLE_NAMING -> TitleNamingInput(uiState, viewModel)
        SixHabitsPracticeTypes.PERSPECTIVE_SHIFT -> PerspectiveShiftInput(uiState, viewModel)
        SixHabitsPracticeTypes.WHY_CHAIN -> WhyChainInput(uiState, viewModel)
        // 習慣②
        SixHabitsPracticeTypes.NEW_METAPHOR -> NewMetaphorInput(uiState, viewModel)
        SixHabitsPracticeTypes.TRANSFORM_METAPHOR -> TransformMetaphorInput(uiState, viewModel)
        SixHabitsPracticeTypes.ABSTRACT_TO_CONCRETE -> AbstractToConcreteInput(uiState, viewModel)
        // 習慣③
        SixHabitsPracticeTypes.TEN_MINUTE_OBSERVATION -> ObservationInput(uiState, viewModel)
        SixHabitsPracticeTypes.NEGATIVE_SPACE -> NegativeSpaceInput(uiState, viewModel)
        SixHabitsPracticeTypes.QUESTION_TO_OBJECT -> QuestionToObjectInput(uiState, viewModel)
        // 習慣④
        SixHabitsPracticeTypes.THREE_LAYER_RECORD -> ThreeLayerInput(uiState, viewModel)
        SixHabitsPracticeTypes.EMOTION_TO_SENSE -> EmotionToSenseInput(uiState, viewModel)
        SixHabitsPracticeTypes.FAILURE_AS_MATERIAL -> FailureAsMaterialInput(uiState, viewModel)
        // 習慣⑤
        SixHabitsPracticeTypes.SELF_QUESTIONING -> SelfQuestioningInput(uiState, viewModel)
        SixHabitsPracticeTypes.FRIEND_ADVICE -> FriendAdviceInput(uiState, viewModel)
        SixHabitsPracticeTypes.DAILY_SCORING -> DailyScoringInput(uiState, viewModel)
        // 習慣⑥
        SixHabitsPracticeTypes.SACRED_SPACE -> SacredSpaceInput(uiState, viewModel)
        SixHabitsPracticeTypes.START_RITUAL -> StartRitualInput(uiState, viewModel)
        SixHabitsPracticeTypes.END_RITUAL -> EndRitualInput(uiState, viewModel)
        else -> Text("練習タイプが選択されていません")
    }
}

// === 習慣①の入力コンポーネント ===
@Composable
private fun TitleNamingInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("今見えている風景や出来事を描写してください", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputSceneDescription,
            onValueChange = { viewModel.updateSceneDescription(it) },
            label = { Text("シーンの描写") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 5,
            placeholder = { Text("例: 夕暮れ時のカフェ、窓際の席で本を読む女性...") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("このシーンにタイトルをつけるとしたら？", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputSceneTitle,
            onValueChange = { viewModel.updateSceneTitle(it) },
            label = { Text("タイトル") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 「琥珀色の孤独」「ページをめくる指先」") }
        )
    }
}

@Composable
private fun PerspectiveShiftInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("まず、シーンを描写してください", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputSceneDescription,
            onValueChange = { viewModel.updateSceneDescription(it) },
            label = { Text("シーンの描写") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
        HorizontalDivider()
        Text("3つの視点で書き直してください", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputPerspective1,
            onValueChange = { viewModel.updatePerspective1(it) },
            label = { Text("①一人称視点（私は...）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            minLines = 2
        )
        OutlinedTextField(
            value = uiState.inputPerspective2,
            onValueChange = { viewModel.updatePerspective2(it) },
            label = { Text("②三人称視点（彼/彼女は...）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            minLines = 2
        )
        OutlinedTextField(
            value = uiState.inputPerspective3,
            onValueChange = { viewModel.updatePerspective3(it) },
            label = { Text("③物の視点（机/窓/etc.から見ると...）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            minLines = 2
        )
    }
}

@Composable
private fun WhyChainInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("掘り下げたいテーマを書いてください", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputSceneDescription,
            onValueChange = { viewModel.updateSceneDescription(it) },
            label = { Text("テーマ・事象") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 「私はダメな人間だ」「自由になりたい」") }
        )
        HorizontalDivider()
        Text("「なぜ？」を5回繰り返します", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        listOf(
            Triple("なぜ？（1回目）", uiState.inputWhyLevel1, viewModel::updateWhyLevel1),
            Triple("なぜ？（2回目）", uiState.inputWhyLevel2, viewModel::updateWhyLevel2),
            Triple("なぜ？（3回目）", uiState.inputWhyLevel3, viewModel::updateWhyLevel3),
            Triple("なぜ？（4回目）", uiState.inputWhyLevel4, viewModel::updateWhyLevel4),
            Triple("なぜ？（5回目）→本質", uiState.inputWhyLevel5, viewModel::updateWhyLevel5)
        ).forEach { (label, value, onValueChange) ->
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

// === 習慣②の入力コンポーネント ===
@Composable
private fun NewMetaphorInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("比喩にしたい抽象概念を入力", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputTargetConcept,
            onValueChange = { viewModel.updateTargetConcept(it) },
            label = { Text("抽象概念") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 孤独、希望、時間、愛...") }
        )
        HorizontalDivider()
        Text("3つの比喩を考えてください", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputMetaphorAttempt1,
            onValueChange = { viewModel.updateMetaphorAttempt1(it) },
            label = { Text("比喩①") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("○○は△△だ") }
        )
        OutlinedTextField(
            value = uiState.inputMetaphorAttempt2,
            onValueChange = { viewModel.updateMetaphorAttempt2(it) },
            label = { Text("比喩②") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputMetaphorAttempt3,
            onValueChange = { viewModel.updateMetaphorAttempt3(it) },
            label = { Text("比喩③") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TransformMetaphorInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("壊したい既存の比喩", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputOriginalMetaphor,
            onValueChange = { viewModel.updateOriginalMetaphor(it) },
            label = { Text("既存の比喩") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 「心が重い」「希望の光」") }
        )
        HorizontalDivider()
        Text("新しく作り直した比喩", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputTransformedMetaphor,
            onValueChange = { viewModel.updateTransformedMetaphor(it) },
            label = { Text("新しい比喩") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3,
            placeholder = { Text("まったく違う角度から表現してみましょう") }
        )
    }
}

@Composable
private fun AbstractToConcreteInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("具体物に変換したい感情", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputAbstractEmotion,
            onValueChange = { viewModel.updateAbstractEmotion(it) },
            label = { Text("感情") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 悲しみ、喜び、不安、期待...") }
        )
        HorizontalDivider()
        Text("その感情を「物体」として描写", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputConcreteObject,
            onValueChange = { viewModel.updateConcreteObject(it) },
            label = { Text("具体的な描写") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 5,
            placeholder = { Text("形、色、質感、重さ、温度、動きなどを含めて描写してください") }
        )
    }
}

// === 習慣③の入力コンポーネント ===
@Composable
private fun ObservationInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("観察対象を決めてください", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputObservationTarget,
            onValueChange = { viewModel.updateObservationTarget(it) },
            label = { Text("観察対象") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        HorizontalDivider()
        Text("細かく観察して記録", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputObservationShape,
            onValueChange = { viewModel.updateObservationShape(it) },
            label = { Text("形") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputObservationColor,
            onValueChange = { viewModel.updateObservationColor(it) },
            label = { Text("色") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputObservationTexture,
            onValueChange = { viewModel.updateObservationTexture(it) },
            label = { Text("質感") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputObservationOther,
            onValueChange = { viewModel.updateObservationOther(it) },
            label = { Text("その他（動き、変化、音など）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            minLines = 2
        )
    }
}

@Composable
private fun NegativeSpaceInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("観察対象", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputObservationTarget,
            onValueChange = { viewModel.updateObservationTarget(it) },
            label = { Text("対象") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        HorizontalDivider()
        Text("「ないもの」「空白」「周りの空間」に注目", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputNegativeSpace,
            onValueChange = { viewModel.updateNegativeSpace(it) },
            label = { Text("ネガティブスペースの描写") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            minLines = 6,
            placeholder = { Text("対象の周りには何がある？何がない？影は？空間は？") }
        )
    }
}

@Composable
private fun QuestionToObjectInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("対話する対象", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputObservationTarget,
            onValueChange = { viewModel.updateObservationTarget(it) },
            label = { Text("対象") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        HorizontalDivider()
        Text("対象に質問を投げかける", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputQuestionToObject,
            onValueChange = { viewModel.updateQuestionToObject(it) },
            label = { Text("質問") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 「ここに何年いるの？」「何を見てきた？」") }
        )
        Text("対象が答えるとしたら？", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputAnswerFromObject,
            onValueChange = { viewModel.updateAnswerFromObject(it) },
            label = { Text("想像上の答え") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            minLines = 4
        )
    }
}

// === 習慣④の入力コンポーネント ===
@Composable
private fun ThreeLayerInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("最近の経験を3層で記録", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputExperienceFact,
            onValueChange = { viewModel.updateExperienceFact(it) },
            label = { Text("①事実の層（何が起きたか）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
        OutlinedTextField(
            value = uiState.inputExperienceEmotion,
            onValueChange = { viewModel.updateExperienceEmotion(it) },
            label = { Text("②感情の層（どう感じたか）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
        OutlinedTextField(
            value = uiState.inputExperienceUniversal,
            onValueChange = { viewModel.updateExperienceUniversal(it) },
            label = { Text("③普遍の層（誰にでも当てはまる真理は？）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
    }
}

@Composable
private fun EmotionToSenseInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("変換したい感情", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputAbstractEmotion,
            onValueChange = { viewModel.updateAbstractEmotion(it) },
            label = { Text("感情") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        HorizontalDivider()
        Text("3つの感覚で表現", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputEmotionToColor,
            onValueChange = { viewModel.updateEmotionToColor(it) },
            label = { Text("この感情が「色」だとしたら？") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputEmotionToSound,
            onValueChange = { viewModel.updateEmotionToSound(it) },
            label = { Text("この感情が「音」だとしたら？") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputEmotionToTexture,
            onValueChange = { viewModel.updateEmotionToTexture(it) },
            label = { Text("この感情が「触感」だとしたら？") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FailureAsMaterialInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("最近の失敗や後悔", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputFailureDescription,
            onValueChange = { viewModel.updateFailureDescription(it) },
            label = { Text("失敗の描写") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
        HorizontalDivider()
        Text("これが主人公に起きたとしたら？", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputFailureAsStory,
            onValueChange = { viewModel.updateFailureAsStory(it) },
            label = { Text("物語として再構成") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 5,
            placeholder = { Text("この失敗から始まる物語を想像してください") }
        )
    }
}

// === 習慣⑤の入力コンポーネント ===
@Composable
private fun SelfQuestioningInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("自分に問いかける", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputSelfQuestion,
            onValueChange = { viewModel.updateSelfQuestion(it) },
            label = { Text("質問") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 「本当にそう思っている？」「何を恐れている？」") }
        )
        HorizontalDivider()
        Text("正直に答える", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputSelfAnswer,
            onValueChange = { viewModel.updateSelfAnswer(it) },
            label = { Text("答え") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 5
        )
    }
}

@Composable
private fun FriendAdviceInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("今抱えている問題を「友人の問題」として設定", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputFriendProblem,
            onValueChange = { viewModel.updateFriendProblem(it) },
            label = { Text("友人の問題（実は自分の問題）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
        HorizontalDivider()
        Text("その友人にアドバイスするなら？", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputFriendAdvice,
            onValueChange = { viewModel.updateFriendAdvice(it) },
            label = { Text("アドバイス") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 5
        )
    }
}

@Composable
private fun DailyScoringInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("今日1日を10点満点で採点", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..10).forEach { score ->
                FilterChip(
                    selected = uiState.inputDailyScore == score,
                    onClick = { viewModel.updateDailyScore(score) },
                    label = { Text("$score") }
                )
            }
        }
        HorizontalDivider()
        Text("その点数の理由を3つ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputScoreReason1,
            onValueChange = { viewModel.updateScoreReason1(it) },
            label = { Text("理由①") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputScoreReason2,
            onValueChange = { viewModel.updateScoreReason2(it) },
            label = { Text("理由②") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.inputScoreReason3,
            onValueChange = { viewModel.updateScoreReason3(it) },
            label = { Text("理由③") },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        Text("明日への改善点", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputTomorrowImprovement,
            onValueChange = { viewModel.updateTomorrowImprovement(it) },
            label = { Text("改善点") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// === 習慣⑥の入力コンポーネント ===
@Composable
private fun SacredSpaceInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("創作のための「聖域」を設計", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputSacredSpaceLocation,
            onValueChange = { viewModel.updateSacredSpaceLocation(it) },
            label = { Text("場所") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 自室の机、カフェの窓際、図書館の隅...") }
        )
        OutlinedTextField(
            value = uiState.inputSacredSpaceSetup,
            onValueChange = { viewModel.updateSacredSpaceSetup(it) },
            label = { Text("環境設定（照明、音、香り、小物など）") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 5
        )
    }
}

@Composable
private fun StartRitualInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("創作を始める前の「儀式」を設計", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputStartRitualAction,
            onValueChange = { viewModel.updateStartRitualAction(it) },
            label = { Text("行動") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: コーヒーを淹れる、深呼吸を3回、特定の音楽を流す...") }
        )
        OutlinedTextField(
            value = uiState.inputStartRitualMeaning,
            onValueChange = { viewModel.updateStartRitualMeaning(it) },
            label = { Text("その行動に込める意味") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
    }
}

@Composable
private fun EndRitualInput(uiState: SixHabitsUiState, viewModel: SixHabitsTrainingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("創作を終える時の「儀式」を設計", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.inputEndRitualAction,
            onValueChange = { viewModel.updateEndRitualAction(it) },
            label = { Text("行動") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 今日の成果をメモ、机を片付ける、窓を開ける...") }
        )
        OutlinedTextField(
            value = uiState.inputEndRitualMeaning,
            onValueChange = { viewModel.updateEndRitualMeaning(it) },
            label = { Text("その行動に込める意味") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            minLines = 3
        )
    }
}

// ====================
// Step 3: 深掘り
// ====================
@Composable
private fun DeepQuestionStepContent(
    uiState: SixHabitsUiState,
    viewModel: SixHabitsTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "深掘りの質問",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "入力した内容をさらに掘り下げましょう。\n" +
                            "以下の質問に答えることで、より深い気づきが得られます。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 練習タイプに応じた深掘り質問
        when (uiState.selectedMindsetType) {
            MindsetType.WORLD_AS_MATERIAL -> {
                Text("このタイトル/視点の中で、最も「意外」だったものは？", style = MaterialTheme.typography.titleSmall)
                Text("なぜそれが意外だったのか、考えてみましょう。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MindsetType.METAPHOR_TRANSLATION -> {
                Text("作った比喩の中で、最も「新しい」と感じたものは？", style = MaterialTheme.typography.titleSmall)
                Text("それが新しい視点を提供する理由を考えてみましょう。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MindsetType.OBSERVATION_AS_DIALOGUE -> {
                Text("観察を通じて、対象に対する見方が変わりましたか？", style = MaterialTheme.typography.titleSmall)
                Text("変わったとしたら、どのように変わりましたか？", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MindsetType.EXPERIENCE_ALCHEMY -> {
                Text("この経験から、普遍的な真理を見出せましたか？", style = MaterialTheme.typography.titleSmall)
                Text("その真理は、他の人にも当てはまりますか？", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MindsetType.METACOGNITION -> {
                Text("自問自答を通じて、何か発見がありましたか？", style = MaterialTheme.typography.titleSmall)
                Text("自分では気づいていなかったことは？", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MindsetType.ROUTINE_AS_RITUAL -> {
                Text("この儀式を続けることで、どんな効果を期待しますか？", style = MaterialTheme.typography.titleSmall)
                Text("明日から実践できそうですか？", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {}
        }

        OutlinedTextField(
            value = uiState.inputLearning,
            onValueChange = { viewModel.updateLearning(it) },
            label = { Text("深掘りの回答") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            minLines = 6
        )
    }
}

// ====================
// Step 4: 振り返り
// ====================
@Composable
private fun ReflectionStepContent(
    uiState: SixHabitsUiState,
    viewModel: SixHabitsTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "振り返り",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "今回のトレーニングを振り返りましょう。\n" +
                            "気づいたこと、学んだこと、次に活かしたいことを記録します。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputReflection,
            onValueChange = { viewModel.updateReflection(it) },
            label = { Text("振り返りメモ") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
            minLines = 6,
            placeholder = { Text("今回の練習で気づいたこと、感じたこと、次に試したいことなど") }
        )

        Text(
            "「完了」を押すと、このセッションが保存されます。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ====================
// ナビゲーションボタン
// ====================
@Composable
private fun TrainingNavigationButtons(
    currentStep: TrainingStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    onComplete: () -> Unit
) {
    val steps = TrainingStep.entries
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
// 完了画面
// ====================
@Composable
private fun CompleteScreen(
    onBackToStart: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "トレーニング完了！",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "お疲れさまでした。\n継続することで、思考習慣が身についていきます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = onExit) {
                        Text("自問自答メニューへ")
                    }
                    Button(onClick = onBackToStart) {
                        Text("別の習慣を練習")
                    }
                }
            }
        }
    }
}
