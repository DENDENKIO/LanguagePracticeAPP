// app/src/main/java/com/example/languagepracticev3/data/database/Daos.kt
package com.example.languagepracticev3.data.database

import androidx.room.*
import com.example.languagepracticev3.data.model.*
import kotlinx.coroutines.flow.Flow
import com.example.languagepracticev3.data.model.AbstractionSession

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: KvSetting)
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

    // ★追加: suspend版 getAll
    @Query("SELECT * FROM work ORDER BY id DESC LIMIT 100")
    suspend fun getAll(): List<Work>

    @Query("""
        SELECT * FROM work 
        WHERE title LIKE '%' || :query || '%'
           OR bodyText LIKE '%' || :query || '%'
           OR writerName LIKE '%' || :query || '%'
           OR toneLabel LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<Work>>

    // ★追加: suspend版 search
    @Query("""
        SELECT * FROM work 
        WHERE title LIKE :query 
           OR bodyText LIKE :query 
           OR writerName LIKE :query 
           OR toneLabel LIKE :query
           OR readerNote LIKE :query
        ORDER BY id DESC LIMIT 100
    """)
    suspend fun searchSuspend(query: String): List<Work>

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

    // ★追加: suspend版 getAll
    @Query("SELECT * FROM study_card ORDER BY id DESC LIMIT 100")
    suspend fun getAll(): List<StudyCard>

    @Query("""
        SELECT * FROM study_card 
        WHERE focus LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
           OR fullParsedContent LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<StudyCard>>

    // ★追加: suspend版 search
    @Query("""
        SELECT * FROM study_card 
        WHERE focus LIKE :query 
           OR tags LIKE :query 
           OR bestExpressionsRaw LIKE :query
           OR metaphorChainsRaw LIKE :query
           OR doNextRaw LIKE :query
           OR fullParsedContent LIKE :query
        ORDER BY id DESC LIMIT 100
    """)
    suspend fun searchSuspend(query: String): List<StudyCard>
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

    @Query("SELECT * FROM custom_route ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CustomRoute>

    @Query("SELECT * FROM custom_route WHERE id = :id")
    suspend fun getById(id: String): CustomRoute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(route: CustomRoute)
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

    // ★追加: suspend版 getAll
    @Query("SELECT * FROM persona ORDER BY id DESC LIMIT 100")
    suspend fun getAll(): List<Persona>

    @Query("""
        SELECT * FROM persona 
        WHERE name LIKE '%' || :query || '%'
           OR bio LIKE '%' || :query || '%'
           OR style LIKE '%' || :query || '%'
    """)
    fun search(query: String): Flow<List<Persona>>

    // ★追加: suspend版 search
    @Query("""
        SELECT * FROM persona 
        WHERE name LIKE :query 
           OR location LIKE :query 
           OR bio LIKE :query 
           OR style LIKE :query 
           OR tags LIKE :query
           OR verificationStatus LIKE :query
        ORDER BY id DESC LIMIT 100
    """)
    suspend fun searchSuspend(query: String): List<Persona>

    // ★追加: ステータス更新
    @Query("UPDATE persona SET verificationStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
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

    @Query("SELECT * FROM practice_session ORDER BY createdAt DESC")
    suspend fun getAll(): List<PracticeSession>

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

    // ★追加: suspend版 getAll
    @Query("SELECT * FROM topic ORDER BY id DESC LIMIT 100")
    suspend fun getAll(): List<Topic>

    @Query("""
        SELECT * FROM topic 
        WHERE title LIKE '%' || :query || '%'
           OR emotion LIKE '%' || :query || '%'
           OR scene LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
    """)
    fun search(query: String): Flow<List<Topic>>

    // ★追加: suspend版 search
    @Query("""
        SELECT * FROM topic 
        WHERE title LIKE :query 
           OR emotion LIKE :query 
           OR scene LIKE :query 
           OR tags LIKE :query 
           OR fixConditions LIKE :query
        ORDER BY id DESC LIMIT 100
    """)
    suspend fun searchSuspend(query: String): List<Topic>

    // ★追加: 最近のトピック取得
    @Query("SELECT * FROM topic ORDER BY id DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<Topic>
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

    // ★追加: suspend版 getAll
    @Query("SELECT * FROM observation ORDER BY id DESC LIMIT 100")
    suspend fun getAll(): List<Observation>

    @Query("""
        SELECT * FROM observation 
        WHERE motif LIKE '%' || :query || '%'
           OR fullContent LIKE '%' || :query || '%'
    """)
    fun search(query: String): Flow<List<Observation>>

    // ★追加: suspend版 search
    @Query("""
        SELECT * FROM observation 
        WHERE motif LIKE :query 
           OR visualRaw LIKE :query 
           OR soundRaw LIKE :query 
           OR metaphorsRaw LIKE :query 
           OR coreCandidatesRaw LIKE :query
           OR fullContent LIKE :query
        ORDER BY id DESC LIMIT 100
    """)
    suspend fun searchSuspend(query: String): List<Observation>
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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: PlProject): Long

    @Update
    suspend fun updateProject(project: PlProject)

    @Delete
    suspend fun deleteProject(project: PlProject)

    @Query("SELECT * FROM pl_project ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<PlProject>>

    @Query("SELECT * FROM pl_project WHERE id = :id")
    suspend fun getProjectById(id: Long): PlProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: PlRun): Long

    @Query("SELECT * FROM pl_run WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getRunsByProject(projectId: Long): Flow<List<PlRun>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: PlTextAsset): Long

    @Query("SELECT * FROM pl_text_asset WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getAssetsByProject(projectId: Long): Flow<List<PlTextAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: PlIssue): Long

    @Update
    suspend fun updateIssue(issue: PlIssue)

    @Query("SELECT * FROM pl_issue WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getIssuesByProject(projectId: Long): Flow<List<PlIssue>>
}

// ========== MindsetLab DAO ==========
@Dao
interface MindsetLabDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: MsDay): Long

    @Update
    suspend fun updateDay(day: MsDay)

    @Delete
    suspend fun deleteDay(day: MsDay)

    @Query("SELECT * FROM ms_day ORDER BY createdAt DESC")
    fun getAllDays(): Flow<List<MsDay>>

    @Query("SELECT * FROM ms_day WHERE id = :id")
    suspend fun getDayById(id: Long): MsDay?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MsEntry): Long

    @Update
    suspend fun updateEntry(entry: MsEntry)

    @Delete
    suspend fun deleteEntry(entry: MsEntry)

    @Query("SELECT * FROM ms_entry WHERE dayId = :dayId ORDER BY createdAt DESC")
    fun getEntriesByDay(dayId: Long): Flow<List<MsEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: MsReview): Long

    @Query("SELECT * FROM ms_review WHERE dayId = :dayId")
    fun getReviewsByDay(dayId: Long): Flow<List<MsReview>>
}

// =====================================
// GlobalRevisionSessionDao
// =====================================
@Dao
interface GlobalRevisionSessionDao {
    @Query("SELECT * FROM global_revision_sessions ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<GlobalRevisionSession>>

    @Query("SELECT * FROM global_revision_sessions WHERE id = :id")
    suspend fun getById(id: Long): GlobalRevisionSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: GlobalRevisionSession): Long

    @Update
    suspend fun update(session: GlobalRevisionSession)

    @Delete
    suspend fun delete(session: GlobalRevisionSession)

    @Query("DELETE FROM global_revision_sessions")
    suspend fun deleteAll()
}

// ========== 6つの思考習慣 DAO ==========
// 以下を Daos.kt の末尾に追加

@Dao
interface SixHabitsSessionDao {
    @Query("SELECT * FROM six_habits_sessions ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SixHabitsSession>>

    @Query("SELECT * FROM six_habits_sessions WHERE mindsetType = :mindsetType ORDER BY updatedAt DESC")
    fun observeByMindsetType(mindsetType: Int): Flow<List<SixHabitsSession>>

    @Query("SELECT * FROM six_habits_sessions WHERE id = :id")
    suspend fun getById(id: Long): SixHabitsSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SixHabitsSession): Long

    @Update
    suspend fun update(session: SixHabitsSession)

    @Delete
    suspend fun delete(session: SixHabitsSession)

    @Query("DELETE FROM six_habits_sessions")
    suspend fun deleteAll()

    @Query("SELECT * FROM six_habits_sessions WHERE sessionDate = :date")
    suspend fun getByDate(date: String): List<SixHabitsSession>
}

@Dao
interface SixHabitsDailyTrackingDao {
    @Query("SELECT * FROM six_habits_daily_tracking ORDER BY date DESC")
    fun observeAll(): Flow<List<SixHabitsDailyTracking>>

    @Query("SELECT * FROM six_habits_daily_tracking WHERE date = :date")
    suspend fun getByDate(date: String): SixHabitsDailyTracking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracking: SixHabitsDailyTracking): Long

    @Update
    suspend fun update(tracking: SixHabitsDailyTracking)

    @Delete
    suspend fun delete(tracking: SixHabitsDailyTracking)

    @Query("SELECT * FROM six_habits_daily_tracking ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<SixHabitsDailyTracking>
}

@Dao
interface SixHabitsMaterialDao {
    @Query("SELECT * FROM six_habits_materials ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SixHabitsMaterial>>

    @Query("SELECT * FROM six_habits_materials WHERE materialType = :type ORDER BY createdAt DESC")
    fun observeByType(type: String): Flow<List<SixHabitsMaterial>>

    @Query("SELECT * FROM six_habits_materials WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<SixHabitsMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(material: SixHabitsMaterial): Long

    @Update
    suspend fun update(material: SixHabitsMaterial)

    @Delete
    suspend fun delete(material: SixHabitsMaterial)

    @Query("SELECT * FROM six_habits_materials WHERE id = :id")
    suspend fun getById(id: Long): SixHabitsMaterial?

    @Query("SELECT COUNT(*) FROM six_habits_materials WHERE materialType = :type")
    suspend fun countByType(type: String): Int
}

@Dao
interface AbstractionSessionDao {
    @Query("SELECT * FROM abstraction_sessions ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<AbstractionSession>>

    @Query("SELECT * FROM abstraction_sessions WHERE id = :id")
    suspend fun getById(id: Long): AbstractionSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AbstractionSession): Long

    @Update
    suspend fun update(session: AbstractionSession)

    @Delete
    suspend fun delete(session: AbstractionSession)

    @Query("DELETE FROM abstraction_sessions")
    suspend fun deleteAll()

    @Query("SELECT * FROM abstraction_sessions WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLastIncomplete(): AbstractionSession?

    @Query("SELECT * FROM abstraction_sessions WHERE isCompleted = 1 ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentCompleted(limit: Int): List<AbstractionSession>
}

