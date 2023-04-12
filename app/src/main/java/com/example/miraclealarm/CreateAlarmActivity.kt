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
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.apply {
            var alarm = AlarmData( title = "",time = "", date = "23-04-11", flag = true, sound = "", vibrate = "", off_way = null, repeat = null)
            btnSave.setOnClickListener {
                alarmViewModel.insert(alarm)
                finish()
            }
            btnCancel.setOnClickListener {
                finish()
            }
        }
    }
}