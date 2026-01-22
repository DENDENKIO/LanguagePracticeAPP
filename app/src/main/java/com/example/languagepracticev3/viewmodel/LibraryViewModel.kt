package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.MindsetLabDao
import com.example.languagepracticev3.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MindsetLabViewModel @Inject constructor(
    private val mindsetLabDao: MindsetLabDao
) : ViewModel() {

    val days: StateFlow<List<MsDay>> = mindsetLabDao.getAllDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDay = MutableStateFlow<MsDay?>(null)
    val selectedDay: StateFlow<MsDay?> = _selectedDay.asStateFlow()

    val currentDayEntries: StateFlow<List<MsEntry>> = _selectedDay
        .flatMapLatest { day ->
            day?.let { mindsetLabDao.getEntriesByDay(it.id) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(MindsetLabScreen.HOME)
    val currentScreen: StateFlow<MindsetLabScreen> = _currentScreen.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Set<Int> から List<Int> に変更
    private val _selectedMindsets = MutableStateFlow<List<Int>>(emptyList())
    val selectedMindsets: StateFlow<List<Int>> = _selectedMindsets.asStateFlow()

    private val _scene = MutableStateFlow("")
    val scene: StateFlow<String> = _scene.asStateFlow()

    val availableMindsets: List<MindsetInfo> = MindsetDefinitions.all.values.toList()

    fun navigateTo(screen: MindsetLabScreen) {
        _currentScreen.value = screen
    }

    fun selectDay(day: MsDay?) {
        _selectedDay.value = day
        if (day != null) {
            _currentScreen.value = MindsetLabScreen.SESSION
        }
    }

    fun createNewDay() {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val day = MsDay(
                dateKey = today,
                focusMindsets = _selectedMindsets.value.joinToString(","),
                scene = _scene.value,
                createdAt = today
            )
            val id = mindsetLabDao.insertDay(day)
            selectDay(day.copy(id = id))
        }
    }

    fun addEntry(entryType: String, bodyText: String) {
        viewModelScope.launch {
            _selectedDay.value?.let { day ->
                val now = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val entry = MsEntry(
                    dayId = day.id,
                    entryType = entryType,
                    bodyText = bodyText,
                    createdAt = now
                )
                mindsetLabDao.insertEntry(entry)
            }
        }
    }

    fun deleteDay(day: MsDay) {
        viewModelScope.launch {
            mindsetLabDao.deleteDay(day)
            if (_selectedDay.value?.id == day.id) {
                _selectedDay.value = null
                _currentScreen.value = MindsetLabScreen.HOME
            }
        }
    }

    fun toggleMindset(id: Int) {
        val current = _selectedMindsets.value
        _selectedMindsets.value = if (current.contains(id)) {
            current - id
        } else {
            if (current.size < 3) {
                current + id
            } else {
                current // 最大3つまで
            }
        }
    }

    fun updateScene(newScene: String) {
        _scene.value = newScene
    }

    fun createOrUpdateToday() {
        createNewDay()
    }

    fun saveEntry(type: String, body: String) = addEntry(type, body)

    // 引数を2つ受け取るように変更
    fun updateEntry(entry: MsEntry, newText: String) {
        viewModelScope.launch {
            val updated = entry.copy(bodyText = newText)
            mindsetLabDao.updateEntry(updated)
        }
    }

    fun deleteEntry(entry: MsEntry) {
        viewModelScope.launch {
            mindsetLabDao.deleteEntry(entry)
        }
    }

    fun getDrillsForSelectedMindsets(): List<DrillDef> {
        return _selectedMindsets.value.flatMap { MindsetDefinitions.getDrillsByMindset(it) }
    }

    fun goBack() {
        when (_currentScreen.value) {
            MindsetLabScreen.SESSION -> {
                _selectedDay.value = null
                _currentScreen.value = MindsetLabScreen.HOME
            }
            MindsetLabScreen.HISTORY -> _currentScreen.value = MindsetLabScreen.HOME
            MindsetLabScreen.REVIEW -> _currentScreen.value = MindsetLabScreen.HOME
            else -> {}
        }
    }
}

enum class MindsetLabScreen {
    HOME,
    SESSION,
    HISTORY,
    REVIEW
}
