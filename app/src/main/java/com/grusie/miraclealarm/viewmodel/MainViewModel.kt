package com.grusie.miraclealarm.viewmodel

import androidx.lifecycle.viewModelScope
import com.grusie.domain.usecase.alarmdata.AlarmDataUseCases
import com.grusie.domain.usecase.alarmtime.AlarmTimeUseCases
import com.grusie.miraclealarm.mapper.toDomainModel
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.model.data.AlarmTimeData
import com.grusie.miraclealarm.model.data.AlarmUiModel
import com.grusie.miraclealarm.uistate.BaseEventState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
    private val _isDeleteMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _minAlarmTimeData: MutableStateFlow<AlarmTimeData?> = MutableStateFlow(null)
    val allAlarmList: StateFlow<List<AlarmUiModel>> get() = _allAlarmList
    val isDeleteMode: StateFlow<Boolean> get() = _isDeleteMode
    val minAlarmTimeData: StateFlow<AlarmTimeData?> get() = _minAlarmTimeData

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

    fun changeModifyMode(modifyMode: Boolean){
        viewModelScope.launch {
            _isDeleteMode.emit(modifyMode)
            if(!modifyMode) {
                clearDeleteAlarmList()
            }
        }
    }

    fun deleteAlarm() {
        viewModelScope.launch {
            var allSuccess = true
            deleteAlarmList.forEach {alarmUiModel ->
                val result = async {
                    alarmDataUseCases.deleteAlarmDataUseCase(alarmUiModel.toDomainModel())
                }

                if(result.await().isFailure) {
                    allSuccess = false
                }
            }
            if(allSuccess) setEventState(BaseEventState.Alert(MSG_TYPE_SUCCESS_DELETE))
        }
    }

    private fun clearDeleteAlarmList(){
        deleteAlarmList.clear()
    }

    fun setDeleteAlarmList(deleteAlarmList: List<AlarmUiModel>){
        this.deleteAlarmList.addAll(deleteAlarmList)
    }

    fun getMinAlarmTime() {
        viewModelScope.launch {
            alarmTimeUseCases.getMinAlarmTimeUseCase().onSuccess {
                it
            }.onFailure {

            }
        }
    }

    /**
     * 알람 정렬
     * @param alarmList
     */
    fun sortAlarm(alarmList: MutableList<AlarmData>) {
        alarmList.sortWith(compareByDescending<AlarmData> { it.enabled }.thenBy { alarm ->
            val timeString = alarm.time
            val isAfterNoon = timeString.startsWith("오후")
            val timeWithoutSuffix = timeString.substringAfter(' ')
            val (hour, minute) = timeWithoutSuffix.split(':').map { it.toInt() }

            val convertedHour = if (isAfterNoon && hour != 12) hour + 12 else hour
            convertedHour * 60 + minute
        })
    }

    companion object {
        const val MSG_TYPE_SUCCESS_DELETE = 0
    }
}