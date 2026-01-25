// app/src/main/java/com/example/languagepracticev3/data/database/MaterialAbstractionSessionDao.kt
package com.example.languagepracticev3.data.database

import androidx.room.*
import com.example.languagepracticev3.data.model.MaterialAbstractionSession
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialAbstractionSessionDao {

    @Query("SELECT * FROM material_abstraction_sessions ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<MaterialAbstractionSession>>

    @Query("SELECT * FROM material_abstraction_sessions WHERE id = :id")
    suspend fun getById(id: Long): MaterialAbstractionSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: MaterialAbstractionSession): Long

    @Update
    suspend fun update(session: MaterialAbstractionSession)

    @Delete
    suspend fun delete(session: MaterialAbstractionSession)

    @Query("SELECT * FROM material_abstraction_sessions WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLastIncomplete(): MaterialAbstractionSession?

    @Query("SELECT * FROM material_abstraction_sessions WHERE isCompleted = 1 ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentCompleted(limit: Int): List<MaterialAbstractionSession>
}
