package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_log")
data class RunLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationKind: String = "",
    val status: String = "",
    val createdAt: String = "",
    val promptText: String? = null,
    val rawOutput: String? = null,
    val errorCode: String? = null
)