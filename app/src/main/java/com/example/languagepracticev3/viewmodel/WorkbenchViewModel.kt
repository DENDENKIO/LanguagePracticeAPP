// app/src/main/java/com/example/languagepracticev3/viewmodel/WorkbenchViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkbenchViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    // 作業台の現在のコンテンツ
    private val _currentContent = MutableStateFlow("")
    val currentContent: StateFlow<String> = _currentContent.asStateFlow()

    // 選択中の作品ID
    private val _selectedWorkId = MutableStateFlow<Long?>(null)
    val selectedWorkId: StateFlow<Long?> = _selectedWorkId.asStateFlow()

    // 最近使った作品リスト
    private val _recentWorks = MutableStateFlow<List<Work>>(emptyList())
    val recentWorks: StateFlow<List<Work>> = _recentWorks.asStateFlow()

    init {
        loadRecentWorks()
    }

    private fun loadRecentWorks() {
        viewModelScope.launch {
            _recentWorks.value = repository.getRecentWorks(10)
        }
    }

    // データ管理画面から作品を読み込む
    fun loadWork(workId: Long) {
        viewModelScope.launch {
            repository.getWorkById(workId)?.let { work ->
                _currentContent.value = work.bodyText ?: ""
                _selectedWorkId.value = work.id
            }
        }
    }

    // 現在の内容を新しい作品として保存
    fun saveAsNewWork(title: String, kind: String, writerName: String? = null) {
        viewModelScope.launch {
            val work = Work(
                kind = kind,
                title = title,
                bodyText = _currentContent.value,
                createdAt = java.time.Instant.now().toString(),
                runLogId = null,
                writerName = writerName,
                readerNote = null,
                toneLabel = null
            )
            val newId = repository.insertWork(work)
            _selectedWorkId.value = newId
            loadRecentWorks()
        }
    }

    // 既存の作品を更新
    fun updateWork(work: Work) {
        viewModelScope.launch {
            val updated = work.copy(bodyText = _currentContent.value)
            repository.insertWork(updated)
        }
    }

    // コンテンツを更新
    fun updateContent(content: String) {
        _currentContent.value = content
    }

    // 作業台をクリア
    fun clearWorkbench() {
        _currentContent.value = ""
        _selectedWorkId.value = null
    }

    // トピックから作業台に読み込む
    fun loadFromTopic(topicId: Long) {
        viewModelScope.launch {
            // トピックの内容を作業台に展開
            // 実装はアプリの要件に応じて
        }
    }

    // ペルソナから作業台に読み込む
    fun loadFromPersona(personaId: Long) {
        viewModelScope.launch {
            repository.getAllPersonas().first()
                .find { it.id == personaId }
                ?.let { persona ->
                    val template = buildString {
                        appendLine("# ${persona.name}")
                        persona.location?.let { appendLine("場所: $it") }
                        persona.bio?.let { appendLine("\n$it") }
                        persona.style?.let { appendLine("\nスタイル: $it") }
                    }
                    _currentContent.value = template
                }
        }
    }
}
