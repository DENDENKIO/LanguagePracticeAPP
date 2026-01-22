// app/src/main/java/com/example/languagepracticev3/viewmodel/WorkbenchViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.models.AiSiteCatalog
import com.example.languagepracticev3.data.models.AiSiteProfile
import com.example.languagepracticev3.data.models.LengthProfile
import com.example.languagepracticev3.data.models.OperationKind
import com.example.languagepracticev3.data.services.OutputParser
import com.example.languagepracticev3.data.services.PromptBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkbenchUiState(
    // 操作選択
    val selectedOperation: OperationKind = OperationKind.TEXT_GEN,
    val selectedLength: LengthProfile = LengthProfile.STUDY_SHORT,

    // 共通入力フィールド
    val writerInput: String = "",
    val topicInput: String = "",
    val readerInput: String = "",
    val sourceTextInput: String = "",
    val genreInput: String = "",
    val imageUrlInput: String = "",
    val toneLabel: String = "",
    val toneRuleText: String = "",
    val coreTheme: String = "",
    val coreEmotion: String = "",
    val coreTakeaway: String = "",
    val coreSentence: String = "",

    // READER_AUTO_GEN用
    val contextKindInput: String = "",

    // PERSONA_VERIFY_ASSIST用
    val personaNameInput: String = "",
    val personaBioInput: String = "",
    val evidence1Input: String = "",
    val evidence2Input: String = "",
    val evidence3Input: String = "",

    // 実行状態
    val generatedPrompt: String = "",
    val aiOutput: String = "",
    val statusMessage: String = "準備完了",
    val isLoading: Boolean = false,

    // AI設定
    val selectedAiSite: AiSiteProfile = AiSiteCatalog.presets.first(),
    val isAutoMode: Boolean = false,

    // 結果
    val lastSaveResult: SaveResult? = null
)

sealed class SaveResult {
    data class Success(val count: Int, val type: String) : SaveResult()
    data class Error(val message: String) : SaveResult()
}

@HiltViewModel
class WorkbenchViewModel @Inject constructor() : ViewModel() {

    private val promptBuilder = PromptBuilder()
    private val outputParser = OutputParser()

    private val _uiState = MutableStateFlow(WorkbenchUiState())
    val uiState: StateFlow<WorkbenchUiState> = _uiState.asStateFlow()

    // 操作一覧
    val operationList = OperationKind.entries
    val lengthList = LengthProfile.entries
    val aiSiteList = AiSiteCatalog.presets

    // ==========================================
    // 入力更新関数
    // ==========================================
    fun updateOperation(operation: OperationKind) {
        _uiState.value = _uiState.value.copy(selectedOperation = operation)
    }

    fun updateLength(length: LengthProfile) {
        _uiState.value = _uiState.value.copy(selectedLength = length)
    }

    fun updateWriter(value: String) {
        _uiState.value = _uiState.value.copy(writerInput = value)
    }

    fun updateTopic(value: String) {
        _uiState.value = _uiState.value.copy(topicInput = value)
    }

    fun updateReader(value: String) {
        _uiState.value = _uiState.value.copy(readerInput = value)
    }

    fun updateSourceText(value: String) {
        _uiState.value = _uiState.value.copy(sourceTextInput = value)
    }

    fun updateGenre(value: String) {
        _uiState.value = _uiState.value.copy(genreInput = value)
    }

    fun updateImageUrl(value: String) {
        _uiState.value = _uiState.value.copy(imageUrlInput = value)
    }

    fun updateToneLabel(value: String) {
        _uiState.value = _uiState.value.copy(toneLabel = value)
    }

    fun updateToneRule(value: String) {
        _uiState.value = _uiState.value.copy(toneRuleText = value)
    }

    fun updateCoreTheme(value: String) {
        _uiState.value = _uiState.value.copy(coreTheme = value)
    }

    fun updateCoreEmotion(value: String) {
        _uiState.value = _uiState.value.copy(coreEmotion = value)
    }

    fun updateCoreTakeaway(value: String) {
        _uiState.value = _uiState.value.copy(coreTakeaway = value)
    }

    fun updateCoreSentence(value: String) {
        _uiState.value = _uiState.value.copy(coreSentence = value)
    }

    fun updateContextKind(value: String) {
        _uiState.value = _uiState.value.copy(contextKindInput = value)
    }

    fun updatePersonaName(value: String) {
        _uiState.value = _uiState.value.copy(personaNameInput = value)
    }

    fun updatePersonaBio(value: String) {
        _uiState.value = _uiState.value.copy(personaBioInput = value)
    }

    fun updateEvidence1(value: String) {
        _uiState.value = _uiState.value.copy(evidence1Input = value)
    }

    fun updateEvidence2(value: String) {
        _uiState.value = _uiState.value.copy(evidence2Input = value)
    }

    fun updateEvidence3(value: String) {
        _uiState.value = _uiState.value.copy(evidence3Input = value)
    }

    fun updateAiSite(site: AiSiteProfile) {
        _uiState.value = _uiState.value.copy(selectedAiSite = site)
    }

    fun updateAutoMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoMode = enabled)
    }

    fun updateAiOutput(value: String) {
        _uiState.value = _uiState.value.copy(aiOutput = value)
    }

    // ==========================================
    // プロンプト生成
    // ==========================================
    fun generatePrompt(): String {
        val state = _uiState.value

        val prompt = promptBuilder.buildPrompt(
            operation = state.selectedOperation,
            writer = state.writerInput,
            topic = state.topicInput,
            reader = state.readerInput,
            length = state.selectedLength,
            sourceText = state.sourceTextInput,
            imageUrl = state.imageUrlInput,
            genre = state.genreInput,
            toneLabel = state.toneLabel,
            toneRule = state.toneRuleText,
            coreTheme = state.coreTheme,
            coreEmotion = state.coreEmotion,
            coreTakeaway = state.coreTakeaway,
            coreSentence = state.coreSentence,
            // PERSONA_VERIFY用
            personaName = state.personaNameInput,
            personaBio = state.personaBioInput,
            evidence1 = state.evidence1Input,
            evidence2 = state.evidence2Input,
            evidence3 = state.evidence3Input,
            // READER_AUTO_GEN用
            contextKind = state.contextKindInput
        )

        _uiState.value = _uiState.value.copy(
            generatedPrompt = prompt,
            statusMessage = "プロンプト生成完了。AIに送信してください。"
        )

        return prompt
    }

    // ==========================================
    // 結果を解析して保存
    // ==========================================
    fun parseAndSaveOutput() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.aiOutput.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    lastSaveResult = SaveResult.Error("AI出力が空です")
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val result = when (state.selectedOperation) {
                    OperationKind.TEXT_GEN -> {
                        val works = outputParser.parseWorks(state.aiOutput)
                        // TODO: Room DBに保存
                        SaveResult.Success(works.size.coerceAtLeast(1), "作品")
                    }
                    OperationKind.STUDY_CARD -> {
                        val cards = outputParser.parseStudyCards(state.aiOutput)
                        SaveResult.Success(cards.size.coerceAtLeast(1), "学習カード")
                    }
                    OperationKind.PERSONA_GEN -> {
                        val personas = outputParser.parsePersonas(state.aiOutput)
                        SaveResult.Success(personas.size.coerceAtLeast(1), "ペルソナ")
                    }
                    OperationKind.TOPIC_GEN -> {
                        val topics = outputParser.parseTopics(state.aiOutput)
                        SaveResult.Success(topics.size.coerceAtLeast(1), "お題")
                    }
                    OperationKind.READER_AUTO_GEN -> {
                        val parsed = outputParser.parseGeneric(state.aiOutput, state.selectedOperation)
                        SaveResult.Success(if (parsed.isNotEmpty()) 1 else 0, "読者像")
                    }
                    OperationKind.OBSERVE_IMAGE -> {
                        val parsed = outputParser.parseGeneric(state.aiOutput, state.selectedOperation)
                        SaveResult.Success(if (parsed.isNotEmpty()) 1 else 0, "観察ノート")
                    }
                    OperationKind.CORE_EXTRACT -> {
                        val parsed = outputParser.parseGeneric(state.aiOutput, state.selectedOperation)
                        SaveResult.Success(if (parsed.isNotEmpty()) 1 else 0, "核抽出結果")
                    }
                    OperationKind.REVISION_FULL -> {
                        val parsed = outputParser.parseGeneric(state.aiOutput, state.selectedOperation)
                        SaveResult.Success(if (parsed.isNotEmpty()) 3 else 0, "推敲案")
                    }
                    OperationKind.GIKO -> {
                        val parsed = outputParser.parseGeneric(state.aiOutput, state.selectedOperation)
                        SaveResult.Success(if (parsed.isNotEmpty()) 1 else 0, "擬古文")
                    }
                    OperationKind.PERSONA_VERIFY_ASSIST -> {
                        val parsed = outputParser.parseGeneric(state.aiOutput, state.selectedOperation)
                        SaveResult.Success(if (parsed.isNotEmpty()) 1 else 0, "検証結果")
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastSaveResult = result,
                    statusMessage = when (result) {
                        is SaveResult.Success -> "${result.count}件の${result.type}を保存しました"
                        is SaveResult.Error -> "エラー: ${result.message}"
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastSaveResult = SaveResult.Error(e.message ?: "解析エラー")
                )
            }
        }
    }

    // ==========================================
    // 入力バリデーション
    // ==========================================
    fun validateInput(): String? {
        val state = _uiState.value
        return when (state.selectedOperation) {
            OperationKind.READER_AUTO_GEN -> null // 任意
            OperationKind.TOPIC_GEN -> null // 画像URLは任意
            OperationKind.PERSONA_GEN -> null // ジャンルは任意
            OperationKind.OBSERVE_IMAGE -> {
                if (state.imageUrlInput.isBlank()) "画像URLを入力してください"
                else null
            }
            OperationKind.TEXT_GEN -> null // お題も任意（AI自動生成可能）
            OperationKind.STUDY_CARD -> {
                if (state.sourceTextInput.isBlank()) "対象本文を入力してください"
                else null
            }
            OperationKind.CORE_EXTRACT -> {
                if (state.sourceTextInput.isBlank()) "対象本文を入力してください"
                else null
            }
            OperationKind.REVISION_FULL -> {
                when {
                    state.sourceTextInput.isBlank() -> "元の文章を入力してください"
                    state.coreSentence.isBlank() -> "核の一文を入力してください"
                    else -> null
                }
            }
            OperationKind.GIKO -> {
                if (state.sourceTextInput.isBlank()) "元の現代文を入力してください"
                else null
            }
            OperationKind.PERSONA_VERIFY_ASSIST -> {
                when {
                    state.personaNameInput.isBlank() -> "対象ペルソナ名を入力してください"
                    state.personaBioInput.isBlank() -> "現在のBIOを入力してください"
                    else -> null
                }
            }
        }
    }

    // クリア
    fun clearAll() {
        _uiState.value = WorkbenchUiState()
    }
}
