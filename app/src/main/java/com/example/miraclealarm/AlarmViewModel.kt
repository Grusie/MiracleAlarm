package com.example.miraclealarm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository

    val allAlarms: LiveData<MutableList<AlarmData>>
    val alarm: MutableLiveData<AlarmData>


    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
        alarm = MutableLiveData<AlarmData>(AlarmData())
    }

    fun insert() = viewModelScope.launch {
        Log.d("insert", "${alarm.value}")
        alarm.value?.let { repository.insert(it) }
    }

    fun update(alarm: AlarmData) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun delete(alarm: AlarmData) = viewModelScope.launch {
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

}