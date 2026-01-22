// app/src/main/java/com/example/languagepracticev3/viewmodel/WorkbenchViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.*
import com.example.languagepracticev3.data.model.*
import com.example.languagepracticev3.data.services.OutputParser
import com.example.languagepracticev3.data.services.PromptBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// 保存結果
sealed class SaveResult {
    data class Success(val count: Int, val message: String = "") : SaveResult()
    data class Error(val message: String) : SaveResult()
}

// UI状態
data class WorkbenchUiState(
    val selectedOperation: OperationKind = OperationKind.TEXT_GEN,
    val inputTopic: String = "",
    val inputWriter: String = "",
    val inputReader: String = "",
    val inputSourceText: String = "",
    val inputGenre: String = "",
    val inputImageUrl: String = "",
    val inputToneLabel: String = "",
    val inputToneRule: String = "",
    val inputCoreTheme: String = "",
    val inputCoreEmotion: String = "",
    val inputCoreTakeaway: String = "",
    val inputCoreSentence: String = "",
    val inputTargetPersonaName: String = "",
    val inputTargetPersonaBio: String = "",
    val inputEvidence1: String = "",
    val selectedLength: LengthProfile = LengthProfile.STUDY_SHORT,
    val generatedPrompt: String = "",
    val aiOutput: String = "",
    val lastSaveResult: SaveResult? = null,
    val isProcessing: Boolean = false,
    val showAiBrowser: Boolean = false,
    val selectedAiSite: AiSiteProfile? = null
)

@HiltViewModel
class WorkbenchViewModel @Inject constructor(
    private val runLogDao: RunLogDao,
    private val workDao: WorkDao,
    private val studyCardDao: StudyCardDao,
    private val personaDao: PersonaDao,
    private val topicDao: TopicDao,
    private val observationDao: ObservationDao,
    private val kvSettingDao: KvSettingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkbenchUiState())
    val uiState: StateFlow<WorkbenchUiState> = _uiState.asStateFlow()

    private val promptBuilder = PromptBuilder()
    private val outputParser = OutputParser()

    // ====================
    // 入力更新
    // ====================
    fun updateOperation(op: OperationKind) = _uiState.update { it.copy(selectedOperation = op) }
    fun updateInputTopic(v: String) = _uiState.update { it.copy(inputTopic = v) }
    fun updateInputWriter(v: String) = _uiState.update { it.copy(inputWriter = v) }
    fun updateInputReader(v: String) = _uiState.update { it.copy(inputReader = v) }
    fun updateInputSourceText(v: String) = _uiState.update { it.copy(inputSourceText = v) }
    fun updateInputGenre(v: String) = _uiState.update { it.copy(inputGenre = v) }
    fun updateInputImageUrl(v: String) = _uiState.update { it.copy(inputImageUrl = v) }
    fun updateInputToneLabel(v: String) = _uiState.update { it.copy(inputToneLabel = v) }
    fun updateInputToneRule(v: String) = _uiState.update { it.copy(inputToneRule = v) }
    fun updateInputCoreTheme(v: String) = _uiState.update { it.copy(inputCoreTheme = v) }
    fun updateInputCoreEmotion(v: String) = _uiState.update { it.copy(inputCoreEmotion = v) }
    fun updateInputCoreTakeaway(v: String) = _uiState.update { it.copy(inputCoreTakeaway = v) }
    fun updateInputCoreSentence(v: String) = _uiState.update { it.copy(inputCoreSentence = v) }
    fun updateInputTargetPersonaName(v: String) = _uiState.update { it.copy(inputTargetPersonaName = v) }
    fun updateInputTargetPersonaBio(v: String) = _uiState.update { it.copy(inputTargetPersonaBio = v) }
    fun updateInputEvidence1(v: String) = _uiState.update { it.copy(inputEvidence1 = v) }
    fun updateSelectedLength(v: LengthProfile) = _uiState.update { it.copy(selectedLength = v) }
    fun updateAiOutput(v: String) = _uiState.update { it.copy(aiOutput = v) }

    // ====================
    // AIサイト選択
    // ====================
    val aiSiteProfiles = listOf(
        AiSiteProfile("GENSPARK", "Genspark", "https://www.genspark.ai/"),
        AiSiteProfile("PERPLEXITY", "Perplexity", "https://www.perplexity.ai/"),
        AiSiteProfile("CHATGPT", "ChatGPT", "https://chat.openai.com/"),
        AiSiteProfile("CLAUDE", "Claude", "https://claude.ai/"),
        AiSiteProfile("GEMINI", "Gemini", "https://gemini.google.com/")
    )

    fun selectAiSite(site: AiSiteProfile) {
        _uiState.update { it.copy(selectedAiSite = site) }
    }

    // ====================
    // 入力検証
    // ====================
    fun validateInput(): String? {
        val state = _uiState.value
        return when (state.selectedOperation) {
            OperationKind.TEXT_GEN -> {
                if (state.inputTopic.isBlank()) "トピックを入力してください"
                else null
            }
            OperationKind.STUDY_CARD -> {
                if (state.inputSourceText.isBlank()) "対象本文を入力してください"
                else null
            }
            OperationKind.PERSONA_GEN -> null // ジャンルは任意
            OperationKind.TOPIC_GEN -> null // 画像URLは任意
            OperationKind.OBSERVE_IMAGE -> {
                if (state.inputImageUrl.isBlank()) "画像URLを入力してください"
                else null
            }
            OperationKind.CORE_EXTRACT -> {
                if (state.inputSourceText.isBlank()) "対象本文を入力してください"
                else null
            }
            OperationKind.REVISION_FULL -> {
                when {
                    state.inputSourceText.isBlank() -> "元原稿を入力してください"
                    state.inputCoreSentence.isBlank() -> "核の一文を入力してください"
                    else -> null
                }
            }
            OperationKind.GIKO -> {
                if (state.inputSourceText.isBlank()) "元の現代文を入力してください"
                else null
            }
            OperationKind.PERSONA_VERIFY_ASSIST -> {
                when {
                    state.inputTargetPersonaName.isBlank() -> "対象ペルソナ名を入力してください"
                    state.inputTargetPersonaBio.isBlank() -> "対象ペルソナのBioを入力してください"
                    else -> null
                }
            }
            else -> null
        }
    }

    // ====================
    // プロンプト生成
    // ====================
    fun generatePrompt() {
        val state = _uiState.value
        val prompt = when (state.selectedOperation) {
            OperationKind.TEXT_GEN -> promptBuilder.buildTextGenPrompt(
                writer = state.inputWriter,
                topic = state.inputTopic,
                reader = state.inputReader,
                constraint = "",
                length = state.selectedLength
            )
            OperationKind.STUDY_CARD -> promptBuilder.buildStudyCardPrompt(
                reader = state.inputReader,
                constraint = "",
                sourceText = state.inputSourceText
            )
            OperationKind.PERSONA_GEN -> promptBuilder.buildPersonaGenPrompt(state.inputGenre)
            OperationKind.TOPIC_GEN -> promptBuilder.buildTopicGenPrompt(state.inputImageUrl)
            OperationKind.OBSERVE_IMAGE -> promptBuilder.buildObserveImagePrompt(state.inputImageUrl)
            OperationKind.CORE_EXTRACT -> promptBuilder.buildCoreExtractPrompt(
                reader = state.inputReader,
                sourceText = state.inputSourceText
            )
            OperationKind.GIKO -> promptBuilder.buildGikoPrompt(
                toneLabel = state.inputToneLabel,
                toneRule = state.inputToneRule,
                reader = state.inputReader,
                topic = state.inputTopic,
                sourceText = state.inputSourceText
            )
            OperationKind.REVISION_FULL -> promptBuilder.buildRevisionFullPrompt(
                sourceText = state.inputSourceText,
                coreTheme = state.inputCoreTheme,
                coreEmotion = state.inputCoreEmotion,
                coreTakeaway = state.inputCoreTakeaway,
                reader = state.inputReader,
                coreSentence = state.inputCoreSentence
            )
            OperationKind.PERSONA_VERIFY_ASSIST -> promptBuilder.buildPersonaVerifyPrompt(
                personaName = state.inputTargetPersonaName,
                personaBio = state.inputTargetPersonaBio,
                evidence1 = state.inputEvidence1,
                evidence2 = "",
                evidence3 = ""
            )
            else -> ""
        }
        _uiState.update { it.copy(generatedPrompt = prompt) }
    }

    // ====================
    // AIブラウザ表示制御
    // ====================
    fun openAiBrowser() {
        _uiState.update { it.copy(showAiBrowser = true) }
    }

    fun closeAiBrowser() {
        _uiState.update { it.copy(showAiBrowser = false) }
    }

    // ====================
    // AI結果受信 & 自動保存
    // ====================
    fun onAiResultReceived(resultText: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    aiOutput = resultText,
                    showAiBrowser = false,
                    isProcessing = true
                )
            }
            // 自動的に解析・保存
            parseAndSaveOutput()
        }
    }

    // ====================
    // 解析して保存
    // ====================
    fun parseAndSaveOutput() {
        viewModelScope.launch {
            val state = _uiState.value
            val output = state.aiOutput
            val op = state.selectedOperation

            if (output.isBlank()) {
                _uiState.update {
                    it.copy(
                        lastSaveResult = SaveResult.Error("AI出力が空です"),
                        isProcessing = false
                    )
                }
                return@launch
            }

            try {
                // 1. 実行ログを保存
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val runLog = RunLog(
                    operationKind = op.name,
                    status = "SUCCESS",
                    createdAt = now,
                    promptText = state.generatedPrompt,
                    rawOutput = output,
                    errorCode = null
                )
                val runLogId = runLogDao.insert(runLog)

                // 2. 操作種別に応じて解析・保存
                val result = when (op) {
                    OperationKind.TEXT_GEN -> saveTextGenResult(output, runLogId, now)
                    OperationKind.STUDY_CARD -> saveStudyCardResult(output, runLogId, now)
                    OperationKind.PERSONA_GEN -> savePersonaGenResult(output, now)
                    OperationKind.TOPIC_GEN -> saveTopicGenResult(output, now)
                    OperationKind.OBSERVE_IMAGE -> saveObservationResult(output, now)
                    OperationKind.CORE_EXTRACT -> saveCoreExtractResult(output, runLogId, now)
                    OperationKind.GIKO -> saveGikoResult(output, runLogId, now)
                    OperationKind.REVISION_FULL -> saveRevisionResult(output, runLogId, now)
                    else -> SaveResult.Success(0, "この操作は保存対象外です")
                }

                _uiState.update {
                    it.copy(
                        lastSaveResult = result,
                        isProcessing = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        lastSaveResult = SaveResult.Error("保存エラー: ${e.message}"),
                        isProcessing = false
                    )
                }
            }
        }
    }

    // ====================
    // 各操作の保存処理
    // ====================

    private suspend fun saveTextGenResult(output: String, runLogId: Long, now: String): SaveResult {
        val parsed = outputParser.parseTextGenOutput(output)
        if (parsed == null) {
            return SaveResult.Error("テキスト生成結果の解析に失敗しました")
        }

        val work = Work(
            kind = "TEXT_GEN",
            title = parsed.title,
            bodyText = parsed.bodyText,
            createdAt = now,
            runLogId = runLogId,
            writerName = _uiState.value.inputWriter,
            readerNote = _uiState.value.inputReader,
            toneLabel = null
        )
        workDao.insert(work)
        return SaveResult.Success(1, "作品を保存しました")
    }

    private suspend fun saveStudyCardResult(output: String, runLogId: Long, now: String): SaveResult {
        val parsed = outputParser.parseStudyCardOutput(output)
        if (parsed == null) {
            return SaveResult.Error("学習カードの解析に失敗しました")
        }

        val card = StudyCard(
            sourceWorkId = null,
            createdAt = now,
            focus = parsed.focus,
            level = parsed.level,
            bestExpressionsRaw = parsed.bestExpressions,
            metaphorChainsRaw = parsed.metaphorChains,
            doNextRaw = parsed.doNext,
            tags = parsed.tags,
            fullParsedContent = output
        )
        studyCardDao.insert(card)
        return SaveResult.Success(1, "学習カードを保存しました")
    }

    private suspend fun savePersonaGenResult(output: String, now: String): SaveResult {
        val personas = outputParser.parsePersonaGenOutput(output)
        if (personas.isEmpty()) {
            return SaveResult.Error("ペルソナの解析に失敗しました")
        }

        var count = 0
        for (p in personas) {
            val persona = Persona(
                name = p.name,
                location = p.location,
                bio = p.bio,
                style = p.style,
                tags = p.tags,
                verificationStatus = "未検証",
                createdAt = now
            )
            personaDao.insert(persona)
            count++
        }
        return SaveResult.Success(count, "${count}件のペルソナを保存しました")
    }

    private suspend fun saveTopicGenResult(output: String, now: String): SaveResult {
        val topics = outputParser.parseTopicGenOutput(output)
        if (topics.isEmpty()) {
            return SaveResult.Error("トピックの解析に失敗しました")
        }

        var count = 0
        for (t in topics) {
            val topic = Topic(
                title = t.title,
                emotion = t.emotion,
                scene = t.scene,
                tags = t.tags,
                fixConditions = t.fixConditions,
                createdAt = now
            )
            topicDao.insert(topic)
            count++
        }
        return SaveResult.Success(count, "${count}件のトピックを保存しました")
    }

    private suspend fun saveObservationResult(output: String, now: String): SaveResult {
        val parsed = outputParser.parseObservationOutput(output)
        if (parsed == null) {
            return SaveResult.Error("観察ノートの解析に失敗しました")
        }

        val observation = Observation(
            imageUrl = _uiState.value.inputImageUrl,
            motif = parsed.motif,
            visualRaw = parsed.visual,
            soundRaw = parsed.sound,
            metaphorsRaw = parsed.metaphors,
            coreCandidatesRaw = parsed.coreCandidates,
            fullContent = output,
            createdAt = now
        )
        observationDao.insert(observation)
        return SaveResult.Success(1, "観察ノートを保存しました")
    }

    private suspend fun saveCoreExtractResult(output: String, runLogId: Long, now: String): SaveResult {
        val parsed = outputParser.parseCoreExtractOutput(output)
        if (parsed == null) {
            return SaveResult.Error("核抽出結果の解析に失敗しました")
        }

        val work = Work(
            kind = "CORE_EXTRACT",
            title = "核抽出: ${parsed.theme.take(20)}...",
            bodyText = output,
            createdAt = now,
            runLogId = runLogId,
            writerName = null,
            readerNote = _uiState.value.inputReader,
            toneLabel = null
        )
        workDao.insert(work)
        return SaveResult.Success(1, "核抽出結果を保存しました")
    }

    private suspend fun saveGikoResult(output: String, runLogId: Long, now: String): SaveResult {
        val parsed = outputParser.parseGikoOutput(output)
        if (parsed == null) {
            return SaveResult.Error("擬古文の解析に失敗しました")
        }

        val work = Work(
            kind = "GIKO",
            title = parsed.title,
            bodyText = parsed.bodyText,
            createdAt = now,
            runLogId = runLogId,
            writerName = null,
            readerNote = _uiState.value.inputReader,
            toneLabel = _uiState.value.inputToneLabel
        )
        workDao.insert(work)
        return SaveResult.Success(1, "擬古文を保存しました")
    }

    private suspend fun saveRevisionResult(output: String, runLogId: Long, now: String): SaveResult {
        val revisions = outputParser.parseRevisionOutput(output)
        if (revisions.isEmpty()) {
            return SaveResult.Error("推敲案の解析に失敗しました")
        }

        var count = 0
        for ((index, r) in revisions.withIndex()) {
            val work = Work(
                kind = "REVISION",
                title = "推敲案 ${index + 1}",
                bodyText = r.bodyText,
                createdAt = now,
                runLogId = runLogId,
                writerName = null,
                readerNote = r.comment,
                toneLabel = null
            )
            workDao.insert(work)
            count++
        }
        return SaveResult.Success(count, "${count}件の推敲案を保存しました")
    }

    // ====================
    // 入力クリア
    // ====================
    fun clearInputs() {
        _uiState.update {
            it.copy(
                inputTopic = "",
                inputWriter = "",
                inputReader = "",
                inputSourceText = "",
                inputGenre = "",
                inputImageUrl = "",
                inputToneLabel = "",
                inputToneRule = "",
                inputCoreTheme = "",
                inputCoreEmotion = "",
                inputCoreTakeaway = "",
                inputCoreSentence = "",
                inputTargetPersonaName = "",
                inputTargetPersonaBio = "",
                inputEvidence1 = "",
                generatedPrompt = "",
                aiOutput = "",
                lastSaveResult = null
            )
        }
    }
}
