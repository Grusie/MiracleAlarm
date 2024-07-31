package com.grusie.domain.usecase.alarmtime

import com.grusie.domain.repository.AlarmTimeRepository

class DeleteByAlarmIdUseCase(private val alarmTimeRepository: AlarmTimeRepository) {
    suspend operator fun invoke(alarmId: Long): Result<Unit> {
        return alarmTimeRepository.deleteByAlarmId(alarmId)
    }
}