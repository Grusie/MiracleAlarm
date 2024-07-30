package com.grusie.domain.usecase.turnoff

import com.grusie.domain.model.AlarmTurnOffDomainModel
import com.grusie.domain.repository.TurnOffWayRepository

class InsertTurnOffWayUseCase(private val turnOffWayRepository: TurnOffWayRepository) {
    suspend operator fun invoke(alarmTurnOffData: AlarmTurnOffDomainModel): Result<Long> {
        return turnOffWayRepository.insertTurnOffWay(alarmTurnOffData)
    }
}