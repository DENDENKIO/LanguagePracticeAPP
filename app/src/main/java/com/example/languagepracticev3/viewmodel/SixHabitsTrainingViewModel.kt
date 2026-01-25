// app/src/main/java/com/example/languagepracticev3/viewmodel/SixHabitsTrainingViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.model.MindsetType
import com.example.languagepracticev3.data.model.SixHabitsSession
import com.example.languagepracticev3.data.model.SixHabitsPracticeTypes
import com.example.languagepracticev3.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 6つの思考習慣トレーニングの画面状態
 */
enum class SixHabitsScreenState {
    HABIT_SELECTION,      // 習慣選択
    PRACTICE_SELECTION,   // 練習タイプ選択
    TRAINING,             // トレーニング実行中
    COMPLETE              // 完了
}

/**
 * トレーニングステップ
 */
enum class TrainingStep(val displayName: String) {
    GUIDE("ガイド"),
    INPUT("入力"),
    DEEP_QUESTION("深掘り"),
    REFLECTION("振り返り")
}

/**
 * 6つの思考習慣トレーニングの UI 状態
 */
data class SixHabitsUiState(
    // 画面状態
    val screenState: SixHabitsScreenState = SixHabitsScreenState.HABIT_SELECTION,

    // 選択情報
    val selectedMindsetType: MindsetType? = null,
    val selectedPracticeType: String = "",

    // トレーニングステップ
    val currentStep: TrainingStep = TrainingStep.GUIDE,

    // セッション管理
    val currentSession: SixHabitsSession? = null,
    val sessions: List<SixHabitsSession> = emptyList(),

    // ===== 習慣①: 世界を「素材」として見る =====
    val inputSceneDescription: String = "",      // シーンの描写
    val inputSceneTitle: String = "",            // シーンにつけるタイトル
    val inputPerspective1: String = "",          // 一人称視点
    val inputPerspective2: String = "",          // 三人称視点
    val inputPerspective3: String = "",          // 物の視点
    val inputWhyLevel1: String = "",             // なぜ？（1回目）
    val inputWhyLevel2: String = "",             // なぜ？（2回目）
    val inputWhyLevel3: String = "",             // なぜ？（3回目）
    val inputWhyLevel4: String = "",             // なぜ？（4回目）
    val inputWhyLevel5: String = "",             // なぜ？（5回目）

    // ===== 習慣②: 比喩で世界を「翻訳」する =====
    val inputTargetConcept: String = "",         // 比喩の対象（抽象概念）
    val inputMetaphorAttempt1: String = "",      // 比喩の試み1
    val inputMetaphorAttempt2: String = "",      // 比喩の試み2
    val inputMetaphorAttempt3: String = "",      // 比喩の試み3
    val inputOriginalMetaphor: String = "",      // 壊す対象の既存比喩
    val inputTransformedMetaphor: String = "",   // 作り直した比喩
    val inputAbstractEmotion: String = "",       // 抽象的な感情
    val inputConcreteObject: String = "",        // 具体物への変換

    // ===== 習慣③: 観察を「対話」として扱う =====
    val inputObservationTarget: String = "",     // 観察対象
    val inputObservationShape: String = "",      // 形の観察
    val inputObservationColor: String = "",      // 色の観察
    val inputObservationTexture: String = "",    // 質感の観察
    val inputObservationOther: String = "",      // その他の観察
    val inputNegativeSpace: String = "",         // ネガティブスペース
    val inputQuestionToObject: String = "",      // 対象への質問
    val inputAnswerFromObject: String = "",      // 対象からの想像上の答え

    // ===== 習慣④: 経験を「錬金術」で変換する =====
    val inputExperienceFact: String = "",        // 事実の層
    val inputExperienceEmotion: String = "",     // 感情の層
    val inputExperienceUniversal: String = "",   // 普遍の層
    val inputEmotionToColor: String = "",        // 感情→色
    val inputEmotionToSound: String = "",        // 感情→音
    val inputEmotionToTexture: String = "",      // 感情→触感
    val inputFailureDescription: String = "",    // 失敗の描写
    val inputFailureAsStory: String = "",        // 失敗を物語に

    // ===== 習慣⑤: メタ認知を育てる =====
    val inputSelfQuestion: String = "",          // 自問
    val inputSelfAnswer: String = "",            // 自答
    val inputFriendProblem: String = "",         // 友人の問題（仮定）
    val inputFriendAdvice: String = "",          // 友人へのアドバイス
    val inputDailyScore: Int = 5,                // 今日の自己採点（1-10）
    val inputScoreReason1: String = "",          // 採点理由1
    val inputScoreReason2: String = "",          // 採点理由2
    val inputScoreReason3: String = "",          // 採点理由3
    val inputTomorrowImprovement: String = "",   // 明日への改善

    // ===== 習慣⑥: ルーティンを「儀式」として設計 =====
    val inputSacredSpaceLocation: String = "",   // 聖域の場所
    val inputSacredSpaceSetup: String = "",      // 聖域の設定
    val inputStartRitualAction: String = "",     // 始まりの儀式（行動）
    val inputStartRitualMeaning: String = "",    // 始まりの儀式（意味）
    val inputEndRitualAction: String = "",       // 終わりの儀式（行動）
    val inputEndRitualMeaning: String = "",      // 終わりの儀式（意味）

    // ===== 振り返り =====
    val inputReflection: String = "",            // 振り返りメモ
    val inputLearning: String = "",              // 学んだこと

    // UI状態
    val isLoading: Boolean = false,
    val showSessionPicker: Boolean = false,
    val showConfirmExit: Boolean = false,
    val statusMessage: String = ""
)

/**
 * 練習タイプの情報
 */
data class PracticeTypeInfo(
    val type: String,
    val displayName: String,
    val description: String,
    val guideText: String
)

@HiltViewModel
class SixHabitsTrainingViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SixHabitsUiState())
    val uiState: StateFlow<SixHabitsUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    // ====================
    // 練習タイプ定義
    // ====================

    fun getPracticeTypes(mindsetType: MindsetType): List<PracticeTypeInfo> {
        return when (mindsetType) {
            MindsetType.WORLD_AS_MATERIAL -> listOf(
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.TITLE_NAMING,
                    "シーンにタイトルをつける",
                    "日常の風景に文学的なタイトルをつける訓練",
                    "目の前の風景や出来事を観察し、それを一言で表す「タイトル」を考えましょう。\n" +
                            "良いタイトルは、見たものの本質を捉えつつ、詩的な余韻を残します。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.PERSPECTIVE_SHIFT,
                    "3つの視点で見る",
                    "同じシーンを異なる視点から描写する",
                    "同じ場面を3つの異なる視点から描写します。\n" +
                            "①一人称（私）②三人称（彼/彼女）③物の視点（机、窓など）"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.WHY_CHAIN,
                    "なぜ？を5回繰り返す",
                    "表面的な答えから本質へ掘り下げる",
                    "ある事象や感情について「なぜ？」を5回繰り返します。\n" +
                            "1回目の答えに対して「なぜ？」、その答えにまた「なぜ？」と掘り下げていきます。"
                )
            )
            MindsetType.METAPHOR_TRANSLATION -> listOf(
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.NEW_METAPHOR,
                    "新しい比喩を作る",
                    "抽象概念を具体的なイメージで表現",
                    "抽象的な概念（孤独、希望、時間など）を、\n" +
                            "具体的な物や現象に例える新しい比喩を3つ作りましょう。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.TRANSFORM_METAPHOR,
                    "既存の比喩を壊して作り直す",
                    "陳腐な比喩を新鮮な表現に変換",
                    "よく使われる比喩（「心が重い」「光が見える」など）を取り上げ、\n" +
                            "それを壊して、まったく新しい比喩に作り直します。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.ABSTRACT_TO_CONCRETE,
                    "感情を具体物に変換",
                    "抽象的な感情を触れられるものに",
                    "「悲しみ」「喜び」などの抽象的な感情を、\n" +
                            "具体的な物体として描写します。形、色、質感、重さなどを含めて。"
                )
            )
            MindsetType.OBSERVATION_AS_DIALOGUE -> listOf(
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.TEN_MINUTE_OBSERVATION,
                    "10分間の観察",
                    "一つの対象を深く観察する",
                    "一つの対象（物、人、風景）を10分間じっくり観察します。\n" +
                            "形、色、質感、動き、変化を細かく記録しましょう。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.NEGATIVE_SPACE,
                    "ネガティブスペースを見る",
                    "「ないもの」「空白」に注目する",
                    "対象そのものではなく、その周りの空間、\n" +
                            "「ないもの」「空白」に注目して描写します。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.QUESTION_TO_OBJECT,
                    "対象に質問する",
                    "観察対象と対話する想像力を鍛える",
                    "観察対象に質問を投げかけ、\n" +
                            "その対象が答えるとしたら何と言うか想像します。"
                )
            )
            MindsetType.EXPERIENCE_ALCHEMY -> listOf(
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.THREE_LAYER_RECORD,
                    "3層記録",
                    "事実・感情・普遍の3層で経験を記録",
                    "最近の経験を3つの層で記録します。\n" +
                            "①事実の層（何が起きたか）\n" +
                            "②感情の層（どう感じたか）\n" +
                            "③普遍の層（誰にでも当てはまる真理は？）"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.EMOTION_TO_SENSE,
                    "感情を五感に変換",
                    "抽象的な感情を感覚的に表現",
                    "ある感情を、色・音・触感の3つの感覚で表現します。\n" +
                            "「この感情が色だとしたら？」「音だとしたら？」「触感だとしたら？」"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.FAILURE_AS_MATERIAL,
                    "失敗を素材にする",
                    "失敗体験を創作の素材に変換",
                    "最近の失敗や後悔を、物語の素材として再構成します。\n" +
                            "その失敗が主人公に起きたとしたら、どんな物語になる？"
                )
            )
            MindsetType.METACOGNITION -> listOf(
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.SELF_QUESTIONING,
                    "自問自答",
                    "自分に問いかけ、自分で答える",
                    "今の自分に対して質問を投げかけ、正直に答えます。\n" +
                            "「本当にそう思っている？」「何を恐れている？」など。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.FRIEND_ADVICE,
                    "友人へのアドバイス",
                    "自分の問題を客観視する",
                    "今抱えている問題を、「友人の問題」として設定し、\n" +
                            "その友人にアドバイスするつもりで考えます。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.DAILY_SCORING,
                    "今日の採点",
                    "1日を振り返り自己評価する",
                    "今日1日を10点満点で採点し、\n" +
                            "その点数の理由を3つ、明日への改善点を1つ書きます。"
                )
            )
            MindsetType.ROUTINE_AS_RITUAL -> listOf(
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.SACRED_SPACE,
                    "聖域を設計する",
                    "創作のための特別な空間を作る",
                    "創作に集中するための「聖域」を設計します。\n" +
                            "場所、照明、音、香りなど、環境を具体的に決めます。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.START_RITUAL,
                    "始まりの儀式を作る",
                    "創作モードに入るためのルーティン",
                    "創作を始める前に行う「儀式」を設計します。\n" +
                            "特定の行動と、その行動に込める意味を決めます。"
                ),
                PracticeTypeInfo(
                    SixHabitsPracticeTypes.END_RITUAL,
                    "終わりの儀式を作る",
                    "創作モードを閉じるルーティン",
                    "創作を終える時に行う「儀式」を設計します。\n" +
                            "1日の創作を区切り、次回への橋渡しをする行動です。"
                )
            )
        }
    }

    // ====================
    // セッション管理
    // ====================

    private fun loadSessions() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.getAllSixHabitsSessions().collect { sessions ->
                    _uiState.update { it.copy(
                        sessions = sessions,
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    statusMessage = "セッション読み込みエラー: ${e.message}"
                )}
            }
        }
    }

    // ====================
    // ナビゲーション
    // ====================

    fun selectMindsetType(type: MindsetType) {
        _uiState.update { it.copy(
            selectedMindsetType = type,
            screenState = SixHabitsScreenState.PRACTICE_SELECTION
        )}
    }

    fun selectPracticeType(practiceType: String) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val newSession = SixHabitsSession(
            mindsetType = _uiState.value.selectedMindsetType?.number ?: 1,
            practiceType = practiceType,
            sessionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            createdAt = now,
            updatedAt = now
        )
        _uiState.update { it.copy(
            selectedPracticeType = practiceType,
            currentSession = newSession,
            currentStep = TrainingStep.GUIDE,
            screenState = SixHabitsScreenState.TRAINING
        )}
    }

    fun backToHabitSelection() {
        _uiState.update { it.copy(
            screenState = SixHabitsScreenState.HABIT_SELECTION,
            selectedMindsetType = null,
            selectedPracticeType = "",
            currentSession = null,
            currentStep = TrainingStep.GUIDE
        )}
        resetInputFields()
    }

    fun backToPracticeSelection() {
        _uiState.update { it.copy(
            screenState = SixHabitsScreenState.PRACTICE_SELECTION,
            selectedPracticeType = "",
            currentSession = null,
            currentStep = TrainingStep.GUIDE
        )}
        resetInputFields()
    }

    private fun resetInputFields() {
        _uiState.update { it.copy(
            // 習慣①
            inputSceneDescription = "",
            inputSceneTitle = "",
            inputPerspective1 = "",
            inputPerspective2 = "",
            inputPerspective3 = "",
            inputWhyLevel1 = "",
            inputWhyLevel2 = "",
            inputWhyLevel3 = "",
            inputWhyLevel4 = "",
            inputWhyLevel5 = "",
            // 習慣②
            inputTargetConcept = "",
            inputMetaphorAttempt1 = "",
            inputMetaphorAttempt2 = "",
            inputMetaphorAttempt3 = "",
            inputOriginalMetaphor = "",
            inputTransformedMetaphor = "",
            inputAbstractEmotion = "",
            inputConcreteObject = "",
            // 習慣③
            inputObservationTarget = "",
            inputObservationShape = "",
            inputObservationColor = "",
            inputObservationTexture = "",
            inputObservationOther = "",
            inputNegativeSpace = "",
            inputQuestionToObject = "",
            inputAnswerFromObject = "",
            // 習慣④
            inputExperienceFact = "",
            inputExperienceEmotion = "",
            inputExperienceUniversal = "",
            inputEmotionToColor = "",
            inputEmotionToSound = "",
            inputEmotionToTexture = "",
            inputFailureDescription = "",
            inputFailureAsStory = "",
            // 習慣⑤
            inputSelfQuestion = "",
            inputSelfAnswer = "",
            inputFriendProblem = "",
            inputFriendAdvice = "",
            inputDailyScore = 5,
            inputScoreReason1 = "",
            inputScoreReason2 = "",
            inputScoreReason3 = "",
            inputTomorrowImprovement = "",
            // 習慣⑥
            inputSacredSpaceLocation = "",
            inputSacredSpaceSetup = "",
            inputStartRitualAction = "",
            inputStartRitualMeaning = "",
            inputEndRitualAction = "",
            inputEndRitualMeaning = "",
            // 振り返り
            inputReflection = "",
            inputLearning = ""
        )}
    }

    // ====================
    // トレーニングステップ
    // ====================

    fun nextStep() {
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex < TrainingStep.entries.size - 1) {
            _uiState.update { it.copy(
                currentStep = TrainingStep.entries[currentIndex + 1]
            )}
        }
    }

    fun previousStep() {
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex > 0) {
            _uiState.update { it.copy(
                currentStep = TrainingStep.entries[currentIndex - 1]
            )}
        }
    }

    // ====================
    // 保存・完了
    // ====================

    fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updatedSession = buildSessionFromState(session, state, now, isCompleted = false)

            try {
                val savedId = repository.saveSixHabitsSession(updatedSession)
                _uiState.update { it.copy(
                    currentSession = updatedSession.copy(id = savedId),
                    statusMessage = "保存しました"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    statusMessage = "保存エラー: ${e.message}"
                )}
            }
        }
    }

    fun completeSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val completedSession = buildSessionFromState(session, state, now, isCompleted = true)

            try {
                repository.saveSixHabitsSession(completedSession)
                _uiState.update { it.copy(
                    screenState = SixHabitsScreenState.COMPLETE,
                    statusMessage = "トレーニングを完了しました"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    statusMessage = "完了エラー: ${e.message}"
                )}
            }
        }
    }

    private fun buildSessionFromState(
        session: SixHabitsSession,
        state: SixHabitsUiState,
        now: String,
        isCompleted: Boolean
    ): SixHabitsSession {
        return session.copy(
            // 習慣①
            sceneTitle = state.inputSceneTitle,
            perspective1 = state.inputPerspective1,
            perspective2 = state.inputPerspective2,
            perspective3 = state.inputPerspective3,
            whyChain = listOf(
                state.inputWhyLevel1,
                state.inputWhyLevel2,
                state.inputWhyLevel3,
                state.inputWhyLevel4,
                state.inputWhyLevel5
            ).filter { it.isNotBlank() }.joinToString("\n---\n"),
            // 習慣②
            originalMetaphor = state.inputOriginalMetaphor,
            transformedMetaphor = state.inputTransformedMetaphor,
            abstractConcept = state.inputAbstractEmotion,
            concreteThing = state.inputConcreteObject,
            // 習慣③
            observationTarget = state.inputObservationTarget,
            observationNotes = listOf(
                "形: ${state.inputObservationShape}",
                "色: ${state.inputObservationColor}",
                "質感: ${state.inputObservationTexture}",
                "その他: ${state.inputObservationOther}"
            ).joinToString("\n"),
            negativeSpace = state.inputNegativeSpace,
            dialogueQuestion = state.inputQuestionToObject,
            dialogueAnswer = state.inputAnswerFromObject,
            // 習慣④
            factLayer = state.inputExperienceFact,
            emotionLayer = state.inputExperienceEmotion,
            universalLayer = state.inputExperienceUniversal,
            emotionToColor = state.inputEmotionToColor,
            emotionToSound = state.inputEmotionToSound,
            emotionToTexture = state.inputEmotionToTexture,
            failureAsStory = state.inputFailureAsStory,
            // 習慣⑤
            selfQuestion = state.inputSelfQuestion,
            selfAnswer = state.inputSelfAnswer,
            friendAdvice = state.inputFriendAdvice,
            dailyScore = state.inputDailyScore,
            scoreReason1 = state.inputScoreReason1,
            scoreReason2 = state.inputScoreReason2,
            scoreReason3 = state.inputScoreReason3,
            tomorrowPlan = state.inputTomorrowImprovement,
            // 習慣⑥
            sacredSpace = "${state.inputSacredSpaceLocation}\n${state.inputSacredSpaceSetup}",
            startRitual = "${state.inputStartRitualAction}\n意味: ${state.inputStartRitualMeaning}",
            endRitual = "${state.inputEndRitualAction}\n意味: ${state.inputEndRitualMeaning}",
            // 共通
            inputText = state.inputSceneDescription,
            outputText = state.inputReflection,
            isCompleted = isCompleted,
            updatedAt = now
        )
    }

    // ====================
    // 中断確認
    // ====================

    fun showExitConfirmation() {
        _uiState.update { it.copy(showConfirmExit = true) }
    }

    fun hideExitConfirmation() {
        _uiState.update { it.copy(showConfirmExit = false) }
    }

    fun confirmExit() {
        _uiState.update { it.copy(
            showConfirmExit = false,
            screenState = SixHabitsScreenState.HABIT_SELECTION,
            selectedMindsetType = null,
            selectedPracticeType = "",
            currentSession = null
        )}
        resetInputFields()
    }

    // ====================
    // 入力フィールド更新
    // ====================

    // 習慣①
    fun updateSceneDescription(value: String) = _uiState.update { it.copy(inputSceneDescription = value) }
    fun updateSceneTitle(value: String) = _uiState.update { it.copy(inputSceneTitle = value) }
    fun updatePerspective1(value: String) = _uiState.update { it.copy(inputPerspective1 = value) }
    fun updatePerspective2(value: String) = _uiState.update { it.copy(inputPerspective2 = value) }
    fun updatePerspective3(value: String) = _uiState.update { it.copy(inputPerspective3 = value) }
    fun updateWhyLevel1(value: String) = _uiState.update { it.copy(inputWhyLevel1 = value) }
    fun updateWhyLevel2(value: String) = _uiState.update { it.copy(inputWhyLevel2 = value) }
    fun updateWhyLevel3(value: String) = _uiState.update { it.copy(inputWhyLevel3 = value) }
    fun updateWhyLevel4(value: String) = _uiState.update { it.copy(inputWhyLevel4 = value) }
    fun updateWhyLevel5(value: String) = _uiState.update { it.copy(inputWhyLevel5 = value) }

    // 習慣②
    fun updateTargetConcept(value: String) = _uiState.update { it.copy(inputTargetConcept = value) }
    fun updateMetaphorAttempt1(value: String) = _uiState.update { it.copy(inputMetaphorAttempt1 = value) }
    fun updateMetaphorAttempt2(value: String) = _uiState.update { it.copy(inputMetaphorAttempt2 = value) }
    fun updateMetaphorAttempt3(value: String) = _uiState.update { it.copy(inputMetaphorAttempt3 = value) }
    fun updateOriginalMetaphor(value: String) = _uiState.update { it.copy(inputOriginalMetaphor = value) }
    fun updateTransformedMetaphor(value: String) = _uiState.update { it.copy(inputTransformedMetaphor = value) }
    fun updateAbstractEmotion(value: String) = _uiState.update { it.copy(inputAbstractEmotion = value) }
    fun updateConcreteObject(value: String) = _uiState.update { it.copy(inputConcreteObject = value) }

    // 習慣③
    fun updateObservationTarget(value: String) = _uiState.update { it.copy(inputObservationTarget = value) }
    fun updateObservationShape(value: String) = _uiState.update { it.copy(inputObservationShape = value) }
    fun updateObservationColor(value: String) = _uiState.update { it.copy(inputObservationColor = value) }
    fun updateObservationTexture(value: String) = _uiState.update { it.copy(inputObservationTexture = value) }
    fun updateObservationOther(value: String) = _uiState.update { it.copy(inputObservationOther = value) }
    fun updateNegativeSpace(value: String) = _uiState.update { it.copy(inputNegativeSpace = value) }
    fun updateQuestionToObject(value: String) = _uiState.update { it.copy(inputQuestionToObject = value) }
    fun updateAnswerFromObject(value: String) = _uiState.update { it.copy(inputAnswerFromObject = value) }

    // 習慣④
    fun updateExperienceFact(value: String) = _uiState.update { it.copy(inputExperienceFact = value) }
    fun updateExperienceEmotion(value: String) = _uiState.update { it.copy(inputExperienceEmotion = value) }
    fun updateExperienceUniversal(value: String) = _uiState.update { it.copy(inputExperienceUniversal = value) }
    fun updateEmotionToColor(value: String) = _uiState.update { it.copy(inputEmotionToColor = value) }
    fun updateEmotionToSound(value: String) = _uiState.update { it.copy(inputEmotionToSound = value) }
    fun updateEmotionToTexture(value: String) = _uiState.update { it.copy(inputEmotionToTexture = value) }
    fun updateFailureDescription(value: String) = _uiState.update { it.copy(inputFailureDescription = value) }
    fun updateFailureAsStory(value: String) = _uiState.update { it.copy(inputFailureAsStory = value) }

    // 習慣⑤
    fun updateSelfQuestion(value: String) = _uiState.update { it.copy(inputSelfQuestion = value) }
    fun updateSelfAnswer(value: String) = _uiState.update { it.copy(inputSelfAnswer = value) }
    fun updateFriendProblem(value: String) = _uiState.update { it.copy(inputFriendProblem = value) }
    fun updateFriendAdvice(value: String) = _uiState.update { it.copy(inputFriendAdvice = value) }
    fun updateDailyScore(value: Int) = _uiState.update { it.copy(inputDailyScore = value.coerceIn(1, 10)) }
    fun updateScoreReason1(value: String) = _uiState.update { it.copy(inputScoreReason1 = value) }
    fun updateScoreReason2(value: String) = _uiState.update { it.copy(inputScoreReason2 = value) }
    fun updateScoreReason3(value: String) = _uiState.update { it.copy(inputScoreReason3 = value) }
    fun updateTomorrowImprovement(value: String) = _uiState.update { it.copy(inputTomorrowImprovement = value) }

    // 習慣⑥
    fun updateSacredSpaceLocation(value: String) = _uiState.update { it.copy(inputSacredSpaceLocation = value) }
    fun updateSacredSpaceSetup(value: String) = _uiState.update { it.copy(inputSacredSpaceSetup = value) }
    fun updateStartRitualAction(value: String) = _uiState.update { it.copy(inputStartRitualAction = value) }
    fun updateStartRitualMeaning(value: String) = _uiState.update { it.copy(inputStartRitualMeaning = value) }
    fun updateEndRitualAction(value: String) = _uiState.update { it.copy(inputEndRitualAction = value) }
    fun updateEndRitualMeaning(value: String) = _uiState.update { it.copy(inputEndRitualMeaning = value) }

    // 振り返り
    fun updateReflection(value: String) = _uiState.update { it.copy(inputReflection = value) }
    fun updateLearning(value: String) = _uiState.update { it.copy(inputLearning = value) }

    // ステータス
    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = "") }
}
