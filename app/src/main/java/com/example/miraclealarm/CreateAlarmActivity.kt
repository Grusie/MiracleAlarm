package com.example.miraclealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.miraclealarm.databinding.ActivityCreateAlarmBinding

class CreateAlarmActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateAlarmBinding
    lateinit var alarmViewModel: AlarmViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        setContentView(binding.root)
        initUi()

    }

    private fun initUi() {
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.viewModel = alarmViewModel
        binding.apply {
            viewModel?.apply {
                alarm.value?.apply {
                    swSound.value = sound.isNotEmpty()
                    swVibe.value = vibrate.isNotEmpty()
                    swOffWay.value = off_way.isNotEmpty()
                    swRepeat.value = repeat.isNotEmpty()

                    if (time != "") {
                        val displayTime = time.split(' ', ':')
                        if (displayTime[0] == "오전") {
                            tpAlarmTime.hour = displayTime[1].toInt()
                        } else
                            tpAlarmTime.hour = displayTime[1].toInt() + 12

                        tpAlarmTime.minute = displayTime[2].toInt()
                    }

                    time(tpAlarmTime.hour, tpAlarmTime.minute)
                    btnSave.setOnClickListener {
                        title = etAlarmTitle.text.toString()
                        //date = lvAlarmDate
                        holiday = swHoliday.value!!
                        flag = false
                        sound =
                            if (swSound.value == true) tvSoundSub.text.toString() else ""
                        vibrate =
                            if (swVibe.value == true) tvVibeSub.text.toString() else ""
                        off_way =
                            if (swOffWay.value == true) tvOffWaySub.text.toString() else ""
                        repeat = if (swRepeat.value == true)
                            tvRepeatSub.text.toString()
                        else ""
                        insert()
                        finish()
                    }
                    btnCancel.setOnClickListener {
                        finish()
                    }
                }
            }
        }
    }
}