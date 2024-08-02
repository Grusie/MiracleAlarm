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
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.grusie.miraclealarm.interfaces.AlarmListClickListener
import com.grusie.miraclealarm.interfaces.MessageUpdateListener
import com.grusie.miraclealarm.model.data.AlarmUiModel
import com.grusie.miraclealarm.model.data.DayOfWeekProvider
import com.grusie.miraclealarm.receiver.TimeChangeReceiver
import com.grusie.miraclealarm.service.ForegroundAlarmService
import com.grusie.miraclealarm.uistate.BaseEventState
import com.grusie.miraclealarm.uistate.BaseUiState
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.Utils.Companion.createConfirm
import com.grusie.miraclealarm.util.Utils.Companion.createPermission
import com.grusie.miraclealarm.util.Utils.Companion.getWidthInDp
import com.grusie.miraclealarm.util.Utils.Companion.showConfirmDialog
import com.grusie.miraclealarm.util.collectStateFlow
import com.grusie.miraclealarm.util.makeSnackbar
import com.grusie.miraclealarm.util.setOnSingleClickListener
import com.grusie.miraclealarm.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Collections


class MainActivity : AppCompatActivity(), MessageUpdateListener, AlarmListClickListener {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels()
    private val alarmListAdapter: AlarmListAdapter by lazy {
        AlarmListAdapter(this)
    }

    private lateinit var layoutManager: LinearLayoutManager
    private var backpressedTime: Long = 0
    private lateinit var timeChangeReceiver: TimeChangeReceiver
    private lateinit var splashScreen: androidx.core.splashscreen.SplashScreen

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewModel.isDeleteMode.value) {
                changeDeleteMode(false)
                alarmListAdapter.notifyDataSetChanged()
            } else {
                if (System.currentTimeMillis() > backpressedTime + 2000) {
                    backpressedTime = System.currentTimeMillis()
                    Toast.makeText(
                        this@MainActivity,
                        getString(com.grusie.miraclealarm.R.string.str_back_press),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (System.currentTimeMillis() <= backpressedTime + 2000) {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()
        startSplash()
        DayOfWeekProvider.initialize(this)
        initUi()
        collectData()
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

    override fun onStart() {
        super.onStart()
        viewModel.getAllAlarmList()
    }

    private fun initUi() {
        this.onBackPressedDispatcher.addCallback(
            onBackPressedCallback
        )
        checkVersion()
        stopMissedAlarmService()
        initTimeChangeReceiver()
        initPermission()

        layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        //adapter = AlarmListAdapter(this, alarmViewModel, this@MainActivity)

        binding.apply {
            rvAlarmList.adapter = alarmListAdapter
            rvAlarmList.layoutManager = layoutManager

            llAdViewContainer.viewTreeObserver.addOnGlobalLayoutListener(adViewWidthObserver)

            ibAlarmAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, CreateAlarmActivity::class.java)
                startActivity(intent)
            }

            tvBtnDelete.setOnSingleClickListener {
                if (viewModel.deleteAlarmList.size > 0)
                    viewModel.deleteAlarm()
            }
        }
    }

    private fun collectData() {
        collectStateFlow(viewModel.allAlarmList) {
            alarmListAdapter.submitList(it)
        }

        collectStateFlow(viewModel.baseUiState) { uiState ->
            when (uiState) {
                is BaseUiState.Loading -> {
                    binding.isLoading = true
                }

                else -> {
                    binding.isLoading = false
                }
            }
        }

        collectStateFlow(viewModel.baseEventState) { eventState ->
            when (eventState) {
                is BaseEventState.Alert -> {
                    val toastMsg = when (eventState.msgType) {
                        MainViewModel.MSG_TYPE_SUCCESS_DELETE -> {
                            getString(R.string.str_delete_alarm)
                        }

                        else -> {
                            getString(R.string.str_delete_alarm)
                        }
                    }
                    Toast.makeText(this@MainActivity, toastMsg, Toast.LENGTH_SHORT).show()
                }

                is BaseEventState.Error -> {
                    binding.root.makeSnackbar(eventState.description)
                }
            }
        }

        collectStateFlow(viewModel.minAlarmTimeData) { alarmTimeData ->
            alarmTimeData?.let { binding.minAlarmTime = it.timeInMillis }
        }

        collectStateFlow(viewModel.isDeleteMode) {
            binding.isDeleteMode = it
        }

        collectStateFlow(viewModel.deleteAlarmTimeList) { list ->
            if (list.isNotEmpty()) {
                list.forEach { alarmTimeUiModel ->
                    alarmTimeUiModel.id.let {
                        Utils.delAlarm(this@MainActivity, it.toInt())
                    }
                }
                viewModel.clearDeleteAlarmTimeList()
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
    private fun changeDeleteMode(isDeleteMode: Boolean) {
        if (!isDeleteMode) viewModel.deleteAlarm()

        viewModel.changeDeleteMode(isDeleteMode)
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
     * 부재중 알람 서비스 정지
     **/
    private fun stopMissedAlarmService() {
        if (intent?.action == Const.ACTION_STOP_SERVICE) {
            val stopServiceIntent = Intent(this, ForegroundAlarmService::class.java)
            stopService(stopServiceIntent)
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
     * 현재 시간이 변경되었을 때, 다음 알람 시간 수정
     **/
    override fun onMessageUpdated(message: String) {
        binding.tvMinAlarm.text = message
    }

    override fun alarmOnClickListener(alarmUiModel: AlarmUiModel) {
        if (viewModel.isDeleteMode.value) {
            viewModel.changeDeleteChecked(alarmUiModel)
        } else {
            val intent = Intent(this@MainActivity, CreateAlarmActivity::class.java)
            intent.putExtra(CreateAlarmActivity.EXTRA_ALARM_ID, alarmUiModel.id)
            binding.root.context.startActivity(intent)
        }
    }

    override fun alarmOnLongClickListener(alarmUiModel: AlarmUiModel) {
        if (!viewModel.isDeleteMode.value) {
            changeDeleteMode(true)
        }
    }

    override fun changeAlarmEnable(alarmUiModel: AlarmUiModel) {
        lifecycleScope.launch {
            viewModel.changeAlarmEnable(alarmUiModel)

            viewModel.selectedAlarmData.value?.let { selectedAlarmData ->
                if (selectedAlarmData.enabled) {
                    viewModel.checkPastDate(selectedAlarmData, true)
                    val alarmTimeList = Utils.setAlarm(this@MainActivity, selectedAlarmData)
                    alarmTimeList.forEach {
                        viewModel.insertAlarmTime(it)
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
                        viewModel.deleteAlarmTimeListByAlarmId(alarmUiModel)
                    }
                }
            }
        }
    }
}