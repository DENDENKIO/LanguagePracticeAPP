package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagepracticev3.data.database.PoetryLabDao
import com.example.languagepracticev3.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class PoetryLabViewModel @Inject constructor(
    private val poetryLabDao: PoetryLabDao
) : ViewModel() {

    val projects: StateFlow<List<PlProject>> = poetryLabDao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProject = MutableStateFlow<PlProject?>(null)
    val selectedProject: StateFlow<PlProject?> = _selectedProject.asStateFlow()

    val currentProjectRuns: StateFlow<List<PlRun>> = _selectedProject
        .flatMapLatest { project ->
            project?.let { poetryLabDao.getRunsByProject(it.id) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProjectAssets: StateFlow<List<PlTextAsset>> = _selectedProject
        .flatMapLatest { project ->
            project?.let { poetryLabDao.getAssetsByProject(it.id) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProjectIssues: StateFlow<List<PlIssue>> = _selectedProject
        .flatMapLatest { project ->
            project?.let { poetryLabDao.getIssuesByProject(it.id) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentScreen = MutableStateFlow(PoetryLabScreen.HOME)
    val currentScreen: StateFlow<PoetryLabScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: PoetryLabScreen) {
        _currentScreen.value = screen
    }

    fun selectProject(project: PlProject?) {
        _selectedProject.value = project
        if (project != null) {
            _currentScreen.value = PoetryLabScreen.PROJECT
        }
    }

    fun createProject(title: String, styleType: String = "KOU") {
        viewModelScope.launch {
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val project = PlProject(
                title = title,
                styleType = styleType,
                createdAt = now,
                updatedAt = now
            )
            val id = poetryLabDao.insertProject(project)
            selectProject(project.copy(id = id))
        }
    }

    fun deleteProject(project: PlProject) {
        viewModelScope.launch {
            poetryLabDao.deleteProject(project)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = null
                _currentScreen.value = PoetryLabScreen.HOME
            }
        }
    }

    fun createAsset(assetType: String, bodyText: String, runId: Long? = null) {
        viewModelScope.launch {
            _selectedProject.value?.let { project ->
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val asset = PlTextAsset(
                    projectId = project.id,
                    runId = runId,
                    assetType = assetType,
                    bodyText = bodyText,
                    createdAt = now
                )
                poetryLabDao.insertAsset(asset)
            }
        }
    }

    fun createIssue(level: String, symptom: String, severity: String = "B") {
        viewModelScope.launch {
            _selectedProject.value?.let { project ->
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val issue = PlIssue(
                    projectId = project.id,
                    level = level,
                    symptom = symptom,
                    severity = severity,
                    createdAt = now
                )
                poetryLabDao.insertIssue(issue)
            }
        }
    }

    fun updateIssueStatus(issue: PlIssue, status: String) {
        viewModelScope.launch {
            poetryLabDao.updateIssue(issue.copy(status = status))
        }
    }

    fun goBack() {
        when (_currentScreen.value) {
            PoetryLabScreen.PROJECT -> {
                _selectedProject.value = null
                _currentScreen.value = PoetryLabScreen.HOME
            }
            PoetryLabScreen.RUN -> _currentScreen.value = PoetryLabScreen.PROJECT
            PoetryLabScreen.COMPARE -> _currentScreen.value = PoetryLabScreen.PROJECT
            else -> {}
        }
    }
}

enum class PoetryLabScreen {
    HOME,
    PROJECT,
    RUN,
    COMPARE
}