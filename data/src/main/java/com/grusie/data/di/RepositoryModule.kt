package com.grusie.data.di

import com.grusie.data.repository.AlarmRepositoryImpl
import com.grusie.data.repository.AlarmTimeRepositoryImpl
import com.grusie.data.repository.TurnOffWayRepositoryImpl
import com.grusie.domain.repository.AlarmDataRepository
import com.grusie.domain.repository.AlarmTimeRepository
import com.grusie.domain.repository.TurnOffWayRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindAlarmDataRepository(alarmDataRepositoryImpl: AlarmRepositoryImpl): AlarmDataRepository

    @Singleton
    @Binds
    abstract fun bindAlarmTimeRepository(alarmTimeRepositoryImpl: AlarmTimeRepositoryImpl): AlarmTimeRepository

    @Singleton
    @Binds
    abstract fun bindTurnOffWayRepository(turnOffWayRepositoryImpl: TurnOffWayRepositoryImpl): TurnOffWayRepository
}