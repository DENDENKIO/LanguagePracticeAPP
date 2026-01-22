package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "work",
    foreignKeys = [
        ForeignKey(
            entity = RunLog::class,
            parentColumns = ["id"],
            childColumns = ["runLogId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Work(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kind: String = "",
    val title: String = "",
    val bodyText: String = "",
    val createdAt: String = "",
    val runLogId: Long? = null,
    val writerName: String = "",
    val readerNote: String = "",
    val toneLabel: String = ""
)