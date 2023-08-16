package com.grusie.miraclealarm.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityTurnOffAlarmBinding
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.viewmodel.AlarmTurnOffViewModel


class TurnOffAlarmActivity : AppCompatActivity() {
    lateinit var binding: ActivityTurnOffAlarmBinding
    lateinit var alarm: AlarmData
    private lateinit var offWayArray: Array<String>
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var glideFlag = false
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

    private fun initUi() {
        val turnOffViewModel = ViewModelProvider(this)[AlarmTurnOffViewModel::class.java]
        binding.lifecycleOwner = this
        binding.viewModel = turnOffViewModel
        offWayArray = resources.getStringArray(R.array.off_way_array)
        binding.offWayArray = offWayArray

        alarm =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableExtra(
                "alarm",
                AlarmData::class.java
            ) ?: AlarmData()
            else intent.getParcelableExtra("alarm") ?: AlarmData()

        observing()

        binding.apply {
            viewModel?.initOffWayById(alarm)

            etProblem.setOnKeyListener { _, keyCode, _ ->
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        if (viewModel?.answer?.value!!.toString() == etProblem.text.toString()) {
                            viewModel?.increaseCurrentCount()
                            viewModel?.createProblem()
                        }
                        etProblem.setText("")
                        true
                    }

                    else -> {
                        true
                    }
                }
            }

            binding.btnTurnOff.setOnClickListener {
                turnOffAlarm()
            }
        }
    }

    private fun observing() {
        binding.viewModel?.offWay?.observe(this@TurnOffAlarmActivity) {
            Log.d("confirm offWay", "confirm offWay Observing $it")
            if (!it.isNullOrEmpty()) {
                if (glideFlag)
                    Glide.with(this@TurnOffAlarmActivity).clear(binding.ivOffWayContent)
                when (it) {
                    offWayArray[0] -> {
                        Glide.with(this).asGif().load(R.drawable.shaking)
                            .into(binding.ivOffWayContent)
                        glideFlag = true
                    }

                    offWayArray[1] -> {
                        binding.viewModel?.createProblem()
                    }

                    offWayArray[2] -> {

                    }
                }
            }
        }
        binding.viewModel?.turnOffFlag?.observe(this) {
            if (it) turnOffAlarm()
        }
    }

    private fun turnOffAlarm() {
        Utils.stopAlarm(this)

        Log.d("confirm turnOffAlarm", "turnOffAlarm $alarm")

        editor.putBoolean("openMainActivity", true)
        editor.apply()
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (glideFlag)
            Glide.with(this).clear(binding.ivOffWayContent)
    }
}