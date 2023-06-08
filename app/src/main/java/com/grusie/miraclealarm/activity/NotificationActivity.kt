package com.grusie.miraclealarm.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ServiceCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityNotificationBinding
import com.grusie.miraclealarm.function.ForegroundAlarmService
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private var alarmId by Delegates.notNull<Int>()
    private lateinit var contentValue : String
    private lateinit var title : String
    private var turnOffFlag = true
    private lateinit var preferences :SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var keyguardManager: KeyguardManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compareMainActivity()
        initUi()
    }

    /**
     * 메인 액티비티로 넘겨야 하는지 결정
     *  */
    private fun compareMainActivity() {
        preferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        editor = preferences.edit()

        val openMainActivity = preferences.getBoolean("openMainActivity", false)

        if (openMainActivity) {
            // 필요한 경우, NotificationActivity에서 메인 액티비티로 이동
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


    private fun initUi() {
        initKeyguard()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel
        binding.viewModel?.logLine(
            "lifecycleConfirm",
            "onCreate $this"
        )

        alarmId = intent.getIntExtra("alarmId", -1)
        title = intent.getStringExtra("title").toString()
        contentValue = intent.getStringExtra("contentValue").toString()

        alarmViewModel.initAlarmData(alarmId)
        binding.viewModel?.logLine(
            "alarmConfirm",
            "알람 초기화 확인 $alarmId ${binding.viewModel?.alarm?.value}"
        )

        binding.btnTurnOff.setOnClickListener {
            turnOffAlarm()
        }
    }

    private fun turnOffAlarm() {
        val intent = Intent(this, ForegroundAlarmService::class.java)
        stopService(intent)
        turnOffFlag = false
        if(binding.viewModel?.alarm?.value?.dateRepeat == false) {
            binding.viewModel?.onAlarmFlagClicked(binding.viewModel?.alarm?.value!!)
        }
        editor.putBoolean("openMainActivity", true)
        editor.apply()
        finish()
    }

    /**
     * 잠금화면 설정
     * */
    private fun initKeyguard() {
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)     //FLAG_SHOW_WHEN_LOCKED 대체
            setTurnScreenOn(true)       //FLAG_TURN_SCREEN_ON 대체
            keyguardManager.requestDismissKeyguard(this, null)      //FLAG_DISMISS_KEYGUARD 대체
        }else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        
        if(turnOffFlag) {
            binding.viewModel?.logLine(
                "lifecycleConfirm",
                "onStop, $this"
            )
            val intent = Intent(this, ForegroundAlarmService::class.java).apply {
                putExtra("alarmId", alarmId)
                putExtra("title", title)
                putExtra("contentValue", contentValue)
                action = "startActivity"
            }
            startForegroundService(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    /**
     * 전체화면 설정
     * */
    private fun hideSystemUI() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if(controller != null){
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

}