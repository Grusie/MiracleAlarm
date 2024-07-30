package com.grusie.domain.usecase.alarmtime

import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.domain.repository.AlarmTimeRepository

class DeleteAlarmTimeUseCase(private val alarmTimeRepository: AlarmTimeRepository) {
    suspend operator fun invoke(alarmTime: AlarmTimeDomainModel): Result<Unit> {
        return alarmTimeRepository.deleteAlarmTime(alarmTime)
    }
}