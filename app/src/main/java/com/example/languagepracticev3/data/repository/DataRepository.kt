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
    private val globalRevisionSessionDao: GlobalRevisionSessionDao  // ★追加
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
    // ★追加: グローバル・リビジョンセッション
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
}
