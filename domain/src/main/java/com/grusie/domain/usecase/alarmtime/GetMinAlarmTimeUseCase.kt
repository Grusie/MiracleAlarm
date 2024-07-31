package com.grusie.domain.usecase.alarmtime

import com.grusie.domain.model.AlarmTimeDomainModel
import com.grusie.domain.repository.AlarmTimeRepository

class GetMinAlarmTimeUseCase(private val alarmTimeRepository: AlarmTimeRepository) {
    suspend operator fun invoke(): Result<AlarmTimeDomainModel?> {
        return alarmTimeRepository.getMinAlarmTime()
    }
}