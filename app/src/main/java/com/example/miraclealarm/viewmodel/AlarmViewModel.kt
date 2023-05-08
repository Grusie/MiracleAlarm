package com.example.miraclealarm.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.miraclealarm.model.AlarmData
import com.example.miraclealarm.model.AlarmDatabase
import com.example.miraclealarm.model.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository

    val allAlarms: LiveData<MutableList<AlarmData>>

    val alarm = MutableLiveData<AlarmData?>()
    val flagHoliday = MutableLiveData<Boolean>()            //공휴일 스위치 값
    val flagSound = MutableLiveData<Boolean>()              //소리 스위치 값
    val flagVibe = MutableLiveData<Boolean>()               //진동 스위치 값
    val flagOffWay = MutableLiveData<Boolean>()             //끄는 방법 스위치 값
    val flagRepeat = MutableLiveData<Boolean>()             //반복 스위치 값
    val time = MutableLiveData<String>()                    //알람 시간 값
    val dateList = MutableLiveData<MutableList<String>>()   //날짜 리스트 값
    val date = MutableLiveData<String>()                    //날짜 값

    val dayOfWeekMap = mapOf(
        "월" to 1,
        "화" to 2,
        "수" to 3,
        "목" to 4,
        "금" to 5,
        "토" to 6,
        "일" to 7
    )

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

    fun onDateClicked(dateItem: String, isRepeat: Boolean) {
        if(isRepeat) {
            if (alarm.value?.dateRepeat == false) {
                dateList.value = mutableListOf(dateItem)
            } else {
                if (dateList.value?.contains(dateItem) == false)
                    dateList.value?.add(dateItem)
                else dateList.value?.remove(dateItem)

                sortDate()
            }
            if(dateList.value?.size!! <= 0) alarm.value?.dateRepeat = false
        } else {
            dateList.value = mutableListOf(dateItem)
            logLine("confirm date2", "${dateList.value}")
        }
        alarm.value?.dateRepeat = isRepeat
        date.value = dateList.value!!.joinToString(",")
        logLine("confirm date", "${dateList.value}, $dateItem, $isRepeat")
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
            this@AlarmViewModel.dateList.value = if(date.isNotEmpty()) date.split(",").toMutableList() else mutableListOf()
            this@AlarmViewModel.date.value = dateList.value!!.joinToString(",")     //초기 알람 날짜 값
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

    fun sortDate() {
        dateList.value?.sortBy { dayOfWeekMap[it] }
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
