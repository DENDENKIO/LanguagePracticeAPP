// app/src/main/java/com/example/languagepracticev3/data/repository/DataRepository.kt
package com.example.languagepracticev3.data.repository

import com.example.languagepracticev3.data.database.*
import com.example.languagepracticev3.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val kvSettingDao: KvSettingDao,
    private val runLogDao: RunLogDao,
    private val workDao: WorkDao,
    private val studyCardDao: StudyCardDao,
    private val customRouteDao: CustomRouteDao,
    private val personaDao: PersonaDao,
    private val practiceSessionDao: PracticeSessionDao,
    private val topicDao: TopicDao,
    private val observationDao: ObservationDao,
    private val compareDao: CompareDao,
    private val experimentDao: ExperimentDao,
    private val poetryLabDao: PoetryLabDao,
    private val mindsetLabDao: MindsetLabDao,
    private val globalRevisionSessionDao: GlobalRevisionSessionDao,
    private val sixHabitsSessionDao: SixHabitsSessionDao,
    private val sixHabitsDailyTrackingDao: SixHabitsDailyTrackingDao,
    private val sixHabitsMaterialDao: SixHabitsMaterialDao,
    private val abstractionSessionDao: AbstractionSessionDao,
    // ★追加: 物質-抽象変換
    private val materialAbstractionSessionDao: MaterialAbstractionSessionDao
) {

    // ========== KvSetting ==========
    suspend fun getSetting(key: String): KvSetting? = kvSettingDao.get(key)
    suspend fun saveSetting(setting: KvSetting) = kvSettingDao.upsert(setting)
    suspend fun deleteSetting(key: String) = kvSettingDao.delete(key)
    fun observeAllSettings(): Flow<List<KvSetting>> = kvSettingDao.observeAll()

    // ========== RunLog ==========
    suspend fun insertRunLog(log: RunLog): Long = runLogDao.insert(log)
    fun observeAllRunLogs(): Flow<List<RunLog>> = runLogDao.observeAll()
    suspend fun getRunLogById(id: Long): RunLog? = runLogDao.getById(id)
    suspend fun deleteRunLogById(id: Long) = runLogDao.deleteById(id)
    suspend fun deleteOldRunLogs(threshold: String) = runLogDao.deleteOlderThan(threshold)

    // ========== Work ==========
    suspend fun insertWork(work: Work): Long = workDao.insert(work)
    suspend fun updateWork(work: Work) = workDao.update(work)
    suspend fun deleteWork(work: Work) = workDao.delete(work)
    suspend fun deleteWorkById(id: Long) = workDao.deleteById(id)
    suspend fun getWorkById(id: Long): Work? = workDao.getById(id)
    fun observeAllWorks(): Flow<List<Work>> = workDao.observeAll()
    suspend fun getAllWorks(): List<Work> = workDao.getAll()
    fun searchWorks(query: String): Flow<List<Work>> = workDao.search(query)
    suspend fun searchWorksSuspend(query: String): List<Work> = workDao.searchSuspend(query)
    fun filterWorksByKind(kind: String): Flow<List<Work>> = workDao.filterByKind(kind)
    suspend fun getRecentWorks(limit: Int): List<Work> = workDao.getRecent(limit)

    // ========== StudyCard ==========
    suspend fun insertStudyCard(card: StudyCard): Long = studyCardDao.insert(card)
    suspend fun updateStudyCard(card: StudyCard) = studyCardDao.update(card)
    suspend fun deleteStudyCard(card: StudyCard) = studyCardDao.delete(card)
    suspend fun getStudyCardById(id: Long): StudyCard? = studyCardDao.getById(id)
    fun observeAllStudyCards(): Flow<List<StudyCard>> = studyCardDao.observeAll()
    suspend fun getAllStudyCards(): List<StudyCard> = studyCardDao.getAll()
    fun searchStudyCards(query: String): Flow<List<StudyCard>> = studyCardDao.search(query)
    suspend fun searchStudyCardsSuspend(query: String): List<StudyCard> = studyCardDao.searchSuspend(query)

    // ========== CustomRoute ==========
    suspend fun insertCustomRoute(route: CustomRoute) = customRouteDao.insert(route)
    suspend fun updateCustomRoute(route: CustomRoute) = customRouteDao.update(route)
    suspend fun deleteCustomRoute(route: CustomRoute) = customRouteDao.delete(route)
    fun observeAllCustomRoutes(): Flow<List<CustomRoute>> = customRouteDao.observeAll()
    suspend fun getAllCustomRoutes(): List<CustomRoute> = customRouteDao.getAll()
    suspend fun getCustomRouteById(id: String): CustomRoute? = customRouteDao.getById(id)
    suspend fun insertOrUpdateCustomRoute(route: CustomRoute) = customRouteDao.insertOrUpdate(route)

    // ========== Persona ==========
    suspend fun insertPersona(persona: Persona): Long = personaDao.insert(persona)
    suspend fun updatePersona(persona: Persona) = personaDao.update(persona)
    suspend fun deletePersona(persona: Persona) = personaDao.delete(persona)
    suspend fun getPersonaById(id: Long): Persona? = personaDao.getById(id)
    fun observeAllPersonas(): Flow<List<Persona>> = personaDao.observeAll()
    suspend fun getAllPersonas(): List<Persona> = personaDao.getAll()
    fun searchPersonas(query: String): Flow<List<Persona>> = personaDao.search(query)
    suspend fun searchPersonasSuspend(query: String): List<Persona> = personaDao.searchSuspend(query)
    suspend fun updatePersonaStatus(id: Long, status: String) = personaDao.updateStatus(id, status)

    // ========== PracticeSession ==========
    suspend fun insertPracticeSession(session: PracticeSession): Long = practiceSessionDao.insert(session)
    suspend fun updatePracticeSession(session: PracticeSession) = practiceSessionDao.update(session)
    suspend fun deletePracticeSession(session: PracticeSession) = practiceSessionDao.delete(session)
    suspend fun getPracticeSessionById(id: Long): PracticeSession? = practiceSessionDao.getById(id)
    fun observeAllPracticeSessions(): Flow<List<PracticeSession>> = practiceSessionDao.observeAll()
    suspend fun getAllPracticeSessions(): List<PracticeSession> = practiceSessionDao.getAll()
    suspend fun getLastIncompletePracticeSession(): PracticeSession? = practiceSessionDao.getLastIncomplete()

    // ========== Topic ==========
    suspend fun insertTopic(topic: Topic): Long = topicDao.insert(topic)
    suspend fun updateTopic(topic: Topic) = topicDao.update(topic)
    suspend fun deleteTopic(topic: Topic) = topicDao.delete(topic)
    suspend fun getTopicById(id: Long): Topic? = topicDao.getById(id)
    fun observeAllTopics(): Flow<List<Topic>> = topicDao.observeAll()
    suspend fun getAllTopics(): List<Topic> = topicDao.getAll()
    fun searchTopics(query: String): Flow<List<Topic>> = topicDao.search(query)
    suspend fun searchTopicsSuspend(query: String): List<Topic> = topicDao.searchSuspend(query)
    suspend fun getRecentTopics(limit: Int): List<Topic> = topicDao.getRecent(limit)

    // ========== Observation ==========
    suspend fun insertObservation(observation: Observation): Long = observationDao.insert(observation)
    suspend fun updateObservation(observation: Observation) = observationDao.update(observation)
    suspend fun deleteObservation(observation: Observation) = observationDao.delete(observation)
    suspend fun getObservationById(id: Long): Observation? = observationDao.getById(id)
    fun observeAllObservations(): Flow<List<Observation>> = observationDao.observeAll()
    suspend fun getAllObservations(): List<Observation> = observationDao.getAll()
    fun searchObservations(query: String): Flow<List<Observation>> = observationDao.search(query)
    suspend fun searchObservationsSuspend(query: String): List<Observation> = observationDao.searchSuspend(query)

    // ========== Compare ==========
    suspend fun insertCompareSet(compareSet: CompareSet): Long = compareDao.insertSet(compareSet)
    suspend fun insertCompareItem(item: CompareItem): Long = compareDao.insertItem(item)
    suspend fun deleteCompareSet(compareSet: CompareSet) = compareDao.deleteSet(compareSet)
    fun observeAllCompareSets(): Flow<List<CompareSet>> = compareDao.observeAllSets()
    fun getCompareItemsForSet(setId: Long): Flow<List<CompareItem>> = compareDao.getItemsForSet(setId)

    // ========== Experiment ==========
    suspend fun insertExperiment(experiment: Experiment): Long = experimentDao.insertExperiment(experiment)
    suspend fun insertExperimentTrial(trial: ExperimentTrial): Long = experimentDao.insertTrial(trial)
    suspend fun deleteExperiment(experiment: Experiment) = experimentDao.deleteExperiment(experiment)
    fun observeAllExperiments(): Flow<List<Experiment>> = experimentDao.observeAll()
    fun getTrialsForExperiment(experimentId: Long): Flow<List<ExperimentTrial>> = experimentDao.getTrialsForExperiment(experimentId)

    // ========== PoetryLab ==========
    suspend fun insertPlProject(project: PlProject): Long = poetryLabDao.insertProject(project)
    suspend fun updatePlProject(project: PlProject) = poetryLabDao.updateProject(project)
    suspend fun deletePlProject(project: PlProject) = poetryLabDao.deleteProject(project)
    fun getAllPlProjects(): Flow<List<PlProject>> = poetryLabDao.getAllProjects()
    suspend fun getPlProjectById(id: Long): PlProject? = poetryLabDao.getProjectById(id)
    suspend fun insertPlRun(run: PlRun): Long = poetryLabDao.insertRun(run)
    fun getPlRunsByProject(projectId: Long): Flow<List<PlRun>> = poetryLabDao.getRunsByProject(projectId)
    suspend fun insertPlAsset(asset: PlTextAsset): Long = poetryLabDao.insertAsset(asset)
    fun getPlAssetsByProject(projectId: Long): Flow<List<PlTextAsset>> = poetryLabDao.getAssetsByProject(projectId)
    suspend fun insertPlIssue(issue: PlIssue): Long = poetryLabDao.insertIssue(issue)
    suspend fun updatePlIssue(issue: PlIssue) = poetryLabDao.updateIssue(issue)
    fun getPlIssuesByProject(projectId: Long): Flow<List<PlIssue>> = poetryLabDao.getIssuesByProject(projectId)

    // ========== MindsetLab ==========
    suspend fun insertMsDay(day: MsDay): Long = mindsetLabDao.insertDay(day)
    suspend fun updateMsDay(day: MsDay) = mindsetLabDao.updateDay(day)
    suspend fun deleteMsDay(day: MsDay) = mindsetLabDao.deleteDay(day)
    fun getAllMsDays(): Flow<List<MsDay>> = mindsetLabDao.getAllDays()
    suspend fun getMsDayById(id: Long): MsDay? = mindsetLabDao.getDayById(id)
    suspend fun insertMsEntry(entry: MsEntry): Long = mindsetLabDao.insertEntry(entry)
    suspend fun updateMsEntry(entry: MsEntry) = mindsetLabDao.updateEntry(entry)
    suspend fun deleteMsEntry(entry: MsEntry) = mindsetLabDao.deleteEntry(entry)
    fun getMsEntriesByDay(dayId: Long): Flow<List<MsEntry>> = mindsetLabDao.getEntriesByDay(dayId)
    suspend fun insertMsReview(review: MsReview): Long = mindsetLabDao.insertReview(review)
    fun getMsReviewsByDay(dayId: Long): Flow<List<MsReview>> = mindsetLabDao.getReviewsByDay(dayId)

    // ========== GlobalRevisionSession ==========
    fun getAllGlobalRevisionSessions(): Flow<List<GlobalRevisionSession>> = globalRevisionSessionDao.observeAll()

    suspend fun getGlobalRevisionSessionById(id: Long): GlobalRevisionSession? =
        globalRevisionSessionDao.getById(id)

    suspend fun saveGlobalRevisionSession(session: GlobalRevisionSession): Long {
        return if (session.id == 0L) {
            globalRevisionSessionDao.insert(session)
        } else {
            globalRevisionSessionDao.update(session)
            session.id
        }
    }

    suspend fun deleteGlobalRevisionSession(session: GlobalRevisionSession) =
        globalRevisionSessionDao.delete(session)

    // ========== SixHabitsSession ==========
    fun getAllSixHabitsSessions(): Flow<List<SixHabitsSession>> = sixHabitsSessionDao.observeAll()

    fun getSixHabitsSessionsByMindsetType(mindsetType: Int): Flow<List<SixHabitsSession>> =
        sixHabitsSessionDao.observeByMindsetType(mindsetType)

    suspend fun getSixHabitsSessionById(id: Long): SixHabitsSession? =
        sixHabitsSessionDao.getById(id)

    suspend fun saveSixHabitsSession(session: SixHabitsSession): Long {
        return if (session.id == 0L) {
            sixHabitsSessionDao.insert(session)
        } else {
            sixHabitsSessionDao.update(session)
            session.id
        }
    }

    suspend fun deleteSixHabitsSession(session: SixHabitsSession) =
        sixHabitsSessionDao.delete(session)

    suspend fun getSixHabitsSessionsByDate(date: String): List<SixHabitsSession> =
        sixHabitsSessionDao.getByDate(date)

    // ========== SixHabitsDailyTracking ==========
    fun getAllSixHabitsDailyTracking(): Flow<List<SixHabitsDailyTracking>> =
        sixHabitsDailyTrackingDao.observeAll()

    suspend fun getSixHabitsDailyTrackingByDate(date: String): SixHabitsDailyTracking? =
        sixHabitsDailyTrackingDao.getByDate(date)

    suspend fun saveSixHabitsDailyTracking(tracking: SixHabitsDailyTracking): Long {
        val existing = sixHabitsDailyTrackingDao.getByDate(tracking.date)
        return if (existing == null) {
            sixHabitsDailyTrackingDao.insert(tracking)
        } else {
            sixHabitsDailyTrackingDao.update(tracking.copy(id = existing.id))
            existing.id
        }
    }

    suspend fun getRecentSixHabitsDailyTracking(limit: Int): List<SixHabitsDailyTracking> =
        sixHabitsDailyTrackingDao.getRecent(limit)

    // ========== SixHabitsMaterial ==========
    fun getAllSixHabitsMaterials(): Flow<List<SixHabitsMaterial>> =
        sixHabitsMaterialDao.observeAll()

    fun getSixHabitsMaterialsByType(type: String): Flow<List<SixHabitsMaterial>> =
        sixHabitsMaterialDao.observeByType(type)

    fun getFavoriteSixHabitsMaterials(): Flow<List<SixHabitsMaterial>> =
        sixHabitsMaterialDao.observeFavorites()

    suspend fun saveSixHabitsMaterial(material: SixHabitsMaterial): Long {
        return if (material.id == 0L) {
            sixHabitsMaterialDao.insert(material)
        } else {
            sixHabitsMaterialDao.update(material)
            material.id
        }
    }

    suspend fun deleteSixHabitsMaterial(material: SixHabitsMaterial) =
        sixHabitsMaterialDao.delete(material)

    suspend fun getSixHabitsMaterialById(id: Long): SixHabitsMaterial? =
        sixHabitsMaterialDao.getById(id)

    suspend fun countSixHabitsMaterialsByType(type: String): Int =
        sixHabitsMaterialDao.countByType(type)

    // ========== AbstractionSession (抽象化テクニック) ==========
    fun getAllAbstractionSessions(): Flow<List<AbstractionSession>> =
        abstractionSessionDao.observeAll()

    suspend fun getAbstractionSessionById(id: Long): AbstractionSession? =
        abstractionSessionDao.getById(id)

    suspend fun saveAbstractionSession(session: AbstractionSession): Long {
        return if (session.id == 0L) {
            abstractionSessionDao.insert(session)
        } else {
            abstractionSessionDao.update(session)
            session.id
        }
    }

    suspend fun deleteAbstractionSession(session: AbstractionSession) =
        abstractionSessionDao.delete(session)

    suspend fun getLastIncompleteAbstractionSession(): AbstractionSession? =
        abstractionSessionDao.getLastIncomplete()

    suspend fun getRecentCompletedAbstractionSessions(limit: Int): List<AbstractionSession> =
        abstractionSessionDao.getRecentCompleted(limit)

    // ========== MaterialAbstractionSession (物質-抽象変換) ==========
    fun getAllMaterialAbstractionSessions(): Flow<List<MaterialAbstractionSession>> =
        materialAbstractionSessionDao.observeAll()

    suspend fun getMaterialAbstractionSessionById(id: Long): MaterialAbstractionSession? =
        materialAbstractionSessionDao.getById(id)

    suspend fun saveMaterialAbstractionSession(session: MaterialAbstractionSession): Long {
        return if (session.id == 0L) {
            materialAbstractionSessionDao.insert(session)
        } else {
            materialAbstractionSessionDao.update(session)
            session.id
        }
    }

    suspend fun deleteMaterialAbstractionSession(session: MaterialAbstractionSession) =
        materialAbstractionSessionDao.delete(session)

    suspend fun getLastIncompleteMaterialAbstractionSession(): MaterialAbstractionSession? =
        materialAbstractionSessionDao.getLastIncomplete()

    suspend fun getRecentCompletedMaterialAbstractionSessions(limit: Int): List<MaterialAbstractionSession> =
        materialAbstractionSessionDao.getRecentCompleted(limit)
}
