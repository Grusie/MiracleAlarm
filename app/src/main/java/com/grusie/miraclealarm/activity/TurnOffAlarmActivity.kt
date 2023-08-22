package com.grusie.miraclealarm.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityTurnOffAlarmBinding
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.viewmodel.AlarmTurnOffViewModel
import kotlin.math.sqrt


class TurnOffAlarmActivity : AppCompatActivity(), SensorEventListener {
    lateinit var binding: ActivityTurnOffAlarmBinding
    lateinit var alarm: AlarmData
    private lateinit var offWayArray: Array<String>
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var glideFlag = false
    private var shakeSensorManager: SensorManager? = null
    private var shakeAccelerometer: Sensor? = null
    private var shakeTime = 0L
    private lateinit var keyguardManager: KeyguardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_turn_off_alarm)
        compareMainActivity()
        initUi()
    }

    /**
     * 메인 액티비티로 넘겨야 하는지 결정
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initUi() {
        initKeyguard()
        val turnOffViewModel = ViewModelProvider(this)[AlarmTurnOffViewModel::class.java]
        binding.lifecycleOwner = this
        binding.viewModel = turnOffViewModel
        offWayArray = resources.getStringArray(R.array.off_way_array)
        binding.offWayArray = offWayArray

        alarm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableExtra(
                "alarm",
                AlarmData::class.java
            ) ?: AlarmData() else intent.getParcelableExtra("alarm") ?: AlarmData()

        observing()

        binding.apply {
            viewModel?.initOffWayById(alarm)

            etProblem.setOnKeyListener { _, keyCode, _ ->
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        val answer = viewModel?.answer?.value
                        val inputText = etProblem.text.toString()

                        try {
                            val inputNumber = inputText.toInt()
                            if (answer == inputNumber) {
                                viewModel?.increaseCurrentCount()
                                viewModel?.createProblem()
                            } else {
                                clOffWayProblem.startAnimation(
                                    AnimationUtils.loadAnimation(
                                        this@TurnOffAlarmActivity,
                                        R.anim.wrong_answer_anim
                                    )
                                )
                            }
                            etProblem.setText("")
                        } catch (e: NumberFormatException) {
                            // 입력값을 정수로 파싱할 수 없는 경우의 예외 처리
                            // 이 부분에 적절한 오류 처리를 추가하세요.
                        }
                        true
                    }
                    else -> false
                }
            }
            btnQuickness.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        viewModel?.increaseCurrentCount()
                        viewModel?.changeEnabled(false)
                        true
                    }

                    else -> false
                }
            }

            tvOffWayChange.setOnClickListener {
                viewModel?.changeOffWay()
            }

            setSupportActionBar(icToolbar.tbTitle)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    private fun observing() {
        binding.viewModel?.offWay?.observe(this@TurnOffAlarmActivity) {
            if (!it.isNullOrEmpty()) {
                if (glideFlag)
                    Glide.with(this@TurnOffAlarmActivity).clear(binding.ivOffWayContent)

                shakeSensorManager?.unregisterListener(this)

                binding.viewModel?.stopQuickness()
                binding.btnQuickness.visibility = View.GONE

                binding.icToolbar.title = it

                when (it) {
                    offWayArray[0] -> {
                        Glide.with(this).asGif().load(R.drawable.shaking)
                            .into(binding.ivOffWayContent)
                        glideFlag = true

                        observeShaking()
                    }

                    offWayArray[1] -> {
                        binding.viewModel?.createProblem()
                    }

                    offWayArray[2] -> {
                        binding.btnQuickness.visibility = View.VISIBLE

                        binding.btnQuickness.viewTreeObserver.addOnGlobalLayoutListener(object :
                            ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                binding.btnQuickness.viewTreeObserver.removeOnGlobalLayoutListener(
                                    this
                                )

                                binding.viewModel?.startQuickness(
                                    Pair(
                                        binding.clOffWayContentInner.left,
                                        binding.clOffWayContentInner.top
                                    ),
                                    Pair(
                                        binding.clOffWayContentInner.right,
                                        binding.clOffWayContentInner.bottom
                                    ),
                                    binding.btnQuickness.width
                                )
                            }
                        })
                        observeQuickness()
                    }
                }
            }
        }
        binding.viewModel?.turnOffFlag?.observe(this) {
            if (it) turnOffAlarm()
        }
    }

    private fun observeShaking() {
        shakeSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        shakeAccelerometer = shakeSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeSensorManager?.registerListener(
            this,
            shakeAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun observeQuickness() {
        binding.viewModel?.randomXY?.observe(this) {
            binding.btnQuickness.x = it.first
            binding.btnQuickness.y = it.second

            binding.viewModel?.changeEnabled(true)
        }
    }

    private fun turnOffAlarm() {
        Utils.stopAlarm(this)

        editor.putBoolean("openMainActivity", true)
        editor.apply()
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (glideFlag) {
            Glide.with(this).asGif().load(R.drawable.shaking)
                .into(binding.ivOffWayContent)
            glideFlag = true
        }
        shakeSensorManager?.registerListener(
            this,
            shakeAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        shakeSensorManager?.unregisterListener(this)
        if (glideFlag)
            Glide.with(this).clear(binding.ivOffWayContent)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewModel?.stopQuickness()
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

    private fun hideKeyboard() {
        binding.etProblem.clearFocus()
        val inputManager: InputMethodManager =
            this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            this.currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values?.get(0)!!
            val y = event.values?.get(1)!!
            val z = event.values?.get(2)!!

            val gravityX = x.div(SensorManager.GRAVITY_EARTH)
            val gravityY = y.div(SensorManager.GRAVITY_EARTH)
            val gravityZ = z.div(SensorManager.GRAVITY_EARTH)

            val calc = gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ
            val squaredD = sqrt(calc.toDouble())
            val force = squaredD.toFloat()

            if (force > Const.SHAKE_THRESHOLD_GRAVITY) {
                val currentTime = System.currentTimeMillis()
                if (shakeTime + Const.SHAKE_SKIP_TIME > currentTime) {
                    return
                }

                shakeTime = currentTime
                binding.viewModel?.increaseCurrentCount()
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboard()
        return super.dispatchTouchEvent(ev)
    }
}