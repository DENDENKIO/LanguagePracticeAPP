package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persona")
data class Persona(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val location: String = "",
    val bio: String = "",
    val style: String = "",
    val tags: String = "",
    val verificationStatus: String = "",
    val createdAt: String = ""
)