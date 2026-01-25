// app/src/main/java/com/example/languagepracticev3/viewmodel/SixHabitsViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.model.*
import com.example.languagepracticev3.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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
enum class SixHabitsTrainingStep(val displayName: String) {
    GUIDE("ガイド"),
    INPUT("入力"),
    DEEP_QUESTION("深掘り"),
    REFLECTION("振り返り")
}

/**
 * 練習タイプの情報
 */
data class PracticeTypeInfo(
    val type: String,
    val displayName: String,
    val description: String,
    val guideText: String
)

/**
 * 6つの思考習慣 UI状態
 */
data class SixHabitsUiState(
    // ★追加: 画面状態管理
    val screenState: SixHabitsScreenState = SixHabitsScreenState.HABIT_SELECTION,
    val currentTrainingStep: SixHabitsTrainingStep = SixHabitsTrainingStep.GUIDE,
    val showConfirmExit: Boolean = false,

    // 現在選択中のマインドセット
    val selectedMindset: MindsetType? = null,
    val selectedPracticeType: String? = null,

    // セッション
    val currentSession: SixHabitsSession? = null,
    val sessions: List<SixHabitsSession> = emptyList(),

    // 日次トラッキング
    val todayTracking: SixHabitsDailyTracking? = null,

    // 素材
    val materials: List<SixHabitsMaterial> = emptyList(),
    val materialStats: Map<String, Int> = emptyMap(),

    // マインドセット①用入力
    val inputScene: String = "",
    val inputTitle: String = "",
    val inputPerspective1: String = "",
    val inputPerspective2: String = "",
    val inputPerspective3: String = "",
    val inputWhyChain: List<String> = listOf("", "", "", "", ""),
    val inputFeeling: String = "",

    // マインドセット②用入力
    val inputPhenomenon: String = "",
    val inputNewMetaphor: String = "",
    val inputOriginalMetaphor: String = "",
    val inputTransformedMetaphor1: String = "",
    val inputTransformedMetaphor2: String = "",
    val inputTransformedMetaphor3: String = "",
    val inputAbstractEmotion: String = "",
    val inputConcreteThing: String = "",

    // マインドセット③用入力
    val inputObservationTarget: String = "",
    val inputFormColor: String = "",
    val inputTextureWeight: String = "",
    val inputDialogueImagination: String = "",
    val inputNegativeSpace: String = "",
    val inputQuestion1: String = "",
    val inputQuestion2: String = "",
    val inputQuestion3: String = "",

    // マインドセット④用入力
    val inputFactLayer: String = "",
    val inputEmotionLayer: String = "",
    val inputUniversalLayer: String = "",
    val inputEmotionToColor: String = "",
    val inputEmotionToSound: String = "",
    val inputEmotionToTexture: String = "",
    val inputFailure: String = "",
    val inputFailureAsStory: String = "",

    // マインドセット⑤用入力
    val inputCurrentActivity: String = "",
    val inputWhyActivity: String = "",
    val inputDilemma: String = "",
    val inputFriendAdvice: String = "",
    val inputDailyScore: Int = 5,
    val inputScoreReason1: String = "",
    val inputScoreReason2: String = "",
    val inputScoreReason3: String = "",
    val inputTomorrowPlan: String = "",

    // マインドセット⑥用入力
    val inputSacredSpace: String = "",
    val inputStartRitual: String = "",
    val inputEndRitual: String = "",
    val inputRitualNotes: String = "",

    // ★追加: 振り返り用
    val inputReflection: String = "",
    val inputLearning: String = "",

    // UI状態
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val showMaterialPicker: Boolean = false
)

@HiltViewModel
class SixHabitsViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SixHabitsUiState())
    val uiState: StateFlow<SixHabitsUiState> = _uiState.asStateFlow()

    init {
        loadTodayTracking()
        loadMaterialStats()
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
    // ★追加: 画面ナビゲーション
    // ====================

    fun selectMindset(mindset: MindsetType) {
        _uiState.update { it.copy(
            selectedMindset = mindset,
            selectedPracticeType = null,
            screenState = SixHabitsScreenState.PRACTICE_SELECTION
        )}
        loadSessionsForMindset(mindset)
    }

    fun selectPracticeType(practiceType: String) {
        _uiState.update { it.copy(selectedPracticeType = practiceType) }
        startNewSession()
        _uiState.update { it.copy(
            screenState = SixHabitsScreenState.TRAINING,
            currentTrainingStep = SixHabitsTrainingStep.GUIDE
        )}
    }

    fun backToHabitSelection() {
        _uiState.update { it.copy(
            screenState = SixHabitsScreenState.HABIT_SELECTION,
            selectedMindset = null,
            selectedPracticeType = null,
            currentSession = null,
            currentTrainingStep = SixHabitsTrainingStep.GUIDE
        )}
        resetInputFields()
    }

    fun backToPracticeSelection() {
        _uiState.update { it.copy(
            screenState = SixHabitsScreenState.PRACTICE_SELECTION,
            selectedPracticeType = null,
            currentSession = null,
            currentTrainingStep = SixHabitsTrainingStep.GUIDE
        )}
        resetInputFields()
    }

    fun clearSelection() {
        _uiState.update { it.copy(
            selectedMindset = null,
            selectedPracticeType = null,
            currentSession = null,
            screenState = SixHabitsScreenState.HABIT_SELECTION
        )}
    }

    // ====================
    // ★追加: トレーニングステップ管理
    // ====================

    fun nextTrainingStep() {
        val currentIndex = _uiState.value.currentTrainingStep.ordinal
        if (currentIndex < SixHabitsTrainingStep.entries.size - 1) {
            _uiState.update { it.copy(
                currentTrainingStep = SixHabitsTrainingStep.entries[currentIndex + 1]
            )}
        }
    }

    fun previousTrainingStep() {
        val currentIndex = _uiState.value.currentTrainingStep.ordinal
        if (currentIndex > 0) {
            _uiState.update { it.copy(
                currentTrainingStep = SixHabitsTrainingStep.entries[currentIndex - 1]
            )}
        }
    }

    // ====================
    // ★追加: 中断確認
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
            selectedMindset = null,
            selectedPracticeType = null,
            currentSession = null
        )}
        resetInputFields()
    }

    // ====================
    // セッション管理
    // ====================

    private fun loadSessionsForMindset(mindset: MindsetType) {
        viewModelScope.launch {
            repository.getSixHabitsSessionsByMindsetType(mindset.number).collect { sessions ->
                _uiState.update { it.copy(sessions = sessions) }
            }
        }
    }

    private fun startNewSession() {
        val state = _uiState.value
        val mindset = state.selectedMindset ?: return
        val practiceType = state.selectedPracticeType ?: return

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val newSession = SixHabitsSession(
            mindsetType = mindset.number,
            practiceType = practiceType,
            sessionDate = today,
            createdAt = now,
            updatedAt = now
        )

        _uiState.update { it.copy(currentSession = newSession) }
        resetInputFields()
    }

    fun loadSession(session: SixHabitsSession) {
        _uiState.update { it.copy(
            currentSession = session,
            selectedMindset = MindsetType.fromNumber(session.mindsetType),
            selectedPracticeType = session.practiceType,
            screenState = SixHabitsScreenState.TRAINING,
            currentTrainingStep = SixHabitsTrainingStep.GUIDE
        )}
        populateInputFields(session)
    }

    fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val updatedSession = buildSessionFromInputs(session.copy(updatedAt = now))

            try {
                val savedId = repository.saveSixHabitsSession(updatedSession)
                _uiState.update { it.copy(
                    currentSession = updatedSession.copy(id = savedId),
                    statusMessage = "保存しました"
                )}

                // 素材として保存
                saveMaterialsFromSession(updatedSession.copy(id = savedId))

                // 日次トラッキング更新
                updateDailyTracking()
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "保存エラー: ${e.message}") }
            }
        }
    }

    fun deleteSession(session: SixHabitsSession) {
        viewModelScope.launch {
            try {
                repository.deleteSixHabitsSession(session)
                _uiState.update { it.copy(statusMessage = "削除しました") }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "削除エラー: ${e.message}") }
            }
        }
    }

    fun completeSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val completedSession = buildSessionFromInputs(
                session.copy(isCompleted = true, updatedAt = now)
            )

            try {
                repository.saveSixHabitsSession(completedSession)
                saveMaterialsFromSession(completedSession)
                updateDailyTracking()
                _uiState.update { it.copy(
                    currentSession = null,
                    screenState = SixHabitsScreenState.COMPLETE,
                    statusMessage = "セッションを完了しました"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "完了エラー: ${e.message}") }
            }
        }
    }

    // ====================
    // 日次トラッキング
    // ====================

    private fun loadTodayTracking() {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val tracking = repository.getSixHabitsDailyTrackingByDate(today)
            _uiState.update { it.copy(todayTracking = tracking) }
        }
    }

    private fun updateDailyTracking() {
        viewModelScope.launch {
            val state = _uiState.value
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val currentTracking = state.todayTracking ?: SixHabitsDailyTracking(
                date = today,
                createdAt = now
            )

            val practiceType = state.selectedPracticeType ?: return@launch

            val updatedTracking = when (practiceType) {
                SixHabitsPracticeTypes.TITLE_NAMING ->
                    currentTracking.copy(titleCount = currentTracking.titleCount + 1, updatedAt = now)
                SixHabitsPracticeTypes.PERSPECTIVE_SHIFT ->
                    currentTracking.copy(perspectiveChanged = true, updatedAt = now)
                SixHabitsPracticeTypes.WHY_CHAIN ->
                    currentTracking.copy(whyChainDone = true, updatedAt = now)
                SixHabitsPracticeTypes.NEW_METAPHOR ->
                    currentTracking.copy(newMetaphorCount = currentTracking.newMetaphorCount + 1, updatedAt = now)
                SixHabitsPracticeTypes.TRANSFORM_METAPHOR ->
                    currentTracking.copy(metaphorTransformed = true, updatedAt = now)
                SixHabitsPracticeTypes.ABSTRACT_TO_CONCRETE ->
                    currentTracking.copy(emotionToConcreteCount = currentTracking.emotionToConcreteCount + 1, updatedAt = now)
                SixHabitsPracticeTypes.TEN_MINUTE_OBSERVATION ->
                    currentTracking.copy(observationMinutes = currentTracking.observationMinutes + 10, updatedAt = now)
                SixHabitsPracticeTypes.NEGATIVE_SPACE ->
                    currentTracking.copy(negativeSpaceDone = true, updatedAt = now)
                SixHabitsPracticeTypes.QUESTION_TO_OBJECT ->
                    currentTracking.copy(objectQuestionDone = true, updatedAt = now)
                SixHabitsPracticeTypes.THREE_LAYER_RECORD ->
                    currentTracking.copy(threeLayerRecordDone = true, updatedAt = now)
                SixHabitsPracticeTypes.EMOTION_TO_SENSE ->
                    currentTracking.copy(emotionToSenseDone = true, updatedAt = now)
                SixHabitsPracticeTypes.FAILURE_AS_MATERIAL ->
                    currentTracking.copy(failureAsMaterialDone = true, updatedAt = now)
                SixHabitsPracticeTypes.SELF_QUESTIONING ->
                    currentTracking.copy(selfQuestionCount = currentTracking.selfQuestionCount + 1, updatedAt = now)
                SixHabitsPracticeTypes.FRIEND_ADVICE ->
                    currentTracking.copy(friendAdviceDone = true, updatedAt = now)
                SixHabitsPracticeTypes.DAILY_SCORING ->
                    currentTracking.copy(dailyScoringDone = true, updatedAt = now)
                SixHabitsPracticeTypes.START_RITUAL ->
                    currentTracking.copy(startRitualDone = true, updatedAt = now)
                SixHabitsPracticeTypes.END_RITUAL ->
                    currentTracking.copy(endRitualDone = true, updatedAt = now)
                else -> currentTracking.copy(updatedAt = now)
            }

            repository.saveSixHabitsDailyTracking(updatedTracking)
            _uiState.update { it.copy(todayTracking = updatedTracking) }
        }
    }

    // ====================
    // 素材管理
    // ====================

    private fun loadMaterialStats() {
        viewModelScope.launch {
            val stats = mutableMapOf<String, Int>()
            listOf("metaphor", "title", "emotion_to_sense", "perspective", "observation").forEach { type ->
                stats[type] = repository.countSixHabitsMaterialsByType(type)
            }
            _uiState.update { it.copy(materialStats = stats) }
        }
    }

    private suspend fun saveMaterialsFromSession(session: SixHabitsSession) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        // タイトルを保存
        if (session.sceneTitle.isNotBlank()) {
            repository.saveSixHabitsMaterial(SixHabitsMaterial(
                materialType = "title",
                content = session.sceneTitle,
                category = "scene",
                sourceSessionId = session.id,
                createdAt = now
            ))
        }

        // 比喩を保存
        if (session.transformedMetaphor.isNotBlank()) {
            repository.saveSixHabitsMaterial(SixHabitsMaterial(
                materialType = "metaphor",
                content = session.transformedMetaphor,
                category = "transformed",
                sourceSessionId = session.id,
                createdAt = now
            ))
        }

        // 感情→五感変換を保存
        if (session.emotionToColor.isNotBlank() || session.emotionToSound.isNotBlank() || session.emotionToTexture.isNotBlank()) {
            val content = buildString {
                appendLine("色: ${session.emotionToColor}")
                appendLine("音: ${session.emotionToSound}")
                appendLine("触感: ${session.emotionToTexture}")
            }
            repository.saveSixHabitsMaterial(SixHabitsMaterial(
                materialType = "emotion_to_sense",
                content = content,
                category = session.emotionLayer,
                sourceSessionId = session.id,
                createdAt = now
            ))
        }

        loadMaterialStats()
    }

    // ====================
    // 入力フィールド更新
    // ====================

    // マインドセット①
    fun updateInputScene(value: String) = _uiState.update { it.copy(inputScene = value) }
    fun updateInputTitle(value: String) = _uiState.update { it.copy(inputTitle = value) }
    fun updateInputPerspective1(value: String) = _uiState.update { it.copy(inputPerspective1 = value) }
    fun updateInputPerspective2(value: String) = _uiState.update { it.copy(inputPerspective2 = value) }
    fun updateInputPerspective3(value: String) = _uiState.update { it.copy(inputPerspective3 = value) }
    fun updateInputWhyChain(index: Int, value: String) {
        _uiState.update {
            val newList = it.inputWhyChain.toMutableList()
            if (index in newList.indices) newList[index] = value
            it.copy(inputWhyChain = newList)
        }
    }
    fun updateInputFeeling(value: String) = _uiState.update { it.copy(inputFeeling = value) }

    // マインドセット②
    fun updateInputPhenomenon(value: String) = _uiState.update { it.copy(inputPhenomenon = value) }
    fun updateInputNewMetaphor(value: String) = _uiState.update { it.copy(inputNewMetaphor = value) }
    fun updateInputOriginalMetaphor(value: String) = _uiState.update { it.copy(inputOriginalMetaphor = value) }
    fun updateInputTransformedMetaphor1(value: String) = _uiState.update { it.copy(inputTransformedMetaphor1 = value) }
    fun updateInputTransformedMetaphor2(value: String) = _uiState.update { it.copy(inputTransformedMetaphor2 = value) }
    fun updateInputTransformedMetaphor3(value: String) = _uiState.update { it.copy(inputTransformedMetaphor3 = value) }
    fun updateInputAbstractEmotion(value: String) = _uiState.update { it.copy(inputAbstractEmotion = value) }
    fun updateInputConcreteThing(value: String) = _uiState.update { it.copy(inputConcreteThing = value) }

    // マインドセット③
    fun updateInputObservationTarget(value: String) = _uiState.update { it.copy(inputObservationTarget = value) }
    fun updateInputFormColor(value: String) = _uiState.update { it.copy(inputFormColor = value) }
    fun updateInputTextureWeight(value: String) = _uiState.update { it.copy(inputTextureWeight = value) }
    fun updateInputDialogueImagination(value: String) = _uiState.update { it.copy(inputDialogueImagination = value) }
    fun updateInputNegativeSpace(value: String) = _uiState.update { it.copy(inputNegativeSpace = value) }
    fun updateInputQuestion1(value: String) = _uiState.update { it.copy(inputQuestion1 = value) }
    fun updateInputQuestion2(value: String) = _uiState.update { it.copy(inputQuestion2 = value) }
    fun updateInputQuestion3(value: String) = _uiState.update { it.copy(inputQuestion3 = value) }

    // マインドセット④
    fun updateInputFactLayer(value: String) = _uiState.update { it.copy(inputFactLayer = value) }
    fun updateInputEmotionLayer(value: String) = _uiState.update { it.copy(inputEmotionLayer = value) }
    fun updateInputUniversalLayer(value: String) = _uiState.update { it.copy(inputUniversalLayer = value) }
    fun updateInputEmotionToColor(value: String) = _uiState.update { it.copy(inputEmotionToColor = value) }
    fun updateInputEmotionToSound(value: String) = _uiState.update { it.copy(inputEmotionToSound = value) }
    fun updateInputEmotionToTexture(value: String) = _uiState.update { it.copy(inputEmotionToTexture = value) }
    fun updateInputFailure(value: String) = _uiState.update { it.copy(inputFailure = value) }
    fun updateInputFailureAsStory(value: String) = _uiState.update { it.copy(inputFailureAsStory = value) }

    // マインドセット⑤
    fun updateInputCurrentActivity(value: String) = _uiState.update { it.copy(inputCurrentActivity = value) }
    fun updateInputWhyActivity(value: String) = _uiState.update { it.copy(inputWhyActivity = value) }
    fun updateInputDilemma(value: String) = _uiState.update { it.copy(inputDilemma = value) }
    fun updateInputFriendAdvice(value: String) = _uiState.update { it.copy(inputFriendAdvice = value) }
    fun updateInputDailyScore(value: Int) = _uiState.update { it.copy(inputDailyScore = value.coerceIn(1, 10)) }
    fun updateInputScoreReason1(value: String) = _uiState.update { it.copy(inputScoreReason1 = value) }
    fun updateInputScoreReason2(value: String) = _uiState.update { it.copy(inputScoreReason2 = value) }
    fun updateInputScoreReason3(value: String) = _uiState.update { it.copy(inputScoreReason3 = value) }
    fun updateInputTomorrowPlan(value: String) = _uiState.update { it.copy(inputTomorrowPlan = value) }

    // マインドセット⑥
    fun updateInputSacredSpace(value: String) = _uiState.update { it.copy(inputSacredSpace = value) }
    fun updateInputStartRitual(value: String) = _uiState.update { it.copy(inputStartRitual = value) }
    fun updateInputEndRitual(value: String) = _uiState.update { it.copy(inputEndRitual = value) }
    fun updateInputRitualNotes(value: String) = _uiState.update { it.copy(inputRitualNotes = value) }

    // ★追加: 振り返り
    fun updateInputReflection(value: String) = _uiState.update { it.copy(inputReflection = value) }
    fun updateInputLearning(value: String) = _uiState.update { it.copy(inputLearning = value) }

    // ====================
    // ヘルパー関数
    // ====================

    private fun resetInputFields() {
        _uiState.update { it.copy(
            inputScene = "",
            inputTitle = "",
            inputPerspective1 = "",
            inputPerspective2 = "",
            inputPerspective3 = "",
            inputWhyChain = listOf("", "", "", "", ""),
            inputFeeling = "",
            inputPhenomenon = "",
            inputNewMetaphor = "",
            inputOriginalMetaphor = "",
            inputTransformedMetaphor1 = "",
            inputTransformedMetaphor2 = "",
            inputTransformedMetaphor3 = "",
            inputAbstractEmotion = "",
            inputConcreteThing = "",
            inputObservationTarget = "",
            inputFormColor = "",
            inputTextureWeight = "",
            inputDialogueImagination = "",
            inputNegativeSpace = "",
            inputQuestion1 = "",
            inputQuestion2 = "",
            inputQuestion3 = "",
            inputFactLayer = "",
            inputEmotionLayer = "",
            inputUniversalLayer = "",
            inputEmotionToColor = "",
            inputEmotionToSound = "",
            inputEmotionToTexture = "",
            inputFailure = "",
            inputFailureAsStory = "",
            inputCurrentActivity = "",
            inputWhyActivity = "",
            inputDilemma = "",
            inputFriendAdvice = "",
            inputDailyScore = 5,
            inputScoreReason1 = "",
            inputScoreReason2 = "",
            inputScoreReason3 = "",
            inputTomorrowPlan = "",
            inputSacredSpace = "",
            inputStartRitual = "",
            inputEndRitual = "",
            inputRitualNotes = "",
            inputReflection = "",
            inputLearning = ""
        )}
    }

    private fun populateInputFields(session: SixHabitsSession) {
        _uiState.update { it.copy(
            inputTitle = session.sceneTitle,
            inputPerspective1 = session.perspective1,
            inputPerspective2 = session.perspective2,
            inputPerspective3 = session.perspective3,
            inputOriginalMetaphor = session.originalMetaphor,
            inputNewMetaphor = session.transformedMetaphor,
            inputAbstractEmotion = session.abstractConcept,
            inputConcreteThing = session.concreteThing,
            inputObservationTarget = session.observationTarget,
            inputFormColor = session.observationNotes,
            inputDialogueImagination = session.dialogueAnswer,
            inputNegativeSpace = session.negativeSpace,
            inputFactLayer = session.factLayer,
            inputEmotionLayer = session.emotionLayer,
            inputUniversalLayer = session.universalLayer,
            inputEmotionToColor = session.emotionToColor,
            inputEmotionToSound = session.emotionToSound,
            inputEmotionToTexture = session.emotionToTexture,
            inputFailureAsStory = session.failureAsStory,
            inputFriendAdvice = session.friendAdvice,
            inputDailyScore = session.dailyScore,
            inputScoreReason1 = session.scoreReason1,
            inputScoreReason2 = session.scoreReason2,
            inputScoreReason3 = session.scoreReason3,
            inputTomorrowPlan = session.tomorrowPlan,
            inputSacredSpace = session.sacredSpace,
            inputStartRitual = session.startRitual,
            inputEndRitual = session.endRitual,
            inputRitualNotes = session.ritualNotes
        )}
    }

    private fun buildSessionFromInputs(session: SixHabitsSession): SixHabitsSession {
        val state = _uiState.value
        return session.copy(
            sceneTitle = state.inputTitle,
            perspective1 = state.inputPerspective1,
            perspective2 = state.inputPerspective2,
            perspective3 = state.inputPerspective3,
            whyChain = state.inputWhyChain.joinToString("\n"),
            originalMetaphor = state.inputOriginalMetaphor,
            transformedMetaphor = state.inputNewMetaphor,
            abstractConcept = state.inputAbstractEmotion,
            concreteThing = state.inputConcreteThing,
            observationTarget = state.inputObservationTarget,
            observationNotes = state.inputFormColor,
            dialogueQuestion = "${state.inputQuestion1}\n${state.inputQuestion2}\n${state.inputQuestion3}",
            dialogueAnswer = state.inputDialogueImagination,
            negativeSpace = state.inputNegativeSpace,
            factLayer = state.inputFactLayer,
            emotionLayer = state.inputEmotionLayer,
            universalLayer = state.inputUniversalLayer,
            emotionToColor = state.inputEmotionToColor,
            emotionToSound = state.inputEmotionToSound,
            emotionToTexture = state.inputEmotionToTexture,
            failureAsStory = state.inputFailureAsStory,
            selfQuestion = state.inputCurrentActivity,
            selfAnswer = state.inputWhyActivity,
            friendAdvice = state.inputFriendAdvice,
            dailyScore = state.inputDailyScore,
            scoreReason1 = state.inputScoreReason1,
            scoreReason2 = state.inputScoreReason2,
            scoreReason3 = state.inputScoreReason3,
            tomorrowPlan = state.inputTomorrowPlan,
            sacredSpace = state.inputSacredSpace,
            startRitual = state.inputStartRitual,
            endRitual = state.inputEndRitual,
            ritualNotes = state.inputRitualNotes
        )
    }

    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = "") }
}
