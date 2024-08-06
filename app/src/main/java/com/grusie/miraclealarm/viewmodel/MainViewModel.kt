package com.grusie.miraclealarm.viewmodel

import androidx.lifecycle.viewModelScope
import com.grusie.domain.usecase.alarmdata.AlarmDataUseCases
import com.grusie.domain.usecase.alarmtime.AlarmTimeUseCases
import com.grusie.miraclealarm.mapper.toDomainModel
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.data.AlarmTimeUiModel
import com.grusie.miraclealarm.model.data.AlarmUiModel
import com.grusie.miraclealarm.uistate.BaseEventState
import com.grusie.miraclealarm.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmDataUseCases: AlarmDataUseCases,
    private val alarmTimeUseCases: AlarmTimeUseCases
) : BaseViewModel() {
    private val _allAlarmList: MutableStateFlow<List<AlarmUiModel>> = MutableStateFlow(emptyList())
    private val _isDeleteMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _minAlarmTimeData: MutableStateFlow<AlarmTimeUiModel?> = MutableStateFlow(null)
    private val _selectedAlarmData: MutableStateFlow<AlarmUiModel?> = MutableStateFlow(null)
    private val _deleteAlarmTimeList: MutableStateFlow<List<AlarmTimeUiModel>> = MutableStateFlow(
        emptyList()
    )
    val allAlarmList: StateFlow<List<AlarmUiModel>> get() = _allAlarmList
    val isDeleteMode: StateFlow<Boolean> get() = _isDeleteMode
    val minAlarmTimeData: StateFlow<AlarmTimeUiModel?> get() = _minAlarmTimeData
    val selectedAlarmData: StateFlow<AlarmUiModel?> = _selectedAlarmData
    val deleteAlarmTimeList: StateFlow<List<AlarmTimeUiModel>> = _deleteAlarmTimeList

    var deleteAlarmList: MutableList<AlarmUiModel> = mutableListOf()
        private set

    fun getAllAlarmList() {
        viewModelScope.launch {
            alarmDataUseCases.getAllAlarmListUseCases().onSuccess { list ->
                val alarmUiModelList = list.map { it.toUiModel() }
                alarmUiModelList.forEach { alarmUiModel ->
                    checkPastDate(alarmUiModel, false)
                }
                setAlarmList(sortAlarm(alarmUiModelList.toMutableList()))
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

    fun changeDeleteMode(isDeleteMode: Boolean? = null) {
        viewModelScope.launch {
            if (isDeleteMode != null) {
                _isDeleteMode.emit(isDeleteMode)
            } else {
                _isDeleteMode.emit(!_isDeleteMode.value)
            }
            if (!_isDeleteMode.value) {
                clearDeleteAlarmList()
            }
        }
    }

    fun deleteAlarm() {
        viewModelScope.launch {
            var allSuccess = true
            deleteAlarmList.forEach { alarmUiModel ->
                val result = async {
                    alarmTimeUseCases.getAlarmTimesByAlarmIdUseCase(alarmUiModel.id!!)
                        .onSuccess { list ->
                            setClearAlarmTimeList(list.map { it.toUiModel() })
                        }
                    alarmDataUseCases.deleteAlarmDataUseCase(alarmUiModel.toDomainModel())
                }

                if (result.await().isFailure) {
                    allSuccess = false
                }
            }
            if (allSuccess) {
                clearDeleteAlarmList()
                clearDeleteAlarmTimeList()
                setEventState(BaseEventState.Alert(MSG_TYPE_SUCCESS_DELETE))
            }
        }
    }

    fun clearDeleteAlarmList() {
        deleteAlarmList.clear()
    }

    suspend fun changeAlarmEnable(alarmUiModel: AlarmUiModel) {
        val replaceAlarm = alarmUiModel.copy(enabled = !alarmUiModel.enabled)
        val tempAlarmList = _allAlarmList.value.toMutableList()
        val index = tempAlarmList.indexOf(alarmUiModel)
        tempAlarmList[index] = replaceAlarm

        setAlarmList(tempAlarmList)
        _selectedAlarmData.emit(replaceAlarm)
    }

    fun getMinAlarmTime() {
        viewModelScope.launch {
            alarmTimeUseCases.getMinAlarmTimeUseCase().onSuccess { alarmTimeDomainModel ->
                alarmTimeDomainModel?.toUiModel()?.let {
                    _minAlarmTimeData.emit(it)
                }
            }.onFailure {
                handleError(it)
            }
        }
    }

    fun insertAlarmTime(alarmTime: AlarmTimeUiModel) = viewModelScope.launch {
        alarmTimeUseCases.insertAlarmTime(alarmTime.toDomainModel())
    }

    fun changeDeleteChecked(alarmUiModel: AlarmUiModel) {
        val replaceAlarm = alarmUiModel.copy(isChecked = !alarmUiModel.isChecked)
        val tempAlarmList = _allAlarmList.value.toMutableList()
        val index = tempAlarmList.indexOf(alarmUiModel)
        tempAlarmList[index] = replaceAlarm
        setAlarmList(tempAlarmList)
    }

    private fun getAlarmTimes(alarmUiModel: AlarmUiModel): List<AlarmTimeUiModel>? {
        var result: List<AlarmTimeUiModel>? = null
        viewModelScope.launch {
            alarmUiModel.id?.let {
                alarmTimeUseCases.getAlarmTimesByAlarmIdUseCase(alarmId = alarmUiModel.id)
                    .onSuccess { list ->
                        result = list.map { it.toUiModel() }
                    }.onFailure {
                        result = null
                    }
            }
        }
        return result
    }


    /**
     * 지난 알람 날짜 하루 추가
     **/
    fun checkPastDate(alarmUiModel: AlarmUiModel, enabled: Boolean) {
        if (!alarmUiModel.dateRepeat) {
            var alarmCal = Utils.dateToCal(alarmUiModel.date, alarmUiModel.time)
            val currentCal = Calendar.getInstance()
            if (currentCal > alarmCal && getAlarmTimes(alarmUiModel).isNullOrEmpty()) {
                alarmCal = currentCal
                alarmCal.add(Calendar.DAY_OF_YEAR, 1)

                setAlarmData(
                    alarmUiModel.copy(
                        date = Utils.calendarToString(alarmCal),
                        enabled = enabled
                    )
                )
            }
        }
    }


    private fun setAlarmData(alarmUiModel: AlarmUiModel) {
        viewModelScope.launch {
            alarmDataUseCases.insertAlarmDataUseCase(alarmUiModel.toDomainModel())
            getAllAlarmList()
        }
    }

    fun deleteAlarmTimeListByAlarmId(alarmUiModel: AlarmUiModel) {
        viewModelScope.launch {
            alarmUiModel.id?.let { alarmId ->
                alarmTimeUseCases.getAlarmTimesByAlarmIdUseCase(alarmId).onSuccess { list ->
                    setClearAlarmTimeList(list.map { it.toUiModel() })
                }
            }
        }
    }

    private fun setClearAlarmTimeList(deleteAlarmTimeList: List<AlarmTimeUiModel>) {
        viewModelScope.launch {
            _deleteAlarmTimeList.emit(deleteAlarmTimeList)
        }
    }

    fun clearDeleteAlarmTimeList() {
        viewModelScope.launch {
            _deleteAlarmTimeList.emit(emptyList())
        }
    }


    /**
     * 알람 정렬
     * @param alarmList
     */
    private fun sortAlarm(alarmList: MutableList<AlarmUiModel>): List<AlarmUiModel> {
        return alarmList.sortedWith(compareByDescending<AlarmUiModel> { it.enabled }.thenBy { alarm ->
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