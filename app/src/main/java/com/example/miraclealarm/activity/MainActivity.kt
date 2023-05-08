package com.example.miraclealarm.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miraclealarm.viewmodel.AlarmViewModel
import com.example.miraclealarm.R
import com.example.miraclealarm.adapter.AlarmListAdapter
import com.example.miraclealarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter : AlarmListAdapter
    private lateinit var alarmViewModel: AlarmViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        initUi()
    }

    private fun initUi() {
        adapter = AlarmListAdapter(alarmViewModel)
        alarmViewModel.allAlarms.observe(this) { alarm ->
            alarmViewModel.logLine("alarmList : ", "$alarm")
            adapter.alarmList = alarm
            adapter.notifyDataSetChanged()
        }
        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel
        binding.apply {
            val intent = Intent(this@MainActivity, CreateAlarmActivity::class.java)

            rvAlarmList.adapter = adapter
            rvAlarmList.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            fbAlarmAdd.setOnClickListener {
                startActivity(intent)
            }
        }
    }
}