// app/src/main/java/com/example/languagepracticev3/viewmodel/SettingsViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.KvSettingDao
import com.example.languagepracticev3.data.model.KvSetting
import com.example.languagepracticev3.data.model.AiSiteCatalog
import com.example.languagepracticev3.data.model.AiSiteProfile
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

    // AI設定
    private val _selectedAiSiteId = MutableStateFlow("GENSPARK")
    val selectedAiSiteId: StateFlow<String> = _selectedAiSiteId.asStateFlow()

    private val _customAiSiteUrl = MutableStateFlow("")
    val customAiSiteUrl: StateFlow<String> = _customAiSiteUrl.asStateFlow()

    private val _isAutoMode = MutableStateFlow(false)
    val isAutoMode: StateFlow<Boolean> = _isAutoMode.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // AIサイト一覧
    val aiSitePresets = AiSiteCatalog.presets

    // 現在選択中のAIサイトプロファイル
    val selectedAiSiteProfile: AiSiteProfile
        get() {
            val preset = AiSiteCatalog.getByIdOrDefault(_selectedAiSiteId.value)
            // カスタムURLが設定されていればそれを使用
            return if (_customAiSiteUrl.value.isNotBlank()) {
                preset.copy(url = _customAiSiteUrl.value)
            } else {
                preset
            }
        }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _writerName.value = kvSettingDao.get("writer_name")?.value ?: ""
            _readerNote.value = kvSettingDao.get("reader_note")?.value ?: ""
            _selectedAiSiteId.value = kvSettingDao.get("ai_site_id")?.value ?: "GENSPARK"
            _customAiSiteUrl.value = kvSettingDao.get("ai_site_url")?.value ?: ""
            _isAutoMode.value = kvSettingDao.get("auto_mode")?.value?.toBooleanStrictOrNull() ?: false
        }
    }

    fun updateWriterName(name: String) {
        _writerName.value = name
    }

    fun updateReaderNote(note: String) {
        _readerNote.value = note
    }

    fun updateSelectedAiSiteId(siteId: String) {
        _selectedAiSiteId.value = siteId
        // プリセット選択時、カスタムURLをプリセットのURLにリセット
        val preset = AiSiteCatalog.getByIdOrDefault(siteId)
        _customAiSiteUrl.value = preset.url
    }

    fun updateCustomAiSiteUrl(url: String) {
        _customAiSiteUrl.value = url
    }

    fun updateAutoMode(enabled: Boolean) {
        _isAutoMode.value = enabled
    }

    fun saveSettings() {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            kvSettingDao.insertOrUpdate(KvSetting("writer_name", _writerName.value, now))
            kvSettingDao.insertOrUpdate(KvSetting("reader_note", _readerNote.value, now))
            kvSettingDao.insertOrUpdate(KvSetting("ai_site_id", _selectedAiSiteId.value, now))
            kvSettingDao.insertOrUpdate(KvSetting("ai_site_url", _customAiSiteUrl.value, now))
            kvSettingDao.insertOrUpdate(KvSetting("auto_mode", _isAutoMode.value.toString(), now))

            _statusMessage.value = "設定を保存しました"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = ""
    }
}
