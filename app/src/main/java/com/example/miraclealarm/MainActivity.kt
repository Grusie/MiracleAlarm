package com.example.miraclealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miraclealarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var adapter = AlarmListAdapter()
    private lateinit var alarmViewModel: AlarmViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        initUi()
    }

    private fun initUi() {
        alarmViewModel.allAlarms.observe(this, Observer<MutableList<AlarmData>> { alarm ->
            adapter.alarmList = alarm
            adapter.notifyDataSetChanged()
        })
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