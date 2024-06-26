package com.grusie.miraclealarm.activity

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.AlarmListAdapter
import com.grusie.miraclealarm.databinding.ActivityMainBinding
import com.grusie.miraclealarm.interfaces.MessageUpdateListener
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.receiver.TimeChangeReceiver
import com.grusie.miraclealarm.service.ForegroundAlarmService
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.Utils.Companion.createConfirm
import com.grusie.miraclealarm.util.Utils.Companion.createPermission
import com.grusie.miraclealarm.util.Utils.Companion.getWidthInDp
import com.grusie.miraclealarm.util.Utils.Companion.showConfirmDialog
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Collections


class MainActivity : AppCompatActivity(), MessageUpdateListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AlarmListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var alarmViewModel: AlarmViewModel
    private var backpressedTime: Long = 0
    private lateinit var currentCal: Calendar
    private lateinit var timeChangeReceiver: TimeChangeReceiver
    private lateinit var splashScreen: androidx.core.splashscreen.SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()
        startSplash()
        binding =
            DataBindingUtil.setContentView(this, com.grusie.miraclealarm.R.layout.activity_main)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        initUi()
    }

    private fun startSplash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 5f, 1f)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 5f, 1f)

                ObjectAnimator.ofPropertyValuesHolder(splashScreenView.iconView, scaleX, scaleY)
                    .run {
                        interpolator = AnticipateInterpolator()
                        duration = 1000L
                        doOnEnd {
                            splashScreenView.remove()
                        }
                        start()
                    }
            }
        }
    }

    private fun initUi() {
        checkVersion()
        stopMissedAlarmService()
        initTimeChangeReceiver()
        initPermission()

        layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        adapter = AlarmListAdapter(this, alarmViewModel, this@MainActivity)

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel


        binding.apply {
            rvAlarmList.adapter = adapter
            rvAlarmList.layoutManager = layoutManager

            llAdViewContainer.viewTreeObserver.addOnGlobalLayoutListener(adViewWidthObserver)

            ibAlarmAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, CreateAlarmActivity::class.java)
                startActivity(intent)
            }

            observing()

            btnDelete.setOnClickListener {
                if (viewModel?.modifyList?.value!!.size > 0)
                    deleteAlarm()
            }
        }
    }

    private fun checkVersion() {
        //플레이스토어의 버전과 비교
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                showConfirmDialog(
                    supportFragmentManager,
                    title = getString(R.string.str_new_version),
                    content = getString(R.string.str_new_version_content),
                    positiveCallback = {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse("market://details?id=$packageName"))
                        startActivity(intent)

                        finish()
                    },
                    negativeCallback = {
                        finish()
                    }
                )
            }
        }.addOnFailureListener { e: java.lang.Exception ->
            Log.e("confirm version playStore error", "${e.message}")
        }
    }

    /**
     * 다음 알람 시간 변경을 위한 시간 변경 리시버 등록
     **/
    private fun initTimeChangeReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
        }

        val timeChangeReceiver = TimeChangeReceiver()
        registerReceiver(timeChangeReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(timeChangeReceiver)
        } catch (e: Exception) {
        }
    }

    /**
     * 알람 삭제
     **/
    private fun deleteAlarm() {
        binding.apply {
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
                getString(com.grusie.miraclealarm.R.string.str_delete_alarm),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * AdView 너비 옵저빙
     **/
    private val adViewWidthObserver = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val widthInDp = binding.llAdViewContainer.getWidthInDp()
            Utils.loadAdView(this@MainActivity, widthInDp, binding.llAdViewContainer)
            binding.llAdViewContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }


    /**
     * 각 값들 옵저빙
     **/
    private fun observing() {
        binding.apply {
            viewModel?.minAlarmTime?.observe(this@MainActivity) {
                tvMinAlarm.text = if (it != null) {
                    Utils.createAlarmMessage(false, it.timeInMillis)
                } else getString(com.grusie.miraclealarm.R.string.str_turn_off_all)
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

    /**
     * 부재중 알람 서비스 정지
     **/
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

    /**
     * 지난 알람 날짜 하루 추가
     **/
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

    /**
     * 퍼미션 체크
     **/
    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Utils.checkPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                createPermission(Manifest.permission.POST_NOTIFICATIONS, null)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!Utils.checkPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                Utils.showConfirmDialog(
                    supportFragmentManager = supportFragmentManager,
                    title = getString(com.grusie.miraclealarm.R.string.str_permission_title),
                    content = getString(com.grusie.miraclealarm.R.string.str_schedule_exact_alarms),
                    positiveCallback = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)
                    },
                    negativeCallback = {
                        finish()
                    }
                )
            }
        }

        if (!Settings.canDrawOverlays(this))
            createConfirm(
                this,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                getString(com.grusie.miraclealarm.R.string.str_permission_title),
                getString(com.grusie.miraclealarm.R.string.str_can_draw_overlays)
            )
    }

    /**
     * 뒤로가기 버튼
     **/
    override fun onBackPressed() {
        if (binding.viewModel?.modifyMode?.value == true) {
            binding.viewModel?.changeModifyMode()
            binding.viewModel?.modifyList?.value?.clear()
            adapter.notifyDataSetChanged()
        } else {
            if (System.currentTimeMillis() > backpressedTime + 2000) {
                backpressedTime = System.currentTimeMillis()
                Toast.makeText(
                    this,
                    getString(com.grusie.miraclealarm.R.string.str_back_press),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (System.currentTimeMillis() <= backpressedTime + 2000) {
                finish()
            }
        }
    }

    /**
     * 현재 시간이 변경되었을 때, 다음 알람 시간 수정
     **/
    override fun onMessageUpdated(message: String) {
        binding.tvMinAlarm.text = message
    }
}