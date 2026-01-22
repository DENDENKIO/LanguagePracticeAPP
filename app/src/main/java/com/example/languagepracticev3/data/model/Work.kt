// app/src/main/java/com/example/languagepracticev3/data/model/Work.kt
package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work",
    foreignKeys = [ForeignKey(
        entity = RunLog::class,
        parentColumns = ["id"],
        childColumns = ["runLogId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(value = ["runLogId"])]  // ← インデックス追加
)
data class Work(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String = "",
    val title: String? = null,
    val bodyText: String? = null,
    val createdAt: String? = null,
    val runLogId: Long? = null,
    val writerName: String? = null,
    val readerNote: String? = null,
    val toneLabel: String? = null
)
