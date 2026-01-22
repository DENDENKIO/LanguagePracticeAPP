package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topic")
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val emotion: String = "",
    val scene: String = "",
    val tags: String = "",
    val fixConditions: String = "",
    val createdAt: String = ""
)