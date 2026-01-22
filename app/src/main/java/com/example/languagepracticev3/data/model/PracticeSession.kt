package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_session")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packId: String = "",
    val createdAt: String = "",
    val drillAMemo: String = "",
    val drillBMetaphors: String = "",
    val drillCDraft: String = "",
    val drillCCore: String = "",
    val drillCRevision: String = "",
    val wrapBestOne: String = "",
    val wrapTodo: String = "",
    val elapsedSeconds: Int = 0,
    val isCompleted: Boolean = false
)