package com.grusie.miraclealarm.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityNotificationBinding
import com.grusie.miraclealarm.function.ForegroundAlarmService
import com.grusie.miraclealarm.viewmodel.AlarmViewModel

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var alarmViewModel: AlarmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        initUi()
    }

    private fun initUi() {
        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

        val alarmId = intent.getIntExtra("alarmId", -1)
        alarmViewModel.initAlarmData(alarmId)
        binding.viewModel?.logLine(
            "alarmConfirm",
            "알람 초기화 확인 $alarmId ${binding.viewModel?.alarm?.value}"
        )

        binding.btnTurnOff.setOnClickListener {
            val intent = Intent(this, ForegroundAlarmService::class.java)
            intent.action = getString(R.string.ACTION_DISMISS)
            stopService(intent)
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

}