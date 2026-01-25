// app/src/main/java/com/example/languagepracticev3/di/AppModule.kt
package com.example.languagepracticev3.di

import android.content.Context
import com.example.languagepracticev3.data.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideKvSettingDao(database: AppDatabase): KvSettingDao {
        return database.kvSettingDao()
    }

    @Provides
    fun provideRunLogDao(database: AppDatabase): RunLogDao {
        return database.runLogDao()
    }

    @Provides
    fun provideWorkDao(database: AppDatabase): WorkDao {
        return database.workDao()
    }

    @Provides
    fun provideStudyCardDao(database: AppDatabase): StudyCardDao {
        return database.studyCardDao()
    }

    @Provides
    fun provideCustomRouteDao(database: AppDatabase): CustomRouteDao {
        return database.customRouteDao()
    }

    @Provides
    fun providePersonaDao(database: AppDatabase): PersonaDao {
        return database.personaDao()
    }

    @Provides
    fun providePracticeSessionDao(database: AppDatabase): PracticeSessionDao {
        return database.practiceSessionDao()
    }

    @Provides
    fun provideTopicDao(database: AppDatabase): TopicDao {
        return database.topicDao()
    }

    @Provides
    fun provideObservationDao(database: AppDatabase): ObservationDao {
        return database.observationDao()
    }

    @Provides
    fun provideCompareDao(database: AppDatabase): CompareDao {
        return database.compareDao()
    }

    @Provides
    fun provideExperimentDao(database: AppDatabase): ExperimentDao {
        return database.experimentDao()
    }

    @Provides
    fun providePoetryLabDao(database: AppDatabase): PoetryLabDao {
        return database.poetryLabDao()
    }

    @Provides
    fun provideMindsetLabDao(database: AppDatabase): MindsetLabDao {
        return database.mindsetLabDao()
    }

    @Provides
    fun provideGlobalRevisionSessionDao(database: AppDatabase): GlobalRevisionSessionDao {
        return database.globalRevisionSessionDao()
    }

    // 6つの思考習慣
    @Provides
    fun provideSixHabitsSessionDao(database: AppDatabase): SixHabitsSessionDao {
        return database.sixHabitsSessionDao()
    }

    @Provides
    fun provideSixHabitsDailyTrackingDao(database: AppDatabase): SixHabitsDailyTrackingDao {
        return database.sixHabitsDailyTrackingDao()
    }

    @Provides
    fun provideSixHabitsMaterialDao(database: AppDatabase): SixHabitsMaterialDao {
        return database.sixHabitsMaterialDao()
    }

    // ★追加: 抽象化テクニック
    @Provides
    fun provideAbstractionSessionDao(database: AppDatabase): AbstractionSessionDao {
        return database.abstractionSessionDao()
    }
}