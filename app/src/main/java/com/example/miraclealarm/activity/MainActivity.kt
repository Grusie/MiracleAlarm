package com.example.miraclealarm.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miraclealarm.R
import com.example.miraclealarm.adapter.AlarmListAdapter
import com.example.miraclealarm.databinding.ActivityMainBinding
import com.example.miraclealarm.function.AlarmNotiReceiver
import com.example.miraclealarm.function.Utils
import com.example.miraclealarm.model.AlarmData
import com.example.miraclealarm.viewmodel.AlarmViewModel
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlarmListAdapter
    private lateinit var alarmViewModel: AlarmViewModel
    private var backpressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        initUi()
    }

    private fun initUi() {
        createPermission()
        adapter = AlarmListAdapter(alarmViewModel, this@MainActivity)

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
            viewModel?.modifyMode?.observe(this@MainActivity) {
                llModifyTab.visibility = if (it) View.VISIBLE else View.GONE

                if(!it){
                    viewModel?.modifyList?.value?.clear()
                }
            }
            btnDelete.setOnClickListener {
                for (alarm in viewModel?.modifyList?.value!!) {
                    viewModel?.delete(alarm)
                    Utils.delAlarm(this@MainActivity, alarm)
                }
                viewModel?.modifyList!!.value = mutableSetOf()
                viewModel?.modifyMode!!.value = false
                Toast.makeText(this@MainActivity, "알람이 삭제 되었습니다.", Toast.LENGTH_SHORT).show()
            }

            viewModel?.clearAlarm?.observe(this@MainActivity) { alarm ->
                if (alarm.enabled) {
                    viewModel?.getAlarmTime()?.forEach {
                        Utils.setAlarm(this@MainActivity, it, alarm)
                    }
                } else
                    Utils.delAlarm(this@MainActivity, alarm)
            }
        }
    }
    fun createPermission() {

        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
            }

            override fun onPermissionDenied(deniedPermissions: List<String?>) {
            }
        }

        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("알람을 사용하려면 알림 권한을 허용하여 주셔야 합니다.")
            .setPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
            .check()
    }

    override fun onBackPressed() {
        if (binding.viewModel?.modifyMode?.value == true) {
            binding.viewModel?.modifyMode?.value = false
        } else {
            if (System.currentTimeMillis() > backpressedTime + 2000) {
                backpressedTime = System.currentTimeMillis()
                Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            } else if (System.currentTimeMillis() <= backpressedTime + 2000) {
                finish()
            }
        }
    }
}