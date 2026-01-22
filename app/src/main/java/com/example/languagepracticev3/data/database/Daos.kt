package com.example.languagepracticev3.data.database

import androidx.room.*
import com.example.languagepracticev3.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KvSettingDao {
    @Query("SELECT * FROM kv_settings WHERE `key` = :key")
    suspend fun get(key: String): KvSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: KvSetting)

    @Query("DELETE FROM kv_settings WHERE `key` = :key")
    suspend fun delete(key: String)
}

@Dao
interface RunLogDao {
    @Query("SELECT * FROM run_log ORDER BY id DESC")
    fun getAll(): Flow<List<RunLog>>

    @Insert
    suspend fun insert(runLog: RunLog): Long

    @Update
    suspend fun update(runLog: RunLog)

    @Delete
    suspend fun delete(runLog: RunLog)
}

@Dao
interface WorkDao {
    @Query("SELECT * FROM work ORDER BY id DESC")
    fun getAll(): Flow<List<Work>>

    @Query("SELECT * FROM work WHERE id = :id")
    suspend fun getById(id: Long): Work?

    @Query("SELECT * FROM work WHERE kind = :kind ORDER BY id DESC")
    fun getByKind(kind: String): Flow<List<Work>>

    @Insert
    suspend fun insert(work: Work): Long

    @Update
    suspend fun update(work: Work)

    @Delete
    suspend fun delete(work: Work)

    @Query("SELECT * FROM work WHERE title LIKE '%' || :query || '%' OR bodyText LIKE '%' || :query || '%' ORDER BY id DESC")
    fun search(query: String): Flow<List<Work>>
}

@Dao
interface StudyCardDao {
    @Query("SELECT * FROM study_card ORDER BY id DESC")
    fun getAll(): Flow<List<StudyCard>>

    @Query("SELECT * FROM study_card WHERE id = :id")
    suspend fun getById(id: Long): StudyCard?

    @Insert
    suspend fun insert(studyCard: StudyCard): Long

    @Update
    suspend fun update(studyCard: StudyCard)

    @Delete
    suspend fun delete(studyCard: StudyCard)
}

@Dao
interface CustomRouteDao {
    @Query("SELECT * FROM custom_route ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<CustomRoute>>

    @Query("SELECT * FROM custom_route WHERE id = :id")
    suspend fun getById(id: String): CustomRoute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(route: CustomRoute)

    @Delete
    suspend fun delete(route: CustomRoute)
}

@Dao
interface PersonaDao {
    @Query("SELECT * FROM persona ORDER BY id DESC")
    fun getAll(): Flow<List<Persona>>

    @Query("SELECT * FROM persona WHERE id = :id")
    suspend fun getById(id: Long): Persona?

    @Insert
    suspend fun insert(persona: Persona): Long

    @Update
    suspend fun update(persona: Persona)

    @Delete
    suspend fun delete(persona: Persona)
}

@Dao
interface PracticeSessionDao {
    @Query("SELECT * FROM practice_session ORDER BY id DESC")
    fun getAll(): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_session WHERE id = :id")
    suspend fun getById(id: Long): PracticeSession?

    @Insert
    suspend fun insert(session: PracticeSession): Long

    @Update
    suspend fun update(session: PracticeSession)

    @Delete
    suspend fun delete(session: PracticeSession)
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topic ORDER BY id DESC")
    fun getAll(): Flow<List<Topic>>

    @Query("SELECT * FROM topic WHERE id = :id")
    suspend fun getById(id: Long): Topic?

    @Insert
    suspend fun insert(topic: Topic): Long

    @Update
    suspend fun update(topic: Topic)

    @Delete
    suspend fun delete(topic: Topic)
}

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observation ORDER BY id DESC")
    fun getAll(): Flow<List<Observation>>

    @Query("SELECT * FROM observation WHERE id = :id")
    suspend fun getById(id: Long): Observation?

    @Insert
    suspend fun insert(observation: Observation): Long

    @Update
    suspend fun update(observation: Observation)

    @Delete
    suspend fun delete(observation: Observation)
}

@Dao
interface CompareDao {
    @Query("SELECT * FROM compare_set ORDER BY id DESC")
    fun getAllSets(): Flow<List<CompareSet>>

    @Query("SELECT * FROM compare_item WHERE compareSetId = :setId")
    fun getItemsBySetId(setId: Long): Flow<List<CompareItem>>

    @Insert
    suspend fun insertSet(set: CompareSet): Long

    @Insert
    suspend fun insertItem(item: CompareItem): Long

    @Update
    suspend fun updateSet(set: CompareSet)

    @Delete
    suspend fun deleteSet(set: CompareSet)
}

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM experiment ORDER BY id DESC")
    fun getAll(): Flow<List<Experiment>>

    @Query("SELECT * FROM experiment_trial WHERE experimentId = :experimentId")
    fun getTrials(experimentId: Long): Flow<List<ExperimentTrial>>

    @Insert
    suspend fun insert(experiment: Experiment): Long

    @Insert
    suspend fun insertTrial(trial: ExperimentTrial): Long

    @Update
    suspend fun update(experiment: Experiment)

    @Delete
    suspend fun delete(experiment: Experiment)
}

@Dao
interface PoetryLabDao {
    @Query("SELECT * FROM pl_project ORDER BY id DESC")
    fun getAllProjects(): Flow<List<PlProject>>

    @Query("SELECT * FROM pl_project WHERE id = :id")
    suspend fun getProjectById(id: Long): PlProject?

    @Insert
    suspend fun insertProject(project: PlProject): Long

    @Update
    suspend fun updateProject(project: PlProject)

    @Delete
    suspend fun deleteProject(project: PlProject)

    @Query("SELECT * FROM pl_run WHERE projectId = :projectId ORDER BY id DESC")
    fun getRunsByProject(projectId: Long): Flow<List<PlRun>>

    @Insert
    suspend fun insertRun(run: PlRun): Long

    @Update
    suspend fun updateRun(run: PlRun)

    @Query("SELECT * FROM pl_text_asset WHERE projectId = :projectId ORDER BY id DESC")
    fun getAssetsByProject(projectId: Long): Flow<List<PlTextAsset>>

    @Insert
    suspend fun insertAsset(asset: PlTextAsset): Long

    @Query("SELECT * FROM pl_issue WHERE projectId = :projectId ORDER BY id DESC")
    fun getIssuesByProject(projectId: Long): Flow<List<PlIssue>>

    @Insert
    suspend fun insertIssue(issue: PlIssue): Long

    @Update
    suspend fun updateIssue(issue: PlIssue)
}

@Dao
interface MindsetLabDao {
    @Query("SELECT * FROM ms_day ORDER BY id DESC")
    fun getAllDays(): Flow<List<MsDay>>

    @Query("SELECT * FROM ms_day WHERE dateKey = :dateKey")
    suspend fun getDayByDateKey(dateKey: String): MsDay?

    @Query("SELECT * FROM ms_day WHERE id = :id")
    suspend fun getDayById(id: Long): MsDay?

    @Insert
    suspend fun insertDay(day: MsDay): Long

    @Update
    suspend fun updateDay(day: MsDay)

    @Delete
    suspend fun deleteDay(day: MsDay)

    @Query("SELECT * FROM ms_entry WHERE dayId = :dayId ORDER BY id")
    fun getEntriesByDay(dayId: Long): Flow<List<MsEntry>>

    @Insert
    suspend fun insertEntry(entry: MsEntry): Long

    @Update
    suspend fun updateEntry(entry: MsEntry)

    @Delete
    suspend fun deleteEntry(entry: MsEntry)

    @Query("SELECT * FROM ms_review WHERE dayId = :dayId")
    suspend fun getReviewByDay(dayId: Long): MsReview?

    @Insert
    suspend fun insertReview(review: MsReview): Long

    @Update
    suspend fun updateReview(review: MsReview)
}