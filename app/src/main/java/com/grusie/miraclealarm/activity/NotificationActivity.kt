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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityNotificationBinding
import com.grusie.miraclealarm.function.AlarmNotiReceiver
import com.grusie.miraclealarm.function.ForegroundAlarmService
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.model.AlarmData
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
    private lateinit var alarmNotiReceiver: AlarmNotiReceiver
    private lateinit var currentTime: Calendar

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
            finish()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


    private fun initUi() {
        initKeyguard()
        alarmNotiReceiver = AlarmNotiReceiver()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

        alarm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent?.getParcelableExtra(
                "alarmData",
                AlarmData::class.java
            ) ?: AlarmData()
            else intent?.getParcelableExtra("alarmData") ?: AlarmData()
        binding.viewModel?.initAlarmData(alarm)

        if (intent.action == Const.ACTION_NOTIFICATION)
            binding.btnDelay.visibility = View.GONE

        currentTime = Calendar.getInstance()

        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        binding.tvNotificationTime.text = binding.viewModel?.timePickerToTime(hour, minute)
        Log.d(
            "confirm alarmData noti",
            "$alarm, $hour, $minute, ${binding.viewModel?.timePickerToTime(hour, minute)}"
        )

        if (!alarm.dateRepeat) {
            binding.viewModel?.onAlarmFlagClicked(alarm)
        }

        binding.btnTurnOff.setOnClickListener {
            binding.viewModel?.changeDelayCount(alarm, false)
            turnOffFlag = false

            val intent = Intent(this, TurnOffAlarmActivity::class.java)
            intent.putExtra("alarm", alarm)
            startActivity(intent)
            finish()

            //turnOffAlarm()
        }

        binding.btnDelay.setOnClickListener {
            if (!alarm.dateRepeat) {
                binding.viewModel?.onAlarmFlagClicked(alarm)
            }

            if (alarm.delayCount > 0) {
                binding.viewModel?.changeDelayCount(alarm, true)
                turnOffAlarm()
                val minutes = alarm.delay.replace("분", "").toInt()
                currentTime.add(Calendar.MINUTE, minutes)
                val alarmTimeData =
                    Utils.setAlarm(this, currentTime.timeInMillis, alarm)

                binding.viewModel?.insertAlarmTime(alarmTimeData)
                Log.d("confirm alarmData noti", "$alarm")

                Toast.makeText(
                    this@NotificationActivity,
                    Utils.createAlarmMessage(
                        true,
                        alarmTimeData.timeInMillis
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@NotificationActivity,
                    "남은 미루기 횟수가 없습니다.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        alarm = intent?.getParcelableExtra("alarmData") ?: AlarmData()
        binding.viewModel?.initAlarmData(alarm)

        currentTime = Calendar.getInstance()

        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)


        binding.tvNotificationTime.text = binding.viewModel?.timePickerToTime(hour, minute)
        Log.d(
            "confirm alarmData noti",
            "$alarm, $hour, $minute, ${binding.viewModel?.timePickerToTime(hour, minute)}"
        )
    }

    private fun turnOffAlarm() {
        Utils.stopAlarm(this)
        turnOffFlag = false

        Log.d("confirm turnOffAlarm", "turnOffAlarm $alarm")

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
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()

        if (turnOffFlag) {
            binding.viewModel?.logLine(
                "lifecycleConfirm",
                "onStop, $this"
            )
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
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

}