// app/src/main/java/com/example/languagepracticev3/ui/screens/selfquestioning/trainings/AbstractionTrainingContent.kt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagepracticev3.data.model.AbstractionSession
import com.example.languagepracticev3.data.model.AbstractionStep
import com.example.languagepracticev3.data.model.SensoryGuide
import com.example.languagepracticev3.data.model.MetaphorGuide
import com.example.languagepracticev3.viewmodel.AbstractionTrainingViewModel
import com.example.languagepracticev3.viewmodel.AbstractionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbstractionTrainingContent(
    viewModel: AbstractionTrainingViewModel = hiltViewModel(),
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
            // ヘッダー with 戻るボタン
            TrainingHeader(
                title = "抽象化テクニック",
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
                // セッション未開始
                AbstractionSessionStartScreen(
                    sessions = uiState.sessions,
                    onNewSession = { viewModel.startNewSession() },
                    onLoadSession = { viewModel.loadSession(it) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // セッション進行中
                AbstractionTrainingFlow(
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
            text = { Text("保存されていない変更は失われます。中断前に保存することをお勧めします。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmExit()
                        onExitTraining()
                    }
                ) {
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
        AbstractionSessionPickerDialog(
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
private fun AbstractionSessionStartScreen(
    sessions: List<AbstractionSession>,
    onNewSession: () -> Unit,
    onLoadSession: (AbstractionSession) -> Unit,
    onDeleteSession: (AbstractionSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "抽象化テクニック トレーニング",
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
                    "このトレーニングでは、以下の5ステップで文章の「具体」と「抽象」を往復します：",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. 具体的な情景を書く", style = MaterialTheme.typography.bodySmall)
                Text("2. つっこみを入れる（本質を問う）", style = MaterialTheme.typography.bodySmall)
                Text("3. 抽象化する", style = MaterialTheme.typography.bodySmall)
                Text("4. 感覚的詳細を追加（Show, Don't Tell）", style = MaterialTheme.typography.bodySmall)
                Text("5. メタファーを検討する", style = MaterialTheme.typography.bodySmall)
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
                style = MaterialTheme.typography.titleSmall
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    AbstractionSessionCard(
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
private fun AbstractionSessionCard(
    session: AbstractionSession,
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
                    session.sessionTitle.ifEmpty { "無題のセッション #${session.id}" }
                )
            },
            supportingContent = {
                Column {
                    Text(
                        "ステップ: ${AbstractionStep.entries.getOrElse(session.currentStep) { AbstractionStep.CONCRETE_SCENE }.displayName}",
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
// トレーニングフロー
// ====================
@Composable
private fun AbstractionTrainingFlow(
    uiState: AbstractionUiState,
    viewModel: AbstractionTrainingViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ステップインジケーター
        AbstractionStepIndicator(
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
                    AbstractionStep.CONCRETE_SCENE -> ConcreteSceneStep(uiState, viewModel)
                    AbstractionStep.DEEP_QUESTIONING -> DeepQuestioningStep(uiState, viewModel)
                    AbstractionStep.ABSTRACTION -> AbstractionStepContent(uiState, viewModel)
                    AbstractionStep.SENSORY_DETAILS -> SensoryDetailsStep(uiState, viewModel)
                    AbstractionStep.METAPHOR -> MetaphorStep(uiState, viewModel)
                }
            }
        }

        // ナビゲーションボタン
        AbstractionNavigationButtons(
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
private fun AbstractionStepIndicator(
    currentStep: AbstractionStep,
    onStepClick: (AbstractionStep) -> Unit
) {
    val steps = AbstractionStep.entries
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
// Step 1: 具体的な情景
// ====================
@Composable
private fun ConcreteSceneStep(
    uiState: AbstractionUiState,
    viewModel: AbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "💡 ガイド",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "まずは、書きたい場面を具体的に描写してください。\n" +
                            "「誰が、いつ、どこで、何をしたか」を意識すると書きやすくなります。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedTextField(
            value = uiState.inputConcreteScene,
            onValueChange = { viewModel.updateConcreteScene(it) },
            label = { Text("具体的な情景を書く") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            minLines = 6,
            placeholder = {
                Text("例: 夜中の3時、私は一人きりの駅のベンチに座っていた。電車は来ない。駅員もいない。")
            }
        )

        HorizontalDivider()

        Text(
            "補助入力（任意）",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.inputSceneWho,
                onValueChange = { viewModel.updateSceneWho(it) },
                label = { Text("誰が") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.inputSceneWhen,
                onValueChange = { viewModel.updateSceneWhen(it) },
                label = { Text("いつ") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.inputSceneWhere,
                onValueChange = { viewModel.updateSceneWhere(it) },
                label = { Text("どこで") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.inputSceneWhat,
                onValueChange = { viewModel.updateSceneWhat(it) },
                label = { Text("何を") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

// ====================
// Step 2: つっこみを入れる
// ====================
@Composable
private fun DeepQuestioningStep(
    uiState: AbstractionUiState,
    viewModel: AbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🔍 つっこみを入れる",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "書いた情景に対して、自分自身に質問を投げかけます。\n" +
                            "「本当に？」「なぜ？」「具体的には？」と問い続けることで、本質に近づきます。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 書いた情景の参照
        if (uiState.inputConcreteScene.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("あなたが書いた情景:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        uiState.inputConcreteScene.take(200) +
                                if (uiState.inputConcreteScene.length > 200) "..." else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        HorizontalDivider()

        // 本質を問う質問
        Text(
            "本質を問う質問",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.inputQuestionWhatItShows,
            onValueChange = { viewModel.updateQuestionWhatItShows(it) },
            label = { Text("これは何を示しているのか？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputQuestionWhyImpressive,
            onValueChange = { viewModel.updateQuestionWhyImpressive(it) },
            label = { Text("なぜそれが印象に残ったのか？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputQuestionWhatToFeel,
            onValueChange = { viewModel.updateQuestionWhatToFeel(it) },
            label = { Text("読者に何を感じてほしいのか？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        HorizontalDivider()

        // 深掘り質問
        Text(
            "深掘り質問（さらに具体化）",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.inputQuestionWhoDecided,
            onValueChange = { viewModel.updateQuestionWhoDecided(it) },
            label = { Text("誰がそう決めたの？（自分？他者？）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.inputQuestionByWhatStandard,
            onValueChange = { viewModel.updateQuestionByWhatStandard(it) },
            label = { Text("どんな基準で？") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.inputQuestionSpecifically,
            onValueChange = { viewModel.updateQuestionSpecifically(it) },
            label = { Text("具体的には？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}

// ====================
// Step 3: 抽象化する
// ====================
@Composable
private fun AbstractionStepContent(
    uiState: AbstractionUiState,
    viewModel: AbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "✨ 抽象化する",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "つっこみの回答から、この場面が象徴する「本質」を抽出します。\n" +
                            "他の場面にも当てはまる、より普遍的な表現を目指しましょう。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 前ステップの回答を参照
        if (uiState.inputQuestionWhatItShows.isNotBlank() ||
            uiState.inputQuestionWhyImpressive.isNotBlank()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("つっこみの回答:", style = MaterialTheme.typography.labelSmall)
                    if (uiState.inputQuestionWhatItShows.isNotBlank()) {
                        Text("• ${uiState.inputQuestionWhatItShows}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (uiState.inputQuestionWhyImpressive.isNotBlank()) {
                        Text("• ${uiState.inputQuestionWhyImpressive}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        OutlinedTextField(
            value = uiState.inputAbstractedSentence,
            onValueChange = { viewModel.updateAbstractedSentence(it) },
            label = { Text("この場面が象徴する本質を一文で") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            placeholder = { Text("例: 「孤立というものを初めて感覚した時間」") }
        )

        HorizontalDivider()

        OutlinedTextField(
            value = uiState.inputCoreTheme,
            onValueChange = { viewModel.updateCoreTheme(it) },
            label = { Text("主題（何について書いているか）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: 孤独、喪失、希望...") }
        )

        OutlinedTextField(
            value = uiState.inputCoreEmotion,
            onValueChange = { viewModel.updateCoreEmotion(it) },
            label = { Text("中心感情・態度") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例: さびしいけれど少し心地よい") }
        )
    }
}

// ====================
// Step 4: 感覚的詳細
// ====================
@Composable
private fun SensoryDetailsStep(
    uiState: AbstractionUiState,
    viewModel: AbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "👁️ Show, Don't Tell",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "抽象化した概念を、5つの感覚を使って「体験できる」形に戻します。\n" +
                            "すべての感覚を使う必要はありません。効果的なものを選んでください。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 5つの感覚入力
        SensoryGuide.prompts.forEach { prompt ->
            Column {
                Text(
                    "${prompt.sense}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    prompt.question,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = when (prompt.sense) {
                        "視覚" -> uiState.inputSensoryVisual
                        "聴覚" -> uiState.inputSensoryAuditory
                        "触覚" -> uiState.inputSensoryTactile
                        "嗅覚" -> uiState.inputSensoryOlfactory
                        "味覚" -> uiState.inputSensoryGustatory
                        else -> ""
                    },
                    onValueChange = {
                        when (prompt.sense) {
                            "視覚" -> viewModel.updateSensoryVisual(it)
                            "聴覚" -> viewModel.updateSensoryAuditory(it)
                            "触覚" -> viewModel.updateSensoryTactile(it)
                            "嗅覚" -> viewModel.updateSensoryOlfactory(it)
                            "味覚" -> viewModel.updateSensoryGustatory(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text(prompt.example) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        HorizontalDivider()

        // POV（視点）の確認
        Text(
            "視点（POV）の確認",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = uiState.inputPovCharacter,
            onValueChange = { viewModel.updatePovCharacter(it) },
            label = { Text("この場面を見ているのは誰か？") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.inputPovFocus,
            onValueChange = { viewModel.updatePovFocus(it) },
            label = { Text("その人は、何に注意を向けるか？") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.inputPovIgnore,
            onValueChange = { viewModel.updatePovIgnore(it) },
            label = { Text("その人は、何を見落とすか？") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

// ====================
// Step 5: メタファー
// ====================
@Composable
private fun MetaphorStep(
    uiState: AbstractionUiState,
    viewModel: AbstractionTrainingViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "🎨 メタファーを検討する",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "この体験を別の何かに例えてみましょう。\n" +
                            "複数のメタファーを出して、最も「新しい視点を提供する」ものを選びます。",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 陳腐な比喩の警告
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "⚠️ 避けたい陳腐な比喩",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    MetaphorGuide.avoidClicheList.joinToString("、"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 抽象化した内容の参照
        if (uiState.inputAbstractedSentence.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("抽象化した本質:", style = MaterialTheme.typography.labelSmall)
                    Text(uiState.inputAbstractedSentence, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        HorizontalDivider()

        Text(
            "メタファー候補を3つ考えてみましょう",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        // メタファー候補入力
        listOf(
            Triple(1, uiState.inputMetaphorCandidate1, viewModel::updateMetaphorCandidate1),
            Triple(2, uiState.inputMetaphorCandidate2, viewModel::updateMetaphorCandidate2),
            Triple(3, uiState.inputMetaphorCandidate3, viewModel::updateMetaphorCandidate3)
        ).forEach { (index, value, onValueChange) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = uiState.selectedMetaphor == index,
                    onClick = { viewModel.selectMetaphor(index) }
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("候補$index") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = {
                        Text(
                            when (index) {
                                1 -> "自然現象に例えると？"
                                2 -> "日常の物体に例えると？"
                                3 -> "場所や動きに例えると？"
                                else -> ""
                            }
                        )
                    }
                )
            }
        }

        if (uiState.selectedMetaphor > 0) {
            OutlinedTextField(
                value = uiState.inputMetaphorReason,
                onValueChange = { viewModel.updateMetaphorReason(it) },
                label = { Text("選択理由（なぜこのメタファーが新しい視点を提供するか）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

// ====================
// ナビゲーションボタン
// ====================
@Composable
private fun AbstractionNavigationButtons(
    currentStep: AbstractionStep,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit,
    onComplete: () -> Unit
) {
    val steps = AbstractionStep.entries
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
private fun AbstractionSessionPickerDialog(
    sessions: List<AbstractionSession>,
    onSelect: (AbstractionSession) -> Unit,
    onNewSession: () -> Unit,
    onDelete: (AbstractionSession) -> Unit,
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