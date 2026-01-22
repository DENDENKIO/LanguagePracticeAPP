package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "compare_set")
data class CompareSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val note: String = "",
    val winnerWorkId: Long? = null,
    val createdAt: String = ""
)

@Entity(
    tableName = "compare_item",
    foreignKeys = [
        ForeignKey(
            entity = CompareSet::class,
            parentColumns = ["id"],
            childColumns = ["compareSetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CompareItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val compareSetId: Long = 0,
    val workId: Long? = null,
    val position: String = ""
)