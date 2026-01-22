package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.TopicDao
import com.example.languagepracticev3.data.database.WorkDao
import com.example.languagepracticev3.data.model.Topic
import com.example.languagepracticev3.data.model.Work
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class WorkbenchViewModel @Inject constructor(
    private val workDao: WorkDao,
    private val topicDao: TopicDao
) : ViewModel() {

    val works: StateFlow<List<Work>> = workDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topics: StateFlow<List<Topic>> = topicDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTopic = MutableStateFlow<Topic?>(null)
    val selectedTopic: StateFlow<Topic?> = _selectedTopic.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _outputText = MutableStateFlow("")
    val outputText: StateFlow<String> = _outputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun selectTopic(topic: Topic?) {
        _selectedTopic.value = topic
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun createNewTopic(title: String, emotion: String, scene: String, tags: String) {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val topic = Topic(
                title = title,
                emotion = emotion,
                scene = scene,
                tags = tags,
                createdAt = now
            )
            topicDao.insert(topic)
        }
    }

    fun saveWork(title: String, bodyText: String, kind: String = "TEXT_GEN") {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val work = Work(
                kind = kind,
                title = title,
                bodyText = bodyText,
                createdAt = now
            )
            workDao.insert(work)
        }
    }

    fun deleteWork(work: Work) {
        viewModelScope.launch {
            workDao.delete(work)
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            topicDao.delete(topic)
        }
    }

    fun generateText() {
        viewModelScope.launch {
            _isLoading.value = true
            _outputText.value = "AI生成機能は外部APIとの連携が必要です。\n設定画面でAIサイトURLを設定してください。"
            _isLoading.value = false
        }
    }

    fun clearOutput() {
        _outputText.value = ""
    }
}