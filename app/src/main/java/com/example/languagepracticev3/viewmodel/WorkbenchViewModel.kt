package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.WorkDao
import com.example.languagepracticev3.data.model.OperationKind
import com.example.languagepracticev3.data.model.Work
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class SaveResult {
    data class Success(val count: Int, val type: String) : SaveResult()
    data class Error(val message: String) : SaveResult()
}

data class WorkbenchUiState(
    val selectedOperation: OperationKind = OperationKind.TEXT_GEN,
    val statusMessage: String = "準備完了",
    val generatedPrompt: String = "",
    val aiOutput: String = "",
    val lastSaveResult: SaveResult? = null,
    val isLoading: Boolean = false,
    // 入力データ
    val inputSourceText: String = "",
    val inputTopic: String = "",
    val inputContext: String = "",
    val inputPersonaId: Long? = null,
    val targetLength: String = "中",
    val customInstructions: String = ""
)

@HiltViewModel
class WorkbenchViewModel @Inject constructor(
    private val workDao: WorkDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkbenchUiState())
    val uiState: StateFlow<WorkbenchUiState> = _uiState.asStateFlow()

    fun updateOperation(operation: OperationKind) {
        _uiState.update { it.copy(selectedOperation = operation, generatedPrompt = "", lastSaveResult = null) }
    }

    fun updateInputSourceText(text: String) { _uiState.update { it.copy(inputSourceText = text) } }
    fun updateInputTopic(text: String) { _uiState.update { it.copy(inputTopic = text) } }
    fun updateInputContext(text: String) { _uiState.update { it.copy(inputContext = text) } }
    fun updateTargetLength(length: String) { _uiState.update { it.copy(targetLength = length) } }
    fun updateCustomInstructions(text: String) { _uiState.update { it.copy(customInstructions = text) } }

    fun validateInput(): String? {
        val state = _uiState.value
        return when (state.selectedOperation) {
            OperationKind.TEXT_GEN -> if (state.inputTopic.isBlank()) "トピックを入力してください" else null
            OperationKind.REVISION_FULL -> if (state.inputSourceText.isBlank()) "元の文章を入力してください" else null
            else -> null
        }
    }

    fun generatePrompt() {
        val state = _uiState.value
        val prompt = buildString {
            append("あなたは優秀なライティングアシスタントです。\n")
            append("指示: ${state.selectedOperation.displayName}\n")
            when (state.selectedOperation) {
                OperationKind.TEXT_GEN -> append("トピック: ${state.inputTopic}\n")
                OperationKind.REVISION_FULL -> append("原文: ${state.inputSourceText}\n")
                else -> {}
            }
            if (state.customInstructions.isNotBlank()) {
                append("追加指示: ${state.customInstructions}\n")
            }
            append("\n出力は以下のJSON形式でお願いします:\n")
            append("{\"title\": \"...\", \"body\": \"...\"}")
        }
        _uiState.update { it.copy(generatedPrompt = prompt, statusMessage = "プロンプトを生成しました") }
    }

    fun updateAiOutput(output: String) {
        _uiState.update { it.copy(aiOutput = output) }
    }

    fun parseAndSaveOutput() {
        val output = _uiState.value.aiOutput
        viewModelScope.launch {
            try {
                val work = Work(
                    kind = _uiState.value.selectedOperation.name,
                    title = "AI生成作品",
                    bodyText = output,
                    createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
                workDao.insert(work)
                _uiState.update { it.copy(lastSaveResult = SaveResult.Success(1, "作品"), statusMessage = "保存しました") }
            } catch (e: Exception) {
                _uiState.update { it.copy(lastSaveResult = SaveResult.Error("保存に失敗しました: ${e.message}")) }
            }
        }
    }

    fun navigateTo(screen: WorkbenchScreenType) {}
}

enum class WorkbenchScreenType { HOME, SESSION, HISTORY, REVIEW }
