package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_route")
data class CustomRoute(
    @PrimaryKey
    val id: String,
    val title: String = "",
    val description: String = "",
    val stepsJson: String = "",
    val updatedAt: String = ""
)