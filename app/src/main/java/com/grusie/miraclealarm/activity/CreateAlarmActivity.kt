package com.grusie.miraclealarm.activity

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.DayAdapter
import com.grusie.miraclealarm.databinding.ActivityCreateAlarmBinding
import com.grusie.miraclealarm.fragment.DelayBottomFragment
import com.grusie.miraclealarm.interfaces.OnDelayDataPassListener
import com.grusie.miraclealarm.mapper.toData
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.uistate.BaseEventState
import com.grusie.miraclealarm.uistate.BaseUiState
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.collectStateFlow
import com.grusie.miraclealarm.util.makeSnackbar
import com.grusie.miraclealarm.util.setOnSingleClickListener
import com.grusie.miraclealarm.util.timePickerToTimeString
import com.grusie.miraclealarm.viewmodel.CreateAlarmViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import javax.inject.Inject


class CreateAlarmActivity : AppCompatActivity(), OnDelayDataPassListener {
    private val binding: ActivityCreateAlarmBinding by lazy {
        ActivityCreateAlarmBinding.inflate(layoutInflater)
    }

    private var alarmId: Long = -1
    private val dayAdapter: DayAdapter by lazy {
        DayAdapter {
            viewModel.changeDaySelected(it)
        }
    }

    @Inject
    lateinit var createAlarmViewModelFactory: CreateAlarmViewModel.CreateAlarmViewModelFactory
    private val viewModel by viewModels<CreateAlarmViewModel> {
        CreateAlarmViewModel.provideFactory(createAlarmViewModelFactory, alarmId)
    }

    private var alarmCal: Calendar = Calendar.getInstance()
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var oldAlarm: AlarmData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1)
        initUi()
        collectData()
    }

    private fun initUi() {
        initResultLauncher()

        binding.apply {
            btnSave.setOnSingleClickListener {
                saveAlarm()
                //testSaveAlarm()
            }
            btnCancel.setOnSingleClickListener {
                finish()
            }

            ivCalendar.setOnSingleClickListener {
                showCalendar()
            }

            clAlarmSound.setOnSingleClickListener {
                val intent = Intent(this@CreateAlarmActivity, SoundActivity::class.java).apply {
                    putExtra(SoundActivity.EXTRA_SOUND, viewModel.alarmData.value.sound)
                    putExtra(SoundActivity.EXTRA_VOLUME, viewModel.alarmData.value.volume)
                }

                resultLauncher.launch(intent)
            }
            clAlarmVibe.setOnSingleClickListener {
                val intent = Intent(this@CreateAlarmActivity, VibrationActivity::class.java).apply {
                    putExtra(VibrationActivity.EXTRA_VIBRATION, binding.tvVibeSub.text)
                }

                resultLauncher.launch(intent)
            }

            clAlarmDelay.setOnSingleClickListener {
                val delayBottomFragment =
                    DelayBottomFragment.newInstance(viewModel.alarmData.value.delay)
                delayBottomFragment.show(supportFragmentManager, delayBottomFragment.tag)
            }

            clAlarmOffWay.setOnSingleClickListener {
                val intent = Intent(this@CreateAlarmActivity, OffWayActivity::class.java)
                intent.putExtra(
                    OffWayActivity.EXTRA_TURN_OFF_WAY,
                    viewModel.turnOffWay.value.turnOffWay
                )
                intent.putExtra(
                    OffWayActivity.EXTRA_TURN_OFF_COUNT,
                    viewModel.turnOffWay.value.count
                )
                startActivity(intent)
            }

            rvDay.adapter = dayAdapter
            val layoutManager = FlexboxLayoutManager(this@CreateAlarmActivity).apply {
                justifyContent = JustifyContent.SPACE_EVENLY
            }
            rvDay.layoutManager = layoutManager
        }
    }

    /**
     * TimePicker 초기화
     **/
    private fun initTimePicker(time: String? = null) {
        binding.apply {
            tpAlarmTime.setOnTimeChangedListener { _, hour, minute ->
                viewModel.changeTime(timePickerToTimeString(hour, minute))
            }

            val localTime = try {
                val formatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                LocalDateTime.parse(time, formatter)
            } catch (e: Exception) {
                LocalDateTime.now().apply {
                    plusMinutes(1)
                }
            }

            tpAlarmTime.hour = localTime.hour
            tpAlarmTime.minute = localTime.minute
        }
    }

    /**
     * ResultLauncher 초기화
     **/
    private fun initResultLauncher() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Const.RESULT_CODE_SOUND) {
                    val sound = result.data?.getStringExtra(SoundActivity.EXTRA_SOUND)
                    val volume = result.data?.getIntExtra(SoundActivity.EXTRA_VOLUME, -1)
                    if (sound != null) {
                        viewModel.changeSound(sound)
                    }
                    if (volume != null && volume != -1) {
                        viewModel.changeVolume(volume)
                    }
                } else if (result.resultCode == Const.RESULT_CODE_VIBRATION) {
                    val vibration = result.data?.getStringExtra(VibrationActivity.EXTRA_VIBRATION)
                    if (vibration != null) {
                        viewModel.changeVibration(vibration)
                    }
                }
            }
    }

    private fun collectData() {
        collectStateFlow(viewModel.alarmData) {
            if (!it.dateRepeat) {
                viewModel.changeAlarmCal(Utils.dateToCal(it.date, it.time))
            }
            initTimePicker(it.time)

            binding.alarmUiModel = it
        }

        collectStateFlow(viewModel.baseUiState) {
            when (it) {
                is BaseUiState.Loading -> {
                    binding.isLoading = true
                }

                is BaseUiState.Success -> {
                    binding.isLoading = false
                    finish()
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
                        viewModel.MSG_TYPE_DENIED_PAST_TIME -> {
                            getString(R.string.str_past_alarm)
                        }

                        else -> {
                            getString(R.string.str_past_alarm)
                        }
                    }
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT)
                }

                is BaseEventState.Error -> {
                    binding.root.makeSnackbar(eventState.description)
                }
            }
        }
    }

    /**
     * 알람 저장
     **/
    private fun saveAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!Utils.checkPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                Utils.showConfirmDialog(
                    supportFragmentManager = supportFragmentManager,
                    title = getString(R.string.str_permission_title),
                    content = getString(R.string.str_schedule_exact_alarms),
                    positiveCallback = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)
                    },
                    negativeCallback = {
                    }
                )
                return
            }
        }

        if (viewModel.alarmData.value.dateRepeat && viewModel.alarmCal < Calendar.getInstance()) {
            Toast.makeText(this, getString(R.string.str_past_alarm), Toast.LENGTH_SHORT)
        }
        viewModel.confirmPastTime()
        viewModel.createAlarm()

        binding.apply {
            viewModel?.alarm?.value?.apply {
                if (alarmId != -1) {
                    oldAlarm = this.copy()
                }
                title = etAlarmTitle.text.toString()
                flagSound = viewModel?.flagSound?.value == true
                flagVibrate = viewModel?.flagVibe?.value == true
                flagOffWay = viewModel?.flagOffWay?.value == true
                flagDelay = viewModel?.flagDelay?.value == true

                time = viewModel?.time?.value.toString()

                this.date = viewModel?.date?.value!!
                enabled = true
                sound =
                    if (viewModel?.flagSound?.value == true) tvSoundSub.text.toString() else sound
                volume = viewModel?.volume?.value!!
                vibrate =
                    if (viewModel?.flagVibe?.value == true) tvVibeSub.text.toString() else vibrate
                delay =
                    if (viewModel?.flagDelay?.value == true) tvDelaySub.text.toString() else delay
            }

            lifecycleScope.launch {
                viewModel?.updateAlarmData()?.let { alarmData ->
                    oldAlarm?.let { oldAlarm ->
                        viewModel?.getAlarmTimesByAlarmId(oldAlarm)?.forEach {
                            Utils.delAlarm(this@CreateAlarmActivity, it.id)
                        }
                        viewModel?.deleteAlarmTimeById(oldAlarm)
                        viewModel?.updateAlarmTurnOff(Const.DELETE_ALARM_TURN_OFF, oldAlarm)
                    }

                    viewModel?.updateAlarmTurnOff(Const.INSERT_ALARM_TURN_OFF, alarmData)

                    val alarmTimeList =
                        Utils.setAlarm(this@CreateAlarmActivity, alarmData.toUiModel())
                    alarmTimeList.forEach {
                        viewModel?.insertAlarmTime(it.toData())
                    }

                    Toast.makeText(
                        this@CreateAlarmActivity,
                        Utils.createAlarmMessage(
                            true,
                            Collections.min(alarmTimeList.map { it.timeInMillis })
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        finish()
    }
}


/**
 * 테스트 알람 저장(지난 시간 가능)
 **/
/*    private fun testSaveAlarm() {
            binding.apply {
                viewModel?.alarm?.value?.apply {
                    if (alarmId != -1) {
                        oldAlarm = this.copy()
                    }
                    title = etAlarmTitle.text.toString()
                    flagSound = viewModel?.flagSound?.value == true
                    flagVibrate = viewModel?.flagVibe?.value == true
                    flagOffWay = viewModel?.flagOffWay?.value == true
                    flagDelay = viewModel?.flagDelay?.value == true

                    time = viewModel?.time?.value.toString()

                    this.date = viewModel?.date?.value!!
                    enabled = true
                    sound =
                        if (viewModel?.flagSound?.value == true) tvSoundSub.text.toString() else sound
                    volume = viewModel?.volume?.value!!
                    vibrate =
                        if (viewModel?.flagVibe?.value == true) tvVibeSub.text.toString() else vibrate
                    delay =
                        if (viewModel?.flagDelay?.value == true) tvDelaySub.text.toString() else delay
                }

                lifecycleScope.launch {
                    viewModel?.updateAlarmData()?.let { alarmData ->
                        oldAlarm?.let { oldAlarm ->
                            viewModel?.getAlarmTimesByAlarmId(oldAlarm)?.forEach {
                                Utils.delAlarm(this@CreateAlarmActivity, it.id)
                            }
                            viewModel?.deleteAlarmTimeById(oldAlarm)
                            viewModel?.updateAlarmTurnOff(Const.DELETE_ALARM_TURN_OFF, oldAlarm)
                        }

                        viewModel?.updateAlarmTurnOff(Const.INSERT_ALARM_TURN_OFF, alarmData)

                        val alarmTimeList = Utils.setAlarm(this@CreateAlarmActivity, alarmData)
                        alarmTimeList.forEach {
                            viewModel?.insertAlarmTime(it)
                        }

                        Toast.makeText(
                            this@CreateAlarmActivity,
                            Utils.createAlarmMessage(
                                true,
                                Collections.min(alarmTimeList.map { it.timeInMillis })
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            finish()
    }*/


/**
 * 캘린더 보여주기
 **/
private fun showCalendar() {
    val datePickerDialog = DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            alarmCal.set(Calendar.YEAR, year)
            alarmCal.set(Calendar.MONTH, month)
            alarmCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val date = binding.viewModel?.dateFormat(alarmCal)!!
            binding.viewModel?.onDateClicked(date, false)
        },
        alarmCal.get(Calendar.YEAR),
        alarmCal.get(Calendar.MONTH),
        alarmCal.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
    datePickerDialog.show()
}


override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    if (intent != null) {
        val offWay = intent.getStringExtra("offWay")
        val offWayCount = intent.getIntExtra("offWayCount", 0)

        if (offWay != null) {
            binding.viewModel?.changeOffWay(offWay, offWayCount)
        }
    }
}

/**
 * 미루기 데이터 받기
 **/
override fun onDelayDataPass(data: String?) {
    if (data != null)
        binding.viewModel?.changeDelay(data)
}


/**
 * 키보드 숨기기
 **/
private fun hideKeyboard() {
    binding.etAlarmTitle.clearFocus()
    val inputManager: InputMethodManager =
        this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
        this.currentFocus?.windowToken,
        InputMethodManager.HIDE_NOT_ALWAYS
    )
}

override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    hideKeyboard()
    return super.dispatchTouchEvent(ev)
}

companion object {
    const val EXTRA_ALARM_ID = "extra_alarm_id"
}
}