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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 物質-抽象変換 UI状態
 */
data class MaterialAbstractionUiState(
    // コース選択
    val selectedCourse: MaterialAbstractionCourse? = null,

    // セッション
    val currentSession: MaterialAbstractionSession? = null,
    val sessions: List<MaterialAbstractionSession> = emptyList(),

    // 物質→抽象コース用
    val m2aCurrentStep: MaterialToAbstractStep = MaterialToAbstractStep.MATERIAL_SELECTION,

    // 抽象→物質コース用
    val a2mCurrentStep: AbstractToMaterialStep = AbstractToMaterialStep.THEME_SELECTION,

    // ===== 物質→抽象コース入力 =====
    val inputMaterial: String = "",
    val materialValidationError: String = "",

    // 観察フェーズ（7項目に細分化）
    val inputObservationShape: String = "",      // 形
    val inputObservationColor: String = "",      // 色
    val inputObservationLight: String = "",      // 光
    val inputObservationTouch: String = "",      // 触感
    val inputObservationSmell: String = "",      // におい
    val inputObservationSound: String = "",      // 音
    val inputObservationContext: String = "",    // 状況

    // 特徴抽出
    val inputFeatureFormState: String = "",
    val inputFeatureTimePassage: String = "",
    val inputFeaturePositionPlacement: String = "",
    val inputFeatureCustom: String = "",
    val currentFeatureAspect: FeatureAspect = FeatureAspect.FORM_AND_STATE,

    // 連想フェーズ
    val inputAssociationFromFormState: String = "",
    val inputAssociationFromTimePassage: String = "",
    val inputAssociationFromPositionPlacement: String = "",
    val inputAssociationFromCustom: String = "",
    val inputStrongestAssociation: String = "",

    // ===== 抽象→物質コース入力 =====
    // テーマ理解
    val inputThemeDefinition: String = "",
    val inputThemeOrigin: String = "",
    val inputThemeOpposites: String = "",
    val inputThemeCharacteristics: String = "",

    // 物質候補
    val inputMaterialCandidate1: String = "",
    val inputMaterialCandidate2: String = "",
    val inputMaterialCandidate3: String = "",
    val inputMaterialCandidate4: String = "",
    val inputMaterialCandidate5: String = "",
    val inputCandidateReason1: String = "",
    val inputCandidateReason2: String = "",
    val inputCandidateReason3: String = "",
    val inputCandidateReason4: String = "",
    val inputCandidateReason5: String = "",

    // 物質型決定
    val chosenMaterialIndex: Int = -1,
    val inputChosenMaterialReason: String = "",

    // 物質の具体化
    val inputMaterialState: String = "",
    val inputMaterialContext: String = "",
    val inputMaterialCondition: String = "",

    // ===== 共通フィールド =====
    val selectedTheme: String = "",
    val isCustomTheme: Boolean = false,
    val inputCustomThemeDefinition: String = "",
    val currentForbiddenWords: List<String> = emptyList(),
    val suggestedThemes: List<String> = emptyList(),

    // 表現生成
    val inputGeneratedExpression: String = "",
    val forbiddenWordWarnings: List<String> = emptyList(),
    val sensoryWordCounts: Map<String, Int> = emptyMap(),
    val lineCount: Int = 0,
    val charCount: Int = 0,

    // チェックリスト
    val checklistStates: Map<String, Boolean> = ExpressionChecklist.items.associate { it.id to false },

    // UI状態
    val isLoading: Boolean = false,
    val showSessionPicker: Boolean = false,
    val showConfirmExit: Boolean = false,
    val statusMessage: String = ""
)

@HiltViewModel
class MaterialAbstractionViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialAbstractionUiState())
    val uiState: StateFlow<MaterialAbstractionUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    // ====================
    // コース選択
    // ====================

    fun selectCourse(course: MaterialAbstractionCourse) {
        _uiState.update { it.copy(selectedCourse = course) }
    }

    fun clearCourse() {
        _uiState.update { it.copy(selectedCourse = null, currentSession = null) }
        resetAllInputs()
    }

    // ====================
    // セッション管理
    // ====================

    private fun loadSessions() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.getAllMaterialAbstractionSessions().collect { sessions ->
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

    fun startNewSession() {
        val state = _uiState.value
        val course = state.selectedCourse ?: return

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val newSession = MaterialAbstractionSession(
            courseType = course.ordinal,
            createdAt = now,
            updatedAt = now
        )

        _uiState.update { it.copy(
            currentSession = newSession,
            m2aCurrentStep = MaterialToAbstractStep.MATERIAL_SELECTION,
            a2mCurrentStep = AbstractToMaterialStep.THEME_SELECTION
        )}
        resetAllInputs()
    }

    fun loadSession(session: MaterialAbstractionSession) {
        val course = MaterialAbstractionCourse.entries.getOrElse(session.courseType) {
            MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT
        }

        _uiState.update { it.copy(
            selectedCourse = course,
            currentSession = session,
            m2aCurrentStep = MaterialToAbstractStep.entries.getOrElse(session.currentStep) {
                MaterialToAbstractStep.MATERIAL_SELECTION
            },
            a2mCurrentStep = AbstractToMaterialStep.entries.getOrElse(session.currentStep) {
                AbstractToMaterialStep.THEME_SELECTION
            },
            // 物質→抽象コース用
            inputMaterial = session.selectedMaterial,
            inputObservationShape = session.observationVisual,
            inputObservationColor = session.observationTactile,
            inputObservationLight = session.observationAuditory,
            inputObservationTouch = session.observationOlfactory,
            inputObservationSmell = session.observationGustatory,
            inputObservationSound = session.observationSound,
            inputObservationContext = session.observationContext,
            inputFeatureFormState = session.featureFormState,
            inputFeatureTimePassage = session.featureTimePassage,
            inputFeaturePositionPlacement = session.featurePositionPlacement,
            inputFeatureCustom = session.featureCustom,
            inputAssociationFromFormState = session.associationFromFormState,
            inputAssociationFromTimePassage = session.associationFromTimePassage,
            inputAssociationFromPositionPlacement = session.associationFromPositionPlacement,
            inputAssociationFromCustom = session.associationFromCustom,
            inputStrongestAssociation = session.strongestAssociation,
            // 抽象→物質コース用
            inputThemeDefinition = session.themeDefinition,
            inputThemeOrigin = session.themeOrigin,
            inputThemeOpposites = session.themeOpposites,
            inputThemeCharacteristics = session.themeCharacteristics,
            inputMaterialCandidate1 = session.materialCandidate1,
            inputMaterialCandidate2 = session.materialCandidate2,
            inputMaterialCandidate3 = session.materialCandidate3,
            inputMaterialCandidate4 = session.materialCandidate4,
            inputMaterialCandidate5 = session.materialCandidate5,
            inputChosenMaterialReason = session.chosenMaterialReason,
            inputMaterialState = session.materialState,
            inputMaterialContext = session.materialContext,
            inputMaterialCondition = session.materialCondition,
            // 共通
            selectedTheme = session.selectedTheme,
            isCustomTheme = session.isCustomTheme,
            inputCustomThemeDefinition = session.customThemeDefinition,
            currentForbiddenWords = session.forbiddenWords.split(",").filter { it.isNotBlank() },
            inputGeneratedExpression = session.generatedExpression,
            showSessionPicker = false
        )}

        // 選択された物質のインデックスを復元
        if (session.chosenMaterial.isNotBlank()) {
            val candidates = listOf(
                session.materialCandidate1,
                session.materialCandidate2,
                session.materialCandidate3,
                session.materialCandidate4,
                session.materialCandidate5
            )
            val index = candidates.indexOf(session.chosenMaterial)
            if (index >= 0) {
                _uiState.update { it.copy(chosenMaterialIndex = index) }
            }
        }

        // 表現があれば解析
        if (session.generatedExpression.isNotBlank()) {
            checkForbiddenWords(session.generatedExpression)
            updateSensoryWordCounts(session.generatedExpression)
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch
            val course = state.selectedCourse ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val counts = SensoryKeywords.countSensoryWords(state.inputGeneratedExpression)

            val currentStep = when (course) {
                MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT -> state.m2aCurrentStep.ordinal
                MaterialAbstractionCourse.ABSTRACT_TO_MATERIAL -> state.a2mCurrentStep.ordinal
            }

            val sessionTitle = when (course) {
                MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT ->
                    if (state.inputMaterial.isNotBlank() && state.selectedTheme.isNotBlank())
                        "${state.inputMaterial.take(10)}→${state.selectedTheme.take(10)}"
                    else state.inputMaterial.take(20).ifBlank { "新規セッション" }
                MaterialAbstractionCourse.ABSTRACT_TO_MATERIAL ->
                    if (state.selectedTheme.isNotBlank() && getChosenMaterial(state).isNotBlank())
                        "${state.selectedTheme.take(10)}→${getChosenMaterial(state).take(10)}"
                    else state.selectedTheme.take(20).ifBlank { "新規セッション" }
            }

            val updatedSession = session.copy(
                sessionTitle = sessionTitle,
                courseType = course.ordinal,
                currentStep = currentStep,
                // 物質→抽象コース用
                selectedMaterial = state.inputMaterial,
                observationVisual = state.inputObservationShape,
                observationTactile = state.inputObservationColor,
                observationAuditory = state.inputObservationLight,
                observationOlfactory = state.inputObservationTouch,
                observationGustatory = state.inputObservationSmell,
                observationSound = state.inputObservationSound,
                observationContext = state.inputObservationContext,
                featureFormState = state.inputFeatureFormState,
                featureTimePassage = state.inputFeatureTimePassage,
                featurePositionPlacement = state.inputFeaturePositionPlacement,
                featureCustom = state.inputFeatureCustom,
                associationFromFormState = state.inputAssociationFromFormState,
                associationFromTimePassage = state.inputAssociationFromTimePassage,
                associationFromPositionPlacement = state.inputAssociationFromPositionPlacement,
                associationFromCustom = state.inputAssociationFromCustom,
                strongestAssociation = state.inputStrongestAssociation,
                // 抽象→物質コース用
                themeDefinition = state.inputThemeDefinition,
                themeOrigin = state.inputThemeOrigin,
                themeOpposites = state.inputThemeOpposites,
                themeCharacteristics = state.inputThemeCharacteristics,
                materialCandidate1 = state.inputMaterialCandidate1,
                materialCandidate2 = state.inputMaterialCandidate2,
                materialCandidate3 = state.inputMaterialCandidate3,
                materialCandidate4 = state.inputMaterialCandidate4,
                materialCandidate5 = state.inputMaterialCandidate5,
                chosenMaterial = getChosenMaterial(state),
                chosenMaterialReason = state.inputChosenMaterialReason,
                materialState = state.inputMaterialState,
                materialContext = state.inputMaterialContext,
                materialCondition = state.inputMaterialCondition,
                // 共通
                selectedTheme = state.selectedTheme,
                isCustomTheme = state.isCustomTheme,
                customThemeDefinition = state.inputCustomThemeDefinition,
                forbiddenWords = state.currentForbiddenWords.joinToString(","),
                generatedExpression = state.inputGeneratedExpression,
                // フィードバック
                feedbackVisualCount = counts["visual"] ?: 0,
                feedbackTactileCount = counts["tactile"] ?: 0,
                feedbackAuditoryCount = counts["auditory"] ?: 0,
                feedbackOlfactoryCount = counts["olfactory"] ?: 0,
                feedbackGustatoryCount = counts["gustatory"] ?: 0,
                feedbackMetaphorCount = counts["metaphor"] ?: 0,
                feedbackForbiddenWordUsed = state.forbiddenWordWarnings.isNotEmpty(),
                updatedAt = now
            )

            try {
                val savedId = repository.saveMaterialAbstractionSession(updatedSession)
                _uiState.update { it.copy(
                    currentSession = updatedSession.copy(id = savedId),
                    statusMessage = "保存しました"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "保存エラー: ${e.message}") }
            }
        }
    }

    private fun getChosenMaterial(state: MaterialAbstractionUiState): String {
        return when (state.chosenMaterialIndex) {
            0 -> state.inputMaterialCandidate1
            1 -> state.inputMaterialCandidate2
            2 -> state.inputMaterialCandidate3
            3 -> state.inputMaterialCandidate4
            4 -> state.inputMaterialCandidate5
            else -> ""
        }
    }

    fun deleteSession(session: MaterialAbstractionSession) {
        viewModelScope.launch {
            try {
                repository.deleteMaterialAbstractionSession(session)
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
            val course = state.selectedCourse ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val counts = SensoryKeywords.countSensoryWords(state.inputGeneratedExpression)

            val finalStep = when (course) {
                MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT -> MaterialToAbstractStep.RESULT_DISPLAY.ordinal
                MaterialAbstractionCourse.ABSTRACT_TO_MATERIAL -> AbstractToMaterialStep.RESULT_DISPLAY.ordinal
            }

            val completedSession = session.copy(
                currentStep = finalStep,
                isCompleted = true,
                updatedAt = now,
                generatedExpression = state.inputGeneratedExpression,
                feedbackVisualCount = counts["visual"] ?: 0,
                feedbackTactileCount = counts["tactile"] ?: 0,
                feedbackAuditoryCount = counts["auditory"] ?: 0,
                feedbackOlfactoryCount = counts["olfactory"] ?: 0,
                feedbackGustatoryCount = counts["gustatory"] ?: 0,
                feedbackMetaphorCount = counts["metaphor"] ?: 0,
                feedbackForbiddenWordUsed = state.forbiddenWordWarnings.isNotEmpty()
            )

            try {
                repository.saveMaterialAbstractionSession(completedSession)
                // 結果画面へ遷移
                when (course) {
                    MaterialAbstractionCourse.MATERIAL_TO_ABSTRACT ->
                        _uiState.update { it.copy(
                            currentSession = completedSession,
                            m2aCurrentStep = MaterialToAbstractStep.RESULT_DISPLAY,
                            statusMessage = "トレーニングを完了しました"
                        )}
                    MaterialAbstractionCourse.ABSTRACT_TO_MATERIAL ->
                        _uiState.update { it.copy(
                            currentSession = completedSession,
                            a2mCurrentStep = AbstractToMaterialStep.RESULT_DISPLAY,
                            statusMessage = "トレーニングを完了しました"
                        )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "完了エラー: ${e.message}") }
            }
        }
    }

    // ====================
    // ステップナビゲーション（物質→抽象コース）
    // ====================

    fun nextM2AStep() {
        val state = _uiState.value
        val currentIndex = state.m2aCurrentStep.ordinal

        // バリデーション
        when (state.m2aCurrentStep) {
            MaterialToAbstractStep.MATERIAL_SELECTION -> {
                if (!validateMaterial()) return
            }
            MaterialToAbstractStep.OBSERVATION -> {
                if (!validateObservation()) return
            }
            MaterialToAbstractStep.FEATURE_EXTRACTION -> {
                if (!validateFeatureExtraction()) return
            }
            MaterialToAbstractStep.ASSOCIATION -> {
                if (state.inputStrongestAssociation.isBlank()) {
                    _uiState.update { it.copy(statusMessage = "最強の連想を入力してください") }
                    return
                }
            }
            MaterialToAbstractStep.CONCEPTUALIZATION -> {
                if (state.selectedTheme.isBlank()) {
                    _uiState.update { it.copy(statusMessage = "テーマを選択または入力してください") }
                    return
                }
                setupForbiddenWords()
            }
            MaterialToAbstractStep.EXPRESSION_GENERATION -> {
                if (!validateExpression()) return
                completeSession()
                return
            }
            MaterialToAbstractStep.RESULT_DISPLAY -> {
                // 結果画面では何もしない
                return
            }
        }

        if (currentIndex < MaterialToAbstractStep.entries.size - 1) {
            _uiState.update { it.copy(
                m2aCurrentStep = MaterialToAbstractStep.entries[currentIndex + 1]
            )}
            saveSession()
        }
    }

    fun previousM2AStep() {
        val currentIndex = _uiState.value.m2aCurrentStep.ordinal
        if (currentIndex > 0) {
            _uiState.update { it.copy(
                m2aCurrentStep = MaterialToAbstractStep.entries[currentIndex - 1]
            )}
        }
    }

    fun goToM2AStep(step: MaterialToAbstractStep) {
        _uiState.update { it.copy(m2aCurrentStep = step) }
    }

    // ====================
    // ステップナビゲーション（抽象→物質コース）
    // ====================

    fun nextA2MStep() {
        val state = _uiState.value
        val currentIndex = state.a2mCurrentStep.ordinal

        // バリデーション
        when (state.a2mCurrentStep) {
            AbstractToMaterialStep.THEME_SELECTION -> {
                if (state.selectedTheme.isBlank()) {
                    _uiState.update { it.copy(statusMessage = "テーマを選択してください") }
                    return
                }
                prefillThemeInfo()
                setupForbiddenWords()
            }
            AbstractToMaterialStep.THEME_UNDERSTANDING -> {
                if (!validateThemeUnderstanding()) return
            }
            AbstractToMaterialStep.MATERIAL_CANDIDATES -> {
                if (!validateMaterialCandidates()) return
            }
            AbstractToMaterialStep.MATERIAL_DECISION -> {
                if (state.chosenMaterialIndex < 0) {
                    _uiState.update { it.copy(statusMessage = "物質を選択してください") }
                    return
                }
            }
            AbstractToMaterialStep.MATERIAL_SPECIFICATION -> {
                if (!validateMaterialSpecification()) return
            }
            AbstractToMaterialStep.DESCRIPTION -> {
                if (!validateExpression()) return
                completeSession()
                return
            }
            AbstractToMaterialStep.RESULT_DISPLAY -> {
                // 結果画面では何もしない
                return
            }
        }

        if (currentIndex < AbstractToMaterialStep.entries.size - 1) {
            _uiState.update { it.copy(
                a2mCurrentStep = AbstractToMaterialStep.entries[currentIndex + 1]
            )}
            saveSession()
        }
    }

    fun previousA2MStep() {
        val currentIndex = _uiState.value.a2mCurrentStep.ordinal
        if (currentIndex > 0) {
            _uiState.update { it.copy(
                a2mCurrentStep = AbstractToMaterialStep.entries[currentIndex - 1]
            )}
        }
    }

    fun goToA2MStep(step: AbstractToMaterialStep) {
        _uiState.update { it.copy(a2mCurrentStep = step) }
    }

    // ====================
    // バリデーション
    // ====================

    private fun validateMaterial(): Boolean {
        val material = _uiState.value.inputMaterial.trim()

        if (material.isBlank()) {
            _uiState.update { it.copy(materialValidationError = "物質を入力してください") }
            return false
        }

        val abstractWords = listOf("愛", "幸せ", "時間", "希望", "絶望", "自由", "平和", "正義", "悲しみ", "喜び", "怒り", "恐怖")
        if (abstractWords.any { material.contains(it) }) {
            _uiState.update { it.copy(
                materialValidationError = "抽象的な概念ではなく、触れることができる具体的な物を選んでください"
            )}
            return false
        }

        _uiState.update { it.copy(materialValidationError = "") }
        return true
    }

    private fun validateObservation(): Boolean {
        val state = _uiState.value
        val filledCount = listOf(
            state.inputObservationShape,
            state.inputObservationColor,
            state.inputObservationLight,
            state.inputObservationTouch,
            state.inputObservationSmell,
            state.inputObservationSound,
            state.inputObservationContext
        ).count { it.isNotBlank() }

        if (filledCount < 5) {
            _uiState.update { it.copy(statusMessage = "最低5項目は記入してください（現在: ${filledCount}項目）") }
            return false
        }
        return true
    }

    private fun validateFeatureExtraction(): Boolean {
        val state = _uiState.value
        val filledCount = listOf(
            state.inputFeatureFormState,
            state.inputFeatureTimePassage,
            state.inputFeaturePositionPlacement,
            state.inputFeatureCustom
        ).count { it.isNotBlank() }

        if (filledCount < 2) {
            _uiState.update { it.copy(statusMessage = "最低2つの観点で特徴を抽出してください（現在: ${filledCount}つ）") }
            return false
        }
        return true
    }

    private fun validateThemeUnderstanding(): Boolean {
        val state = _uiState.value
        if (state.inputThemeDefinition.isBlank()) {
            _uiState.update { it.copy(statusMessage = "テーマの定義を入力してください") }
            return false
        }
        return true
    }

    private fun validateMaterialCandidates(): Boolean {
        val state = _uiState.value
        val candidates = listOf(
            state.inputMaterialCandidate1,
            state.inputMaterialCandidate2,
            state.inputMaterialCandidate3,
            state.inputMaterialCandidate4,
            state.inputMaterialCandidate5
        ).filter { it.isNotBlank() }

        if (candidates.size < 3) {
            _uiState.update { it.copy(statusMessage = "最低3つの物質候補を入力してください（現在: ${candidates.size}つ）") }
            return false
        }
        return true
    }

    private fun validateMaterialSpecification(): Boolean {
        val state = _uiState.value
        if (state.inputMaterialState.isBlank()) {
            _uiState.update { it.copy(statusMessage = "物質の状態を記述してください") }
            return false
        }
        return true
    }

    private fun validateExpression(): Boolean {
        val state = _uiState.value
        val text = state.inputGeneratedExpression

        if (text.isBlank()) {
            _uiState.update { it.copy(statusMessage = "表現を入力してください") }
            return false
        }

        if (text.length < 50) {
            _uiState.update { it.copy(statusMessage = "表現が短すぎます（最低50文字）") }
            return false
        }

        if (state.forbiddenWordWarnings.isNotEmpty()) {
            _uiState.update { it.copy(
                statusMessage = "禁止ワードが含まれています: ${state.forbiddenWordWarnings.joinToString(", ")}\n書き直してください。"
            )}
            return false
        }

        return true
    }

    // ====================
    // テーマ関連
    // ====================

    fun selectTheme(theme: String, isCustom: Boolean = false) {
        _uiState.update { it.copy(
            selectedTheme = theme,
            isCustomTheme = isCustom
        )}
        if (!isCustom) {
            setupForbiddenWords()
        }
    }

    fun selectRandomTheme() {
        val randomTheme = EmotionThemeDatabase.getRandomTheme()
        _uiState.update { it.copy(
            selectedTheme = randomTheme.name,
            isCustomTheme = false
        )}
        setupForbiddenWords()
    }

    private fun prefillThemeInfo() {
        val theme = _uiState.value.selectedTheme
        val themeInfo = EmotionThemeDatabase.getTheme(theme)

        if (themeInfo != null) {
            _uiState.update { it.copy(
                inputThemeDefinition = themeInfo.definition,
                inputThemeOpposites = themeInfo.opposites.joinToString(", "),
                currentForbiddenWords = themeInfo.forbiddenWords
            )}
        }
    }

    private fun setupForbiddenWords() {
        val state = _uiState.value
        val theme = state.selectedTheme

        val forbiddenWords = if (state.isCustomTheme) {
            EmotionThemeDatabase.generateForbiddenWords(theme)
        } else {
            EmotionThemeDatabase.getTheme(theme)?.forbiddenWords
                ?: EmotionThemeDatabase.generateForbiddenWords(theme)
        }

        _uiState.update { it.copy(currentForbiddenWords = forbiddenWords) }
    }

    fun generateThemeSuggestions() {
        val state = _uiState.value
        val allAssociations = listOf(
            state.inputAssociationFromFormState,
            state.inputAssociationFromTimePassage,
            state.inputAssociationFromPositionPlacement,
            state.inputAssociationFromCustom,
            state.inputStrongestAssociation
        ).filter { it.isNotBlank() }

        val suggestions = EmotionThemeDatabase.themes.keys.filter { themeName ->
            allAssociations.any { association ->
                association.contains(themeName) ||
                        EmotionThemeDatabase.themes[themeName]?.commonFeatures?.any {
                            association.contains(it)
                        } == true
            }
        }.take(5)

        _uiState.update { it.copy(suggestedThemes = suggestions) }
    }

    // ====================
    // 禁止ワード・感覚語チェック
    // ====================

    private fun checkForbiddenWords(text: String) {
        val forbiddenWords = _uiState.value.currentForbiddenWords
        val warnings = forbiddenWords.filter { text.contains(it) }
        _uiState.update { it.copy(forbiddenWordWarnings = warnings) }
    }

    private fun updateSensoryWordCounts(text: String) {
        val counts = SensoryKeywords.countSensoryWords(text)
        val lines = text.split("\n").filter { it.isNotBlank() }.size
        val chars = text.length

        _uiState.update { it.copy(
            sensoryWordCounts = counts,
            lineCount = lines,
            charCount = chars
        )}
    }

    // ====================
    // 入力フィールド更新
    // ====================

    // 物質→抽象コース
    fun updateInputMaterial(value: String) {
        _uiState.update { it.copy(inputMaterial = value, materialValidationError = "") }
    }

    // 観察フェーズ（7項目）
    fun updateObservationShape(value: String) = _uiState.update { it.copy(inputObservationShape = value) }
    fun updateObservationColor(value: String) = _uiState.update { it.copy(inputObservationColor = value) }
    fun updateObservationLight(value: String) = _uiState.update { it.copy(inputObservationLight = value) }
    fun updateObservationTouch(value: String) = _uiState.update { it.copy(inputObservationTouch = value) }
    fun updateObservationSmell(value: String) = _uiState.update { it.copy(inputObservationSmell = value) }
    fun updateObservationSound(value: String) = _uiState.update { it.copy(inputObservationSound = value) }
    fun updateObservationContext(value: String) = _uiState.update { it.copy(inputObservationContext = value) }

    // 特徴抽出
    fun updateFeatureFormState(value: String) = _uiState.update { it.copy(inputFeatureFormState = value) }
    fun updateFeatureTimePassage(value: String) = _uiState.update { it.copy(inputFeatureTimePassage = value) }
    fun updateFeaturePositionPlacement(value: String) = _uiState.update { it.copy(inputFeaturePositionPlacement = value) }
    fun updateFeatureCustom(value: String) = _uiState.update { it.copy(inputFeatureCustom = value) }
    fun selectFeatureAspect(aspect: FeatureAspect) = _uiState.update { it.copy(currentFeatureAspect = aspect) }

    // 連想フェーズ
    fun updateAssociationFromFormState(value: String) = _uiState.update { it.copy(inputAssociationFromFormState = value) }
    fun updateAssociationFromTimePassage(value: String) = _uiState.update { it.copy(inputAssociationFromTimePassage = value) }
    fun updateAssociationFromPositionPlacement(value: String) = _uiState.update { it.copy(inputAssociationFromPositionPlacement = value) }
    fun updateAssociationFromCustom(value: String) = _uiState.update { it.copy(inputAssociationFromCustom = value) }
    fun updateStrongestAssociation(value: String) {
        _uiState.update { it.copy(inputStrongestAssociation = value) }
        generateThemeSuggestions()
    }

    // 抽象→物質コース
    fun updateThemeDefinition(value: String) = _uiState.update { it.copy(inputThemeDefinition = value) }
    fun updateThemeOrigin(value: String) = _uiState.update { it.copy(inputThemeOrigin = value) }
    fun updateThemeOpposites(value: String) = _uiState.update { it.copy(inputThemeOpposites = value) }
    fun updateThemeCharacteristics(value: String) = _uiState.update { it.copy(inputThemeCharacteristics = value) }

    fun updateMaterialCandidate(index: Int, value: String) {
        _uiState.update {
            when (index) {
                0 -> it.copy(inputMaterialCandidate1 = value)
                1 -> it.copy(inputMaterialCandidate2 = value)
                2 -> it.copy(inputMaterialCandidate3 = value)
                3 -> it.copy(inputMaterialCandidate4 = value)
                4 -> it.copy(inputMaterialCandidate5 = value)
                else -> it
            }
        }
    }

    fun updateCandidateReason(index: Int, value: String) {
        _uiState.update {
            when (index) {
                0 -> it.copy(inputCandidateReason1 = value)
                1 -> it.copy(inputCandidateReason2 = value)
                2 -> it.copy(inputCandidateReason3 = value)
                3 -> it.copy(inputCandidateReason4 = value)
                4 -> it.copy(inputCandidateReason5 = value)
                else -> it
            }
        }
    }

    fun selectChosenMaterial(index: Int) {
        _uiState.update { it.copy(chosenMaterialIndex = index) }
    }

    fun updateChosenMaterialReason(value: String) = _uiState.update { it.copy(inputChosenMaterialReason = value) }
    fun updateMaterialState(value: String) = _uiState.update { it.copy(inputMaterialState = value) }
    fun updateMaterialContext(value: String) = _uiState.update { it.copy(inputMaterialContext = value) }
    fun updateMaterialCondition(value: String) = _uiState.update { it.copy(inputMaterialCondition = value) }

    // 共通
    fun updateCustomThemeDefinition(value: String) = _uiState.update { it.copy(inputCustomThemeDefinition = value) }

    fun updateGeneratedExpression(value: String) {
        _uiState.update { it.copy(inputGeneratedExpression = value) }
        checkForbiddenWords(value)
        updateSensoryWordCounts(value)
    }

    fun updateChecklistItem(id: String, isChecked: Boolean) {
        _uiState.update { state ->
            val updatedChecklist = state.checklistStates.toMutableMap()
            updatedChecklist[id] = isChecked
            state.copy(checklistStates = updatedChecklist)
        }
    }

    // ====================
    // UI状態管理
    // ====================

    fun showSessionPicker() = _uiState.update { it.copy(showSessionPicker = true) }
    fun hideSessionPicker() = _uiState.update { it.copy(showSessionPicker = false) }
    fun showExitConfirmation() = _uiState.update { it.copy(showConfirmExit = true) }
    fun hideExitConfirmation() = _uiState.update { it.copy(showConfirmExit = false) }

    fun confirmExit() {
        _uiState.update { it.copy(
            showConfirmExit = false,
            currentSession = null,
            selectedCourse = null
        )}
        resetAllInputs()
    }

    fun finishTraining() {
        _uiState.update { it.copy(
            currentSession = null,
            selectedCourse = null
        )}
        resetAllInputs()
    }

    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = "") }

    private fun resetAllInputs() {
        _uiState.update { it.copy(
            // 物質→抽象コース
            inputMaterial = "",
            materialValidationError = "",
            inputObservationShape = "",
            inputObservationColor = "",
            inputObservationLight = "",
            inputObservationTouch = "",
            inputObservationSmell = "",
            inputObservationSound = "",
            inputObservationContext = "",
            inputFeatureFormState = "",
            inputFeatureTimePassage = "",
            inputFeaturePositionPlacement = "",
            inputFeatureCustom = "",
            currentFeatureAspect = FeatureAspect.FORM_AND_STATE,
            inputAssociationFromFormState = "",
            inputAssociationFromTimePassage = "",
            inputAssociationFromPositionPlacement = "",
            inputAssociationFromCustom = "",
            inputStrongestAssociation = "",
            // 抽象→物質コース
            inputThemeDefinition = "",
            inputThemeOrigin = "",
            inputThemeOpposites = "",
            inputThemeCharacteristics = "",
            inputMaterialCandidate1 = "",
            inputMaterialCandidate2 = "",
            inputMaterialCandidate3 = "",
            inputMaterialCandidate4 = "",
            inputMaterialCandidate5 = "",
            inputCandidateReason1 = "",
            inputCandidateReason2 = "",
            inputCandidateReason3 = "",
            inputCandidateReason4 = "",
            inputCandidateReason5 = "",
            chosenMaterialIndex = -1,
            inputChosenMaterialReason = "",
            inputMaterialState = "",
            inputMaterialContext = "",
            inputMaterialCondition = "",
            // 共通
            selectedTheme = "",
            isCustomTheme = false,
            inputCustomThemeDefinition = "",
            currentForbiddenWords = emptyList(),
            inputGeneratedExpression = "",
            forbiddenWordWarnings = emptyList(),
            sensoryWordCounts = emptyMap(),
            lineCount = 0,
            charCount = 0,
            suggestedThemes = emptyList(),
            checklistStates = ExpressionChecklist.items.associate { it.id to false },
            m2aCurrentStep = MaterialToAbstractStep.MATERIAL_SELECTION,
            a2mCurrentStep = AbstractToMaterialStep.THEME_SELECTION
        )}
    }
}
