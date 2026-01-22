// app/src/main/java/com/example/languagepracticev3/data/model/StudyCard.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_card",
    foreignKeys = [ForeignKey(
        entity = Work::class,
        parentColumns = ["id"],
        childColumns = ["sourceWorkId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["sourceWorkId"])]  // ← インデックス追加
)
data class StudyCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceWorkId: Long? = null,
    val createdAt: String? = null,
    val focus: String? = null,
    val level: String? = null,
    val bestExpressionsRaw: String? = null,
    val metaphorChainsRaw: String? = null,
    val doNextRaw: String? = null,
    val tags: String? = null,
    val fullParsedContent: String? = null
)
