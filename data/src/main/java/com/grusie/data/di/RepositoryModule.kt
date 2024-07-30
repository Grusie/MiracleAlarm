package com.grusie.data.di

import com.grusie.data.repository.AlarmRepositoryImpl
import com.grusie.domain.repository.AlarmDataRepository
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
}