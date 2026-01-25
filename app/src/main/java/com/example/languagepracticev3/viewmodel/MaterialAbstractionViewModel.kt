// app/src/main/java/com/example/languagepracticev3/viewmodel/MaterialAbstractionViewModel.kt
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
    // セッション
    val currentSession: MaterialAbstractionSession? = null,
    val sessions: List<MaterialAbstractionSession> = emptyList(),
    val currentStep: MaterialAbstractionStep = MaterialAbstractionStep.MATERIAL_SELECTION,

    // ステップ1: 物質選択
    val inputMaterial: String = "",
    val materialValidationError: String = "",

    // ステップ2: 観察フェーズ
    val inputObservationVisual: String = "",
    val inputObservationTactile: String = "",
    val inputObservationAuditory: String = "",
    val inputObservationOlfactory: String = "",
    val inputObservationGustatory: String = "",
    val currentSenseTab: SenseType = SenseType.VISUAL,

    // ステップ3: 特徴抽出
    val inputFeatureFormState: String = "",
    val inputFeatureTimePassage: String = "",
    val inputFeaturePositionPlacement: String = "",
    val inputFeatureCustom: String = "",
    val currentFeatureAspect: FeatureAspect = FeatureAspect.FORM_AND_STATE,

    // ステップ4: 連想フェーズ
    val inputAssociationFromFormState: String = "",
    val inputAssociationFromTimePassage: String = "",
    val inputAssociationFromPositionPlacement: String = "",
    val inputAssociationFromCustom: String = "",
    val inputStrongestAssociation: String = "",

    // ステップ5: 概念化フェーズ
    val selectedTheme: String = "",
    val isCustomTheme: Boolean = false,
    val inputCustomThemeDefinition: String = "",
    val currentForbiddenWords: List<String> = emptyList(),
    val suggestedThemes: List<String> = emptyList(),

    // ステップ6: 表現生成
    val inputGeneratedExpression: String = "",
    val forbiddenWordWarnings: List<String> = emptyList(),
    val sensoryWordCounts: Map<String, Int> = emptyMap(),
    val lineCount: Int = 0,
    val charCount: Int = 0,

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
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val newSession = MaterialAbstractionSession(
            createdAt = now,
            updatedAt = now
        )
        _uiState.update { it.copy(
            currentSession = newSession,
            currentStep = MaterialAbstractionStep.MATERIAL_SELECTION
        )}
        resetAllInputs()
    }

    fun loadSession(session: MaterialAbstractionSession) {
        _uiState.update { it.copy(
            currentSession = session,
            currentStep = MaterialAbstractionStep.entries.getOrElse(session.currentStep) {
                MaterialAbstractionStep.MATERIAL_SELECTION
            },
            // 入力フィールドを復元
            inputMaterial = session.selectedMaterial,
            inputObservationVisual = session.observationVisual,
            inputObservationTactile = session.observationTactile,
            inputObservationAuditory = session.observationAuditory,
            inputObservationOlfactory = session.observationOlfactory,
            inputObservationGustatory = session.observationGustatory,
            inputFeatureFormState = session.featureFormState,
            inputFeatureTimePassage = session.featureTimePassage,
            inputFeaturePositionPlacement = session.featurePositionPlacement,
            inputFeatureCustom = session.featureCustom,
            inputAssociationFromFormState = session.associationFromFormState,
            inputAssociationFromTimePassage = session.associationFromTimePassage,
            inputAssociationFromPositionPlacement = session.associationFromPositionPlacement,
            inputAssociationFromCustom = session.associationFromCustom,
            inputStrongestAssociation = session.strongestAssociation,
            selectedTheme = session.selectedTheme,
            isCustomTheme = session.isCustomTheme,
            inputCustomThemeDefinition = session.customThemeDefinition,
            currentForbiddenWords = session.forbiddenWords.split(",").filter { it.isNotBlank() },
            inputGeneratedExpression = session.generatedExpression,
            showSessionPicker = false
        )}

        // 禁止ワード検出を再実行
        if (session.generatedExpression.isNotBlank()) {
            checkForbiddenWords(session.generatedExpression)
            updateSensoryWordCounts(session.generatedExpression)
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val counts = SensoryKeywords.countSensoryWords(state.inputGeneratedExpression)

            val updatedSession = session.copy(
                sessionTitle = state.inputMaterial.take(20),
                currentStep = state.currentStep.ordinal,
                selectedMaterial = state.inputMaterial,
                observationVisual = state.inputObservationVisual,
                observationTactile = state.inputObservationTactile,
                observationAuditory = state.inputObservationAuditory,
                observationOlfactory = state.inputObservationOlfactory,
                observationGustatory = state.inputObservationGustatory,
                featureFormState = state.inputFeatureFormState,
                featureTimePassage = state.inputFeatureTimePassage,
                featurePositionPlacement = state.inputFeaturePositionPlacement,
                featureCustom = state.inputFeatureCustom,
                associationFromFormState = state.inputAssociationFromFormState,
                associationFromTimePassage = state.inputAssociationFromTimePassage,
                associationFromPositionPlacement = state.inputAssociationFromPositionPlacement,
                associationFromCustom = state.inputAssociationFromCustom,
                strongestAssociation = state.inputStrongestAssociation,
                selectedTheme = state.selectedTheme,
                isCustomTheme = state.isCustomTheme,
                customThemeDefinition = state.inputCustomThemeDefinition,
                forbiddenWords = state.currentForbiddenWords.joinToString(","),
                generatedExpression = state.inputGeneratedExpression,
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

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val counts = SensoryKeywords.countSensoryWords(state.inputGeneratedExpression)

            val completedSession = session.copy(
                sessionTitle = state.inputMaterial.take(20),
                currentStep = MaterialAbstractionStep.RESULT_DISPLAY.ordinal,
                selectedMaterial = state.inputMaterial,
                observationVisual = state.inputObservationVisual,
                observationTactile = state.inputObservationTactile,
                observationAuditory = state.inputObservationAuditory,
                observationOlfactory = state.inputObservationOlfactory,
                observationGustatory = state.inputObservationGustatory,
                featureFormState = state.inputFeatureFormState,
                featureTimePassage = state.inputFeatureTimePassage,
                featurePositionPlacement = state.inputFeaturePositionPlacement,
                featureCustom = state.inputFeatureCustom,
                associationFromFormState = state.inputAssociationFromFormState,
                associationFromTimePassage = state.inputAssociationFromTimePassage,
                associationFromPositionPlacement = state.inputAssociationFromPositionPlacement,
                associationFromCustom = state.inputAssociationFromCustom,
                strongestAssociation = state.inputStrongestAssociation,
                selectedTheme = state.selectedTheme,
                isCustomTheme = state.isCustomTheme,
                customThemeDefinition = state.inputCustomThemeDefinition,
                forbiddenWords = state.currentForbiddenWords.joinToString(","),
                generatedExpression = state.inputGeneratedExpression,
                feedbackVisualCount = counts["visual"] ?: 0,
                feedbackTactileCount = counts["tactile"] ?: 0,
                feedbackAuditoryCount = counts["auditory"] ?: 0,
                feedbackOlfactoryCount = counts["olfactory"] ?: 0,
                feedbackGustatoryCount = counts["gustatory"] ?: 0,
                feedbackMetaphorCount = counts["metaphor"] ?: 0,
                feedbackForbiddenWordUsed = state.forbiddenWordWarnings.isNotEmpty(),
                isCompleted = true,
                updatedAt = now
            )

            try {
                repository.saveMaterialAbstractionSession(completedSession)
                _uiState.update { it.copy(
                    currentStep = MaterialAbstractionStep.RESULT_DISPLAY,
                    statusMessage = "セッションを完了しました"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "完了エラー: ${e.message}") }
            }
        }
    }

    // ====================
    // ステップナビゲーション
    // ====================

    fun nextStep() {
        val state = _uiState.value
        val currentIndex = state.currentStep.ordinal

        // バリデーション
        when (state.currentStep) {
            MaterialAbstractionStep.MATERIAL_SELECTION -> {
                if (!validateMaterial()) return
            }
            MaterialAbstractionStep.CONCEPTUALIZATION -> {
                if (state.selectedTheme.isBlank()) {
                    _uiState.update { it.copy(statusMessage = "テーマを選択または入力してください") }
                    return
                }
                // テーマ確定時に禁止ワードを設定
                setupForbiddenWords()
            }
            else -> {}
        }

        if (currentIndex < MaterialAbstractionStep.entries.size - 1) {
            _uiState.update { it.copy(
                currentStep = MaterialAbstractionStep.entries[currentIndex + 1]
            )}
            // 自動保存
            saveSession()
        }
    }

    fun previousStep() {
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex > 0) {
            _uiState.update { it.copy(
                currentStep = MaterialAbstractionStep.entries[currentIndex - 1]
            )}
        }
    }

    fun goToStep(step: MaterialAbstractionStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    // ====================
    // 物質バリデーション
    // ====================

    private fun validateMaterial(): Boolean {
        val material = _uiState.value.inputMaterial.trim()

        // 空白チェック
        if (material.isBlank()) {
            _uiState.update { it.copy(materialValidationError = "物質を入力してください") }
            return false
        }

        // 抽象的な概念ではないかチェック
        val abstractWords = listOf("愛", "幸せ", "時間", "希望", "絶望", "自由", "平和", "正義")
        if (abstractWords.any { material.contains(it) }) {
            _uiState.update { it.copy(
                materialValidationError = "抽象的な概念ではなく、触れることができる具体的な物を選んでください"
            )}
            return false
        }

        _uiState.update { it.copy(materialValidationError = "") }
        return true
    }

    // ====================
    // 禁止ワード処理
    // ====================

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
    // テーマ候補生成
    // ====================

    fun generateThemeSuggestions() {
        val state = _uiState.value
        val allAssociations = listOf(
            state.inputAssociationFromFormState,
            state.inputAssociationFromTimePassage,
            state.inputAssociationFromPositionPlacement,
            state.inputAssociationFromCustom,
            state.inputStrongestAssociation
        ).filter { it.isNotBlank() }

        // 既存のテーマとのマッチングを試みる
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
    // 入力フィールド更新
    // ====================

    // ステップ1
    fun updateInputMaterial(value: String) {
        _uiState.update { it.copy(inputMaterial = value, materialValidationError = "") }
    }

    // ステップ2
    fun updateObservationVisual(value: String) = _uiState.update { it.copy(inputObservationVisual = value) }
    fun updateObservationTactile(value: String) = _uiState.update { it.copy(inputObservationTactile = value) }
    fun updateObservationAuditory(value: String) = _uiState.update { it.copy(inputObservationAuditory = value) }
    fun updateObservationOlfactory(value: String) = _uiState.update { it.copy(inputObservationOlfactory = value) }
    fun updateObservationGustatory(value: String) = _uiState.update { it.copy(inputObservationGustatory = value) }
    fun selectSenseTab(sense: SenseType) = _uiState.update { it.copy(currentSenseTab = sense) }

    // ステップ3
    fun updateFeatureFormState(value: String) = _uiState.update { it.copy(inputFeatureFormState = value) }
    fun updateFeatureTimePassage(value: String) = _uiState.update { it.copy(inputFeatureTimePassage = value) }
    fun updateFeaturePositionPlacement(value: String) = _uiState.update { it.copy(inputFeaturePositionPlacement = value) }
    fun updateFeatureCustom(value: String) = _uiState.update { it.copy(inputFeatureCustom = value) }
    fun selectFeatureAspect(aspect: FeatureAspect) = _uiState.update { it.copy(currentFeatureAspect = aspect) }

    // ステップ4
    fun updateAssociationFromFormState(value: String) = _uiState.update { it.copy(inputAssociationFromFormState = value) }
    fun updateAssociationFromTimePassage(value: String) = _uiState.update { it.copy(inputAssociationFromTimePassage = value) }
    fun updateAssociationFromPositionPlacement(value: String) = _uiState.update { it.copy(inputAssociationFromPositionPlacement = value) }
    fun updateAssociationFromCustom(value: String) = _uiState.update { it.copy(inputAssociationFromCustom = value) }
    fun updateStrongestAssociation(value: String) {
        _uiState.update { it.copy(inputStrongestAssociation = value) }
        generateThemeSuggestions()
    }

    // ステップ5
    fun selectTheme(theme: String, isCustom: Boolean = false) {
        _uiState.update { it.copy(
            selectedTheme = theme,
            isCustomTheme = isCustom
        )}
    }
    fun updateCustomThemeDefinition(value: String) = _uiState.update { it.copy(inputCustomThemeDefinition = value) }

    // ステップ6
    fun updateGeneratedExpression(value: String) {
        _uiState.update { it.copy(inputGeneratedExpression = value) }
        checkForbiddenWords(value)
        updateSensoryWordCounts(value)
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
            currentSession = null
        )}
        resetAllInputs()
    }
    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = "") }

    private fun resetAllInputs() {
        _uiState.update { it.copy(
            inputMaterial = "",
            materialValidationError = "",
            inputObservationVisual = "",
            inputObservationTactile = "",
            inputObservationAuditory = "",
            inputObservationOlfactory = "",
            inputObservationGustatory = "",
            inputFeatureFormState = "",
            inputFeatureTimePassage = "",
            inputFeaturePositionPlacement = "",
            inputFeatureCustom = "",
            inputAssociationFromFormState = "",
            inputAssociationFromTimePassage = "",
            inputAssociationFromPositionPlacement = "",
            inputAssociationFromCustom = "",
            inputStrongestAssociation = "",
            selectedTheme = "",
            isCustomTheme = false,
            inputCustomThemeDefinition = "",
            currentForbiddenWords = emptyList(),
            inputGeneratedExpression = "",
            forbiddenWordWarnings = emptyList(),
            sensoryWordCounts = emptyMap(),
            suggestedThemes = emptyList()
        )}
    }
}
