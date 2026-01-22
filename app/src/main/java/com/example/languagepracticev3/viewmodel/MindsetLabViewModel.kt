// app/src/main/java/com/example/languagepracticev3/viewmodel/MindsetLabViewModel.kt
package com.example.languagepracticev3.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MindsetLabViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {
    // 既存の実装を維持
}
