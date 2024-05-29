package com.grusie.data.di

import com.grusie.data.datasource.alarmdata.AlarmDataSource
import com.grusie.data.datasource.alarmdata.AlarmRemoteDataSource
import com.grusie.data.datasource.alarmtime.AlarmTimeDataSource
import com.grusie.data.datasource.alarmtime.AlarmTimeRemoteDataSource
import com.grusie.data.datasource.turnoffway.TurnOffWayDataSource
import com.grusie.data.datasource.turnoffway.TurnOffWayRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatasourceModule {
    @Singleton
    @Binds
    abstract fun bindAlarmDataSource(
        source: AlarmRemoteDataSource
    ): AlarmDataSource

    @Singleton
    @Binds
    abstract fun bindAlarmTimeDataSource(
        source: AlarmTimeRemoteDataSource
    ): AlarmTimeDataSource

    @Singleton
    @Binds
    abstract fun bindTurnOffWayDataSource(
        source: TurnOffWayRemoteDataSource
    ): TurnOffWayDataSource
}