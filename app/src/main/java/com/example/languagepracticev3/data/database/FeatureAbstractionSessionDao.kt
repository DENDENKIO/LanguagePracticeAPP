// app/src/main/java/com/example/languagepracticev3/data/database/FeatureAbstractionSessionDao.kt
package com.example.languagepracticev3.data.database

import androidx.room.*
import com.example.languagepracticev3.data.model.FeatureAbstractionSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FeatureAbstractionSessionDao {

    @Query("SELECT * FROM feature_abstraction_sessions ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<FeatureAbstractionSession>>

    @Query("SELECT * FROM feature_abstraction_sessions WHERE id = :id")
    suspend fun getById(id: Long): FeatureAbstractionSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FeatureAbstractionSession): Long

    @Update
    suspend fun update(session: FeatureAbstractionSession)

    @Delete
    suspend fun delete(session: FeatureAbstractionSession)

    @Query("DELETE FROM feature_abstraction_sessions")
    suspend fun deleteAll()

    @Query("SELECT * FROM feature_abstraction_sessions WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLastIncomplete(): FeatureAbstractionSession?

    @Query("SELECT * FROM feature_abstraction_sessions WHERE isCompleted = 1 ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentCompleted(limit: Int): List<FeatureAbstractionSession>
}
