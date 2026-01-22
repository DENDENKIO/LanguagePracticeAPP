package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.*
import com.example.languagepracticev3.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val works: List<Work> = emptyList(),
    val studyCards: List<StudyCard> = emptyList(),
    val personas: List<Persona> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val observations: List<Observation> = emptyList(),
    val searchKeyword: String = "",
    val selectedTab: LibraryTab = LibraryTab.WORKS,
    val isLoading: Boolean = false
)

enum class LibraryTab {
    WORKS,
    STUDY_CARDS,
    PERSONAS,
    TOPICS,
    OBSERVATIONS
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val workDao: WorkDao,
    private val studyCardDao: StudyCardDao,
    private val personaDao: PersonaDao,
    private val topicDao: TopicDao,
    private val observationDao: ObservationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchKeyword = MutableStateFlow("")

    init {
        loadAllData()

        // 検索キーワードの変更を監視
        viewModelScope.launch {
            _searchKeyword
                .debounce(300)
                .collectLatest { keyword ->
                    if (keyword.isBlank()) {
                        loadAllData()
                    } else {
                        searchData(keyword)
                    }
                }
        }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                workDao.observeAll(),
                studyCardDao.observeAll(),
                personaDao.observeAll(),
                topicDao.observeAll(),
                observationDao.observeAll()
            ) { works, studyCards, personas, topics, observations ->
                LibraryUiState(
                    works = works,
                    studyCards = studyCards,
                    personas = personas,
                    topics = topics,
                    observations = observations,
                    searchKeyword = _searchKeyword.value,
                    selectedTab = _uiState.value.selectedTab,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun searchData(keyword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                workDao.search(keyword),
                studyCardDao.search(keyword),
                personaDao.search(keyword),
                topicDao.search(keyword),
                observationDao.search(keyword)
            ) { works, studyCards, personas, topics, observations ->
                _uiState.value.copy(
                    works = works,
                    studyCards = studyCards,
                    personas = personas,
                    topics = topics,
                    observations = observations,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _searchKeyword.value = keyword
        _uiState.update { it.copy(searchKeyword = keyword) }
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun deleteWork(work: Work) {
        viewModelScope.launch {
            workDao.delete(work)
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
        if (_searchKeyword.value.isBlank()) {
            loadAllData()
        } else {
            searchData(_searchKeyword.value)
        }
    }
}
