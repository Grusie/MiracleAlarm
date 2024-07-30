package com.grusie.domain.usecase.alarmtime

import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.domain.repository.AlarmTimeRepository

class GetMissedAlarmsUseCase(private val alarmTimeRepository: AlarmTimeRepository) {
    suspend operator fun invoke(currentTime: Long): Result<List<AlarmTimeDomainModel>?> {
        return alarmTimeRepository.getMissedAlarms(currentTime)
    }
}