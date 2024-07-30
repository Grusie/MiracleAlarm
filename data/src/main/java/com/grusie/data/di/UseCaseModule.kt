package com.grusie.data.di

import com.grusie.domain.repository.AlarmDataRepository
import com.grusie.domain.repository.AlarmTimeRepository
import com.grusie.domain.usecase.alarmdata.AlarmDataUseCases
import com.grusie.domain.usecase.alarmdata.DeleteAlarmDataUseCase
import com.grusie.domain.usecase.alarmdata.GetAlarmByIdUseCase
import com.grusie.domain.usecase.alarmdata.GetAllAlarmListUseCase
import com.grusie.domain.usecase.alarmdata.InsertAlarmDataUseCase
import com.grusie.domain.usecase.alarmtime.AlarmTimeUseCases
import com.grusie.domain.usecase.alarmtime.DeleteAlarmTimeUseCase
import com.grusie.domain.usecase.alarmtime.DeleteByAlarmIdUseCase
import com.grusie.domain.usecase.alarmtime.GetAlarmTimesByAlarmIdUseCase
import com.grusie.domain.usecase.alarmtime.GetMinAlarmTimeUseCase
import com.grusie.domain.usecase.alarmtime.GetMissedAlarmsUseCase
import com.grusie.domain.usecase.alarmtime.InsertAlarmTimeUseCase
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
    fun providesAlarmUseCases(repository: AlarmDataRepository): AlarmDataUseCases =
        AlarmDataUseCases(
            getAllAlarmListUseCases = GetAllAlarmListUseCase(repository),
            insertAlarmDataUseCase = InsertAlarmDataUseCase(repository),
            deleteAlarmDataUseCase = DeleteAlarmDataUseCase(repository),
            getAlarmByIdUseCase = GetAlarmByIdUseCase(repository)
        )

    @Singleton
    @Provides
    fun providesAlarmTimeUseCases(repository: AlarmTimeRepository): AlarmTimeUseCases =
        AlarmTimeUseCases(
            insertAlarmTime = InsertAlarmTimeUseCase(repository),
            deleteAlarmTimeUseCase = DeleteAlarmTimeUseCase(repository),
            deleteByAlarmIdUseCase = DeleteByAlarmIdUseCase(repository),
            getAlarmTimesByAlarmIdUseCase = GetAlarmTimesByAlarmIdUseCase(repository),
            getMinAlarmTimeUseCase = GetMinAlarmTimeUseCase(repository),
            getMissedAlarmsUseCase = GetMissedAlarmsUseCase(repository)
        )
}