package com.grusie.miraclealarm.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private lateinit var timeCal: Calendar

    //private lateinit var alarmCal: Calendar
    private lateinit var currCal: Calendar
    private lateinit var resultLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        setContentView(binding.root)
        initUi()
    }

    private fun initUi() {
        alarmId = intent.getIntExtra("id", -1)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        timeCal = Calendar.getInstance()

        currCal = Calendar.getInstance()
        currCal.add(Calendar.DAY_OF_YEAR, 1)

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Const.RESULT_CODE_SOUND) {
                val sound = result.data?.getStringExtra("sound")
                val volume = result.data?.getIntExtra("volume", -1)
                if (sound != null) {
                    binding.viewModel?.changeSound(sound)
                }
                if(volume != null && volume != -1){
                    binding.viewModel?.changeVolume(volume)
                }
            }
        }

        alarmViewModel.initAlarmData(alarmId)


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
        }
    }

    private fun startOptionActivity(intent: Intent, param1: String, param2: Int?) {
        intent.putExtra("param1", param1)
        if(param2 != null) intent.putExtra("param2", param2)

        resultLauncher.launch(intent)
    }

    private fun initDate() {
        val year: Int
        val month: Int
        val day: Int
        val dayOfWeek: Int
        currCal.apply {
            year = get(Calendar.YEAR)
            month = get(Calendar.MONTH)
            day = get(Calendar.DAY_OF_MONTH)
            dayOfWeek = get(Calendar.DAY_OF_WEEK)
        }

        binding.apply {
            viewModel?.date?.observe(this@CreateAlarmActivity) {
                viewModel?.logLine("confirm day_of_week", it)
                if (it.isNullOrEmpty()) {
                    val date = viewModel?.dateFormat(year, month, day, dayOfWeek)!!
                    viewModel?.onDateClicked(date, false)
                } else {
                    if (viewModel?.alarm?.value?.dateRepeat == false)
                        currCal = viewModel?.dateToCal(it, currCal)!!
                }
            }
        }
    }

    private fun saveAlarm() {
        binding.apply {
            viewModel?.alarm?.value?.apply {
                title = etAlarmTitle.text.toString()
                holiday = viewModel?.flagHoliday?.value == true
                flagSound = viewModel?.flagSound?.value == true
                flagVibrate = viewModel?.flagVibe?.value == true
                flagOffWay = viewModel?.flagOffWay?.value == true
                flagRepeat = viewModel?.flagRepeat?.value == true

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
                repeat =
                    if (viewModel?.flagRepeat?.value == true) tvRepeatSub.text.toString() else repeat
            }
            lifecycleScope.launch {
                viewModel?.getAlarmTime()!!.forEach { alarmTime ->
                    viewModel?.updateAlarmData()?.let {
                        Utils.setAlarm(
                            this@CreateAlarmActivity,
                            alarmTime,
                            it,
                        )
                    }
                }
            }
        }
        finish()
    }


    private fun showDateDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                currCal.set(Calendar.YEAR, year)
                currCal.set(Calendar.MONTH, month)
                currCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dayOfWeek = currCal.get(Calendar.DAY_OF_WEEK)

                val date = binding.viewModel?.dateFormat(year, month, dayOfMonth, dayOfWeek)!!
                binding.viewModel?.onDateClicked(date, false)
            },
            currCal.get(Calendar.YEAR),
            currCal.get(Calendar.MONTH),
            currCal.get(Calendar.DAY_OF_MONTH)
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
                timeCal.time = date
                tpAlarmTime.hour = timeCal.get(Calendar.HOUR_OF_DAY)
                tpAlarmTime.minute = timeCal.get(Calendar.MINUTE)
                viewModel?.logLine(
                    "confirm Time",
                    "time = $it, hour = ${tpAlarmTime.hour}, minute = ${tpAlarmTime.minute}"
                )
                executePendingBindings()
            }
        }
    }
}