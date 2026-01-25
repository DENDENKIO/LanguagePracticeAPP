// app/src/main/java/com/example/languagepracticev3/data/database/AppDatabase.kt
package com.example.languagepracticev3.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.languagepracticev3.data.model.*

@Database(
    entities = [
        KvSetting::class,
        RunLog::class,
        Work::class,
        StudyCard::class,
        CustomRoute::class,
        Persona::class,
        PersonaVerification::class,
        PracticeSession::class,
        Topic::class,
        Observation::class,
        CompareSet::class,
        CompareItem::class,
        Experiment::class,
        ExperimentTrial::class,
        PlProject::class,
        PlRun::class,
        PlAiStepLog::class,
        PlTextAsset::class,
        PlIssue::class,
        PlCompare::class,
        PlExportLog::class,
        MsDay::class,
        MsEntry::class,
        MsAiStepLog::class,
        MsReview::class,
        MsExportLog::class,
        GlobalRevisionSession::class,
        // ★追加: 6つの思考習慣
        SixHabitsSession::class,
        SixHabitsDailyTracking::class,
        SixHabitsMaterial::class
    ],
    version = 4,  // ★バージョンをインクリメント（3→4）
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kvSettingDao(): KvSettingDao
    abstract fun runLogDao(): RunLogDao
    abstract fun workDao(): WorkDao
    abstract fun studyCardDao(): StudyCardDao
    abstract fun customRouteDao(): CustomRouteDao
    abstract fun personaDao(): PersonaDao
    abstract fun practiceSessionDao(): PracticeSessionDao
    abstract fun topicDao(): TopicDao
    abstract fun observationDao(): ObservationDao
    abstract fun compareDao(): CompareDao
    abstract fun experimentDao(): ExperimentDao
    abstract fun poetryLabDao(): PoetryLabDao
    abstract fun mindsetLabDao(): MindsetLabDao
    abstract fun globalRevisionSessionDao(): GlobalRevisionSessionDao
    // ★追加: 6つの思考習慣
    abstract fun sixHabitsSessionDao(): SixHabitsSessionDao
    abstract fun sixHabitsDailyTrackingDao(): SixHabitsDailyTrackingDao
    abstract fun sixHabitsMaterialDao(): SixHabitsMaterialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "language_practice_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}