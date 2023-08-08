package com.grusie.miraclealarm.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.model.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository
    private val alarmTimeDao: AlarmTimeDao

    val allAlarms: LiveData<MutableList<AlarmData>>

    private val _alarm = MutableLiveData<AlarmData?>()
    private val _flagHoliday = MutableLiveData<Boolean>()                //공휴일 스위치 값
    private val _flagSound = MutableLiveData<Boolean>()                  //소리 스위치 값
    private val _flagVibe = MutableLiveData<Boolean>()                   //진동 스위치 값
    private val _flagOffWay = MutableLiveData<Boolean>()                 //끄는 방법 스위치 값
    private val _flagDelay = MutableLiveData<Boolean>()                 //미루기 스위치 값
    private val _time = MutableLiveData<String>()                        //알람 시간 값
    private val _dateList = MutableLiveData<MutableSet<String>>()        //날짜 리스트 값
    private val _date = MutableLiveData<String>()                        //날짜 값
    private val _sound = MutableLiveData<String>()                       //알람 소리 값
    private val _volume = MutableLiveData<Int>()                         //알람 볼륨 값
    private val _vibrate = MutableLiveData<String>()                     //알람 진동 값
    private val _offWay = MutableLiveData<String>()                      //알람 끄는 방법 값
    private val _delay = MutableLiveData<String>()                       //알람 미루기 값
    private val _modifyMode = MutableLiveData<Boolean>()                 //수정 모드 플래그
    private val _modifyList = MutableLiveData<MutableSet<AlarmData>>()   //수정 알람 데이터 리스트 값
    private val _clearAlarm = MutableLiveData<AlarmData>()               //제거할 알람 값

    val alarm: LiveData<AlarmData?> = _alarm
    val flagHoliday: LiveData<Boolean> = _flagHoliday
    val flagSound: LiveData<Boolean> = _flagSound
    val flagVibe: LiveData<Boolean> = _flagVibe
    val flagOffWay: LiveData<Boolean> = _flagOffWay
    val flagDelay: LiveData<Boolean> = _flagDelay
    val time: LiveData<String> = _time
    val dateList: LiveData<MutableSet<String>> = _dateList
    val date: LiveData<String> = _date
    val sound: LiveData<String> = _sound
    val volume: LiveData<Int> = _volume
    val vibrate: LiveData<String> = _vibrate
    val offWay: LiveData<String> = _offWay
    val delay: LiveData<String> = _delay
    val modifyMode: LiveData<Boolean> = _modifyMode
    val modifyList: LiveData<MutableSet<AlarmData>> = _modifyList
    val clearAlarm: LiveData<AlarmData> = _clearAlarm
    private val daysOfWeek: Array<String>
    private val dayOfWeekMap: Map<String, Int>
    private val question = MutableLiveData<String>()
    private val answer = MutableLiveData<Int>()
    val minAlarmTime : LiveData<AlarmTimeData>

    /**
     * 초기화 작업
     * **/
    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        alarmTimeDao = AlarmDatabase.getDatabase(application).alarmTimeDao()
        repository = AlarmRepository(alarmDao)

        allAlarms = repository.allAlarms
        minAlarmTime = alarmTimeDao.getMinAlarmTime()
        _modifyMode.value = false
        _modifyList.value = mutableSetOf()
        daysOfWeek = arrayOf(
            getString(R.string.string_sunday),
            getString(R.string.string_monday),
            getString(R.string.string_tuesday),
            getString(R.string.string_wednesday),
            getString(R.string.string_thursday),
            getString(R.string.string_friday),
            getString(R.string.string_saturday)
        )
        dayOfWeekMap = mapOf(
            getString(R.string.string_sunday) to 1,
            getString(R.string.string_monday) to 2,
            getString(R.string.string_tuesday) to 3,
            getString(R.string.string_wednesday) to 4,
            getString(R.string.string_thursday) to 5,
            getString(R.string.string_friday) to 6,
            getString(R.string.string_saturday) to 7
        )
        logLine("confirm instance", "인스턴스 생성 완료")
    }

    private fun getString(id: Int): String {
        return getApplication<Application>().getString(id)
    }


    /**
     * 뷰모델 알람 초기화
     * **/
    fun initAlarmData(alarmId: Int) {
        logLine("confirm init AlarmData", "initalarmdata테스트")
        viewModelScope.launch {
            if (alarmId != -1) {
                _alarm.value = getAlarmById(alarmId)
            } else {
                _alarm.value = AlarmData()
            }
            initEmptyAlarmData()
        }
    }

    /**
     * 뷰모델 알람 초기화
     * **/
    fun initAlarmData(alarm: AlarmData) {
        logLine("confirm init AlarmData", "initalarmdata테스트")
        this@AlarmViewModel._alarm.value = alarm
        initEmptyAlarmData()
    }


    /**
     * 뷰모델 알람 초기화
     * **/
    private fun initEmptyAlarmData() {
        this.alarm.value?.apply {
            this@AlarmViewModel._flagHoliday.value = holiday
            this@AlarmViewModel._flagVibe.value = flagVibrate
            this@AlarmViewModel._flagSound.value = flagSound
            this@AlarmViewModel._flagDelay.value = flagDelay
            this@AlarmViewModel._flagOffWay.value = flagOffWay
            this@AlarmViewModel._sound.value = sound
            this@AlarmViewModel._volume.value = volume
            this@AlarmViewModel._offWay.value = off_way
            this@AlarmViewModel._vibrate.value = vibrate
            this@AlarmViewModel._delay.value = delay
            this@AlarmViewModel._time.value = time
            this@AlarmViewModel._dateList.value =
                if (date.isNotEmpty()) date.split(",").toMutableSet() else mutableSetOf()
            this@AlarmViewModel._date.value = dateList.value!!.joinToString(",")     //초기 알람 날짜 값
        }
    }

    /**
     * AlarmRepository CRUD
     * **/
    private suspend fun insert(alarm: AlarmData): Long {
        logLine("confirm insert", "$alarm")
        return repository.insert(alarm)
    }

    private suspend fun update(alarm: AlarmData) {
        logLine("confirm update", "$alarm")
        repository.update(alarm)
    }

    fun delete(alarm: AlarmData) = viewModelScope.launch {
        logLine("confirm delete", "$alarm")
        repository.delete(alarm)
    }

    /**
     * AlarmTimeDAO CRUD
     * **/
    private suspend fun insertAlarmTime(alarmTime: AlarmTimeData){
        logLine("confirm insert", "$alarmTime")
        alarmTimeDao.insert(alarmTime)
    }
    private fun deleteAlarmTime(alarmTime: AlarmTimeData) = viewModelScope.launch {
        logLine("confirm delete", "$alarmTime")
        alarmTimeDao.delete(alarmTime)
    }


    fun updateAlarmTimeData(flag: Int ,alarmTimeData: AlarmTimeData){
        viewModelScope.launch {
            when(flag) {
                Const.INSERT_ALARM_TIME -> insertAlarmTime(alarmTimeData)
                Const.DELETE_ALARM_TIME -> deleteAlarmTime(alarmTimeData)
            }
        }
    }



    /**
     * 해당 알람 객체 가져오기
     * **/
    private suspend fun getAlarmById(id: Int): AlarmData {
        return withContext(Dispatchers.IO) { repository.getAlarmById(id) }
    }

    /**
     * 스위치 이벤트
     * **/
    fun onSwSoundClicked() {
        _flagSound.value = !flagSound.value!!
        logLine("confirm sound", "${flagSound.value}")
    }

    fun onSwVibeClicked() {
        _flagVibe.value = !flagVibe.value!!
    }

    fun onSwOffWayClicked() {
        _flagOffWay.value = !flagOffWay.value!!
    }

    fun onSwDelayClicked() {
        _flagDelay.value = !flagDelay.value!!
    }

    fun onSwHolidayClicked() {
        _flagHoliday.value = !flagHoliday.value!!
    }

    fun onAlarmFlagClicked(alarm: AlarmData) {
        alarm.enabled = !alarm.enabled
        _clearAlarm.value = alarm
        viewModelScope.launch { update(alarm) }
        logLine("flag confirm", "${alarm.enabled}")
    }

    fun changeAlarmDate(alarm: AlarmData, date: String, enabled: Boolean) {
        alarm.date = date
        alarm.enabled = enabled
        viewModelScope.launch { update(alarm) }
    }


    /**
     * 날짜 및 요일 선택 이벤트
     * **/
    fun onDateClicked(dateItem: String, isRepeat: Boolean) {
        if (isRepeat) {
            if (alarm.value?.dateRepeat == false) {
                _dateList.value = mutableSetOf(dateItem)
            } else {
                if (dateList.value?.contains(dateItem) == false)
                    dateList.value?.add(dateItem)
                else dateList.value?.remove(dateItem)

                sortDate()
            }
            if (dateList.value?.size!! <= 0) alarm.value?.dateRepeat = false
        } else {
            _dateList.value = mutableSetOf(dateItem)
            logLine("confirm date2", "${dateList.value}")
        }
        alarm.value?.dateRepeat = isRepeat
        _date.value = dateList.value!!.joinToString(",")
        logLine("confirm date", "${dateList.value}, $dateItem, $isRepeat")
    }

    fun timePickerToTime(hour: Int, minute: Int) {
        val timeString: String
        val amPm = if (hour >= 12) "오후" else "오전"
        val displayHour = if (hour > 12) hour - 12 else hour

        timeString = String.format("$amPm %02d:%02d", displayHour, minute)
        _time.value = timeString
        logLine("confirm time - timePickerToTime", "${time.value}")
    }

    /**
     * 알람 삽입 or 업데이트
     * **/
    suspend fun updateAlarmData(): AlarmData {
        val deferred = CompletableDeferred<AlarmData>()
        viewModelScope.launch {
            val alarmId = if (alarm.value?.id == 0) {
                alarm.value?.let { insert(it) } ?: 0
            } else {
                alarm.value?.let { update(it) }
                alarm.value?.id
            }

            _alarm.value = getAlarmById(alarmId!!.toInt())

            alarm.value?.apply {
                logLine(
                    "confirm viewModelVariable",
                    "alarmId = ${alarm.value?.id}, time = ${time}, title = ${title}, date = ${date}, dateRepeat = ${dateRepeat}, holiday = ${holiday}, sound = ${sound}, volume = ${volume} vibrate = ${vibrate}, offWay = ${off_way}, delay = ${delay}"
                )
            }
            deferred.complete(alarm.value!!) // 알람 데이터를 complete() 메서드로 설정
        }

        return deferred.await()
    }

    /**
     * 요일 정렬
     * **/
    private fun sortDate() {
        _dateList.value = dateList.value?.sortedBy { dayOfWeekMap[it] }?.toMutableSet()
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

    /**
     * 로그
     * **/
    fun logLine(tag: String, log: String) {
        if (log.length > 1500) {
            Log.d(tag, log.substring(0, 1500))
            logLine(tag, log.substring(1500))
        } else
            Log.d(tag, log)
    }


    /**
     * 날짜 포맷
     * **/
    fun dateFormat(cal: Calendar): String {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        logLine("confirm dateFormat", "$dayOfWeek, ${daysOfWeek[dayOfWeek - 1]}")
        if (year == Calendar.getInstance().get(Calendar.YEAR)) {
            return String.format(
                "%02d월 %02d일 (%s)",
                month + 1,
                day,
                daysOfWeek[dayOfWeek - 1]
            )
        } else {
            return String.format(
                "%04d년 %02d월 %02d일 (%s)",
                year,
                month + 1,
                day,
                daysOfWeek[dayOfWeek - 1]
            )
        }
    }

    /**
     * 알람 수정 리스트 추가 삭제
     * **/
    fun onCheckAlarmList(alarm: AlarmData, isChecked: Boolean) {
        if (isChecked) modifyList.value?.add(alarm)
        else modifyList.value?.remove(alarm)
        logLine("confirm modifyList2", "${modifyList.value}, $alarm, $isChecked")
    }


    /**
     * 각 값들 변경
     **/
    fun changeSound(sound: String) {
        this._sound.value = sound
        logLine("confirm sound", "${this.sound.value}, $sound")
    }

    fun changeVolume(volume: Int) {
        this._volume.value = volume
        logLine("confirm volume", "${this.volume.value}, $volume")
    }

    fun changeVibration(vibration: String) {
        this._vibrate.value = vibration
        logLine("confirm vibration", "${this.vibrate.value}, $vibration")
    }

    fun changeModifyMode() {
        this._modifyMode.value = !this._modifyMode.value!!
    }

/*    fun createOffWay(){
        val a = (1..100).random()
        val b = (1..100).random()
        a + b
    }*/

    override fun onCleared() {
        super.onCleared()
        logLine("confirm lifecycle", "confirm onCleared")
    }
}
