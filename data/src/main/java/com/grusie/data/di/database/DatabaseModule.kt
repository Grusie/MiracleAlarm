package com.grusie.data.di.database

import android.content.Context
import com.grusie.data.database.AlarmDao
import com.grusie.data.database.AlarmDatabase
import com.grusie.data.database.AlarmTimeDao
import com.grusie.data.database.AlarmTurnOffDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAlarmDatabase(
        @ApplicationContext context: Context
    ): AlarmDatabase = AlarmDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideAlarmDao(alarmDatabase: AlarmDatabase): AlarmDao =
        alarmDatabase.alarmDao()

    @Provides
    @Singleton
    fun provideAlarmTimeDao(alarmDatabase: AlarmDatabase): AlarmTimeDao =
        alarmDatabase.alarmTimeDao()

    @Provides
    @Singleton
    fun provideAlarmTurnOffDao(alarmDatabase: AlarmDatabase): AlarmTurnOffDao =
        alarmDatabase.alarmTurnOffDao()
}