package com.grusie.miraclealarm.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.AlarmListAdapter
import com.grusie.miraclealarm.databinding.ActivityMainBinding
import com.grusie.miraclealarm.function.ForegroundAlarmService
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.function.Utils.Companion.createConfirm
import com.grusie.miraclealarm.function.Utils.Companion.createPermission
import com.grusie.miraclealarm.function.Utils.Companion.getWidthInDp
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Collections

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlarmListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var alarmViewModel: AlarmViewModel
    private var backpressedTime: Long = 0
    private lateinit var currentCal: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        initUi()
    }

    private fun initUi() {
        stopMissedAlarmService()
        initPermission()

        layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        adapter = AlarmListAdapter(alarmViewModel, this@MainActivity)

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel


        binding.llAdViewContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val widthInDp = binding.llAdViewContainer.getWidthInDp()
                loadAdView(widthInDp)
                binding.llAdViewContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        binding.apply {
            rvAlarmList.adapter = adapter
            rvAlarmList.layoutManager = layoutManager

            ibAlarmAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, CreateAlarmActivity::class.java)
                startActivity(intent)
            }

            observing()

            btnDelete.setOnClickListener {
                for (alarm in viewModel?.modifyList?.value!!) {
                    viewModel?.delete(alarm)

                    lifecycleScope.launch {
                        viewModel?.getAlarmTimesByAlarmId(alarm)?.forEach {
                            Utils.delAlarm(this@MainActivity, it.id)
                        }
                        viewModel?.deleteAlarmTimeById(alarm)
                    }
                }
                viewModel?.modifyList?.value?.clear()
                viewModel?.changeModifyMode()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.string_delete_alarm),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observing() {
        binding.apply {
            viewModel?.minAlarmTime?.observe(this@MainActivity) {
                tvMinAlarm.text = if (it != null) {
                    Utils.createAlarmMessage(false, it.timeInMillis)
                } else getString(R.string.string_turn_off_all)
            }

            viewModel?.modifyMode?.observe(this@MainActivity) {
                llModifyTab.visibility = if (it) View.VISIBLE else View.GONE

                if (!it) {
                    viewModel?.modifyList?.value?.clear()
                }
            }
            viewModel?.clearAlarm?.observe(this@MainActivity) { alarm ->
                currentCal = Calendar.getInstance()
                viewModel?.initAlarmData(alarm)
                if (alarm.enabled) {
                    checkPastDate(alarm, true)
                    val alarmTimeList = Utils.setAlarm(this@MainActivity, alarm)
                    alarmTimeList.forEach {
                        viewModel?.insertAlarmTime(it)
                    }
                    Toast.makeText(
                        this@MainActivity,
                        Utils.createAlarmMessage(
                            true,
                            Collections.min(alarmTimeList.map { it.timeInMillis })
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    lifecycleScope.launch {
                        viewModel?.getAlarmTimesByAlarmId(alarm)?.forEach {
                            Utils.delAlarm(this@MainActivity, it.id)
                        }
                        viewModel?.deleteAlarmTimeById(alarm)
                    }
                }
            }
        }
    }
    private fun loadAdView(width: Int) {
        val adView = Utils.initAdView(this, width)

        binding.llAdViewContainer.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun stopMissedAlarmService() {
        if (intent?.action == Const.ACTION_STOP_SERVICE) {
            val stopServiceIntent = Intent(this, ForegroundAlarmService::class.java)
            stopService(stopServiceIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.viewModel?.allAlarms?.observe(this) { alarmList ->

            currentCal = Calendar.getInstance()
            alarmList.forEach {
                checkPastDate(it, false)
            }
            alarmViewModel.sortAlarm(alarmList)
            adapter.alarmList = alarmList
            adapter.notifyDataSetChanged()
        }
    }

    private fun checkPastDate(alarm: AlarmData, enabled: Boolean) {
        if (!alarm.dateRepeat) {
            lifecycleScope.launch {
                val alarmCal = Utils.dateToCal(alarm.date, alarm.time)
                if (currentCal > alarmCal && binding.viewModel?.getAlarmTimesByAlarmId(alarm)?.size == 0) {
                    alarmCal.add(Calendar.DAY_OF_YEAR, 1)
                    val date = binding.viewModel?.dateFormat(alarmCal)!!

                    binding.viewModel?.changeAlarmDate(alarm, date, enabled)
                }
            }
        }
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            createPermission(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!Settings.canDrawOverlays(this))
            createConfirm(
                this,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                "권한 허용",
                "알람을 사용하려면 다른 앱 위에 표시권한을 허용해주세요."
            )
    }

    override fun onBackPressed() {
        if (binding.viewModel?.modifyMode?.value == true) {
            binding.viewModel?.changeModifyMode()
            binding.viewModel?.modifyList?.value?.clear()
            adapter.notifyDataSetChanged()
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