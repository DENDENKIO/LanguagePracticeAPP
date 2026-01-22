package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "experiment")
data class Experiment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val createdAt: String = "",
    val variableName: String = "",
    val commonTopic: String = "",
    val commonWriter: String = ""
)

@Entity(
    tableName = "experiment_trial",
    foreignKeys = [
        ForeignKey(
            entity = Experiment::class,
            parentColumns = ["id"],
            childColumns = ["experimentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExperimentTrial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val experimentId: Long = 0,
    val variableValue: String = "",
    val resultWorkId: Long? = null,
    val rating: String = ""
)