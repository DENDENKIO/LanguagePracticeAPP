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

    // 検索結果（Works）
    val searchResults: StateFlow<List<Work>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                workDao.observeAll()
            } else {
                workDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // StudyCards
    val studyCards: StateFlow<List<StudyCard>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                studyCardDao.observeAll()
            } else {
                studyCardDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Personas
    val personas: StateFlow<List<Persona>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                personaDao.observeAll()
            } else {
                personaDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Topics
    val topics: StateFlow<List<Topic>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                topicDao.observeAll()
            } else {
                topicDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observations
    val observations: StateFlow<List<Observation>> = _searchQuery
        .debounce(300)
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

    // 削除メソッド
    fun deleteWork(work: Work) {
        viewModelScope.launch {
            workDao.delete(work)
            if (_selectedWork.value?.id == work.id) {
                _selectedWork.value = null
            }
        }
    }

    fun deleteStudyCard(card: StudyCard) {
        viewModelScope.launch {
            studyCardDao.delete(card)
        }
    }

    fun deletePersona(persona: Persona) {
        viewModelScope.launch {
            personaDao.delete(persona)
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            topicDao.delete(topic)
        }
    }

    fun deleteObservation(observation: Observation) {
        viewModelScope.launch {
            observationDao.delete(observation)
        }
    }

    fun refresh() {
        // 検索クエリを再発行してリフレッシュ
        val current = _searchQuery.value
        _searchQuery.value = ""
        _searchQuery.value = current
    }
}
