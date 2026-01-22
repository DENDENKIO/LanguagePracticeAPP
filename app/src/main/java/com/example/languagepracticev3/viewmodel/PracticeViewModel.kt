package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.PracticeSessionDao
import com.example.languagepracticev3.data.model.PracticeSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val practiceSessionDao: PracticeSessionDao
) : ViewModel() {

    val sessions: StateFlow<List<PracticeSession>> = practiceSessionDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSession = MutableStateFlow<PracticeSession?>(null)
    val currentSession: StateFlow<PracticeSession?> = _currentSession.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _drillAMemo = MutableStateFlow("")
    val drillAMemo: StateFlow<String> = _drillAMemo.asStateFlow()

    private val _drillBMetaphors = MutableStateFlow("")
    val drillBMetaphors: StateFlow<String> = _drillBMetaphors.asStateFlow()

    private val _drillCDraft = MutableStateFlow("")
    val drillCDraft: StateFlow<String> = _drillCDraft.asStateFlow()

    private val _drillCCore = MutableStateFlow("")
    val drillCCore: StateFlow<String> = _drillCCore.asStateFlow()

    private val _drillCRevision = MutableStateFlow("")
    val drillCRevision: StateFlow<String> = _drillCRevision.asStateFlow()

    private val _wrapBestOne = MutableStateFlow("")
    val wrapBestOne: StateFlow<String> = _wrapBestOne.asStateFlow()

    private val _wrapTodo = MutableStateFlow("")
    val wrapTodo: StateFlow<String> = _wrapTodo.asStateFlow()

    fun startNewSession(packId: String = "default") {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val session = PracticeSession(
                packId = packId,
                createdAt = now
            )
            val id = practiceSessionDao.insert(session)
            _currentSession.value = session.copy(id = id)
            _currentStep.value = 0
            clearAllInputs()
        }
    }

    fun loadSession(session: PracticeSession) {
        _currentSession.value = session
        _drillAMemo.value = session.drillAMemo
        _drillBMetaphors.value = session.drillBMetaphors
        _drillCDraft.value = session.drillCDraft
        _drillCCore.value = session.drillCCore
        _drillCRevision.value = session.drillCRevision
        _wrapBestOne.value = session.wrapBestOne
        _wrapTodo.value = session.wrapTodo
    }

    private fun clearAllInputs() {
        _drillAMemo.value = ""
        _drillBMetaphors.value = ""
        _drillCDraft.value = ""
        _drillCCore.value = ""
        _drillCRevision.value = ""
        _wrapBestOne.value = ""
        _wrapTodo.value = ""
    }

    fun updateDrillAMemo(text: String) { _drillAMemo.value = text }
    fun updateDrillBMetaphors(text: String) { _drillBMetaphors.value = text }
    fun updateDrillCDraft(text: String) { _drillCDraft.value = text }
    fun updateDrillCCore(text: String) { _drillCCore.value = text }
    fun updateDrillCRevision(text: String) { _drillCRevision.value = text }
    fun updateWrapBestOne(text: String) { _wrapBestOne.value = text }
    fun updateWrapTodo(text: String) { _wrapTodo.value = text }

    fun nextStep() {
        if (_currentStep.value < 4) {
            _currentStep.value++
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value--
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                val updated = session.copy(
                    drillAMemo = _drillAMemo.value,
                    drillBMetaphors = _drillBMetaphors.value,
                    drillCDraft = _drillCDraft.value,
                    drillCCore = _drillCCore.value,
                    drillCRevision = _drillCRevision.value,
                    wrapBestOne = _wrapBestOne.value,
                    wrapTodo = _wrapTodo.value
                )
                practiceSessionDao.update(updated)
                _currentSession.value = updated
            }
        }
    }

    fun completeSession() {
        viewModelScope.launch {
            _currentSession.value?.let { session ->
                val completed = session.copy(
                    drillAMemo = _drillAMemo.value,
                    drillBMetaphors = _drillBMetaphors.value,
                    drillCDraft = _drillCDraft.value,
                    drillCCore = _drillCCore.value,
                    drillCRevision = _drillCRevision.value,
                    wrapBestOne = _wrapBestOne.value,
                    wrapTodo = _wrapTodo.value,
                    isCompleted = true
                )
                practiceSessionDao.update(completed)
                _currentSession.value = null
            }
        }
    }

    fun deleteSession(session: PracticeSession) {
        viewModelScope.launch {
            practiceSessionDao.delete(session)
            if (_currentSession.value?.id == session.id) {
                _currentSession.value = null
            }
        }
    }
}