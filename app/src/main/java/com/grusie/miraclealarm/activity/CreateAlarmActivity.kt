package com.grusie.miraclealarm.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
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
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.model.AlarmData
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.properties.Delegates


class CreateAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAlarmBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private var alarmId by Delegates.notNull<Int>()

    private lateinit var alarmCal: Calendar
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var oldAlarm : AlarmData? = null

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

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Const.RESULT_CODE_SOUND) {
                    val sound = result.data?.getStringExtra("sound")
                    val volume = result.data?.getIntExtra("volume", -1)
                    if (sound != null) {
                        binding.viewModel?.changeSound(sound)
                    }
                    if (volume != null && volume != -1) {
                        binding.viewModel?.changeVolume(volume)
                    }
                }
                else if(result.resultCode == Const.RESULT_CODE_VIBRATION){
                    val vibration = result.data?.getStringExtra("vibration")
                    if(vibration != null){
                        binding.viewModel?.changeVibration(vibration)
                    }
                }
            }

        alarmViewModel.initAlarmData(alarmId)

        binding.tpAlarmTime.hour = alarmCal.get(Calendar.HOUR_OF_DAY)
        binding.tpAlarmTime.minute = alarmCal.get(Calendar.MINUTE)

        binding.viewModel?.timePickerToTime(
            binding.tpAlarmTime.hour,
            binding.tpAlarmTime.minute
        )

        binding.apply {

            initTime()
            initDate()

            tpAlarmTime.setOnTimeChangedListener { _, hour, minute ->
                viewModel?.timePickerToTime(hour, minute)
            }

            btnSave.setOnClickListener {
                saveAlarm()
            }
            btnCancel.setOnClickListener {
                finish()
            }
            ivCalendar.setOnClickListener {
                showDateDialog()
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
        }
    }

    private fun startOptionActivity(intent: Intent, param1: String, param2: Int?) {
        intent.putExtra("param1", param1)
        if (param2 != null) intent.putExtra("param2", param2)

        resultLauncher.launch(intent)
    }

    private fun initDate() {
        binding.apply {
            viewModel?.date?.observe(this@CreateAlarmActivity) {
                viewModel?.logLine("confirm day_of_week", it)
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

    private fun saveAlarm() {
        if(binding.viewModel?.alarm?.value?.dateRepeat == false && alarmCal < Calendar.getInstance()){
            Toast.makeText(this, "이미 지난 시간은 선택할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            binding.apply {
                viewModel?.alarm?.value?.apply {
                    if (alarmId != -1) {
                        oldAlarm = this.copy()
                    }
                    title = etAlarmTitle.text.toString()
                    holiday = viewModel?.flagHoliday?.value == true
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
                    off_way =
                        if (viewModel?.flagOffWay?.value == true) tvOffWaySub.text.toString() else off_way
                    delay =
                        if (viewModel?.flagDelay?.value == true) tvDelaySub.text.toString() else delay
                }

                lifecycleScope.launch {
                    viewModel?.updateAlarmData()?.let {alarmData ->
                        oldAlarm?.let{Utils.delAlarm(this@CreateAlarmActivity, it)}?.forEach{
                            viewModel?.updateAlarmTimeData(Const.DELETE_ALARM_TIME, it)
                        }
                        Utils.setAlarm(this@CreateAlarmActivity, alarmData).forEach{
                            viewModel?.updateAlarmTimeData(Const.INSERT_ALARM_TIME, it)
                        }
                    }
                }
            }
            finish()
        }
    }


    private fun showDateDialog() {
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

    private fun initTime() {
        binding.apply {
            viewModel?.time?.observe(this@CreateAlarmActivity) { it ->
                val formatter =
                    DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                val localTime = LocalTime.parse(it.toString(), formatter)
                val localDateTime = LocalDateTime.of(LocalDate.now(), localTime)
                val zoneId = ZoneId.systemDefault()
                val date = Date.from(localDateTime.atZone(zoneId).toInstant())
                alarmCal.time = date
                tpAlarmTime.hour = alarmCal.get(Calendar.HOUR_OF_DAY)
                tpAlarmTime.minute = alarmCal.get(Calendar.MINUTE)
                viewModel?.logLine(
                    "confirm Time",
                    "time = $it, hour = ${tpAlarmTime.hour}, minute = ${tpAlarmTime.minute}"
                )
                executePendingBindings()
            }
        }
    }
}