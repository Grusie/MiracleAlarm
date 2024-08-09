package com.grusie.miraclealarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grusie.domain.usecase.alarmdata.AlarmDataUseCases
import com.grusie.domain.usecase.alarmtime.AlarmTimeUseCases
import com.grusie.domain.usecase.turnoff.TurnOffWayUseCases
import com.grusie.miraclealarm.mapper.toDomainModel
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.data.AlarmTimeUiModel
import com.grusie.miraclealarm.model.data.AlarmTurnOffUiModel
import com.grusie.miraclealarm.model.data.AlarmUiModel
import com.grusie.miraclealarm.model.data.DayUiModel
import com.grusie.miraclealarm.uistate.BaseEventState
import com.grusie.miraclealarm.uistate.BaseUiState
import com.grusie.miraclealarm.util.timePickerToTimeString
import com.grusie.miraclealarm.util.toDateFormat
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

class CreateAlarmViewModel @AssistedInject constructor(
    private val alarmTimeUseCases: AlarmTimeUseCases,
    private val alarmDataUseCases: AlarmDataUseCases,
    private val turnOffWayUseCases: TurnOffWayUseCases,
    @Assisted private val alarmId: Long
) : BaseViewModel() {
    @AssistedFactory
    interface CreateAlarmViewModelFactory {
        fun create(alarmId: Long): CreateAlarmViewModel
    }

    private val _alarmData = MutableStateFlow(AlarmUiModel())
    private val _turnOffWay = MutableStateFlow(AlarmTurnOffUiModel())
    private val _dayList = MutableStateFlow(
        listOf(
            DayUiModel("일"),
            DayUiModel("월"),
            DayUiModel("화"),
            DayUiModel("수"),
            DayUiModel("목"),
            DayUiModel("금"),
            DayUiModel("토")
        )
    )

    var oldAlarm: AlarmUiModel? = null
        private set
    val alarmData: StateFlow<AlarmUiModel> get() = _alarmData
    val turnOffWay: StateFlow<AlarmTurnOffUiModel> get() = _turnOffWay
    val dayList: StateFlow<List<DayUiModel>> get() = _dayList
    var alarmCal: Calendar = Calendar.getInstance()
        private set

    init {
        setInitAlarmData()
    }

    /**
     * 아이디로 알람 데이터 조회
     **/
    private fun getAlarmDataById() {
        viewModelScope.launch {
            setUiState(BaseUiState.Loading)
            alarmDataUseCases.getAlarmByIdUseCase(alarmId).onSuccess {
                val alarm = it.toUiModel()
                oldAlarm = alarm
                setAlarmData(it.toUiModel())
                setUiState(BaseUiState.Init)
            }.onFailure {
                handleError(it)
            }
        }
    }

    /**
     * 아이디로 알람 끄는 방법 조회
     **/
    private fun turnOffWayById() {
        viewModelScope.launch {
            setUiState(BaseUiState.Loading)
            turnOffWayUseCases.getOffWayByIdUseCase(alarmId).onSuccess {
                if (it != null) _turnOffWay.emit(it.toUiModel())
                setUiState(BaseUiState.Init)
            }.onFailure {
                handleError(it)
            }
        }
    }

    /**
     * 초기 알람 세팅(값이 있는 경우)
     **/
    private fun setInitAlarmData() {
        if (alarmId != -1L) {
            getAlarmDataById()
            turnOffWayById()
        } else {
            val dateTime = LocalDateTime.now().plusMinutes(1)
            changeTime(timePickerToTimeString(dateTime.hour, dateTime.minute))
            changeDate(alarmCal.toDateFormat(), false)
        }
    }


    /**
     * 요일 선택
     **/
    fun changeDaySelected(dayUiModel: DayUiModel) {
        viewModelScope.launch {
            val tempDayList = _dayList.value.toMutableList()
            val index = tempDayList.indexOf(dayUiModel)
            if (index != -1) {
                tempDayList[index] =
                    tempDayList[index].copy(isSelected = !tempDayList[index].isSelected)
            }

            _dayList.emit(tempDayList)
            changeDate(date = _dayList.value.filter { it.isSelected }.joinToString(","), true)
        }
    }

    fun createAlarm() {
        viewModelScope.launch {
            setUiState(BaseUiState.Loading)
                if (alarmId != -1L) {
                    alarmTimeUseCases.deleteByAlarmIdUseCase(alarmId)
                }

                alarmDataUseCases.insertAlarmDataUseCase(_alarmData.value.toDomainModel())
                    .onSuccess {
                        setUiState(BaseUiState.Success)
                    }.onFailure {
                        handleError(it)
                    }
        }
    }

    fun confirmPastTime() {
        viewModelScope.launch {
            if (!_alarmData.value.dateRepeat && alarmCal < Calendar.getInstance()) {
                setEventState(BaseEventState.Alert(MSG_TYPE_DENIED_PAST_TIME))
            }
        }
    }

    suspend fun getAlarmTimesById(): List<AlarmTimeUiModel> {
        val result = alarmTimeUseCases.getAlarmTimesByAlarmIdUseCase(alarmId)

        result.fold(
            onSuccess =  { list ->
                return list.map { it.toUiModel() }
            },
            onFailure =  {
                return emptyList()
            })
    }

    /**
     * 알람 변경
     **/
    private fun setAlarmData(alarmData: AlarmUiModel) {
        viewModelScope.launch {
            _alarmData.emit(alarmData)
        }
    }

    /**
     * 끄는 방법 변경
     **/
    private fun setTurnOffWay(turnOffWay: AlarmTurnOffUiModel) {
        viewModelScope.launch {
            _turnOffWay.emit(turnOffWay)
        }
    }

    /**
     * 알람 시간 변경
     **/
    fun changeTime(time: String) {
        setAlarmData(_alarmData.value.copy(time = time))
    }

    /**
     * 알람 날짜 변경
     **/
    fun changeDate(date: String, changeDateRepeat: Boolean) {
        viewModelScope.launch {
            if (!changeDateRepeat) {
                _dayList.emit(_dayList.value.map { it.copy(isSelected = false) })
            }
            setAlarmData(_alarmData.value.copy(date = date))
            changeDateRepeat(changeDateRepeat)
        }
    }

    fun changeDateRepeat(dateRepeat: Boolean) {
        setAlarmData(_alarmData.value.copy(dateRepeat = dateRepeat))
    }

    /**
     * 알람 소리 변경
     **/
    fun changeSound(sound: String) {
        setAlarmData(_alarmData.value.copy(sound = sound))
    }

    /**
     * 알람 볼륨 변경
     **/
    fun changeVolume(volume: Int) {
        setAlarmData(_alarmData.value.copy(volume = volume))
    }

    /**
     * 알람 진동 변경
     **/
    fun changeVibration(vibration: String) {
        setAlarmData(_alarmData.value.copy(vibrate = vibration))
    }

    /**
     * 알람 소리 사용여부 변경
     **/
    fun changeSoundFlag(isSoundEnabled: Boolean) {
        setAlarmData(_alarmData.value.copy(flagSound = isSoundEnabled))
    }

    /**
     * 알람 진동 사용여부 변경
     **/
    fun changeVibrateFlag(isVibrationEnabled: Boolean) {
        setAlarmData(_alarmData.value.copy(flagVibrate = isVibrationEnabled))
    }

    /**
     * 알람 끄는 방법 사용여부 변경
     **/
    fun changeTurnOffWayFlag(isTurnOffWayEnabled: Boolean) {
        setAlarmData(_alarmData.value.copy(flagOffWay = isTurnOffWayEnabled))
    }

    fun changeDelayFlag(isDelayEnabled: Boolean) {
        setAlarmData(_alarmData.value.copy(flagDelay = isDelayEnabled))
    }

    /**
     * 알람 미루기 변경
     **/
    fun changeDelay(delay: String) {
        setAlarmData(_alarmData.value.copy(delay = delay))
    }

    /**
     * 알람 끄는 방법 변경
     **/
    fun changeOffWay(offWay: String, offWayCount: Int) {
        setTurnOffWay(_turnOffWay.value.copy(turnOffWay = offWay, count = offWayCount))
    }

    /**
     * 알람 제목 변경
     **/
    fun changeTitle(title: String) {
        setAlarmData(_alarmData.value.copy(title = title))
    }

    /**
     * 알람 캘린더 변경
     **/
    fun changeAlarmCal(alarmCal: Calendar) {
        this.alarmCal = alarmCal
    }

    companion object {
        fun provideFactory(
            createAlarmViewModelFactory: CreateAlarmViewModelFactory,
            alarmId: Long
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return createAlarmViewModelFactory.create(alarmId) as T
            }
        }

        const val MSG_TYPE_DENIED_PAST_TIME = 0
    }
}