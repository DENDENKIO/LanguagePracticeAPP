// app/src/main/java/com/example/languagepracticev3/viewmodel/LibraryViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.*
import com.example.languagepracticev3.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab {
    WORKS,
    STUDY_CARDS,
    PERSONAS,
    TOPICS,
    OBSERVATIONS
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val workDao: WorkDao,
    private val studyCardDao: StudyCardDao,
    private val personaDao: PersonaDao,
    private val topicDao: TopicDao,
    private val observationDao: ObservationDao
) : ViewModel() {

    // 検索クエリ
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 選択中の作品
    private val _selectedWork = MutableStateFlow<Work?>(null)
    val selectedWork: StateFlow<Work?> = _selectedWork.asStateFlow()

    // 選択中のタブ
    private val _selectedTab = MutableStateFlow(LibraryTab.WORKS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    // ローディング状態
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ★追加: リフレッシュトリガー
    private val _refreshTrigger = MutableStateFlow(0)

    // 検索結果（Works）- リフレッシュトリガー追加
    val searchResults: StateFlow<List<Work>> = combine(
        _searchQuery.debounce(300),
        _refreshTrigger
    ) { query, _ -> query }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                workDao.observeAll()
            } else {
                workDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // StudyCards
    val studyCards: StateFlow<List<StudyCard>> = combine(
        _searchQuery.debounce(300),
        _refreshTrigger
    ) { query, _ -> query }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                studyCardDao.observeAll()
            } else {
                studyCardDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Personas
    val personas: StateFlow<List<Persona>> = combine(
        _searchQuery.debounce(300),
        _refreshTrigger
    ) { query, _ -> query }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                personaDao.observeAll()
            } else {
                personaDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Topics
    val topics: StateFlow<List<Topic>> = combine(
        _searchQuery.debounce(300),
        _refreshTrigger
    ) { query, _ -> query }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                topicDao.observeAll()
            } else {
                topicDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observations
    val observations: StateFlow<List<Observation>> = combine(
        _searchQuery.debounce(300),
        _refreshTrigger
    ) { query, _ -> query }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                observationDao.observeAll()
            } else {
                observationDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 検索クエリ更新
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 作品選択
    fun selectWork(work: Work?) {
        _selectedWork.value = work
    }

    // タブ選択
    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    // ========== 削除メソッド ==========
    fun deleteWork(work: Work) {
        viewModelScope.launch {
            workDao.delete(work)
            if (_selectedWork.value?.id == work.id) {
                _selectedWork.value = null
            }
            triggerRefresh()
        }
    }

    fun deleteStudyCard(card: StudyCard) {
        viewModelScope.launch {
            studyCardDao.delete(card)
            triggerRefresh()
        }
    }

    fun deletePersona(persona: Persona) {
        viewModelScope.launch {
            personaDao.delete(persona)
            triggerRefresh()
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            topicDao.delete(topic)
            triggerRefresh()
        }
    }

    fun deleteObservation(observation: Observation) {
        viewModelScope.launch {
            observationDao.delete(observation)
            triggerRefresh()
        }
    }

    // ★追加: 汎用削除メソッド（LibraryScreen用）
    fun deleteItem(item: Any) {
        when (item) {
            is Work -> deleteWork(item)
            is StudyCard -> deleteStudyCard(item)
            is Persona -> deletePersona(item)
            is Topic -> deleteTopic(item)
            is Observation -> deleteObservation(item)
        }
    }

    // ========== ★追加: 更新メソッド ==========
    fun updateWork(work: Work) {
        viewModelScope.launch {
            workDao.update(work)
            triggerRefresh()
        }
    }

    fun updateStudyCard(card: StudyCard) {
        viewModelScope.launch {
            studyCardDao.update(card)
            triggerRefresh()
        }
    }

    fun updatePersona(persona: Persona) {
        viewModelScope.launch {
            personaDao.update(persona)
            triggerRefresh()
        }
    }

    fun updateTopic(topic: Topic) {
        viewModelScope.launch {
            topicDao.update(topic)
            triggerRefresh()
        }
    }

    fun updateObservation(observation: Observation) {
        viewModelScope.launch {
            observationDao.update(observation)
            triggerRefresh()
        }
    }

    // ★追加: ペルソナ検証ステータス更新
    fun updatePersonaStatus(personaId: Long, status: String) {
        viewModelScope.launch {
            personaDao.updateStatus(personaId, status)
            triggerRefresh()
        }
    }

    // リフレッシュ
    fun refresh() {
        triggerRefresh()
    }

    private fun triggerRefresh() {
        _refreshTrigger.value++
    }
}
