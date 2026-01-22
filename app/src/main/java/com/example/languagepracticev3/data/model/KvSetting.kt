package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kv_settings")
data class KvSetting(
    @PrimaryKey
    val key: String,
    val value: String = "",
    val updatedAt: String = ""
)