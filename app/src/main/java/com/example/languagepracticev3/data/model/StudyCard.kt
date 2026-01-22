package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_card",
    foreignKeys = [
        ForeignKey(
            entity = Work::class,
            parentColumns = ["id"],
            childColumns = ["sourceWorkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudyCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceWorkId: Long? = null,
    val createdAt: String = "",
    val focus: String = "",
    val level: String = "",
    val bestExpressionsRaw: String = "",
    val metaphorChainsRaw: String = "",
    val doNextRaw: String = "",
    val tags: String = "",
    val fullParsedContent: String = ""
)