package com.grusie.miraclealarm.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityNotificationBinding
import com.grusie.miraclealarm.mapper.toData
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.service.ForegroundAlarmService
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.Utils.Companion.getAlarmData
import com.grusie.miraclealarm.util.Utils.Companion.getWidthInDp
import com.grusie.miraclealarm.util.Utils.Companion.loadAdView
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import java.util.Calendar

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private lateinit var alarm: AlarmData
    private var turnOffFlag = true
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var currentTime: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compareMainActivity()
        initUi()
    }

    /**
     * 메인 액티비티로 넘겨야 하는지 결정
     * 알람을 끈 뒤 다시 이 페이지로 접근하려고 했을 경우.
     **/
    private fun compareMainActivity() {
        preferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        editor = preferences.edit()

        val openMainActivity = preferences.getBoolean("openMainActivity", false)

        if (openMainActivity) {
            // 필요한 경우, NotificationActivity에서 메인 액티비티로 이동
            finish()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        Glide.with(this).asGif().load(R.drawable.ring_alarm).into(binding.ivRingAlarm)
    }

    override fun onPause() {
        super.onPause()
        Glide.with(this).clear(binding.ivRingAlarm)
    }

    private fun initUi() {
        initKeyguard()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

        initAlarmData()

        binding.apply {
            if (intent.action == Const.ACTION_NOTIFICATION)
                btnDelay.visibility = View.GONE

            llAdViewContainer.viewTreeObserver.addOnGlobalLayoutListener(adViewWidthObserver)

            initTime()

            if (!alarm.dateRepeat) {
                viewModel?.onAlarmFlagClicked(alarm)
            }

            btnTurnOff.setOnClickListener {
                if (!alarm.flagOffWay) {
                    turnOffAlarm(false)
                } else {
                    startTurnOffActivity()
                }
            }

            btnDelay.setOnClickListener {
                delayAlarm()
            }
        }
    }


    /**
     * 알람 끄는 액티비티 실행
     **/
    private fun startTurnOffActivity() {
        val intent = Intent(this@NotificationActivity, TurnOffAlarmActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("alarmData", alarm)
        startActivity(intent)
        finish()
    }

    /**
     * 알람 미루기
     **/
    private fun delayAlarm() {
        binding.apply {
            if (!alarm.dateRepeat) {
                viewModel?.onAlarmFlagClicked(alarm)
            }

            if (alarm.delayCount > 0) {
                viewModel?.changeDelayCount(alarm, true)
                turnOffAlarm(true)
                val minutes = alarm.delay.replace("분", "").toInt()
                currentTime.add(Calendar.MINUTE, minutes)
                val alarmTimeData =
                    Utils.setAlarm(
                        this@NotificationActivity,
                        currentTime.timeInMillis,
                        alarm.toUiModel()
                    )

                viewModel?.insertAlarmTime(alarmTimeData.toData())

                Toast.makeText(
                    this@NotificationActivity, Utils.createAlarmMessage(
                        true, alarmTimeData.timeInMillis
                    ), Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@NotificationActivity,
                    getString(R.string.str_delay_over),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /**
     * 알람 데이터 초기화
     **/
    private fun initAlarmData() {
        alarm = getAlarmData(intent).toData()
        binding.viewModel?.initAlarmData(alarm)
    }

    /**
     * 시간 초기화
     **/
    private fun initTime() {
        currentTime = Calendar.getInstance()

        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        binding.tvNotificationTime.text = binding.viewModel?.timePickerToTime(hour, minute)
    }

    /**
     * AdView 로드
     **/
    private val adViewWidthObserver = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val widthInDp = binding.llAdViewContainer.getWidthInDp()
            loadAdView(this@NotificationActivity, widthInDp, binding.llAdViewContainer)
            binding.llAdViewContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        initAlarmData()
        initTime()
    }


    /**
     * 알람 끄기
     **/
    private fun turnOffAlarm(reduce: Boolean) {
        binding.viewModel?.changeDelayCount(alarm, reduce)
        Utils.stopAlarm(this)
        turnOffFlag = false

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
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()

        if (turnOffFlag && !isFinishing) {
            val intent = Intent(this, ForegroundAlarmService::class.java).apply {
                putExtra("alarmData", alarm)
                action = Const.ACTION_START_ACTIVITY
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

}