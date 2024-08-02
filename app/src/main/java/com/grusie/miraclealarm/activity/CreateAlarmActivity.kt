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
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityCreateAlarmBinding
import com.grusie.miraclealarm.fragment.DelayBottomFragment
import com.grusie.miraclealarm.interfaces.OnDelayDataPassListener
import com.grusie.miraclealarm.mapper.toData
import com.grusie.miraclealarm.mapper.toUiModel
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import kotlin.properties.Delegates


class CreateAlarmActivity : AppCompatActivity(), OnDelayDataPassListener {
    private lateinit var binding: ActivityCreateAlarmBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private var alarmId by Delegates.notNull<Int>()

    private lateinit var alarmCal: Calendar
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var oldAlarm: AlarmData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        setContentView(binding.root)
        initUi()
    }

    private fun initUi() {
        alarmId = intent.getIntExtra("id", -1)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        alarmCal = Calendar.getInstance()
        alarmCal.add(Calendar.MINUTE, 1)

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

        resultLauncher = initResultLauncher()

        alarmViewModel.initAlarmData(alarmId)

        binding.apply {

            tpAlarmTime.hour = alarmCal.get(Calendar.HOUR_OF_DAY)
            tpAlarmTime.minute = alarmCal.get(Calendar.MINUTE)

            viewModel?.changeTime(
                viewModel?.timePickerToTime(
                    tpAlarmTime.hour,
                    tpAlarmTime.minute
                )!!
            )

            initTime()
            initDate()

            tpAlarmTime.setOnTimeChangedListener { _, hour, minute ->
                viewModel?.changeTime(viewModel?.timePickerToTime(hour, minute)!!)
            }

            btnSave.setOnClickListener {
                saveAlarm()
                //testSaveAlarm()
            }
            btnCancel.setOnClickListener {
                finish()
            }
            ivCalendar.setOnClickListener {
                showCalendar()
            }
            clAlarmSound.setOnClickListener {
                startOptionActivity(
                    Intent(this@CreateAlarmActivity, SoundActivity::class.java),
                    binding.tvSoundSub.text as String,
                    binding.viewModel?.volume?.value
                )
            }
            clAlarmVibe.setOnClickListener {
                startOptionActivity(
                    Intent(this@CreateAlarmActivity, VibrationActivity::class.java),
                    binding.tvVibeSub.text as String,
                    null
                )
            }

            clAlarmDelay.setOnClickListener {
                val delayBottomFragment = DelayBottomFragment()
                val bundle = Bundle()
                bundle.putString("delay", tvDelaySub.text.toString())
                delayBottomFragment.arguments = bundle
                delayBottomFragment.show(supportFragmentManager, delayBottomFragment.tag)
            }

            clAlarmOffWay.setOnClickListener {
                val intent = Intent(this@CreateAlarmActivity, OffWayActivity::class.java)
                intent.putExtra("param1", viewModel?.offWay?.value)
                intent.putExtra("param2", viewModel?.offWayCount?.value)
                startActivity(intent)
            }
        }
    }


    /**
     * ResultLauncher 초기화
     **/
    private fun initResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Const.RESULT_CODE_SOUND) {
                val sound = result.data?.getStringExtra("sound")
                val volume = result.data?.getIntExtra("volume", -1)
                if (sound != null) {
                    binding.viewModel?.changeSound(sound)
                }
                if (volume != null && volume != -1) {
                    binding.viewModel?.changeVolume(volume)
                }
            } else if (result.resultCode == Const.RESULT_CODE_VIBRATION) {
                val vibration = result.data?.getStringExtra("vibration")
                if (vibration != null) {
                    binding.viewModel?.changeVibration(vibration)
                }
            }
        }
    }


    /**
     * param을 담은 startActivity
     **/
    private fun startOptionActivity(intent: Intent, param1: String, param2: Int?) {
        intent.putExtra("param1", param1)
        if (param2 != null) intent.putExtra("param2", param2)

        resultLauncher.launch(intent)
    }

    /**
     * 날짜 초기화
     **/
    private fun initDate() {
        binding.apply {
            viewModel?.date?.observe(this@CreateAlarmActivity) {
                if (it.isNullOrEmpty()) {
                    val date = viewModel?.dateFormat(alarmCal)!!
                    viewModel?.onDateClicked(date, false)
                } else {
                    if (viewModel?.alarm?.value?.dateRepeat == false)
                        alarmCal = Utils.dateToCal(it, viewModel?.time?.value!!)
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

        if (binding.viewModel?.alarm?.value?.dateRepeat == false && alarmCal < Calendar.getInstance()) {
            Toast.makeText(this, getString(R.string.str_past_alarm), Toast.LENGTH_SHORT).show()
        } else {
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


    /**
     * 시간 초기화
     **/
    private fun initTime() {
        binding.apply {
            viewModel?.time?.observe(this@CreateAlarmActivity) {
                val formatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                val localTime = LocalTime.parse(it.toString(), formatter)

                alarmCal.apply {
                    set(Calendar.HOUR_OF_DAY, localTime.hour)
                    set(Calendar.MINUTE, localTime.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                tpAlarmTime.hour = alarmCal.get(Calendar.HOUR_OF_DAY)
                tpAlarmTime.minute = alarmCal.get(Calendar.MINUTE)

                executePendingBindings()
            }
        }
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