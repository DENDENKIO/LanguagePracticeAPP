// app/src/main/java/com/example/languagepracticev3/viewmodel/SelfQuestioningViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.model.GlobalRevisionSession
import com.example.languagepracticev3.data.model.GlobalRevisionStep
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
 * 自問自答画面で選択可能なトレーニングモード
 */
enum class SelfQuestioningMode {
    NONE,
    GLOBAL_REVISION,
    SIX_HABITS  // ★追加
}

/**
 * 自問自答画面の UI 状態
 */
data class SelfQuestioningUiState(
    val selectedMode: SelfQuestioningMode = SelfQuestioningMode.NONE,

    // グローバル・リビジョン関連
    val currentSession: GlobalRevisionSession? = null,
    val currentStep: GlobalRevisionStep = GlobalRevisionStep.CORE_DEFINITION,
    val sessions: List<GlobalRevisionSession> = emptyList(),

    // 入力フィールド（一時保持用）
    val inputWorkTitle: String = "",
    val inputOriginalText: String = "",
    val inputCoreSentence: String = "",
    val inputCoreTheme: String = "",
    val inputCoreEmotion: String = "",
    val inputCoreTakeaway: String = "",
    val inputDetectedProblems: String = "",
    val inputDiagnosisContent: String = "",
    val inputDiagnosisStructure: String = "",
    val inputDiagnosisReader: String = "",
    val inputDiagnosisStyle: String = "",
    val inputRevisionPlans: String = "",
    val inputRevisionPriority: String = "",
    val inputReverseOutline: String = "",
    val inputStructureNotes: String = "",
    val inputRevisedText: String = "",

    // UI状態
    val isLoading: Boolean = false,
    val showSessionPicker: Boolean = false,
    val statusMessage: String = ""
)

@HiltViewModel
class SelfQuestioningViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelfQuestioningUiState())
    val uiState: StateFlow<SelfQuestioningUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    // ====================
    // モード選択
    // ====================

    fun selectMode(mode: SelfQuestioningMode) {
        _uiState.update { it.copy(selectedMode = mode) }
        if (mode == SelfQuestioningMode.GLOBAL_REVISION) {
            loadSessions()
        }
        // ★ SIX_HABITS モードの場合は、別のViewModelで処理
    }

    fun clearMode() {
        _uiState.update { it.copy(selectedMode = SelfQuestioningMode.NONE) }
    }

    // ====================
    // セッション管理
    // ====================

    private fun loadSessions() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.getAllGlobalRevisionSessions().collect { sessions ->
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
        val newSession = GlobalRevisionSession(
            createdAt = now,
            updatedAt = now
        )
        _uiState.update { it.copy(
            currentSession = newSession,
            currentStep = GlobalRevisionStep.CORE_DEFINITION,
            // 入力フィールドをリセット
            inputWorkTitle = "",
            inputOriginalText = "",
            inputCoreSentence = "",
            inputCoreTheme = "",
            inputCoreEmotion = "",
            inputCoreTakeaway = "",
            inputDetectedProblems = "",
            inputDiagnosisContent = "",
            inputDiagnosisStructure = "",
            inputDiagnosisReader = "",
            inputDiagnosisStyle = "",
            inputRevisionPlans = "",
            inputRevisionPriority = "",
            inputReverseOutline = "",
            inputStructureNotes = "",
            inputRevisedText = ""
        )}
    }

    fun loadSession(session: GlobalRevisionSession) {
        _uiState.update { it.copy(
            currentSession = session,
            currentStep = GlobalRevisionStep.entries.getOrElse(session.currentStep) {
                GlobalRevisionStep.CORE_DEFINITION
            },
            // セッションのデータで入力フィールドを初期化
            inputWorkTitle = session.workTitle,
            inputOriginalText = session.originalText,
            inputCoreSentence = session.coreSentence,
            inputCoreTheme = session.coreTheme,
            inputCoreEmotion = session.coreEmotion,
            inputCoreTakeaway = session.coreTakeaway,
            inputDetectedProblems = session.detectedProblems,
            inputDiagnosisContent = session.diagnosisContent,
            inputDiagnosisStructure = session.diagnosisStructure,
            inputDiagnosisReader = session.diagnosisReader,
            inputDiagnosisStyle = session.diagnosisStyle,
            inputRevisionPlans = session.revisionPlans,
            inputRevisionPriority = session.revisionPriority,
            inputReverseOutline = session.reverseOutline,
            inputStructureNotes = session.structureNotes,
            inputRevisedText = session.revisedText,
            showSessionPicker = false
        )}
    }

    fun closeSession() {
        _uiState.update { it.copy(
            currentSession = null,
            currentStep = GlobalRevisionStep.CORE_DEFINITION
        )}
    }

    fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updatedSession = session.copy(
                workTitle = state.inputWorkTitle,
                originalText = state.inputOriginalText,
                coreSentence = state.inputCoreSentence,
                coreTheme = state.inputCoreTheme,
                coreEmotion = state.inputCoreEmotion,
                coreTakeaway = state.inputCoreTakeaway,
                detectedProblems = state.inputDetectedProblems,
                diagnosisContent = state.inputDiagnosisContent,
                diagnosisStructure = state.inputDiagnosisStructure,
                diagnosisReader = state.inputDiagnosisReader,
                diagnosisStyle = state.inputDiagnosisStyle,
                revisionPlans = state.inputRevisionPlans,
                revisionPriority = state.inputRevisionPriority,
                reverseOutline = state.inputReverseOutline,
                structureNotes = state.inputStructureNotes,
                revisedText = state.inputRevisedText,
                currentStep = state.currentStep.ordinal,
                updatedAt = now
            )

            try {
                val savedId = repository.saveGlobalRevisionSession(updatedSession)
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

    fun deleteSession(session: GlobalRevisionSession) {
        viewModelScope.launch {
            try {
                repository.deleteGlobalRevisionSession(session)
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
            val completedSession = session.copy(
                workTitle = state.inputWorkTitle,
                originalText = state.inputOriginalText,
                coreSentence = state.inputCoreSentence,
                coreTheme = state.inputCoreTheme,
                coreEmotion = state.inputCoreEmotion,
                coreTakeaway = state.inputCoreTakeaway,
                detectedProblems = state.inputDetectedProblems,
                diagnosisContent = state.inputDiagnosisContent,
                diagnosisStructure = state.inputDiagnosisStructure,
                diagnosisReader = state.inputDiagnosisReader,
                diagnosisStyle = state.inputDiagnosisStyle,
                revisionPlans = state.inputRevisionPlans,
                revisionPriority = state.inputRevisionPriority,
                reverseOutline = state.inputReverseOutline,
                structureNotes = state.inputStructureNotes,
                revisedText = state.inputRevisedText,
                isCompleted = true,
                updatedAt = now
            )

            try {
                repository.saveGlobalRevisionSession(completedSession)
                _uiState.update { it.copy(
                    currentSession = null,
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
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex < GlobalRevisionStep.entries.size - 1) {
            _uiState.update { it.copy(
                currentStep = GlobalRevisionStep.entries[currentIndex + 1]
            )}
        }
    }

    fun previousStep() {
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex > 0) {
            _uiState.update { it.copy(
                currentStep = GlobalRevisionStep.entries[currentIndex - 1]
            )}
        }
    }

    fun goToStep(step: GlobalRevisionStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    // ====================
    // 入力フィールド更新
    // ====================

    fun updateWorkTitle(value: String) = _uiState.update { it.copy(inputWorkTitle = value) }
    fun updateOriginalText(value: String) = _uiState.update { it.copy(inputOriginalText = value) }
    fun updateCoreSentence(value: String) = _uiState.update { it.copy(inputCoreSentence = value) }
    fun updateCoreTheme(value: String) = _uiState.update { it.copy(inputCoreTheme = value) }
    fun updateCoreEmotion(value: String) = _uiState.update { it.copy(inputCoreEmotion = value) }
    fun updateCoreTakeaway(value: String) = _uiState.update { it.copy(inputCoreTakeaway = value) }
    fun updateDetectedProblems(value: String) = _uiState.update { it.copy(inputDetectedProblems = value) }
    fun updateDiagnosisContent(value: String) = _uiState.update { it.copy(inputDiagnosisContent = value) }
    fun updateDiagnosisStructure(value: String) = _uiState.update { it.copy(inputDiagnosisStructure = value) }
    fun updateDiagnosisReader(value: String) = _uiState.update { it.copy(inputDiagnosisReader = value) }
    fun updateDiagnosisStyle(value: String) = _uiState.update { it.copy(inputDiagnosisStyle = value) }
    fun updateRevisionPlans(value: String) = _uiState.update { it.copy(inputRevisionPlans = value) }
    fun updateRevisionPriority(value: String) = _uiState.update { it.copy(inputRevisionPriority = value) }
    fun updateReverseOutline(value: String) = _uiState.update { it.copy(inputReverseOutline = value) }
    fun updateStructureNotes(value: String) = _uiState.update { it.copy(inputStructureNotes = value) }
    fun updateRevisedText(value: String) = _uiState.update { it.copy(inputRevisedText = value) }

    // ====================
    // セッションピッカー
    // ====================

    fun showSessionPicker() = _uiState.update { it.copy(showSessionPicker = true) }
    fun hideSessionPicker() = _uiState.update { it.copy(showSessionPicker = false) }
    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = "") }
}