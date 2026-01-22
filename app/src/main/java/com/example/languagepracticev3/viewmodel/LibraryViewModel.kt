package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.StudyCardDao
import com.example.languagepracticev3.data.database.WorkDao
import com.example.languagepracticev3.data.model.StudyCard
import com.example.languagepracticev3.data.model.Work
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val workDao: WorkDao,
    private val studyCardDao: StudyCardDao
) : ViewModel() {

    val works: StateFlow<List<Work>> = workDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyCards: StateFlow<List<StudyCard>> = studyCardDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Work>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                workDao.getAll()
            } else {
                workDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWork = MutableStateFlow<Work?>(null)
    val selectedWork: StateFlow<Work?> = _selectedWork.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectWork(work: Work?) {
        _selectedWork.value = work
    }

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
}