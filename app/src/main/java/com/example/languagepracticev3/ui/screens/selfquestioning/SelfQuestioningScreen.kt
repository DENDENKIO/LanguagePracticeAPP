// app/src/main/java/com/example/languagepracticev3/ui/screens/selfquestioning/SelfQuestioningScreen.kt
package com.example.languagepracticev3.ui.screens.selfquestioning

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.GlobalRevisionSession
import com.example.languagepracticev3.data.model.GlobalRevisionStep
import com.example.languagepracticev3.ui.screens.selfquestioning.trainings.SixHabitsTrainingContent
import com.example.languagepracticev3.ui.screens.selfquestioning.trainings.AbstractionTrainingContent
import com.example.languagepracticev3.ui.screens.selfquestioning.trainings.MaterialAbstractionTrainingContent
import com.example.languagepracticev3.ui.screens.selfquestioning.trainings.FeatureAbstractionTrainingContent  // ★追加
import com.example.languagepracticev3.viewmodel.SelfQuestioningMode
import com.example.languagepracticev3.viewmodel.SelfQuestioningUiState
import com.example.languagepracticev3.viewmodel.SelfQuestioningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfQuestioningScreen(
    viewModel: SelfQuestioningViewModel = hiltViewModel()
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
        topBar = {
            TopAppBar(
                title = { Text("自問自答") },
                actions = {
                    if (uiState.selectedMode == SelfQuestioningMode.GLOBAL_REVISION &&
                        uiState.currentSession != null) {
                        IconButton(onClick = { viewModel.showSessionPicker() }) {
                            Icon(Icons.Default.FolderOpen, "セッション一覧")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 左側パネル (1/3) - モード選択
            LeftPanel(
                selectedMode = uiState.selectedMode,
                onSelectMode = { viewModel.selectMode(it) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            VerticalDivider()

            // 右側パネル (2/3) - トレーニング画面
            RightPanel(
                uiState = uiState,
                viewModel = viewModel,
                onExitTraining = { viewModel.clearMode() },
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            )
        }
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
// 左側パネル
// ====================
@Composable
private fun LeftPanel(
    selectedMode: SelfQuestioningMode,
    onSelectMode: (SelfQuestioningMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "トレーニング選択",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "左のボタンを押して、トレーニングを開始してください",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6つの思考習慣ボタン
        ElevatedButton(
            onClick = { onSelectMode(SelfQuestioningMode.SIX_HABITS) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (selectedMode == SelfQuestioningMode.SIX_HABITS)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "6つの思考習慣",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "表現者の脳を作る訓練",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // グローバル・リビジョン ボタン
        ElevatedButton(
            onClick = { onSelectMode(SelfQuestioningMode.GLOBAL_REVISION) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (selectedMode == SelfQuestioningMode.GLOBAL_REVISION)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "グローバル・リビジョン",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "文章の核を磨く推敲",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 抽象化テクニック ボタン
        ElevatedButton(
            onClick = { onSelectMode(SelfQuestioningMode.ABSTRACTION) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (selectedMode == SelfQuestioningMode.ABSTRACTION)
                    MaterialTheme.colorScheme.tertiaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "抽象化テクニック",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "具体⇔抽象の往復訓練",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 物質-抽象変換 ボタン（2コース版）
        ElevatedButton(
            onClick = { onSelectMode(SelfQuestioningMode.MATERIAL_ABSTRACTION) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (selectedMode == SelfQuestioningMode.MATERIAL_ABSTRACTION)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Transform,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "物質-抽象変換",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "具体から感情を引き出す",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ★追加: 特徴-抽象変換 ボタン（7ステップ版）
        ElevatedButton(
            onClick = { onSelectMode(SelfQuestioningMode.FEATURE_ABSTRACTION) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = if (selectedMode == SelfQuestioningMode.FEATURE_ABSTRACTION)
                    MaterialTheme.colorScheme.inversePrimary
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "特徴-抽象変換",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "7ステップで感情を引き出す",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 説明カード
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                when (selectedMode) {
                    SelfQuestioningMode.SIX_HABITS -> {
                        Text(
                            "6つの思考習慣とは",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "脳科学・熟達研究に基づく、表現者の思考様式を訓練します。\n" +
                                    "①素材として見る ②比喩で翻訳 ③観察=対話 ④経験の錬金術 ⑤メタ認知 ⑥儀式としてのルーティン",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    SelfQuestioningMode.GLOBAL_REVISION -> {
                        Text(
                            "グローバル・リビジョンとは",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "文章を「書き直す」というより、新しい文章を設計し直す認知的活動です。" +
                                    "熟練者は「核から始める」ことで、文章の品質を飛躍的に高めます。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    SelfQuestioningMode.ABSTRACTION -> {
                        Text(
                            "抽象化テクニックとは",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "「具体」と「抽象」を意識的に往復させることで、" +
                                    "表面的な描写から多層的な意味を持つ文章へと進化させます。\n" +
                                    "Show, Don't Tell、メタファー、感覚的詳細を活用します。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    SelfQuestioningMode.MATERIAL_ABSTRACTION -> {
                        Text(
                            "物質-抽象変換とは",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "日常の身近な物質を観察し、その特徴から感情を引き出し、" +
                                    "禁止ワードを避けながら表現するプロセスです。\n" +
                                    "2つのコース（物質→抽象 / 抽象→物質）を選べます。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    // ★追加: 特徴-抽象変換の説明
                    SelfQuestioningMode.FEATURE_ABSTRACTION -> {
                        Text(
                            "特徴-抽象変換とは",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "7つのステップで物質の特徴から感情を引き出します:\n" +
                                    "①観察 ②特徴抽出 ③軸・タグ選択 ④収束 ⑤連想 ⑥テーマ決定 ⑦抽象語禁止で表現\n" +
                                    "仕様書(JSON.txt)に基づく20軸・200タグを活用します。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    SelfQuestioningMode.NONE -> {
                        Text(
                            "トレーニングを選択",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "上のボタンから、実行したいトレーニングを選んでください。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// ====================
// 右側パネル
// ====================
@Composable
private fun RightPanel(
    uiState: SelfQuestioningUiState,
    viewModel: SelfQuestioningViewModel,
    onExitTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState.selectedMode) {
        SelfQuestioningMode.NONE -> {
            // 何も選択されていない場合
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "左側からトレーニングを選択してください",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        SelfQuestioningMode.GLOBAL_REVISION -> {
            GlobalRevisionTrainingContent(
                uiState = uiState,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        SelfQuestioningMode.SIX_HABITS -> {
            // 6つの思考習慣トレーニング
            SixHabitsTrainingContent(
                onExitTraining = onExitTraining,
                modifier = modifier
            )
        }
        SelfQuestioningMode.ABSTRACTION -> {
            // 抽象化テクニック
            AbstractionTrainingContent(
                onExitTraining = onExitTraining,
                modifier = modifier
            )
        }
        SelfQuestioningMode.MATERIAL_ABSTRACTION -> {
            // 物質-抽象変換（2コース版）
            MaterialAbstractionTrainingContent(
                onExitTraining = onExitTraining,
                modifier = modifier
            )
        }
        // ★追加: 特徴-抽象変換（7ステップ版）
        SelfQuestioningMode.FEATURE_ABSTRACTION -> {
            FeatureAbstractionTrainingContent(
                onExitTraining = onExitTraining,
                modifier = modifier
            )
        }
    }
}

// ====================
// グローバル・リビジョン トレーニング画面
// ====================
@Composable
private fun GlobalRevisionTrainingContent(
    uiState: SelfQuestioningUiState,
    viewModel: SelfQuestioningViewModel,
    modifier: Modifier = Modifier
) {
    if (uiState.currentSession == null) {
        // セッション未開始
        SessionStartScreen(
            sessions = uiState.sessions,
            onNewSession = { viewModel.startNewSession() },
            onLoadSession = { viewModel.loadSession(it) },
            onDeleteSession = { viewModel.deleteSession(it) },
            modifier = modifier
        )
    } else {
        // セッション進行中
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ステップインジケーター
            StepIndicator(
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
                        GlobalRevisionStep.CORE_DEFINITION -> CoreDefinitionStep(uiState, viewModel)
                        GlobalRevisionStep.DETECTION -> DetectionStep(uiState, viewModel)
                        GlobalRevisionStep.DIAGNOSIS -> DiagnosisStep(uiState, viewModel)
                        GlobalRevisionStep.REVISION_PLAN -> RevisionPlanStep(uiState, viewModel)
                        GlobalRevisionStep.REVERSE_OUTLINE -> ReverseOutlineStep(uiState, viewModel)
                    }
                }
            }

            // ナビゲーションボタン
            NavigationButtons(
                currentStep = uiState.currentStep,
                onPrevious = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() },
                onSave = { viewModel.saveSession() },
                onComplete = { viewModel.completeSession() }
            )
        }
    }
}

// ====================
// セッション開始画面
// ====================
@Composable
private fun SessionStartScreen(
    sessions: List<GlobalRevisionSession>,
    onNewSession: () -> Unit,
    onLoadSession: (GlobalRevisionSession) -> Unit,
    onDeleteSession: (GlobalRevisionSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "グローバル・リビジョン",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Hayesらの研究に基づく、熟練者の推敲プロセスを体験します。\n" +
                    "5つのステップで文章の「核」を磨いていきましょう。",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onNewSession,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("新規セッションを開始")
        }

        if (sessions.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "過去のセッション",
                style = MaterialTheme.typography.titleSmall
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(
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
private fun SessionCard(
    session: GlobalRevisionSession,
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
                Text(
                    session.workTitle.ifEmpty { "無題のセッション #${session.id}" }
                )
            },
            supportingContent = {
                Column {
                    Text(
                        "ステップ: ${GlobalRevisionStep.entries.getOrElse(session.currentStep) { GlobalRevisionStep.CORE_DEFINITION }.displayName}",
                        style = MaterialTheme.typography.bodySmall
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
// ステップインジケーター
// ====================
@Composable
private fun StepIndicator(
    currentStep: GlobalRevisionStep,
    onStepClick: (GlobalRevisionStep) -> Unit
) {
    val steps = GlobalRevisionStep.entries
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
// Step 1: 核の文を決める
// ====================
@Composable
private fun CoreDefinitionStep(
    uiState: SelfQuestioningUiState,
    viewModel: SelfQuestioningViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 作品タイトル
        OutlinedTextField(
            value = uiState.inputWorkTitle,
            onValueChange = { viewModel.updateWorkTitle(it) },
            label = { Text("作品タイトル") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 元のテキスト
        OutlinedTextField(
            value = uiState.inputOriginalText,
            onValueChange = { viewModel.updateOriginalText(it) },
            label = { Text("元の文章（推敲対象）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            minLines = 5
        )

        HorizontalDivider()

        // 核の文
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "質問1: この文章で、いちばん伝えたいことを一文で書くとしたら？",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        OutlinedTextField(
            value = uiState.inputCoreSentence,
            onValueChange = { viewModel.updateCoreSentence(it) },
            label = { Text("核の文（一文で）") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 「雨の日のバス停で感じた、理由の分からない孤独について」") }
        )

        // 主題
        OutlinedTextField(
            value = uiState.inputCoreTheme,
            onValueChange = { viewModel.updateCoreTheme(it) },
            label = { Text("主題（何について書いているか）") },
            modifier = Modifier.fillMaxWidth()
        )

        // 中心感情
        OutlinedTextField(
            value = uiState.inputCoreEmotion,
            onValueChange = { viewModel.updateCoreEmotion(it) },
            label = { Text("中心感情・態度（その出来事に対してどう感じたか）") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 「さびしいけれど少し心地よい」") }
        )

        // 読者に渡したいもの
        OutlinedTextField(
            value = uiState.inputCoreTakeaway,
            onValueChange = { viewModel.updateCoreTakeaway(it) },
            label = { Text("読者に渡したい変化・問い") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例: 「孤独は必ずしも悪いものではないかもしれない」") }
        )
    }
}

// ====================
// Step 2: 問題の検出
// ====================
@Composable
private fun DetectionStep(
    uiState: SelfQuestioningUiState,
    viewModel: SelfQuestioningViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "批判的読解のガイド",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "自分の文章を、まるで初見の読者が読むかのように読みましょう:\n" +
                            "• 「これ、何を言いたいのかな？」\n" +
                            "• 「読んで楽しい？」\n" +
                            "• 「どこで混乱する？」",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 元のテキスト（参照用）
        if (uiState.inputOriginalText.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("元の文章:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        uiState.inputOriginalText.take(300) +
                                if (uiState.inputOriginalText.length > 300) "..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.inputDetectedProblems,
            onValueChange = { viewModel.updateDetectedProblems(it) },
            label = { Text("検出した問題（箇条書き）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            placeholder = {
                Text(
                    "例:\n" +
                            "✗ 冒頭の「寒い」だけでは、孤独の質が伝わらない\n" +
                            "✗ 3段落目の「人がたくさん歩いている」が、孤独感を弱めている\n" +
                            "✗ 結末が「バスが来た」で終わり、感情の変化がない"
                )
            }
        )
    }
}

// ====================
// Step 3: 問題の診断
// ====================
@Composable
private fun DiagnosisStep(
    uiState: SelfQuestioningUiState,
    viewModel: SelfQuestioningViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "4レベルの診断",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "検出した問題が、どこから来るのかを分析します",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 内容レベル
        OutlinedTextField(
            value = uiState.inputDiagnosisContent,
            onValueChange = { viewModel.updateDiagnosisContent(it) },
            label = { Text("内容レベル: 主題は明確か？核となる感情が薄いか？") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3
        )

        // 構成レベル
        OutlinedTextField(
            value = uiState.inputDiagnosisStructure,
            onValueChange = { viewModel.updateDiagnosisStructure(it) },
            label = { Text("構成レベル: 段落の順番は論理的か？バランスは？") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3
        )

        // 読者レベル
        OutlinedTextField(
            value = uiState.inputDiagnosisReader,
            onValueChange = { viewModel.updateDiagnosisReader(it) },
            label = { Text("読者レベル: 読者を正しく見積もっているか？") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3
        )

        // 文体レベル
        OutlinedTextField(
            value = uiState.inputDiagnosisStyle,
            onValueChange = { viewModel.updateDiagnosisStyle(it) },
            label = { Text("文体レベル: トーンは統一されているか？") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp),
            minLines = 3
        )
    }
}

// ====================
// Step 4: 大規模修正案
// ====================
@Composable
private fun RevisionPlanStep(
    uiState: SelfQuestioningUiState,
    viewModel: SelfQuestioningViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "修正案のフォーマット",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "【対象】→【変更前】→【変更後】→【理由】の形式で記述",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputRevisionPlans,
            onValueChange = { viewModel.updateRevisionPlans(it) },
            label = { Text("修正案") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8,
            placeholder = {
                Text(
                    "例:\n" +
                            "【冒頭の修正】\n" +
                            "変更前:「寒い」だけ\n" +
                            "変更後:「流れからはじき出されているような気がした」\n" +
                            "理由: 孤独の本質が伝わる比喩に変更"
                )
            }
        )

        OutlinedTextField(
            value = uiState.inputRevisionPriority,
            onValueChange = { viewModel.updateRevisionPriority(it) },
            label = { Text("優先順位") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 4,
            placeholder = {
                Text(
                    "例:\n" +
                            "【優先度1】結末の変更 - 核となる感情の変化を形作る\n" +
                            "【優先度2】冒頭の比喩変更 - 読者の心をつかむ入口を強化\n" +
                            "【優先度3】3段落の対比強化 - 中盤の説得力を高める"
                )
            }
        )
    }
}

// ====================
// Step 5: リバース・アウトライン
// ====================
@Composable
private fun ReverseOutlineStep(
    uiState: SelfQuestioningUiState,
    viewModel: SelfQuestioningViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "リバース・アウトラインの手順",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "1. 各段落の要点を1文でメモ\n" +
                            "2. 縦に並べて眺め、核に向かって進んでいるか確認\n" +
                            "3. 脱線・重複・順序の問題を発見",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputReverseOutline,
            onValueChange = { viewModel.updateReverseOutline(it) },
            label = { Text("段落ごとの要点") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            minLines = 6,
            placeholder = {
                Text(
                    "例:\n" +
                            "段落1の要点: 「朝8時、駅のベンチに座った」\n" +
                            "段落2の要点: 「周りに人がたくさんいた」\n" +
                            "段落3の要点: 「バスが来ない」"
                )
            }
        )

        OutlinedTextField(
            value = uiState.inputStructureNotes,
            onValueChange = { viewModel.updateStructureNotes(it) },
            label = { Text("構成に関するメモ（問題点・改善案）") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 4
        )

        HorizontalDivider()

        Text(
            "推敲後のテキスト",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.inputRevisedText,
            onValueChange = { viewModel.updateRevisedText(it) },
            label = { Text("推敲後の文章") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            minLines = 8
        )
    }
}

// ====================
// ナビゲーションボタン
// ====================
@Composable
private fun NavigationButtons(
    currentStep: GlobalRevisionStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    onComplete: () -> Unit
) {
    val steps = GlobalRevisionStep.entries
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
private fun SessionPickerDialog(
    sessions: List<GlobalRevisionSession>,
    onSelect: (GlobalRevisionSession) -> Unit,
    onNewSession: () -> Unit,
    onDelete: (GlobalRevisionSession) -> Unit,
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
                                    Text(session.workTitle.ifEmpty { "無題 #${session.id}" })
                                },
                                supportingContent = {
                                    Text(session.createdAt.take(10))
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
