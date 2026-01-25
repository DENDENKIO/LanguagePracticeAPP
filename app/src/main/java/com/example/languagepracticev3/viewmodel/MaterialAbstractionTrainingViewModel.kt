// app/src/main/java/com/example/languagepracticev3/viewmodel/MaterialAbstractionTrainingViewModel.kt
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
 * 物質-抽象変換トレーニングの UI 状態
 */
data class MaterialAbstractionUiState(
    // セッション管理
    val currentSession: MaterialAbstractionSession? = null,
    val sessions: List<MaterialAbstractionSession> = emptyList(),
    val currentStep: MaterialAbstractionStep = MaterialAbstractionStep.OBSERVATION,

    // Phase 1: 観察
    val inputTargetMaterial: String = "",
    val inputObservationRaw: String = "",

    // Phase 2: 特徴抽出
    val inputFeatures: List<String> = listOf("", "", "", "", ""),

    // Phase 3.5: 軸・タグ選択
    val selectedAxes: Set<Int> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val generatedTagSentences: List<String> = emptyList(),
    val inputCustomTagSentence: String = "",
    val modePreference: String = "abstract",  // "abstract" or "sensory"

    // Phase 3.6: 収束
    val strongTagSentenceIndices: Set<Int> = emptySet(),

    // Phase 4: 連想
    val inputAssociations: Map<Int, List<String>> = emptyMap(),

    // Phase 5: テーマ決定
    val inputAbstractTheme: String = "",
    val inputForbiddenWords: List<String> = emptyList(),

    // Phase 6: 最終表現
    val inputFinalExpression: String = "",

    // スコアリング
    val abstractScore: Int = 0,
    val sensoryScore: Int = 0,

    // UI状態
    val isLoading: Boolean = false,
    val showSessionPicker: Boolean = false,
    val statusMessage: String = "",
    val showConfirmExit: Boolean = false,
    val showAxisSelector: Boolean = false,
    val showTagSelector: Boolean = false,
    val showTemplateSelector: Boolean = false,
    val showHistoryDetail: Boolean = false,
    val historyDetailSession: MaterialAbstractionSession? = null
)

@HiltViewModel
class MaterialAbstractionTrainingViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialAbstractionUiState())
    val uiState: StateFlow<MaterialAbstractionUiState> = _uiState.asStateFlow()

    // 辞書へのアクセス
    val dictionary = MaterialAbstractionDictionary

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
            currentStep = MaterialAbstractionStep.OBSERVATION,
            inputTargetMaterial = "",
            inputObservationRaw = "",
            inputFeatures = listOf("", "", "", "", ""),
            selectedAxes = emptySet(),
            selectedTags = emptySet(),
            generatedTagSentences = emptyList(),
            inputCustomTagSentence = "",
            strongTagSentenceIndices = emptySet(),
            inputAssociations = emptyMap(),
            inputAbstractTheme = "",
            inputForbiddenWords = emptyList(),
            inputFinalExpression = "",
            abstractScore = 0,
            sensoryScore = 0
        )}
    }

    fun loadSession(session: MaterialAbstractionSession) {
        _uiState.update { it.copy(
            currentSession = session,
            currentStep = MaterialAbstractionStep.entries.getOrElse(session.currentStep) {
                MaterialAbstractionStep.OBSERVATION
            },
            inputTargetMaterial = session.targetMaterial,
            inputObservationRaw = session.observationRaw,
            inputFeatures = session.featureList.split("\n").let { list ->
                if (list.size >= 5) list.take(5) else list + List(5 - list.size) { "" }
            },
            selectedAxes = session.selectedAxes.split(",").mapNotNull { it.toIntOrNull() }.toSet(),
            selectedTags = session.selectedTags.split(",").filter { it.isNotBlank() }.toSet(),
            generatedTagSentences = session.tagSentences.split("\n").filter { it.isNotBlank() },
            strongTagSentenceIndices = session.strongTagSentences.split("\n")
                .mapIndexedNotNull { index, s -> if (s.isNotBlank()) index else null }.toSet(),
            inputAssociations = parseAssociations(session.associations),
            inputAbstractTheme = session.abstractTheme,
            inputForbiddenWords = session.forbiddenWords.split(",").filter { it.isNotBlank() },
            inputFinalExpression = session.finalExpression,
            abstractScore = session.abstractScore,
            sensoryScore = session.sensoryScore,
            showSessionPicker = false
        )}
    }

    private fun parseAssociations(raw: String): Map<Int, List<String>> {
        if (raw.isBlank()) return emptyMap()
        return try {
            raw.split("|||").mapIndexed { index, group ->
                index to group.split(";;").filter { it.isNotBlank() }
            }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun serializeAssociations(associations: Map<Int, List<String>>): String {
        return associations.entries.sortedBy { it.key }
            .joinToString("|||") { (_, list) -> list.joinToString(";;") }
    }

    fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updatedSession = session.copy(
                sessionTitle = state.inputTargetMaterial.take(30),
                targetMaterial = state.inputTargetMaterial,
                observationRaw = state.inputObservationRaw,
                featureList = state.inputFeatures.filter { it.isNotBlank() }.joinToString("\n"),
                selectedAxes = state.selectedAxes.joinToString(","),
                selectedTags = state.selectedTags.joinToString(","),
                tagSentences = state.generatedTagSentences.joinToString("\n"),
                strongTagSentences = state.strongTagSentenceIndices.mapNotNull { idx ->
                    state.generatedTagSentences.getOrNull(idx)
                }.joinToString("\n"),
                associations = serializeAssociations(state.inputAssociations),
                abstractTheme = state.inputAbstractTheme,
                forbiddenWords = state.inputForbiddenWords.joinToString(","),
                finalExpression = state.inputFinalExpression,
                abstractScore = state.abstractScore,
                sensoryScore = state.sensoryScore,
                currentStep = state.currentStep.ordinal,
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

            // スコア計算
            val absScore = calculateAbstractScore(state)
            val senScore = calculateSensoryScore(state)

            val completedSession = session.copy(
                sessionTitle = state.inputTargetMaterial.take(30),
                targetMaterial = state.inputTargetMaterial,
                observationRaw = state.inputObservationRaw,
                featureList = state.inputFeatures.filter { it.isNotBlank() }.joinToString("\n"),
                selectedAxes = state.selectedAxes.joinToString(","),
                selectedTags = state.selectedTags.joinToString(","),
                tagSentences = state.generatedTagSentences.joinToString("\n"),
                strongTagSentences = state.strongTagSentenceIndices.mapNotNull { idx ->
                    state.generatedTagSentences.getOrNull(idx)
                }.joinToString("\n"),
                associations = serializeAssociations(state.inputAssociations),
                abstractTheme = state.inputAbstractTheme,
                forbiddenWords = state.inputForbiddenWords.joinToString(","),
                finalExpression = state.inputFinalExpression,
                abstractScore = absScore,
                sensoryScore = senScore,
                currentStep = state.currentStep.ordinal,
                isCompleted = true,
                updatedAt = now
            )

            try {
                repository.saveMaterialAbstractionSession(completedSession)
                _uiState.update { it.copy(
                    currentSession = null,
                    statusMessage = "セッションを完了しました",
                    abstractScore = absScore,
                    sensoryScore = senScore
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "完了エラー: ${e.message}") }
            }
        }
    }

    private fun calculateAbstractScore(state: MaterialAbstractionUiState): Int {
        var score = 0
        // 変化がある
        if (state.selectedTags.any { it.contains("CORE-004") || it.contains("CORE-005") }) score++
        // 境界がある
        if (state.selectedAxes.contains(3) || state.selectedTags.any { it.startsWith("STR-") }) score++
        // 不可逆がある
        if (state.selectedTags.any { it.contains("CORE-005") || it.contains("TIME-030") }) score++
        // 判断/選別が絡む
        if (state.selectedTags.any { it.contains("CORE-014") || it.startsWith("VALUE-") }) score++
        // 操作主体がいる
        if (state.selectedAxes.contains(19) || state.selectedTags.any { it.startsWith("CTRL-") }) score++
        return score.coerceIn(0, 5)
    }

    private fun calculateSensoryScore(state: MaterialAbstractionUiState): Int {
        var score = 0
        val obs = state.inputObservationRaw.lowercase()
        // 触覚
        if (obs.contains("触") || obs.contains("硬") || obs.contains("柔") || obs.contains("重")) score++
        // 温度
        if (obs.contains("温") || obs.contains("冷") || obs.contains("熱")) score++
        // 湿度
        if (obs.contains("湿") || obs.contains("乾")) score++
        // 匂い
        if (obs.contains("匂") || obs.contains("香") || obs.contains("臭")) score++
        // 音または光
        if (obs.contains("音") || obs.contains("光") || obs.contains("影") || obs.contains("反射")) score++
        return score.coerceIn(0, 5)
    }

    // ====================
    // ステップナビゲーション
    // ====================

    fun nextStep() {
        val currentIndex = _uiState.value.currentStep.ordinal
        if (currentIndex < MaterialAbstractionStep.entries.size - 1) {
            _uiState.update { it.copy(
                currentStep = MaterialAbstractionStep.entries[currentIndex + 1]
            )}
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
    // 軸・タグ選択
    // ====================

    fun toggleAxis(axisId: Int) {
        _uiState.update { state ->
            val newSet = if (axisId in state.selectedAxes) {
                state.selectedAxes - axisId
            } else {
                state.selectedAxes + axisId
            }
            state.copy(selectedAxes = newSet)
        }
    }

    fun toggleTag(tagId: String) {
        _uiState.update { state ->
            val newSet = if (tagId in state.selectedTags) {
                state.selectedTags - tagId
            } else {
                state.selectedTags + tagId
            }
            state.copy(selectedTags = newSet)
        }
    }

    fun setModePreference(mode: String) {
        _uiState.update { it.copy(modePreference = mode) }
    }

    fun getRecommendedTags(): List<MaterialAbstractionDictionary.Tag> {
        val state = _uiState.value
        return dictionary.getRecommendedTagsForAxes(
            state.selectedAxes.toList(),
            state.modePreference
        )
    }

    // ====================
    // タグ文生成
    // ====================

    fun generateTagSentence(templateId: String, customValues: Map<String, String> = emptyMap()) {
        val template = dictionary.templateFrames.find { it.id == templateId } ?: return
        var sentence = template.text

        // 変数を置換
        val state = _uiState.value
        val defaultValues = mapOf(
            "対象" to state.inputTargetMaterial,
            "主体" to "手",
            "行為" to "切る",
            "状態A" to "そのまま",
            "状態B" to "変わる",
            "情報" to "中身",
            "境界" to "切れ目",
            "位置" to "端",
            "基準" to "鮮度"
        )

        template.vars.forEach { varName ->
            val value = customValues[varName] ?: defaultValues[varName] ?: "【$varName】"
            sentence = sentence.replace("【$varName】", value)
        }

        _uiState.update { it.copy(
            generatedTagSentences = it.generatedTagSentences + sentence
        )}
    }

    fun addCustomTagSentence(sentence: String) {
        if (sentence.isBlank()) return
        _uiState.update { it.copy(
            generatedTagSentences = it.generatedTagSentences + sentence,
            inputCustomTagSentence = ""
        )}
    }

    fun removeTagSentence(index: Int) {
        _uiState.update { state ->
            val newList = state.generatedTagSentences.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            state.copy(
                generatedTagSentences = newList,
                strongTagSentenceIndices = state.strongTagSentenceIndices
                    .filter { it != index }
                    .map { if (it > index) it - 1 else it }
                    .toSet()
            )
        }
    }

    // ====================
    // 収束（強いタグ文選択）
    // ====================

    fun toggleStrongTagSentence(index: Int) {
        _uiState.update { state ->
            val newSet = if (index in state.strongTagSentenceIndices) {
                state.strongTagSentenceIndices - index
            } else {
                if (state.strongTagSentenceIndices.size < 4) {
                    state.strongTagSentenceIndices + index
                } else {
                    state.strongTagSentenceIndices
                }
            }
            state.copy(strongTagSentenceIndices = newSet)
        }
    }

    // ====================
    // 連想
    // ====================

    fun updateAssociation(tagSentenceIndex: Int, associationIndex: Int, value: String) {
        _uiState.update { state ->
            val currentList = state.inputAssociations[tagSentenceIndex]?.toMutableList()
                ?: mutableListOf("", "", "", "", "")
            if (associationIndex in currentList.indices) {
                currentList[associationIndex] = value
            }
            state.copy(
                inputAssociations = state.inputAssociations + (tagSentenceIndex to currentList)
            )
        }
    }

    // ====================
    // テーマ決定
    // ====================

    fun updateAbstractTheme(value: String) {
        _uiState.update { state ->
            // テーマが変わったら禁止ワードも更新
            val forbiddenWords = if (value.isNotBlank()) {
                dictionary.commonAbstractThemes.filter {
                    it.contains(value) || value.contains(it)
                } + listOf(value)
            } else emptyList()
            state.copy(
                inputAbstractTheme = value,
                inputForbiddenWords = forbiddenWords.distinct()
            )
        }
    }

    fun addForbiddenWord(word: String) {
        if (word.isBlank()) return
        _uiState.update { it.copy(
            inputForbiddenWords = (it.inputForbiddenWords + word).distinct()
        )}
    }

    fun removeForbiddenWord(word: String) {
        _uiState.update { it.copy(
            inputForbiddenWords = it.inputForbiddenWords - word
        )}
    }

    // ====================
    // 最終表現
    // ====================

    fun updateFinalExpression(value: String) {
        _uiState.update { it.copy(inputFinalExpression = value) }
    }

    fun checkForbiddenWords(): List<String> {
        val state = _uiState.value
        return state.inputForbiddenWords.filter { word ->
            state.inputFinalExpression.contains(word)
        }
    }

    // ====================
    // 入力フィールド更新
    // ====================

    fun updateTargetMaterial(value: String) = _uiState.update { it.copy(inputTargetMaterial = value) }
    fun updateObservationRaw(value: String) = _uiState.update { it.copy(inputObservationRaw = value) }
    fun updateFeature(index: Int, value: String) {
        _uiState.update { state ->
            val newList = state.inputFeatures.toMutableList()
            if (index in newList.indices) {
                newList[index] = value
            }
            state.copy(inputFeatures = newList)
        }
    }
    fun updateCustomTagSentence(value: String) = _uiState.update { it.copy(inputCustomTagSentence = value) }

    // ====================
    // UI状態
    // ====================

    fun showSessionPicker() = _uiState.update { it.copy(showSessionPicker = true) }
    fun hideSessionPicker() = _uiState.update { it.copy(showSessionPicker = false) }
    fun showExitConfirmation() = _uiState.update { it.copy(showConfirmExit = true) }
    fun hideExitConfirmation() = _uiState.update { it.copy(showConfirmExit = false) }
    fun confirmExit() = _uiState.update { it.copy(showConfirmExit = false, currentSession = null) }
    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = "") }

    fun showAxisSelector() = _uiState.update { it.copy(showAxisSelector = true) }
    fun hideAxisSelector() = _uiState.update { it.copy(showAxisSelector = false) }
    fun showTagSelector() = _uiState.update { it.copy(showTagSelector = true) }
    fun hideTagSelector() = _uiState.update { it.copy(showTagSelector = false) }
    fun showTemplateSelector() = _uiState.update { it.copy(showTemplateSelector = true) }
    fun hideTemplateSelector() = _uiState.update { it.copy(showTemplateSelector = false) }

    // ====================
    // 履歴詳細表示
    // ====================

    fun showHistoryDetail(session: MaterialAbstractionSession) {
        _uiState.update { it.copy(
            showHistoryDetail = true,
            historyDetailSession = session
        )}
    }

    fun hideHistoryDetail() {
        _uiState.update { it.copy(
            showHistoryDetail = false,
            historyDetailSession = null
        )}
    }
}
