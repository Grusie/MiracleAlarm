package com.example.miraclealarm

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository

    val allAlarms: LiveData<MutableList<AlarmData>>

    val alarm = MutableLiveData<AlarmData>()
    val flagHoliday = MutableLiveData<Boolean>()
    val flagSound = MutableLiveData<Boolean>()
    val flagVibe = MutableLiveData<Boolean>()
    val flagOffWay = MutableLiveData<Boolean>()
    val flagRepeat = MutableLiveData<Boolean>()
    private val alarmObserver = Observer<AlarmData> { alarm ->
        alarm.let {
            this.alarm.value = alarm
            logLine("initAlarmData", "${this.alarm.value}")

            alarm.apply {
                this@AlarmViewModel.flagHoliday.value = holiday
                this@AlarmViewModel.flagVibe.value = flagVibrate
                this@AlarmViewModel.flagSound.value = flagSound
                this@AlarmViewModel.flagRepeat.value = flagRepeat
                this@AlarmViewModel.flagOffWay.value = flagOffWay
            }
        }
    }

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
    }

    fun insert(alarm: AlarmData) = viewModelScope.launch {
        logLine("confirm insert", "$alarm")
        repository.insert(alarm)
    }

    fun update(alarm: AlarmData) = viewModelScope.launch {
        logLine("confirm update", "$alarm")
        repository.update(alarm)
    }

    fun delete(alarm: AlarmData) = viewModelScope.launch {
        logLine("confirm delete", "$alarm")
        repository.delete(alarm)
    }

    fun getAlarmById(id: Int): LiveData<AlarmData> {
        return repository.getAlarmById(id)
    }

    fun onSwSoundClicked() {
        flagSound.value = !flagSound.value!!
        logLine("confirm sound", "${flagSound.value}")
    }

    fun onSwVibeClicked() {
        flagVibe.value = !flagVibe.value!!
    }

    fun onSwOffWayClicked() {
        flagOffWay.value = !flagOffWay.value!!
    }

    fun onSwRepeatClicked() {
        flagRepeat.value = !flagRepeat.value!!
    }

    fun onSwHolidayClicked() {
        flagHoliday.value = !flagHoliday.value!!
    }

    fun onAlarmFlagClicked(alarm: AlarmData) {
        alarm.enabled = !alarm.enabled
        update(alarm)
        logLine("flag confirm", "${alarm.enabled}")
    }

    fun timePickerToTime(hour: Int, minute: Int) {
        var timeString: String
        val amPm = if (hour >= 12) "오후" else "오전"
        val displayHour = if (hour > 12) hour - 12 else hour

        timeString = String.format("$amPm %02d:%02d", displayHour, minute)
        alarm.value?.time = timeString
        logLine("time.value", "${alarm.value?.time}")
    }

    fun initAlarmData(alarmId: Int) {
        if (alarmId != -1) getAlarmById(alarmId).observeForever(alarmObserver)
        else {
            this.alarm.value = AlarmData().apply {
                this@AlarmViewModel.flagHoliday.value = holiday
                this@AlarmViewModel.flagVibe.value = flagVibrate
                this@AlarmViewModel.flagSound.value = flagSound
                this@AlarmViewModel.flagRepeat.value = flagRepeat
                this@AlarmViewModel.flagOffWay.value = flagOffWay
            }
        }
    }

    fun updateAlarmData() {
        if (alarm.value?.id == 0) {
            alarm.value?.let { insert(it) }
        } else {
            alarm.value?.let { update(it) }
        }

        alarm.value?.apply {
            logLine(
                "viewModelVariable",
                "alarmId = ${alarm.value?.id}, _time = ${time}, _title = ${title}, _holiday = ${holiday}, _sound = ${sound}, _vibrate = ${vibrate}, _offWay = ${off_way}, _repeat = ${repeat}"
            )
        }
    }

    fun logLine(tag: String, log: String) {
        if (log.length > 1500) {
            Log.d(tag, log.substring(0, 1500))
            logLine(tag, log.substring(1500))
        } else
            Log.d(tag, log)
    }

    override fun onCleared() {
        super.onCleared()
        alarm.removeObserver(alarmObserver)
    }
}
