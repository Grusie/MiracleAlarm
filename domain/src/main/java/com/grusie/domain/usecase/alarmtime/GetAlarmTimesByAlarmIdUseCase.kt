package com.grusie.domain.usecase.alarmtime

import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.domain.repository.AlarmTimeRepository

class GetAlarmTimesByAlarmIdUseCase(private val alarmTimeRepository: AlarmTimeRepository) {
    suspend operator fun invoke(alarmId: Long): Result<List<AlarmTimeDomainModel>> {
        return alarmTimeRepository.getAlarmTimesByAlarmId(alarmId)
    }
}