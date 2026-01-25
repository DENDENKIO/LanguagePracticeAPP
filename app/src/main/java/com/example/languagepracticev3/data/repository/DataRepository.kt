// app/src/main/java/com/example/languagepracticev3/data/repository/DataRepository.kt
package com.example.languagepracticev3.data.repository

import com.example.languagepracticev3.data.database.*
import com.example.languagepracticev3.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val workDao: WorkDao,
    private val studyCardDao: StudyCardDao,
    private val topicDao: TopicDao,
    private val personaDao: PersonaDao,
    private val observationDao: ObservationDao,
    private val customRouteDao: CustomRouteDao,
    private val kvSettingDao: KvSettingDao,
    private val runLogDao: RunLogDao,
    private val practiceSessionDao: PracticeSessionDao,
    private val compareDao: CompareDao,
    private val experimentDao: ExperimentDao,
    private val globalRevisionSessionDao: GlobalRevisionSessionDao,
    // 6つの思考習慣
    private val sixHabitsSessionDao: SixHabitsSessionDao,
    private val sixHabitsDailyTrackingDao: SixHabitsDailyTrackingDao,
    private val sixHabitsMaterialDao: SixHabitsMaterialDao,
    // ★追加: 抽象化テクニック
    private val abstractionSessionDao: AbstractionSessionDao
) {
    // Work
    suspend fun insertWork(work: Work): Long = workDao.insert(work)
    suspend fun getWorkById(id: Long): Work? = workDao.getById(id)
    fun getAllWorks(): Flow<List<Work>> = workDao.observeAll()
    fun searchWorks(query: String): Flow<List<Work>> = workDao.search(query)
    suspend fun getRecentWorks(limit: Int): List<Work> = workDao.getRecent(limit)
    suspend fun deleteWork(work: Work) = workDao.delete(work)

    // Persona
    fun getAllPersonas(): Flow<List<Persona>> = personaDao.observeAll()
    suspend fun getPersonaById(id: Long): Persona? = personaDao.getById(id)
    fun searchPersonas(query: String): Flow<List<Persona>> = personaDao.search(query)

    // StudyCard
    fun getAllStudyCards(): Flow<List<StudyCard>> = studyCardDao.observeAll()
    fun searchStudyCards(query: String): Flow<List<StudyCard>> = studyCardDao.search(query)

    // Topic
    fun getAllTopics(): Flow<List<Topic>> = topicDao.observeAll()
    fun searchTopics(query: String): Flow<List<Topic>> = topicDao.search(query)

    // Observation
    fun getAllObservations(): Flow<List<Observation>> = observationDao.observeAll()
    fun searchObservations(query: String): Flow<List<Observation>> = observationDao.search(query)

    // CustomRoute
    fun getAllCustomRoutes(): Flow<List<CustomRoute>> = customRouteDao.observeAll()

    // =====================================
    // グローバル・リビジョンセッション
    // =====================================
    fun getAllGlobalRevisionSessions(): Flow<List<GlobalRevisionSession>> {
        return globalRevisionSessionDao.observeAll()
    }

    suspend fun saveGlobalRevisionSession(session: GlobalRevisionSession): Long {
        return if (session.id == 0L) {
            globalRevisionSessionDao.insert(session)
        } else {
            globalRevisionSessionDao.update(session)
            session.id
        }
    }

    suspend fun deleteGlobalRevisionSession(session: GlobalRevisionSession) {
        globalRevisionSessionDao.delete(session)
    }

    suspend fun getGlobalRevisionSessionById(id: Long): GlobalRevisionSession? {
        return globalRevisionSessionDao.getById(id)
    }

    // =====================================
    // 6つの思考習慣
    // =====================================

    // Session
    fun getAllSixHabitsSessions(): Flow<List<SixHabitsSession>> {
        return sixHabitsSessionDao.observeAll()
    }

    fun getSixHabitsSessionsByMindsetType(mindsetType: Int): Flow<List<SixHabitsSession>> {
        return sixHabitsSessionDao.observeByMindsetType(mindsetType)
    }

    suspend fun saveSixHabitsSession(session: SixHabitsSession): Long {
        return if (session.id == 0L) {
            sixHabitsSessionDao.insert(session)
        } else {
            sixHabitsSessionDao.update(session)
            session.id
        }
    }

    suspend fun deleteSixHabitsSession(session: SixHabitsSession) {
        sixHabitsSessionDao.delete(session)
    }

    suspend fun getSixHabitsSessionById(id: Long): SixHabitsSession? {
        return sixHabitsSessionDao.getById(id)
    }

    // Daily Tracking
    fun getAllSixHabitsDailyTracking(): Flow<List<SixHabitsDailyTracking>> {
        return sixHabitsDailyTrackingDao.observeAll()
    }

    suspend fun getSixHabitsDailyTrackingByDate(date: String): SixHabitsDailyTracking? {
        return sixHabitsDailyTrackingDao.getByDate(date)
    }

    suspend fun saveSixHabitsDailyTracking(tracking: SixHabitsDailyTracking): Long {
        return if (tracking.id == 0L) {
            sixHabitsDailyTrackingDao.insert(tracking)
        } else {
            sixHabitsDailyTrackingDao.update(tracking)
            tracking.id
        }
    }

    suspend fun getRecentSixHabitsDailyTracking(limit: Int): List<SixHabitsDailyTracking> {
        return sixHabitsDailyTrackingDao.getRecent(limit)
    }

    // Materials
    fun getAllSixHabitsMaterials(): Flow<List<SixHabitsMaterial>> {
        return sixHabitsMaterialDao.observeAll()
    }

    fun getSixHabitsMaterialsByType(type: String): Flow<List<SixHabitsMaterial>> {
        return sixHabitsMaterialDao.observeByType(type)
    }

    fun getFavoriteSixHabitsMaterials(): Flow<List<SixHabitsMaterial>> {
        return sixHabitsMaterialDao.observeFavorites()
    }

    suspend fun saveSixHabitsMaterial(material: SixHabitsMaterial): Long {
        return if (material.id == 0L) {
            sixHabitsMaterialDao.insert(material)
        } else {
            sixHabitsMaterialDao.update(material)
            material.id
        }
    }

    suspend fun deleteSixHabitsMaterial(material: SixHabitsMaterial) {
        sixHabitsMaterialDao.delete(material)
    }

    suspend fun countSixHabitsMaterialsByType(type: String): Int {
        return sixHabitsMaterialDao.countByType(type)
    }

    // =====================================
    // ★追加: 抽象化テクニック
    // =====================================

    fun getAllAbstractionSessions(): Flow<List<AbstractionSession>> {
        return abstractionSessionDao.observeAll()
    }

    suspend fun getAbstractionSessionById(id: Long): AbstractionSession? {
        return abstractionSessionDao.getById(id)
    }

    suspend fun saveAbstractionSession(session: AbstractionSession): Long {
        return if (session.id == 0L) {
            abstractionSessionDao.insert(session)
        } else {
            abstractionSessionDao.update(session)
            session.id
        }
    }

    suspend fun deleteAbstractionSession(session: AbstractionSession) {
        abstractionSessionDao.delete(session)
    }

    suspend fun getLastIncompleteAbstractionSession(): AbstractionSession? {
        return abstractionSessionDao.getLastIncomplete()
    }

    suspend fun getRecentCompletedAbstractionSessions(limit: Int): List<AbstractionSession> {
        return abstractionSessionDao.getRecentCompleted(limit)
    }
}