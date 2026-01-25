// app/src/main/java/com/example/languagepracticev3/ui/screens/selfquestioning/trainings/SixHabitsTraining.kt
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
import com.example.languagepracticev3.data.model.MindsetType
import com.example.languagepracticev3.data.model.SixHabitsPracticeTypes
import com.example.languagepracticev3.viewmodel.SixHabitsUiState
import com.example.languagepracticev3.viewmodel.SixHabitsViewModel

/**
 * 6つの思考習慣トレーニング画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SixHabitsTrainingContent(
    modifier: Modifier = Modifier,
    viewModel: SixHabitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // スナックバー
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
                .padding(16.dp)
        ) {
            when {
                uiState.selectedMindset == null -> {
                    // マインドセット選択画面
                    MindsetSelectionContent(
                        uiState = uiState,
                        onSelectMindset = { viewModel.selectMindset(it) }
                    )
                }
                uiState.selectedPracticeType == null -> {
                    // 練習タイプ選択画面
                    PracticeTypeSelectionContent(
                        mindset = uiState.selectedMindset!!,
                        onSelectPracticeType = { viewModel.selectPracticeType(it) },
                        onBack = { viewModel.clearSelection() }
                    )
                }
                else -> {
                    // トレーニング実行画面
                    TrainingExecutionContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onBack = { viewModel.clearSelection() }
                    )
                }
            }
        }
    }
}

// ====================
// マインドセット選択画面
// ====================
@Composable
private fun MindsetSelectionContent(
    uiState: SixHabitsUiState,
    onSelectMindset: (MindsetType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "6つの思考習慣",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "脳科学・熟達研究に基づく、表現者の思考習慣を訓練します。\n" +
                    "習得したいマインドセットを選んでください。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 日次進捗サマリー
        uiState.todayTracking?.let { tracking ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("今日の進捗", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "タイトル: ${tracking.titleCount}個 | 比喩: ${tracking.newMetaphorCount}個 | 観察: ${tracking.observationMinutes}分",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MindsetType.entries) { mindset ->
                MindsetCard(
                    mindset = mindset,
                    materialCount = uiState.materialStats[mindset.name.lowercase()] ?: 0,
                    onClick = { onSelectMindset(mindset) }
                )
            }
        }
    }
}

@Composable
private fun MindsetCard(
    mindset: MindsetType,
    materialCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アイコン
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${mindset.number}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    mindset.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    mindset.description,
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
private fun PracticeTypeSelectionContent(
    mindset: MindsetType,
    onSelectPracticeType: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 戻るボタン + タイトル
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "戻る")
            }
            Text(
                "${mindset.number}. ${mindset.displayName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "トレーニングタイプを選んでください",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val practiceTypes = getPracticeTypesForMindset(mindset)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(practiceTypes) { (type, name, description) ->
                PracticeTypeCard(
                    name = name,
                    description = description,
                    onClick = { onSelectPracticeType(type) }
                )
            }
        }
    }
}

@Composable
private fun PracticeTypeCard(
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
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

private fun getPracticeTypesForMindset(mindset: MindsetType): List<Triple<String, String, String>> {
    return when (mindset) {
        MindsetType.WORLD_AS_MATERIAL -> listOf(
            Triple(SixHabitsPracticeTypes.TITLE_NAMING, "シーンにタイトルをつける", "日常の場面に文学的なタイトルをつける練習"),
            Triple(SixHabitsPracticeTypes.PERSPECTIVE_SHIFT, "3つの視点で見る", "一人称・三人称・物の視点から同じ場面を描写"),
            Triple(SixHabitsPracticeTypes.WHY_CHAIN, "「なぜ？」を5回", "感じたことの深層を探る内省的質問")
        )
        MindsetType.METAPHOR_TRANSLATION -> listOf(
            Triple(SixHabitsPracticeTypes.NEW_METAPHOR, "新しい比喩を作る", "日常の現象を新しい比喩で表現する"),
            Triple(SixHabitsPracticeTypes.TRANSFORM_METAPHOR, "既存比喩を壊して作り直す", "陳腐な比喩をより独創的なものに変換"),
            Triple(SixHabitsPracticeTypes.ABSTRACT_TO_CONCRETE, "抽象→具体変換", "抽象的な感情を具体物に変換")
        )
        MindsetType.OBSERVATION_AS_DIALOGUE -> listOf(
            Triple(SixHabitsPracticeTypes.TEN_MINUTE_OBSERVATION, "10分観察", "1つの物を10分間深く観察"),
            Triple(SixHabitsPracticeTypes.NEGATIVE_SPACE, "ネガティブスペース", "物と物の間の空間を観察"),
            Triple(SixHabitsPracticeTypes.QUESTION_TO_OBJECT, "対象に質問する", "観察対象に質問を投げかける")
        )
        MindsetType.EXPERIENCE_ALCHEMY -> listOf(
            Triple(SixHabitsPracticeTypes.THREE_LAYER_RECORD, "3層記録", "事実→感情→普遍の3層で経験を記録"),
            Triple(SixHabitsPracticeTypes.EMOTION_TO_SENSE, "感情→五感変換", "感情を色・音・触感に変換"),
            Triple(SixHabitsPracticeTypes.FAILURE_AS_MATERIAL, "失敗を素材に", "失敗を物語の素材として再構成")
        )
        MindsetType.METACOGNITION -> listOf(
            Triple(SixHabitsPracticeTypes.SELF_QUESTIONING, "自問自答", "今何をしているか、なぜかを自問"),
            Triple(SixHabitsPracticeTypes.FRIEND_ADVICE, "友人へのアドバイス", "自分の悩みを友人への相談として考える"),
            Triple(SixHabitsPracticeTypes.DAILY_SCORING, "今日の採点", "今日の自分を10点満点で評価")
        )
        MindsetType.ROUTINE_AS_RITUAL -> listOf(
            Triple(SixHabitsPracticeTypes.SACRED_SPACE, "聖域の設計", "創作専用の空間を設計・記録"),
            Triple(SixHabitsPracticeTypes.START_RITUAL, "始まりの儀式", "創作開始前の儀式を設計・実行"),
            Triple(SixHabitsPracticeTypes.END_RITUAL, "終わりの儀式", "創作終了後の儀式を設計・実行")
        )
    }
}

// ====================
// トレーニング実行画面
// ====================
@Composable
private fun TrainingExecutionContent(
    uiState: SixHabitsUiState,
    viewModel: SixHabitsViewModel,
    onBack: () -> Unit
) {
    val mindset = uiState.selectedMindset ?: return
    val practiceType = uiState.selectedPracticeType ?: return

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ヘッダー
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "戻る")
            }
            Column {
                Text(
                    "${mindset.number}. ${mindset.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    getPracticeTypeName(practiceType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // トレーニング内容
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (practiceType) {
                    // マインドセット①
                    SixHabitsPracticeTypes.TITLE_NAMING -> TitleNamingTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.PERSPECTIVE_SHIFT -> PerspectiveShiftTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.WHY_CHAIN -> WhyChainTraining(uiState, viewModel)
                    // マインドセット②
                    SixHabitsPracticeTypes.NEW_METAPHOR -> NewMetaphorTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.TRANSFORM_METAPHOR -> TransformMetaphorTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.ABSTRACT_TO_CONCRETE -> AbstractToConcreteTraining(uiState, viewModel)
                    // マインドセット③
                    SixHabitsPracticeTypes.TEN_MINUTE_OBSERVATION -> TenMinuteObservationTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.NEGATIVE_SPACE -> NegativeSpaceTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.QUESTION_TO_OBJECT -> QuestionToObjectTraining(uiState, viewModel)
                    // マインドセット④
                    SixHabitsPracticeTypes.THREE_LAYER_RECORD -> ThreeLayerRecordTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.EMOTION_TO_SENSE -> EmotionToSenseTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.FAILURE_AS_MATERIAL -> FailureAsMaterialTraining(uiState, viewModel)
                    // マインドセット⑤
                    SixHabitsPracticeTypes.SELF_QUESTIONING -> SelfQuestioningTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.FRIEND_ADVICE -> FriendAdviceTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.DAILY_SCORING -> DailyScoringTraining(uiState, viewModel)
                    // マインドセット⑥
                    SixHabitsPracticeTypes.SACRED_SPACE -> SacredSpaceTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.START_RITUAL -> StartRitualTraining(uiState, viewModel)
                    SixHabitsPracticeTypes.END_RITUAL -> EndRitualTraining(uiState, viewModel)
                }
            }
        }

        // ナビゲーションボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("キャンセル")
            }

            Button(onClick = { viewModel.saveSession() }) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("保存")
            }

            Button(
                onClick = { viewModel.completeSession() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("完了")
            }
        }
    }
}

private fun getPracticeTypeName(practiceType: String): String {
    return when (practiceType) {
        SixHabitsPracticeTypes.TITLE_NAMING -> "シーンにタイトルをつける"
        SixHabitsPracticeTypes.PERSPECTIVE_SHIFT -> "3つの視点で見る"
        SixHabitsPracticeTypes.WHY_CHAIN -> "「なぜ？」を5回"
        SixHabitsPracticeTypes.NEW_METAPHOR -> "新しい比喩を作る"
        SixHabitsPracticeTypes.TRANSFORM_METAPHOR -> "既存比喩を壊して作り直す"
        SixHabitsPracticeTypes.ABSTRACT_TO_CONCRETE -> "抽象→具体変換"
        SixHabitsPracticeTypes.TEN_MINUTE_OBSERVATION -> "10分観察"
        SixHabitsPracticeTypes.NEGATIVE_SPACE -> "ネガティブスペース"
        SixHabitsPracticeTypes.QUESTION_TO_OBJECT -> "対象に質問する"
        SixHabitsPracticeTypes.THREE_LAYER_RECORD -> "3層記録"
        SixHabitsPracticeTypes.EMOTION_TO_SENSE -> "感情→五感変換"
        SixHabitsPracticeTypes.FAILURE_AS_MATERIAL -> "失敗を素材に"
        SixHabitsPracticeTypes.SELF_QUESTIONING -> "自問自答"
        SixHabitsPracticeTypes.FRIEND_ADVICE -> "友人へのアドバイス"
        SixHabitsPracticeTypes.DAILY_SCORING -> "今日の採点"
        SixHabitsPracticeTypes.SACRED_SPACE -> "聖域の設計"
        SixHabitsPracticeTypes.START_RITUAL -> "始まりの儀式"
        SixHabitsPracticeTypes.END_RITUAL -> "終わりの儀式"
        else -> practiceType
    }
}

// ====================
// 個別トレーニングコンポーネント
// ====================

// マインドセット① - タイトルをつける
@Composable
private fun TitleNamingTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "シーンにタイトルをつける",
            instruction = "今見ている場面、または思い浮かべた場面に文学的なタイトルをつけましょう。\n\n" +
                    "例：\n• 「夜明け前の静寂」（通勤電車）\n• 「蛍光灯の下の小さな焦り」（コンビニ）\n• 「ガラスに描かれる一時的な地図」（雨の窓）"
        )

        OutlinedTextField(
            value = uiState.inputScene,
            onValueChange = { viewModel.updateInputScene(it) },
            label = { Text("場面の説明") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("どんな場面ですか？（例：朝の通勤電車の中で）") }
        )

        OutlinedTextField(
            value = uiState.inputTitle,
            onValueChange = { viewModel.updateInputTitle(it) },
            label = { Text("タイトル") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("この場面につけるタイトルは？") }
        )
    }
}

// マインドセット① - 3視点
@Composable
private fun PerspectiveShiftTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "3つの視点で見る",
            instruction = "同じ場面を、3つの異なる視点から描写しましょう。\n\n" +
                    "視点の変換は、前頭前野の柔軟性を鍛えます。"
        )

        OutlinedTextField(
            value = uiState.inputScene,
            onValueChange = { viewModel.updateInputScene(it) },
            label = { Text("場面の説明") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputPerspective1,
            onValueChange = { viewModel.updateInputPerspective1(it) },
            label = { Text("👁️ 一人称視点（私は...）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("例：「私は窓の外の雨を見ている。焦りと諦めを感じている」") }
        )

        OutlinedTextField(
            value = uiState.inputPerspective2,
            onValueChange = { viewModel.updateInputPerspective2(it) },
            label = { Text("👤 三人称視点（彼/彼女は...）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("例：「彼は、朝の電車の中で誰かの視線を感じている」") }
        )

        OutlinedTextField(
            value = uiState.inputPerspective3,
            onValueChange = { viewModel.updateInputPerspective3(it) },
            label = { Text("🪟 物の視点（物は...）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("例：「窓は、毎日、外の世界と室内を隔てている」") }
        )
    }
}

// マインドセット① - なぜ5回
@Composable
private fun WhyChainTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "「なぜ？」を5回",
            instruction = "何かを感じたとき、「なぜそう感じたか？」を5回自問します。\n\n" +
                    "これにより、無意識の感情が意識化されます。"
        )

        OutlinedTextField(
            value = uiState.inputFeeling,
            onValueChange = { viewModel.updateInputFeeling(it) },
            label = { Text("最初の感覚/感情") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例：「雨の音が心地よい」") }
        )

        uiState.inputWhyChain.forEachIndexed { index, answer ->
            OutlinedTextField(
                value = answer,
                onValueChange = { viewModel.updateInputWhyChain(index, it) },
                label = { Text("${index + 1}回目の「なぜ？」への答え") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

// マインドセット② - 新しい比喩
@Composable
private fun NewMetaphorTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "新しい比喩を作る",
            instruction = "日常の現象を、新しい比喩で表現しましょう。\n\n" +
                    "例：\n• スマホの通知音 →「誰かが遠くから投げる小石のようだ」\n• 満員電車 →「誰もが自分の殻に閉じこもる水族館だ」"
        )

        OutlinedTextField(
            value = uiState.inputPhenomenon,
            onValueChange = { viewModel.updateInputPhenomenon(it) },
            label = { Text("現象・対象") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("何を比喩にしますか？") }
        )

        OutlinedTextField(
            value = uiState.inputNewMetaphor,
            onValueChange = { viewModel.updateInputNewMetaphor(it) },
            label = { Text("新しい比喩") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("「〜のようだ」「〜だ」の形で表現") }
        )
    }
}

// マインドセット② - 比喩の変換
@Composable
private fun TransformMetaphorTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "既存比喩を壊して作り直す",
            instruction = "よく使われる陳腐な比喩を、より独創的なものに変換しましょう。\n\n" +
                    "例：「時間はお金だ」→「時間は消しゴムで消せない文字だ」"
        )

        OutlinedTextField(
            value = uiState.inputOriginalMetaphor,
            onValueChange = { viewModel.updateInputOriginalMetaphor(it) },
            label = { Text("元の比喩（陳腐なもの）") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例：「時間はお金だ」") }
        )

        OutlinedTextField(
            value = uiState.inputTransformedMetaphor1,
            onValueChange = { viewModel.updateInputTransformedMetaphor1(it) },
            label = { Text("変換1: より自然的な比喩") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputTransformedMetaphor2,
            onValueChange = { viewModel.updateInputTransformedMetaphor2(it) },
            label = { Text("変換2: より独創的な比喩") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputTransformedMetaphor3,
            onValueChange = { viewModel.updateInputTransformedMetaphor3(it) },
            label = { Text("変換3: 感情的・複層的な比喩") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}

// マインドセット② - 抽象→具体
@Composable
private fun AbstractToConcreteTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "抽象→具体変換",
            instruction = "「孤独」「不安」「希望」といった抽象的な感情を、具体物に変換しましょう。\n\n" +
                    "例：\n• 孤独 →「誰も座っていない椅子」\n• 不安 →「窓の外で鳴き続ける鳥」"
        )

        OutlinedTextField(
            value = uiState.inputAbstractEmotion,
            onValueChange = { viewModel.updateInputAbstractEmotion(it) },
            label = { Text("抽象的な感情") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例：孤独、不安、希望、怒り、悲しみ") }
        )

        OutlinedTextField(
            value = uiState.inputConcreteThing,
            onValueChange = { viewModel.updateInputConcreteThing(it) },
            label = { Text("具体物への変換") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("その感情は、何に例えられますか？") }
        )
    }
}

// マインドセット③ - 10分観察
@Composable
private fun TenMinuteObservationTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "10分間観察",
            instruction = "1つの物（コップ、ペン、葉っぱなど）を10分間見続けましょう。\n\n" +
                    "【最初の3分】形・色を観察\n【次の3分】質感・重さを想像\n【最後の4分】「この物が語りかけてきたら？」"
        )

        OutlinedTextField(
            value = uiState.inputObservationTarget,
            onValueChange = { viewModel.updateInputObservationTarget(it) },
            label = { Text("観察対象") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("何を観察しますか？") }
        )

        OutlinedTextField(
            value = uiState.inputFormColor,
            onValueChange = { viewModel.updateInputFormColor(it) },
            label = { Text("形・色・基本的特徴（3分）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = uiState.inputTextureWeight,
            onValueChange = { viewModel.updateInputTextureWeight(it) },
            label = { Text("質感・重さの想像（3分）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = uiState.inputDialogueImagination,
            onValueChange = { viewModel.updateInputDialogueImagination(it) },
            label = { Text("この物が語りかけてきたら？（4分）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            placeholder = { Text("この物は、何を訴えたいでしょうか？") }
        )
    }
}

// マインドセット③ - ネガティブスペース
@Composable
private fun NegativeSpaceTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "ネガティブスペースを見る",
            instruction = "物そのものではなく、物と物の間の空間を意識的に見ましょう。\n\n" +
                    "例：「枝と枝の間の空が、複数の小さな宇宙を作っている」"
        )

        OutlinedTextField(
            value = uiState.inputObservationTarget,
            onValueChange = { viewModel.updateInputObservationTarget(it) },
            label = { Text("観察対象/場面") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.inputNegativeSpace,
            onValueChange = { viewModel.updateInputNegativeSpace(it) },
            label = { Text("ネガティブスペースに何がある？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            placeholder = { Text("物と物の間、空間に何が見えますか？") }
        )
    }
}

// マインドセット③ - 対象に質問
@Composable
private fun QuestionToObjectTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "対象に質問する",
            instruction = "見ている物に、質問を投げかけましょう。\n\n" +
                    "例：「この椅子は、誰が最後に座ったのだろう？」"
        )

        OutlinedTextField(
            value = uiState.inputObservationTarget,
            onValueChange = { viewModel.updateInputObservationTarget(it) },
            label = { Text("観察対象") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.inputQuestion1,
            onValueChange = { viewModel.updateInputQuestion1(it) },
            label = { Text("質問1") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputQuestion2,
            onValueChange = { viewModel.updateInputQuestion2(it) },
            label = { Text("質問2") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputQuestion3,
            onValueChange = { viewModel.updateInputQuestion3(it) },
            label = { Text("質問3") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputDialogueImagination,
            onValueChange = { viewModel.updateInputDialogueImagination(it) },
            label = { Text("想像した答え") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
    }
}

// マインドセット④ - 3層記録
@Composable
private fun ThreeLayerRecordTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "3層記録",
            instruction = "経験を「事実」「感情」「普遍」の3層で記録しましょう。\n\n" +
                    "これにより、個人的経験が普遍的テーマへ昇華されます。"
        )

        OutlinedTextField(
            value = uiState.inputFactLayer,
            onValueChange = { viewModel.updateInputFactLayer(it) },
            label = { Text("【事実の層】何が起きたか？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("客観的に、感情を混ぜずに") }
        )

        OutlinedTextField(
            value = uiState.inputEmotionLayer,
            onValueChange = { viewModel.updateInputEmotionLayer(it) },
            label = { Text("【感情の層】どう感じたか？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("正直に、その時の感情を") }
        )

        OutlinedTextField(
            value = uiState.inputUniversalLayer,
            onValueChange = { viewModel.updateInputUniversalLayer(it) },
            label = { Text("【普遍の層】これは何についての話か？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            placeholder = { Text("根本的なテーマを言語化") }
        )
    }
}

// マインドセット④ - 感情→五感
@Composable
private fun EmotionToSenseTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "感情→五感変換",
            instruction = "今日感じた感情を「色」「音」「触感」の3つで表現しましょう。\n\n" +
                    "例：不安 → 濁ったグレー / キーンという高い音 / 濡れた新聞紙"
        )

        OutlinedTextField(
            value = uiState.inputEmotionLayer,
            onValueChange = { viewModel.updateInputEmotionLayer(it) },
            label = { Text("今日感じた感情") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例：不安、希望、怒り、喜び、絶望...") }
        )

        OutlinedTextField(
            value = uiState.inputEmotionToColor,
            onValueChange = { viewModel.updateInputEmotionToColor(it) },
            label = { Text("色に例えると？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputEmotionToSound,
            onValueChange = { viewModel.updateInputEmotionToSound(it) },
            label = { Text("音に例えると？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputEmotionToTexture,
            onValueChange = { viewModel.updateInputEmotionToTexture(it) },
            label = { Text("触感に例えると？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}

// マインドセット④ - 失敗を素材に
@Composable
private fun FailureAsMaterialTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "失敗を素材に",
            instruction = "失敗を「学び」ではなく「物語の素材」として見ましょう。\n\n" +
                    "問い：「この失敗は、どんな物語の一部になりうるか？」"
        )

        OutlinedTextField(
            value = uiState.inputFailure,
            onValueChange = { viewModel.updateInputFailure(it) },
            label = { Text("失敗・恥・痛みの経験") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = uiState.inputFailureAsStory,
            onValueChange = { viewModel.updateInputFailureAsStory(it) },
            label = { Text("これはどんな物語の一部か？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            placeholder = { Text("この失敗を、物語の素材として再構成してみてください") }
        )
    }
}

// マインドセット⑤ - 自問自答
@Composable
private fun SelfQuestioningTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "自問自答",
            instruction = "「今、自分は何をしているか？なぜ？」を自問しましょう。\n\n" +
                    "1日10回を目標に、行動中に意識的に問いかけます。"
        )

        OutlinedTextField(
            value = uiState.inputCurrentActivity,
            onValueChange = { viewModel.updateInputCurrentActivity(it) },
            label = { Text("今、自分は何をしている？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputWhyActivity,
            onValueChange = { viewModel.updateInputWhyActivity(it) },
            label = { Text("なぜ、それをしている？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

// マインドセット⑤ - 友人へのアドバイス
@Composable
private fun FriendAdviceTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "友人へのアドバイス",
            instruction = "自分が迷っていることを、「友人が同じ状況なら、何とアドバイスするか？」\n" +
                    "と考えてみましょう。"
        )

        OutlinedTextField(
            value = uiState.inputDilemma,
            onValueChange = { viewModel.updateInputDilemma(it) },
            label = { Text("今、迷っていること") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = uiState.inputFriendAdvice,
            onValueChange = { viewModel.updateInputFriendAdvice(it) },
            label = { Text("友人にアドバイスするなら？") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            placeholder = { Text("「友人には、こう言うだろう：...」") }
        )
    }
}

// マインドセット⑤ - 今日の採点
@Composable
private fun DailyScoringTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "今日の採点",
            instruction = "今日の自分を10点満点で採点し、理由を3つ書きましょう。"
        )

        // スコアスライダー
        Text("今日の自分：${uiState.inputDailyScore}/10点")
        Slider(
            value = uiState.inputDailyScore.toFloat(),
            onValueChange = { viewModel.updateInputDailyScore(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8
        )

        OutlinedTextField(
            value = uiState.inputScoreReason1,
            onValueChange = { viewModel.updateInputScoreReason1(it) },
            label = { Text("理由①") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputScoreReason2,
            onValueChange = { viewModel.updateInputScoreReason2(it) },
            label = { Text("理由②") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputScoreReason3,
            onValueChange = { viewModel.updateInputScoreReason3(it) },
            label = { Text("理由③") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = uiState.inputTomorrowPlan,
            onValueChange = { viewModel.updateInputTomorrowPlan(it) },
            label = { Text("明日への改善") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            placeholder = { Text("明日は、____に注力する") }
        )
    }
}

// マインドセット⑥ - 聖域の設計
@Composable
private fun SacredSpaceTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "聖域の設計",
            instruction = "創作専用の空間を設計しましょう。\n\n" +
                    "この空間に入ると、脳が自動的に「創作モード」に切り替わるようになります。"
        )

        OutlinedTextField(
            value = uiState.inputSacredSpace,
            onValueChange = { viewModel.updateInputSacredSpace(it) },
            label = { Text("聖域の設計") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            placeholder = {
                Text(
                    "以下を記述してください：\n" +
                            "• 場所（机、椅子、照明）\n" +
                            "• 排除するもの（スマホ、テレビなど）\n" +
                            "• 置くもの（ノート、筆記具、インスピレーション用の画像）\n" +
                            "• 温度・湿度・音の環境"
                )
            }
        )

        OutlinedTextField(
            value = uiState.inputRitualNotes,
            onValueChange = { viewModel.updateInputRitualNotes(it) },
            label = { Text("メモ・気づき") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

// マインドセット⑥ - 始まりの儀式
@Composable
private fun StartRitualTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "始まりの儀式",
            instruction = "作業を始める前に、必ず同じ動作をする「儀式」を設計しましょう。\n\n" +
                    "例：\n• 座る → コーヒーを淹れる → 深呼吸3回\n• 同じ曲を聴く → 瞑想30秒 → 今日のテーマを思い浮かべる"
        )

        OutlinedTextField(
            value = uiState.inputStartRitual,
            onValueChange = { viewModel.updateInputStartRitual(it) },
            label = { Text("始まりの儀式") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            placeholder = {
                Text(
                    "ステップを記述してください：\n" +
                            "【第1段階】\n" +
                            "【第2段階】\n" +
                            "【第3段階】\n" +
                            "【第4段階】\n" +
                            "【合計時間】"
                )
            }
        )

        OutlinedTextField(
            value = uiState.inputRitualNotes,
            onValueChange = { viewModel.updateInputRitualNotes(it) },
            label = { Text("実行後のメモ・気づき") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

// マインドセット⑥ - 終わりの儀式
@Composable
private fun EndRitualTraining(uiState: SixHabitsUiState, viewModel: SixHabitsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InstructionCard(
            title = "終わりの儀式",
            instruction = "作業を終えるときも、同じ動作をする「儀式」を設計しましょう。\n\n" +
                    "例：\n• 作業を止める → ノートを閉じる → 窓を開ける → 一言日記 → ストレッチ"
        )

        OutlinedTextField(
            value = uiState.inputEndRitual,
            onValueChange = { viewModel.updateInputEndRitual(it) },
            label = { Text("終わりの儀式") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            placeholder = {
                Text(
                    "ステップを記述してください：\n" +
                            "【第1段階】作業を完全に止める\n" +
                            "【第2段階】\n" +
                            "【第3段階】\n" +
                            "【第4段階】\n" +
                            "【第5段階】\n" +
                            "【合計時間】"
                )
            }
        )

        OutlinedTextField(
            value = uiState.inputRitualNotes,
            onValueChange = { viewModel.updateInputRitualNotes(it) },
            label = { Text("実行後のメモ・気づき") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

// ====================
// 共通コンポーネント
// ====================

@Composable
private fun InstructionCard(
    title: String,
    instruction: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                instruction,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}