// app/src/main/java/com/example/languagepracticev3/viewmodel/MainViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class Screen {
    SETTINGS,
    WORKBENCH,
    ROUTES,
    PRACTICE,
    EXPERIMENT,
    LIBRARY,
    COMPARE,
    POETRY_LAB,
    MINDSET_LAB,
    SELF_QUESTIONING  // ★追加: 自問自答
}

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _currentScreen = MutableStateFlow(Screen.SETTINGS)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showSettings() = navigateTo(Screen.SETTINGS)
    fun showWorkbench() = navigateTo(Screen.WORKBENCH)
    fun showRoutes() = navigateTo(Screen.ROUTES)
    fun showPractice() = navigateTo(Screen.PRACTICE)
    fun showExperiment() = navigateTo(Screen.EXPERIMENT)
    fun showLibrary() = navigateTo(Screen.LIBRARY)
    fun showCompare() = navigateTo(Screen.COMPARE)
    fun showPoetryLab() = navigateTo(Screen.POETRY_LAB)
    fun showMindsetLab() = navigateTo(Screen.MINDSET_LAB)
    fun showSelfQuestioning() = navigateTo(Screen.SELF_QUESTIONING)  // ★追加
}