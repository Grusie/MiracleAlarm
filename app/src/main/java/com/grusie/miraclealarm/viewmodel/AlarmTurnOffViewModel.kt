package com.grusie.miraclealarm.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.AlarmTurnOffDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmTurnOffViewModel(application: Application) : AndroidViewModel(application) {
    private val alarmTurnOffDao: AlarmTurnOffDao
    private val _offWay = MutableLiveData<String>()
    private val _offWayCount = MutableLiveData<Int>()
    private val _currentCount = MutableLiveData<Int>()
    private val _problem = MutableLiveData<String>()
    private val _answer = MutableLiveData<Int>()
    private val _turnOffFlag = MutableLiveData<Boolean>()
    private val _randomXY = MutableLiveData<Pair<Float, Float>>()
    private val operatorArray = arrayOf("+", "-")
    private var job: Job? = null

    val offWay: LiveData<String> = _offWay
    val offWayCount: LiveData<Int> = _offWayCount
    val currentCount: LiveData<Int> = _currentCount
    val problem: LiveData<String> = _problem
    val answer: LiveData<Int> = _answer
    val turnOffFlag: LiveData<Boolean> = _turnOffFlag
    val randomXY: LiveData<Pair<Float, Float>> = _randomXY

    init {
        alarmTurnOffDao = AlarmDatabase.getDatabase(application).alarmTurnOffDao()
        _currentCount.value = 0
        _problem.value = ""
        _turnOffFlag.value = false
    }

    fun initOffWayById(alarm: AlarmData) {
        viewModelScope.launch {
            val alarmTurnOffData = alarmTurnOffDao.getOffWayById(alarm.id)
            _offWay.value = alarmTurnOffData.turnOffWay
            _offWayCount.value = alarmTurnOffData.count
            Log.d("confirm offWay", "${_offWay.value}")
        }
    }

    fun createProblem() {
        val tempProblem = Array(5) { "" }
        for (i in 0 until 5) {
            tempProblem[i] = if (i % 2 != 0)
                operatorArray[kotlin.random.Random.nextInt(0, 2)]
            else
                kotlin.random.Random.nextInt(1, 101).toString()
        }

        _problem.value = tempProblem.joinToString(" ")
        _answer.value = calculate(tempProblem)
    }

    private fun calculate(tempProblem: Array<String>): Int {
        var result = tempProblem[0].toInt()

        for (i in 1 until tempProblem.size step 2) {
            val operator = tempProblem[i]
            val operand = tempProblem[i + 1].toInt()

            when (operator) {
                "+" -> result += operand
                "-" -> result -= operand
            }
        }

        return result
    }

    fun startQuickness(leftTop: Pair<Int, Int>, rightBottom: Pair<Int, Int>, width: Int) {
        job = viewModelScope.launch {
            while (_turnOffFlag.value == false) {

                val randomX = (leftTop.first..(rightBottom.first - width)).random()
                val randomY = (leftTop.second..(rightBottom.second - width)).random()

                _randomXY.value = Pair(randomX.toFloat(), randomY.toFloat())
                delay(700)
            }
        }
    }

    fun stopQuickness() {
        job?.cancel()
    }

    fun increaseCurrentCount() {
        _currentCount.value = _currentCount.value!! + 1

        if (_currentCount.value!! >= _offWayCount.value!!) {
            _turnOffFlag.value = true
        }
    }
}