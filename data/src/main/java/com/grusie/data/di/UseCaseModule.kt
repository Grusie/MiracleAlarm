package com.grusie.data.di

import com.grusie.domain.repository.AlarmDataRepository
import com.grusie.domain.usecase.alarmdata.AlarmDataUseCases
import com.grusie.domain.usecase.alarmdata.DeleteAlarmDataUseCase
import com.grusie.domain.usecase.alarmdata.GetAlarmByIdUseCase
import com.grusie.domain.usecase.alarmdata.GetAllAlarmListUseCase
import com.grusie.domain.usecase.alarmdata.InsertAlarmDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Singleton
    @Provides
    fun providesAlarmDataUseCases(repository: AlarmDataRepository): AlarmDataUseCases =
        AlarmDataUseCases(
            getAllAlarmListUseCases = GetAllAlarmListUseCase(repository),
            insertAlarmDataUseCase = InsertAlarmDataUseCase(repository),
            deleteAlarmDataUseCase = DeleteAlarmDataUseCase(repository),
            getAlarmByIdUseCase = GetAlarmByIdUseCase(repository)
        )
}