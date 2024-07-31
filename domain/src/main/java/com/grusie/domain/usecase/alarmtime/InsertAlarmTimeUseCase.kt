package com.grusie.domain.usecase.alarmtime

import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.domain.repository.AlarmTimeRepository

class InsertAlarmTimeUseCase(private val alarmTimeRepository: AlarmTimeRepository) {
    suspend operator fun invoke(alarmTime: AlarmTimeDomainModel): Result<Long>{
        return alarmTimeRepository.insertAlarmTime(alarmTime)
    }
}