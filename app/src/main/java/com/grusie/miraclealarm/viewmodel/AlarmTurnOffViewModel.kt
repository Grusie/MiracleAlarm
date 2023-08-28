package com.grusie.miraclealarm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.model.AlarmDatabase
import com.grusie.miraclealarm.model.dao.AlarmTurnOffDao
import com.grusie.miraclealarm.model.data.AlarmData
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
    private val _btnQuickEnabled = MutableLiveData<Boolean>()
    private val operatorArray = arrayOf("+", "-")
    private var job: Job? = null
    private val resources = getApplication<Application>().applicationContext.resources
    private val offWayArray = resources.getStringArray(R.array.off_way_array)
    private val offWayCountArray = resources.getIntArray(R.array.off_way_count_array)

    val offWay: LiveData<String> = _offWay
    val offWayCount: LiveData<Int> = _offWayCount
    val currentCount: LiveData<Int> = _currentCount
    val problem: LiveData<String> = _problem
    val answer: LiveData<Int> = _answer
    val turnOffFlag: LiveData<Boolean> = _turnOffFlag
    val randomXY: LiveData<Pair<Float, Float>> = _randomXY
    val btnQuickEnabled: LiveData<Boolean> = _btnQuickEnabled

    init {
        alarmTurnOffDao = AlarmDatabase.getDatabase(application).alarmTurnOffDao()
        _currentCount.value = 0
        _problem.value = ""
        _turnOffFlag.value = false
        _btnQuickEnabled.value = false
    }

    fun initOffWayById(alarm: AlarmData) {
        viewModelScope.launch {
            val alarmTurnOffData = alarmTurnOffDao.getOffWayById(alarm.id)
            _offWay.value = alarmTurnOffData?.turnOffWay
            _offWayCount.value = alarmTurnOffData?.count
        }
    }

    /**
     * 알람 끄는 방법 변경
     **/
    fun changeOffWay() {
        val filteredArray =
            offWayArray.filterIndexed { index, _ -> offWayArray[index] != _offWay.value }

        val randomIndex = (filteredArray.indices).random()
        _offWay.value = filteredArray[randomIndex]
        _offWayCount.value = offWayCountArray[offWayArray.indexOf(_offWay.value)]

        _currentCount.value = 0
    }

    /**
     * 수학 문제 만들기
     **/

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


    /**
     * 수학문제 계산
     **/
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


    /**
     * 순발력 게임 조작
     **/
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

    /**
     * 순발력 게임 정지
     **/
    fun stopQuickness() {
        job?.cancel()
    }


    /**
     * 순발력 게임 버튼 Enabled 조절
     **/
    fun changeEnabled(enabled: Boolean) {
        _btnQuickEnabled.value = enabled
    }


    /**
     * 현재 카운트 증가
     **/
    fun increaseCurrentCount() {
        _currentCount.value = _currentCount.value!! + 1

        if (_currentCount.value!! >= _offWayCount.value!!) {
            _turnOffFlag.value = true
        }
    }
}