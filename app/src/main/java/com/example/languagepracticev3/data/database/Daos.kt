// app/src/main/java/com/example/languagepracticev3/data/database/Daos.kt
package com.example.languagepracticev3.data.database

import androidx.room.*
import com.example.languagepracticev3.data.model.*
import kotlinx.coroutines.flow.Flow

// ========== 設定DAO ==========
@Dao
interface KvSettingDao {
    @Query("SELECT * FROM kv_settings WHERE `key` = :key")
    suspend fun get(key: String): KvSetting?

    @Upsert
    suspend fun upsert(setting: KvSetting)

    @Query("DELETE FROM kv_settings WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("SELECT * FROM kv_settings ORDER BY `key`")
    fun observeAll(): Flow<List<KvSetting>>
}

// ========== 実行ログDAO ==========
@Dao
interface RunLogDao {
    @Insert
    suspend fun insert(log: RunLog): Long

    @Query("SELECT * FROM run_log ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RunLog>>

    @Query("SELECT * FROM run_log WHERE id = :id")
    suspend fun getById(id: Long): RunLog?

    @Query("DELETE FROM run_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM run_log WHERE createdAt < :threshold")
    suspend fun deleteOlderThan(threshold: String)
}

// ========== 作品DAO ==========
@Dao
interface WorkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: Work): Long

    @Update
    suspend fun update(work: Work)

    @Delete
    suspend fun delete(work: Work)

    @Query("DELETE FROM work WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM work WHERE id = :id")
    suspend fun getById(id: Long): Work?

    @Query("SELECT * FROM work ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Work>>

    @Query("""
        SELECT * FROM work 
        WHERE title LIKE '%' || :query || '%'
           OR bodyText LIKE '%' || :query || '%'
           OR writerName LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<Work>>

    @Query("SELECT * FROM work WHERE kind = :kind ORDER BY createdAt DESC")
    fun filterByKind(kind: String): Flow<List<Work>>

    @Query("SELECT * FROM work ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<Work>
}

// ========== 学習カードDAO ==========
@Dao
interface StudyCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: StudyCard): Long

    @Update
    suspend fun update(card: StudyCard)

    @Delete
    suspend fun delete(card: StudyCard)

    @Query("SELECT * FROM study_card WHERE id = :id")
    suspend fun getById(id: Long): StudyCard?

    @Query("SELECT * FROM study_card ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<StudyCard>>

    @Query("""
        SELECT * FROM study_card 
        WHERE focus LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
           OR fullParsedContent LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<StudyCard>>
}

// ========== カスタムルートDAO ==========
@Dao
interface CustomRouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: CustomRoute)

    @Update
    suspend fun update(route: CustomRoute)

    @Delete
    suspend fun delete(route: CustomRoute)

    @Query("SELECT * FROM custom_route ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CustomRoute>>

    @Query("SELECT * FROM custom_route WHERE id = :id")
    suspend fun getById(id: String): CustomRoute?
}

// ========== ペルソナDAO ==========
@Dao
interface PersonaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(persona: Persona): Long

    @Update
    suspend fun update(persona: Persona)

    @Delete
    suspend fun delete(persona: Persona)

    @Query("SELECT * FROM persona WHERE id = :id")
    suspend fun getById(id: Long): Persona?

    @Query("SELECT * FROM persona ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Persona>>

    @Query("""
        SELECT * FROM persona 
        WHERE name LIKE '%' || :query || '%'
           OR bio LIKE '%' || :query || '%'
           OR style LIKE '%' || :query || '%'
    """)
    fun search(query: String): Flow<List<Persona>>
}

// ========== 練習セッションDAO ==========
@Dao
interface PracticeSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PracticeSession): Long

    @Update
    suspend fun update(session: PracticeSession)

    @Delete
    suspend fun delete(session: PracticeSession)

    @Query("SELECT * FROM practice_session WHERE id = :id")
    suspend fun getById(id: Long): PracticeSession?

    @Query("SELECT * FROM practice_session ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_session WHERE isCompleted = 0 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastIncomplete(): PracticeSession?
}

// ========== トピックDAO ==========
@Dao
interface TopicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(topic: Topic): Long

    @Update
    suspend fun update(topic: Topic)

    @Delete
    suspend fun delete(topic: Topic)

    @Query("SELECT * FROM topic WHERE id = :id")
    suspend fun getById(id: Long): Topic?

    @Query("SELECT * FROM topic ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Topic>>

    @Query("""
        SELECT * FROM topic 
        WHERE title LIKE '%' || :query || '%'
           OR emotion LIKE '%' || :query || '%'
           OR scene LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
    """)
    fun search(query: String): Flow<List<Topic>>
}

// ========== 観察DAO ==========
@Dao
interface ObservationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: Observation): Long

    @Update
    suspend fun update(observation: Observation)

    @Delete
    suspend fun delete(observation: Observation)

    @Query("SELECT * FROM observation WHERE id = :id")
    suspend fun getById(id: Long): Observation?

    @Query("SELECT * FROM observation ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Observation>>

    @Query("""
        SELECT * FROM observation 
        WHERE motif LIKE '%' || :query || '%'
           OR fullContent LIKE '%' || :query || '%'
    """)
    fun search(query: String): Flow<List<Observation>>
}

// ========== 比較セットDAO ==========
@Dao
interface CompareDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(compareSet: CompareSet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CompareItem): Long

    @Delete
    suspend fun deleteSet(compareSet: CompareSet)

    @Query("SELECT * FROM compare_set ORDER BY createdAt DESC")
    fun observeAllSets(): Flow<List<CompareSet>>

    @Query("SELECT * FROM compare_item WHERE compareSetId = :setId")
    fun getItemsForSet(setId: Long): Flow<List<CompareItem>>
}

// ========== 実験DAO ==========
@Dao
interface ExperimentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(experiment: Experiment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrial(trial: ExperimentTrial): Long

    @Delete
    suspend fun deleteExperiment(experiment: Experiment)

    @Query("SELECT * FROM experiment ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Experiment>>

    @Query("SELECT * FROM experiment_trial WHERE experimentId = :experimentId")
    fun getTrialsForExperiment(experimentId: Long): Flow<List<ExperimentTrial>>
}

// ========== PoetryLab DAO ==========
@Dao
interface PoetryLabDao {
    // 既存の実装があればそのまま、なければ空のインターフェース
    // 必要に応じてメソッドを追加
}

// ========== MindsetLab DAO ==========
@Dao
interface MindsetLabDao {
    // 既存の実装があればそのまま、なければ空のインターフェース
    // 必要に応じてメソッドを追加
}
