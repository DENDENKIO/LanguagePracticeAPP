package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "pl_project")
data class PlProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val styleType: String = "KOU",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Entity(
    tableName = "pl_run",
    foreignKeys = [
        ForeignKey(
            entity = PlProject::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlRun(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 0,
    val routeName: String = "標準Run",
    val status: String = "RUNNING",
    val createdAt: String = "",
    val finishedAt: String? = null
)

@Entity(
    tableName = "pl_ai_step_log",
    foreignKeys = [
        ForeignKey(
            entity = PlRun::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlAiStepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val runId: Long = 0,
    val stepIndex: Int = 0,
    val stepName: String = "",
    val inputKeys: String? = null,
    val promptText: String? = null,
    val rawOutput: String? = null,
    val parsedJson: String? = null,
    val status: String = "PENDING",
    val createdAt: String = "",
    val finishedAt: String? = null
)

@Entity(
    tableName = "pl_text_asset",
    foreignKeys = [
        ForeignKey(
            entity = PlProject::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlTextAsset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 0,
    val runId: Long? = null,
    val stepLogId: Long? = null,
    val assetType: String = "",
    val inputKeysUsed: String? = null,
    val bodyText: String = "",
    val createdAt: String = ""
)

@Entity(
    tableName = "pl_issue",
    foreignKeys = [
        ForeignKey(
            entity = PlProject::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlIssue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 0,
    val runId: Long? = null,
    val stepLogId: Long? = null,
    val targetType: String = "ALL",
    val targetIndex: Int? = null,
    val level: String = "",
    val symptom: String = "",
    val severity: String = "B",
    val evidence: String? = null,
    val diagnosis: String? = null,
    val fixType: String? = null,
    val planNotes: String? = null,
    val status: String = "OPEN",
    val createdAt: String = ""
)

@Entity(
    tableName = "pl_compare",
    foreignKeys = [
        ForeignKey(
            entity = PlProject::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlCompare(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 0,
    val runId: Long = 0,
    val candidateAssetIds: String = "",
    val winnerAssetId: Long? = null,
    val reasonNote: String? = null,
    val createdAt: String = ""
)

@Entity(tableName = "pl_export_log")
data class PlExportLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 0,
    val runId: Long? = null,
    val filePath: String = "",
    val createdAt: String = ""
)