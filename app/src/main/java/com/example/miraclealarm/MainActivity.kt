package com.example.miraclealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miraclealarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var adapter = AlarmListAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        initUi()
        setContentView(binding.root)
    }

    private fun initUi() {
        binding.apply {
            val intent = Intent(this@MainActivity, CreateAlarmActivity::class.java)
            val createAlarmLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val bundle = result.data?.extras?.getParcelable<Bundle>(Constant.KEY_ALARM_DATA)!!
                    val addAlarm = bundle?.getParcelable<AlarmData>(Constant.KEY_ALARM_DATA)!!
                    adapter.alarmList.add(addAlarm)
                    adapter.notifyDataSetChanged()
                }
            }

            rvAlarmList.adapter = adapter
            rvAlarmList.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            for (i in 0 until 10) {
                adapter.alarmList.add(AlarmData(i, "asd", "16:30", "23-04-11", true, "","",null,null))
            }

            fbAlarmAdd.setOnClickListener {

                createAlarmLauncher.launch(intent)
            }
        }
    }
}