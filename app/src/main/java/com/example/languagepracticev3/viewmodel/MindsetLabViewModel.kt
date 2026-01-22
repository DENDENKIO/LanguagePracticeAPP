package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.MindsetLabDao
import com.example.languagepracticev3.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
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
        day?.let {
            _selectedMindsets.value = it.getFocusMindsetList()
            _scene.value = it.scene
            _currentScreen.value = MindsetLabScreen.SESSION
        }
    }

    fun toggleMindset(mindsetId: Int) {
        val current = _selectedMindsets.value.toMutableList()
        if (current.contains(mindsetId)) {
            current.remove(mindsetId)
        } else {
            if (current.size < 3) {
                current.add(mindsetId)
            }
        }
        _selectedMindsets.value = current
    }

    fun updateScene(newScene: String) {
        _scene.value = newScene
    }

    fun createOrUpdateToday() {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val dateKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val existingDay = mindsetLabDao.getDayByDateKey(dateKey)

            if (existingDay != null) {
                val updated = existingDay.copy(
                    focusMindsets = _selectedMindsets.value.joinToString(","),
                    scene = _scene.value,
                    updatedAt = now
                )
                mindsetLabDao.updateDay(updated)
                _selectedDay.value = updated
            } else {
                val newDay = MsDay(
                    dateKey = dateKey,
                    focusMindsets = _selectedMindsets.value.joinToString(","),
                    scene = _scene.value,
                    createdAt = now,
                    updatedAt = now
                )
                val id = mindsetLabDao.insertDay(newDay)
                _selectedDay.value = newDay.copy(id = id)
            }

            _currentScreen.value = MindsetLabScreen.SESSION
        }
    }

    fun saveEntry(entryType: String, bodyText: String) {
        viewModelScope.launch {
            _selectedDay.value?.let { day ->
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
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

    fun updateEntry(entry: MsEntry, newBodyText: String) {
        viewModelScope.launch {
            mindsetLabDao.updateEntry(entry.copy(bodyText = newBodyText))
        }
    }

    fun deleteEntry(entry: MsEntry) {
        viewModelScope.launch {
            mindsetLabDao.deleteEntry(entry)
        }
    }

    fun getDrillsForSelectedMindsets(): List<DrillDef> {
        return _selectedMindsets.value.flatMap { mindsetId ->
            MindsetDefinitions.getDrillsByMindset(mindsetId)
        }
    }

    fun goBack() {
        when (_currentScreen.value) {
            MindsetLabScreen.SESSION -> {
                _selectedDay.value = null
                _currentScreen.value = MindsetLabScreen.HOME
            }
            MindsetLabScreen.REVIEW -> _currentScreen.value = MindsetLabScreen.SESSION
            MindsetLabScreen.HISTORY -> _currentScreen.value = MindsetLabScreen.HOME
            else -> {}
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
}

enum class MindsetLabScreen {
    HOME,
    SESSION,
    REVIEW,
    HISTORY
}