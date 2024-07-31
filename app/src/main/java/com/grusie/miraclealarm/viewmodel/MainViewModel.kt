package com.grusie.miraclealarm.viewmodel

import androidx.lifecycle.viewModelScope
import com.grusie.domain.usecase.alarmdata.AlarmDataUseCases
import com.grusie.domain.usecase.alarmtime.AlarmTimeUseCases
import com.grusie.miraclealarm.mapper.toDomainModel
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.data.AlarmUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmDataUseCases: AlarmDataUseCases,
    private val alarmTimeUseCases: AlarmTimeUseCases
) : BaseViewModel() {
    private val _allAlarmList: MutableStateFlow<List<AlarmUiModel>> = MutableStateFlow(emptyList())

    val allAlarmList: StateFlow<List<AlarmUiModel>> get() = _allAlarmList

    var deleteAlarmList: MutableList<AlarmUiModel> = mutableListOf()
        private set

    fun getAllAlarmList() {
        viewModelScope.launch {
            alarmDataUseCases.getAllAlarmListUseCases().onSuccess { list ->
                setAlarmList(list.map { it.toUiModel() })
            }.onFailure {
                handleError(it)
            }
        }
    }

    private fun setAlarmList(alarmList: List<AlarmUiModel>) {
        viewModelScope.launch {
            _allAlarmList.emit(alarmList)
        }
    }

    private fun deleteAlarm() {
        viewModelScope.launch {
            deleteAlarmList.forEach { alarmUiModel ->
                alarmDataUseCases.deleteAlarmDataUseCase(alarmUiModel.toDomainModel()).onSuccess {

                }.onFailure {
                    handleError(it)
                }
            }
        }
    }
}