package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observation")
data class Observation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUrl: String = "",
    val motif: String = "",
    val visualRaw: String = "",
    val soundRaw: String = "",
    val metaphorsRaw: String = "",
    val coreCandidatesRaw: String = "",
    val fullContent: String = "",
    val createdAt: String = ""
)