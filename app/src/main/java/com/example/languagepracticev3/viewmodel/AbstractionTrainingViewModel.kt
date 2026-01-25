// app/src/main/java/com/example/languagepracticev3/viewmodel/AbstractionTrainingViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.model.AbstractionSession
import com.example.languagepracticev3.data.model.AbstractionStep
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
 * 抽象化テクニックトレーニングの UI 状態
 */
data class AbstractionUiState(
    // セッション管理
    val currentSession: AbstractionSession? = null,
    val sessions: List<AbstractionSession> = emptyList(),
    val currentStep: AbstractionStep = AbstractionStep.CONCRETE_SCENE,

    // Step 1: 具体的な情景
    val inputConcreteScene: String = "",
    val inputSceneWho: String = "",
    val inputSceneWhat: String = "",
    val inputSceneWhere: String = "",
    val inputSceneWhen: String = "",

    // Step 2: つっこみを入れる
    val inputQuestionWhatItShows: String = "",
    val inputQuestionWhyImpressive: String = "",
    val inputQuestionWhatToFeel: String = "",
    val inputQuestionWhoDecided: String = "",
    val inputQuestionByWhatStandard: String = "",
    val inputQuestionSpecifically: String = "",

    // Step 3: 抽象化
    val inputAbstractedSentence: String = "",
    val inputCoreTheme: String = "",
    val inputCoreEmotion: String = "",

    // Step 4: 感覚的詳細
    val inputSensoryVisual: String = "",
    val inputSensoryAuditory: String = "",
    val inputSensoryTactile: String = "",
    val inputSensoryOlfactory: String = "",
    val inputSensoryGustatory: String = "",
    val inputPovCharacter: String = "",
    val inputPovFocus: String = "",
    val inputPovIgnore: String = "",

    // Step 5: メタファー
    val inputMetaphorCandidate1: String = "",
    val inputMetaphorCandidate2: String = "",
    val inputMetaphorCandidate3: String = "",
    val selectedMetaphor: Int = 0,
    val inputMetaphorReason: String = "",

    // 最終成果物
    val inputFinalText: String = "",

    // UI状態
    val isLoading: Boolean = false,
    val showSessionPicker: Boolean = false,
    val statusMessage: String = "",
    val showConfirmExit: Boolean = false
)

@HiltViewModel
class AbstractionTrainingViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AbstractionUiState())
    val uiState: StateFlow<AbstractionUiState> = _uiState.asStateFlow()

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
                repository.getAllAbstractionSessions().collect { sessions ->
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
        val newSession = AbstractionSession(
            createdAt = now,
            updatedAt = now
        )
        _uiState.update { it.copy(
            currentSession = newSession,
            currentStep = AbstractionStep.CONCRETE_SCENE,
            // 全入力フィールドをリセット
            inputConcreteScene = "",
            inputSceneWho = "",
            inputSceneWhat = "",
            inputSceneWhere = "",
            inputSceneWhen = "",
            inputQuestionWhatItShows = "",
            inputQuestionWhyImpressive = "",
            inputQuestionWhatToFeel = "",
            inputQuestionWhoDecided = "",
            inputQuestionByWhatStandard = "",
            inputQuestionSpecifically = "",
            inputAbstractedSentence = "",
            inputCoreTheme = "",
            inputCoreEmotion = "",
            inputSensoryVisual = "",
            inputSensoryAuditory = "",
            inputSensoryTactile = "",
            inputSensoryOlfactory = "",
            inputSensoryGustatory = "",
            inputPovCharacter = "",
            inputPovFocus = "",
            inputPovIgnore = "",
            inputMetaphorCandidate1 = "",
            inputMetaphorCandidate2 = "",
            inputMetaphorCandidate3 = "",
            selectedMetaphor = 0,
            inputMetaphorReason = "",
            inputFinalText = ""
        )}
    }

    fun loadSession(session: AbstractionSession) {
        _uiState.update { it.copy(
            currentSession = session,
            currentStep = AbstractionStep.entries.getOrElse(session.currentStep) {
                AbstractionStep.CONCRETE_SCENE
            },
            // セッションのデータで入力フィールドを初期化
            inputConcreteScene = session.concreteScene,
            inputSceneWho = session.sceneWho,
            inputSceneWhat = session.sceneWhat,
            inputSceneWhere = session.sceneWhere,
            inputSceneWhen = session.sceneWhen,
            inputQuestionWhatItShows = session.questionWhatItShows,
            inputQuestionWhyImpressive = session.questionWhyImpressive,
            inputQuestionWhatToFeel = session.questionWhatToFeel,
            inputQuestionWhoDecided = session.questionWhoDecided,
            inputQuestionByWhatStandard = session.questionByWhatStandard,
            inputQuestionSpecifically = session.questionSpecifically,
            inputAbstractedSentence = session.abstractedSentence,
            inputCoreTheme = session.coreTheme,
            inputCoreEmotion = session.coreEmotion,
            inputSensoryVisual = session.sensoryVisual,
            inputSensoryAuditory = session.sensoryAuditory,
            inputSensoryTactile = session.sensoryTactile,
            inputSensoryOlfactory = session.sensoryOlfactory,
            inputSensoryGustatory = session.sensoryGustatory,
            inputPovCharacter = session.povCharacter,
            inputPovFocus = session.povFocus,
            inputPovIgnore = session.povIgnore,
            inputMetaphorCandidate1 = session.metaphorCandidate1,
            inputMetaphorCandidate2 = session.metaphorCandidate2,
            inputMetaphorCandidate3 = session.metaphorCandidate3,
            selectedMetaphor = session.selectedMetaphor,
            inputMetaphorReason = session.metaphorReason,
            inputFinalText = session.finalText,
            showSessionPicker = false
        )}
    }

    fun closeSession() {
        _uiState.update { it.copy(
            currentSession = null,
            currentStep = AbstractionStep.CONCRETE_SCENE
        )}
    }

    fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updatedSession = session.copy(
                sessionTitle = state.inputConcreteScene.take(50),
                currentStep = state.currentStep.ordinal,
                concreteScene = state.inputConcreteScene,
                sceneWho = state.inputSceneWho,
                sceneWhat = state.inputSceneWhat,
                sceneWhere = state.inputSceneWhere,
                sceneWhen = state.inputSceneWhen,
                questionWhatItShows = state.inputQuestionWhatItShows,
                questionWhyImpressive = state.inputQuestionWhyImpressive,
                questionWhatToFeel = state.inputQuestionWhatToFeel,
                questionWhoDecided = state.inputQuestionWhoDecided,
                questionByWhatStandard = state.inputQuestionByWhatStandard,
                questionSpecifically = state.inputQuestionSpecifically,
                abstractedSentence = state.inputAbstractedSentence,
                coreTheme = state.inputCoreTheme,
                coreEmotion = state.inputCoreEmotion,
                sensoryVisual = state.inputSensoryVisual,
                sensoryAuditory = state.inputSensoryAuditory,
                sensoryTactile = state.inputSensoryTactile,
                sensoryOlfactory = state.inputSensoryOlfactory,
                sensoryGustatory = state.inputSensoryGustatory,
                povCharacter = state.inputPovCharacter,
                povFocus = state.inputPovFocus,
                povIgnore = state.inputPovIgnore,
                metaphorCandidate1 = state.inputMetaphorCandidate1,
                metaphorCandidate2 = state.inputMetaphorCandidate2,
                metaphorCandidate3 = state.inputMetaphorCandidate3,
                selectedMetaphor = state.selectedMetaphor,
                metaphorReason = state.inputMetaphorReason,
                finalText = state.inputFinalText,
                updatedAt = now
            )

            try {
                val savedId = repository.saveAbstractionSession(updatedSession)
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

    fun deleteSession(session: AbstractionSession) {
        viewModelScope.launch {
            try {
                repository.deleteAbstractionSession(session)
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

            // 最終テキストを生成
            val finalText = generateFinalText(state)

            val completedSession = session.copy(
                sessionTitle = state.inputConcreteScene.take(50),
                currentStep = state.currentStep.ordinal,
                concreteScene = state.inputConcreteScene,
                sceneWho = state.inputSceneWho,
                sceneWhat = state.inputSceneWhat,
                sceneWhere = state.inputSceneWhere,
                sceneWhen = state.inputSceneWhen,
                questionWhatItShows = state.inputQuestionWhatItShows,
                questionWhyImpressive = state.inputQuestionWhyImpressive,
                questionWhatToFeel = state.inputQuestionWhatToFeel,
                questionWhoDecided = state.inputQuestionWhoDecided,
                questionByWhatStandard = state.inputQuestionByWhatStandard,
                questionSpecifically = state.inputQuestionSpecifically,
                abstractedSentence = state.inputAbstractedSentence,
                coreTheme = state.inputCoreTheme,
                coreEmotion = state.inputCoreEmotion,
                sensoryVisual = state.inputSensoryVisual,
                sensoryAuditory = state.inputSensoryAuditory,
                sensoryTactile = state.inputSensoryTactile,
                sensoryOlfactory = state.inputSensoryOlfactory,
                sensoryGustatory = state.inputSensoryGustatory,
                povCharacter = state.inputPovCharacter,
                povFocus = state.inputPovFocus,
                povIgnore = state.inputPovIgnore,
                metaphorCandidate1 = state.inputMetaphorCandidate1,
                metaphorCandidate2 = state.inputMetaphorCandidate2,
                metaphorCandidate3 = state.inputMetaphorCandidate3,
                selectedMetaphor = state.selectedMetaphor,
                metaphorReason = state.inputMetaphorReason,
                finalText = finalText,
                isCompleted = true,
                updatedAt = now
            )

            try {
                repository.saveAbstractionSession(completedSession)
                _uiState.update { it.copy(
                    currentSession = null,
                    inputFinalText = finalText,
                    statusMessage = "セッションを完了しました"
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "完了エラー: ${e.message}") }
            }
        }
    }

    private fun generateFinalText(state: AbstractionUiState): String {
        val sb = StringBuilder()

        // 具体的な情景
        if (state.inputConcreteScene.isNotBlank()) {
            sb.appendLine(state.inputConcreteScene)
            sb.appendLine()
        }

        // 感覚的詳細を織り込む
        val sensoryDetails = listOfNotNull(
            state.inputSensoryVisual.takeIf { it.isNotBlank() },
            state.inputSensoryAuditory.takeIf { it.isNotBlank() },
            state.inputSensoryTactile.takeIf { it.isNotBlank() },
            state.inputSensoryOlfactory.takeIf { it.isNotBlank() },
            state.inputSensoryGustatory.takeIf { it.isNotBlank() }
        )
        if (sensoryDetails.isNotEmpty()) {
            sensoryDetails.forEach { detail ->
                sb.appendLine(detail)
            }
            sb.appendLine()
        }

        // 抽象化された文
        if (state.inputAbstractedSentence.isNotBlank()) {
            sb.appendLine(state.inputAbstractedSentence)
            sb.appendLine()
        }

        // 選択されたメタファー
        val selectedMetaphorText = when (state.selectedMetaphor) {
            1 -> state.inputMetaphorCandidate1
            2 -> state.inputMetaphorCandidate2
            3 -> state.inputMetaphorCandidate3
            else -> ""
        }
        if (selectedMetaphorText.isNotBlank()) {
            sb.appendLine("【メタファー】$selectedMetaphorText")
        }

        return sb.toString().trim()
    }

    // ====================
    // ステップナビゲーション
    // ====================

    fun nextStep() {
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex < AbstractionStep.entries.size - 1) {
            _uiState.update { it.copy(
                currentStep = AbstractionStep.entries[currentIndex + 1]
            )}
        }
    }

    fun previousStep() {
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex > 0) {
            _uiState.update { it.copy(
                currentStep = AbstractionStep.entries[currentIndex - 1]
            )}
        }
    }

    fun goToStep(step: AbstractionStep) {
        _uiState.update { it.copy(currentStep = step) }
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
            currentSession = null
        )}
    }

    // ====================
    // 入力フィールド更新
    // ====================

    // Step 1
    fun updateConcreteScene(value: String) = _uiState.update { it.copy(inputConcreteScene = value) }
    fun updateSceneWho(value: String) = _uiState.update { it.copy(inputSceneWho = value) }
    fun updateSceneWhat(value: String) = _uiState.update { it.copy(inputSceneWhat = value) }
    fun updateSceneWhere(value: String) = _uiState.update { it.copy(inputSceneWhere = value) }
    fun updateSceneWhen(value: String) = _uiState.update { it.copy(inputSceneWhen = value) }

    // Step 2
    fun updateQuestionWhatItShows(value: String) = _uiState.update { it.copy(inputQuestionWhatItShows = value) }
    fun updateQuestionWhyImpressive(value: String) = _uiState.update { it.copy(inputQuestionWhyImpressive = value) }
    fun updateQuestionWhatToFeel(value: String) = _uiState.update { it.copy(inputQuestionWhatToFeel = value) }
    fun updateQuestionWhoDecided(value: String) = _uiState.update { it.copy(inputQuestionWhoDecided = value) }
    fun updateQuestionByWhatStandard(value: String) = _uiState.update { it.copy(inputQuestionByWhatStandard = value) }
    fun updateQuestionSpecifically(value: String) = _uiState.update { it.copy(inputQuestionSpecifically = value) }

    // Step 3
    fun updateAbstractedSentence(value: String) = _uiState.update { it.copy(inputAbstractedSentence = value) }
    fun updateCoreTheme(value: String) = _uiState.update { it.copy(inputCoreTheme = value) }
    fun updateCoreEmotion(value: String) = _uiState.update { it.copy(inputCoreEmotion = value) }

    // Step 4
    fun updateSensoryVisual(value: String) = _uiState.update { it.copy(inputSensoryVisual = value) }
    fun updateSensoryAuditory(value: String) = _uiState.update { it.copy(inputSensoryAuditory = value) }
    fun updateSensoryTactile(value: String) = _uiState.update { it.copy(inputSensoryTactile = value) }
    fun updateSensoryOlfactory(value: String) = _uiState.update { it.copy(inputSensoryOlfactory = value) }
    fun updateSensoryGustatory(value: String) = _uiState.update { it.copy(inputSensoryGustatory = value) }
    fun updatePovCharacter(value: String) = _uiState.update { it.copy(inputPovCharacter = value) }
    fun updatePovFocus(value: String) = _uiState.update { it.copy(inputPovFocus = value) }
    fun updatePovIgnore(value: String) = _uiState.update { it.copy(inputPovIgnore = value) }

    // Step 5
    fun updateMetaphorCandidate1(value: String) = _uiState.update { it.copy(inputMetaphorCandidate1 = value) }
    fun updateMetaphorCandidate2(value: String) = _uiState.update { it.copy(inputMetaphorCandidate2 = value) }
    fun updateMetaphorCandidate3(value: String) = _uiState.update { it.copy(inputMetaphorCandidate3 = value) }
    fun selectMetaphor(index: Int) = _uiState.update { it.copy(selectedMetaphor = index) }
    fun updateMetaphorReason(value: String) = _uiState.update { it.copy(inputMetaphorReason = value) }

    // Final
    fun updateFinalText(value: String) = _uiState.update { it.copy(inputFinalText = value) }

    // ====================
    // セッションピッカー
    // ====================

    fun showSessionPicker() = _uiState.update { it.copy(showSessionPicker = true) }
    fun hideSessionPicker() = _uiState.update { it.copy(showSessionPicker = false) }
    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = "") }
}