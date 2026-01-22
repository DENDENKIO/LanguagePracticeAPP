// app/src/main/java/com/example/languagepracticev3/viewmodel/SettingsViewModel.kt
package com.example.languagepracticev3.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.KvSettingDao
import com.example.languagepracticev3.data.model.KvSetting
import com.example.languagepracticev3.data.model.AiSiteCatalog
import com.example.languagepracticev3.data.model.AiSiteProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val kvSettingDao: KvSettingDao,
    @ApplicationContext private val context: Context
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

    // データ管理状態
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // AIサイト一覧（参考ソフトと同じ）
    val aiSitePresets: List<AiSiteProfile> = AiSiteCatalog.Presets

    // 現在選択中のAIサイトプロファイル
    val selectedAiSiteProfile: AiSiteProfile
        get() {
            val preset = AiSiteCatalog.getByIdOrDefault(_selectedAiSiteId.value)
            return if (_customAiSiteUrl.value.isNotBlank()) {
                preset.copy(url = _customAiSiteUrl.value)
            } else {
                preset
            }
        }

    // 自動モードのヒント（参考ソフトと同じ）
    val autoModeHint: String
        get() {
            val profile = AiSiteCatalog.getByIdOrDefault(_selectedAiSiteId.value)
            return if (profile.supportsAuto) {
                "※このプリセットは自動操作を試行します（UI変更で失敗する場合あり）。"
            } else {
                "※このプリセットは自動操作が不安定/非推奨です。AUTO_MODEはOFF推奨。"
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

            // URLはユーザーが編集している可能性もあるので ai_site_url を読む（無ければプリセット）
            val savedUrl = kvSettingDao.get("ai_site_url")?.value
            val preset = AiSiteCatalog.getByIdOrDefault(_selectedAiSiteId.value)
            _customAiSiteUrl.value = savedUrl ?: preset.url

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

    // ========== データ管理機能 ==========

    /**
     * データベースのバックアップを作成
     */
    fun backupDatabase(destinationUri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val dbFile = context.getDatabasePath("language_practice_db")
                    if (!dbFile.exists()) {
                        throw Exception("データベースファイルが見つかりません")
                    }

                    context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                        FileInputStream(dbFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
                _statusMessage.value = "バックアップを作成しました"
            } catch (e: Exception) {
                _statusMessage.value = "バックアップ失敗: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * データベースを復元
     */
    fun restoreDatabase(sourceUri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                withContext(Dispatchers.IO) {
                    val dbFile = context.getDatabasePath("language_practice_db")

                    // 一時ファイルにコピー
                    val tempFile = File(context.cacheDir, "restore_temp.db")
                    context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    // DBファイルを置き換え
                    if (tempFile.exists()) {
                        dbFile.delete()
                        tempFile.copyTo(dbFile, overwrite = true)
                        tempFile.delete()
                    }
                }
                _statusMessage.value = "データベースを復元しました。アプリを再起動してください。"
            } catch (e: Exception) {
                _statusMessage.value = "復元失敗: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * データベースファイルのパスを取得
     */
    fun getDatabasePath(): String {
        return context.getDatabasePath("language_practice_db").absolutePath
    }

    /**
     * データベースサイズを取得
     */
    fun getDatabaseSize(): String {
        val dbFile = context.getDatabasePath("language_practice_db")
        return if (dbFile.exists()) {
            val sizeKb = dbFile.length() / 1024
            if (sizeKb > 1024) {
                "${sizeKb / 1024} MB"
            } else {
                "$sizeKb KB"
            }
        } else {
            "不明"
        }
    }
}
