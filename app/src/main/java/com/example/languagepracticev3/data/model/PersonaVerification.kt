package com.example.languagepracticev3.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "persona_verification",
    foreignKeys = [
        ForeignKey(
            entity = Persona::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PersonaVerification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personaId: Long? = null,
    val createdAt: String = "",
    val evidence1: String = "",
    val evidence2: String = "",
    val evidence3: String = "",
    val resultJson: String = "",
    val overallVerdict: String = "",
    val revisedBioDraft: String = ""
)