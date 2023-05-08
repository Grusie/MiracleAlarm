package com.example.miraclealarm

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository

    val allAlarms: LiveData<MutableList<AlarmData>>

    val alarm = MutableLiveData<AlarmData?>()
    val flagHoliday = MutableLiveData<Boolean>()
    val flagSound = MutableLiveData<Boolean>()
    val flagVibe = MutableLiveData<Boolean>()
    val flagOffWay = MutableLiveData<Boolean>()
    val flagRepeat = MutableLiveData<Boolean>()
    val time = MutableLiveData<String>()
    val date = MutableLiveData<MutableList<String>>()
    val dayOfWeekMap = mapOf(
        "월" to 1,
        "화" to 2,
        "수" to 3,
        "목" to 4,
        "금" to 5,
        "토" to 6,
        "일" to 7
    )
    var instanceFlag = false

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
        logLine("confirm instance", "인스턴스 생성 완료")
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

    suspend fun getAlarmById(id: Int): AlarmData {
        return withContext(Dispatchers.IO) {repository.getAlarmById(id)}
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

    fun onSwDateClicked(dateItem: String) {
        if(date.value?.contains(dateItem) == false)
            date.value?.add(dateItem)
        else date.value?.remove(dateItem)
        logLine("confirm date", "${date.value}, $dateItem")
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
        time.value = timeString
        logLine("confirm time - timePickerToTime", "${time.value}")
    }

    fun initAlarmData(alarmId: Int) {
        instanceFlag = true
        logLine("confirm init AlarmData", "initalarmdata테스트")
        viewModelScope.launch {
            if (alarmId != -1) {
                alarm.value = getAlarmById(alarmId)
            } else {
                alarm.value = AlarmData()
            }
            initEmptyAlarmData()
        }
    }

    fun initEmptyAlarmData(){
        this.alarm.value?.apply {
            this@AlarmViewModel.flagHoliday.value = holiday
            this@AlarmViewModel.flagVibe.value = flagVibrate
            this@AlarmViewModel.flagSound.value = flagSound
            this@AlarmViewModel.flagRepeat.value = flagRepeat
            this@AlarmViewModel.flagOffWay.value = flagOffWay
            this@AlarmViewModel.time.value = time
            this@AlarmViewModel.date.value = if(date.isNotEmpty()) date.split(",").toMutableList() else mutableListOf()
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

    fun sortDate(): String? {
        val sortedDate = date.value?.sortedBy { dayOfWeekMap[it] }?.joinToString(",")
        logLine("confirm date", "$sortedDate")
        return sortedDate
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
        logLine("confirm lifecycle", "confirm onCleared")
    }
}
