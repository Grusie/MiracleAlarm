package com.grusie.miraclealarm.viewmodel

import android.Manifest
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository

    val allAlarms: LiveData<MutableList<AlarmData>>

    val alarm = MutableLiveData<AlarmData?>()
    val flagHoliday = MutableLiveData<Boolean>()                //공휴일 스위치 값
    val flagSound = MutableLiveData<Boolean>()                  //소리 스위치 값
    val flagVibe = MutableLiveData<Boolean>()                   //진동 스위치 값
    val flagOffWay = MutableLiveData<Boolean>()                 //끄는 방법 스위치 값
    val flagRepeat = MutableLiveData<Boolean>()                 //반복 스위치 값
    val time = MutableLiveData<String>()                        //알람 시간 값
    val dateList = MutableLiveData<MutableSet<String>>()        //날짜 리스트 값
    val date = MutableLiveData<String>()                        //날짜 값
    val sound = MutableLiveData<String>()                       //알람 소리 값
    val volume = MutableLiveData<Int>()                         //알람 볼륨 값
    val vibrate = MutableLiveData<String>()                     //알람 진동 값
    val offWay = MutableLiveData<String>()                      //알람 끄는 방법 값
    val repeat = MutableLiveData<String>()                      //알람 반복 값
    val modifyMode = MutableLiveData<Boolean>()                 //수정 모드 플래그
    val modifyList = MutableLiveData<MutableSet<AlarmData>>()   //수정 알람 데이터 리스트 값
    val clearAlarm = MutableLiveData<AlarmData>()               //제거할 알람 값
    private val daysOfWeek: Array<String>                       //요일 리스트 값
    private val dayOfWeekMap: Map<String, Int>                  //요일 리스트 맵
    private val question = MutableLiveData<String>()
    private val answer = MutableLiveData<Int>()

    /**
     * 초기화 작업
     * **/
    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
        modifyMode.value = false
        modifyList.value = mutableSetOf()
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

    fun getString(id: Int): String {
        return getApplication<Application>().getString(id)
    }


    /**
     * 뷰모델 알람 초기화
     * **/
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

    /**
     * 뷰모델 알람 초기화
     * **/
    fun initAlarmData(alarm: AlarmData) {
        logLine("confirm init AlarmData", "initalarmdata테스트")
        this@AlarmViewModel.alarm.value = alarm
        initEmptyAlarmData()
    }


    /**
     * 뷰모델 알람 초기화
     * **/
    private fun initEmptyAlarmData() {
        this.alarm.value?.apply {
            this@AlarmViewModel.flagHoliday.value = holiday
            this@AlarmViewModel.flagVibe.value = flagVibrate
            this@AlarmViewModel.flagSound.value = flagSound
            this@AlarmViewModel.flagRepeat.value = flagRepeat
            this@AlarmViewModel.flagOffWay.value = flagOffWay
            this@AlarmViewModel.sound.value = sound
            this@AlarmViewModel.volume.value = volume
            this@AlarmViewModel.offWay.value = off_way
            this@AlarmViewModel.vibrate.value = vibrate
            this@AlarmViewModel.repeat.value = repeat
            this@AlarmViewModel.time.value = time
            this@AlarmViewModel.dateList.value =
                if (date.isNotEmpty()) date.split(",").toMutableSet() else mutableSetOf()
            this@AlarmViewModel.date.value = dateList.value!!.joinToString(",")     //초기 알람 날짜 값
        }
    }

    /**
     * DAO CRUD
     * **/
    suspend fun insert(alarm: AlarmData): Long {
        logLine("confirm insert", "$alarm")
        return repository.insert(alarm)
    }

    suspend fun update(alarm: AlarmData) {
        logLine("confirm update", "$alarm")
        repository.update(alarm)
    }

    fun delete(alarm: AlarmData) = viewModelScope.launch {
        logLine("confirm delete", "$alarm")
        repository.delete(alarm)
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
        initAlarmData(alarm.id)
        alarm.enabled = !alarm.enabled
        clearAlarm.value = alarm
        viewModelScope.launch { update(alarm) }
        logLine("flag confirm", "${alarm.enabled}")
    }


    /**
     * 날짜 및 요일 선택 이벤트
     * **/
    fun onDateClicked(dateItem: String, isRepeat: Boolean) {
        if (isRepeat) {
            if (alarm.value?.dateRepeat == false) {
                dateList.value = mutableSetOf(dateItem)
            } else {
                if (dateList.value?.contains(dateItem) == false)
                    dateList.value?.add(dateItem)
                else dateList.value?.remove(dateItem)

                sortDate()
            }
            if (dateList.value?.size!! <= 0) alarm.value?.dateRepeat = false
        } else {
            dateList.value = mutableSetOf(dateItem)
            logLine("confirm date2", "${dateList.value}")
        }
        alarm.value?.dateRepeat = isRepeat
        date.value = dateList.value!!.joinToString(",")
        logLine("confirm date", "${dateList.value}, $dateItem, $isRepeat")
    }

    fun timePickerToTime(hour: Int, minute: Int) {
        var timeString: String
        val amPm = if (hour >= 12) "오후" else "오전"
        val displayHour = if (hour > 12) hour - 12 else hour

        timeString = String.format("$amPm %02d:%02d", displayHour, minute)
        time.value = timeString
        logLine("confirm time - timePickerToTime", "${time.value}")
    }

    /**
     * 알람 삽입 or 업데이트
     * **/
    suspend fun updateAlarmData() : AlarmData{
        val deferred = CompletableDeferred<AlarmData>()
        viewModelScope.launch {
            val alarmId = if (alarm.value?.id == 0) {
                alarm.value?.let { insert(it) } ?: 0
            } else {
                alarm.value?.let { update(it) }
                alarm.value?.id
            }

            alarm.value = getAlarmById(alarmId!!.toInt())

            alarm.value?.apply {
                logLine(
                    "confirm viewModelVariable",
                    "alarmId = ${alarm.value?.id}, time = ${time}, title = ${title}, date = ${date}, dateRepeat = ${dateRepeat}, holiday = ${holiday}, sound = ${sound}, volume = ${volume} vibrate = ${vibrate}, offWay = ${off_way}, repeat = ${repeat}"
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
        dateList.value = dateList.value?.sortedBy { dayOfWeekMap[it] }?.toMutableSet()
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
     * 알람 울릴 시간 가져오기
     * **/
    fun getAlarmTime(): ArrayList<Calendar> {
        val inputFormat = SimpleDateFormat("a hh:mm")
        val calList = ArrayList<Calendar>()

        alarm.value?.apply {
            if (!dateRepeat) {
                calList.add(dateToCal(date, Calendar.getInstance()))
            } else {
                try {
                    val inputCal = Calendar.getInstance()
                    inputCal.time = inputFormat.parse(time) // 입력 시간을 설정

                    for (i in dateToList()) {
                        val cal = Calendar.getInstance()
                        dayOfWeekMap[i]?.let { cal.set(Calendar.DAY_OF_WEEK, it) }

                        // 시간 설정
                        cal.set(Calendar.HOUR_OF_DAY, inputCal.get(Calendar.HOUR_OF_DAY))
                        cal.set(Calendar.MINUTE, inputCal.get(Calendar.MINUTE))
                        cal.set(Calendar.SECOND, 0)

                        // 현재 날짜와 비교하여 이미 지난 날짜라면 다음 주의 동일한 요일로 설정
                        if (cal.before(Calendar.getInstance())) {
                            cal.add(Calendar.WEEK_OF_YEAR, 1)
                        }

                        logLine(
                            "confirm getAlarmTime",
                            "$i, ${dayOfWeekMap[i]}, ${dateToList()}, $time"
                        )
                        logLine("confirm getAlarmTime2", "${cal.time}")

                        calList.add(cal)
                    }
                } catch (e: Exception) {
                    Log.e("confirm getAlarmTime", "getAlarmTime try-catch2 ${e.stackTrace}")
                }
            }
        }

        return calList
    }

    /**
     * date가 요일이 아닌 날짜일 경우 Calendar를 반환해주는 함수
     * */
    fun dateToCal(date: String, cal: Calendar): Calendar {
        var returnCal = cal
        try {
            val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E) a hh:mm", Locale.KOREAN)
            logLine("confirm getAlarmTime", "$date, ${time.value}")
            val dateTime: Date = if (date.split(" ").size >= 4) {
                dateFormat.parse("$date ${time.value}")
            } else {
                val tempDate = cal.get(Calendar.YEAR).toString() + "년 " + date
                logLine("confirm getAlarmTime2", "$tempDate ${time.value}")
                dateFormat.parse("$tempDate ${time.value}")
            }
            logLine("confirm getAlarmTime : ", "${time.value}$dateTime")

            returnCal.time = dateTime
        } catch (e: java.lang.Exception) {
            returnCal = Calendar.getInstance()
            returnCal.add(Calendar.DAY_OF_YEAR, 1)
            Log.e("confirm getAlarmTime", "getAlarmTime try-catch1 $date, ${cal.time} ${Log.getStackTraceString(e)}")
        }
        return returnCal
    }

    /**
     * 날짜 포맷
     * **/
    fun dateFormat(year: Int?, month: Int, day: Int, dayOfWeek: Int): String {
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

    fun dateToList(): List<String> {
        return alarm.value?.date?.split(",")!!
    }

    fun changeSound(sound: String) {
        this.sound.value = sound
        logLine("confirm sound", "${this.sound.value}, $sound")
    }

    fun changeVolume(volume: Int) {
        this.volume.value = volume
        logLine("confirm volume", "${this.volume.value}, $volume")
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
