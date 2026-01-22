package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.KvSettingDao
import com.example.languagepracticev3.data.model.KvSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val kvSettingDao: KvSettingDao
) : ViewModel() {

    private val _writerName = MutableStateFlow("")
    val writerName: StateFlow<String> = _writerName.asStateFlow()

    private val _readerNote = MutableStateFlow("")
    val readerNote: StateFlow<String> = _readerNote.asStateFlow()

    private val _aiSiteUrl = MutableStateFlow("")
    val aiSiteUrl: StateFlow<String> = _aiSiteUrl.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _writerName.value = kvSettingDao.get("writer_name")?.value ?: ""
            _readerNote.value = kvSettingDao.get("reader_note")?.value ?: ""
            _aiSiteUrl.value = kvSettingDao.get("ai_site_url")?.value ?: ""
        }
    }

    fun updateWriterName(name: String) {
        _writerName.value = name
    }

    fun updateReaderNote(note: String) {
        _readerNote.value = note
    }

    fun updateAiSiteUrl(url: String) {
        _aiSiteUrl.value = url
    }

    fun saveSettings() {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            kvSettingDao.insertOrUpdate(KvSetting("writer_name", _writerName.value, now))
            kvSettingDao.insertOrUpdate(KvSetting("reader_note", _readerNote.value, now))
            kvSettingDao.insertOrUpdate(KvSetting("ai_site_url", _aiSiteUrl.value, now))

            _statusMessage.value = "設定を保存しました"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = ""
    }
}