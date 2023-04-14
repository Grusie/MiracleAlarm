package com.example.miraclealarm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.logging.Logger

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository

    val allAlarms: LiveData<MutableList<AlarmData>>
    val alarm: MutableLiveData<AlarmData>
    val swHoliday = MutableLiveData<Boolean>()
    val swSound = MutableLiveData<Boolean>()
    val swVibe = MutableLiveData<Boolean>()
    val swOffWay = MutableLiveData<Boolean>()
    val swRepeat = MutableLiveData<Boolean>()

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
        alarm = MutableLiveData<AlarmData>(AlarmData())

        swHoliday.value = alarm.value?.holiday
        swSound.value = alarm.value?.sound != ""
        swVibe.value = alarm.value?.vibrate != ""
        swOffWay.value = alarm.value?.off_way != ""
        swRepeat.value = alarm.value?.repeat != ""
    }

    fun insert() = viewModelScope.launch {
        Log.d("insert", "${alarm.value}")
        alarm.value?.let { repository.insert(it) }
    }

    fun update(alarm: AlarmData) = viewModelScope.launch {
        Log.d("confirm update", "${alarm}")
        repository.update(alarm)
    }

    fun delete(alarm: AlarmData) = viewModelScope.launch {
        Log.d("confirm delete", "${alarm}")
        repository.delete(alarm)
    }

    fun time(hour: Int, minute: Int) {
        var timeString: String
        val amPm = if (hour >= 12) "오후" else "오전"
        val displayHour = if (hour > 12) hour - 12 else hour

        timeString = String.format("$amPm %02d:%02d", displayHour, minute)
        alarm.value?.time = timeString
        Log.d("alarm.value?.time = ", "${alarm.value?.time}")
    }
    fun onSwSoundClicked(){
        swSound.value = !swSound.value!!
    }
    fun onSwVibeClicked(){
        swVibe.value = !swVibe.value!!
    }
    fun onSwOffWayClicked(){
        swOffWay.value = !swOffWay.value!!
    }
    fun onSwRepeatClicked(){
        swRepeat.value = !swRepeat.value!!
    }
    fun onSwHolidayClicked(){
        alarm.value?.holiday = !alarm.value?.holiday!!
    }
    fun onAlarmFlagClicked(alarm : AlarmData){
        alarm.flag = !alarm.flag
        update(alarm)
        Log.d("flag confirm", "${alarm.flag}")
    }

}