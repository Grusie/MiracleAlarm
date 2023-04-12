package com.example.miraclealarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application){

    private val repository : AlarmRepository

    val allAlarms: LiveData<MutableList<AlarmData>>

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
    }

    fun insert(alarm : AlarmData) = viewModelScope.launch {
        repository.insert(alarm)
    }

    fun update(alarm: AlarmData) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun delete(alarm: AlarmData) = viewModelScope.launch {
        repository.delete(alarm)
    }

}