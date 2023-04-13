package com.example.miraclealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.miraclealarm.databinding.ActivityCreateAlarmBinding

class CreateAlarmActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateAlarmBinding
    lateinit var alarmViewModel: AlarmViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        initUi()
        setContentView(binding.root)

    }

    private fun initUi() {
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.viewModel = alarmViewModel
        binding.apply {

            btnSave.setOnClickListener {
                alarmViewModel.alarm.value?.apply {
                    title = etAlarmTitle.text.toString()
                    //date = lvAlarmDate
                    flag = true
                    sound = if (swSound.isChecked) tvSoundSub.text.toString() else ""
                    vibrate = if (swVibe.isChecked) tvVibeSub.text.toString() else ""
                    off_way = if (swOffWay.isChecked) tvOffWaySub.text.toString() else ""
                    repeat = if (swRepeat.isChecked)
                        tvRepeatSub.text.toString()
                    else ""
                }
                alarmViewModel.insert()
                finish()
            }
            btnCancel.setOnClickListener {
                finish()
            }
        }
    }
}