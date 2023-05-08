package com.example.miraclealarm.activity

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miraclealarm.AlarmDateAdapter
import com.example.miraclealarm.viewmodel.AlarmViewModel
import com.example.miraclealarm.R
import com.example.miraclealarm.databinding.ActivityCreateAlarmBinding
import java.text.SimpleDateFormat
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
    private lateinit var cal: Calendar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        setContentView(binding.root)
        initUi()

    }

    private fun initUi() {
        alarmId = intent.getIntExtra("id", -1)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        cal = Calendar.getInstance()

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

            alarmViewModel.initAlarmData(alarmId)
            binding.viewModel?.timePickerToTime(
                binding.tpAlarmTime.hour,
                binding.tpAlarmTime.minute
            )

        binding.apply {
            val date = resources.getStringArray(R.array.array_date)

            val adapter = AlarmDateAdapter(alarmViewModel, date, this@CreateAlarmActivity)
            rvAlarmDate.adapter = adapter
            rvAlarmDate.layoutManager =
                LinearLayoutManager(this@CreateAlarmActivity, LinearLayoutManager.HORIZONTAL, false)

            tpAlarmTime.setOnTimeChangedListener { _, hour, minute ->
                viewModel?.timePickerToTime(hour, minute)
            }

            initTime()

            btnSave.setOnClickListener {
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
                    vibrate =
                        if (viewModel?.flagVibe?.value == true) tvVibeSub.text.toString() else vibrate
                    off_way =
                        if (viewModel?.flagOffWay?.value == true) tvOffWaySub.text.toString() else off_way
                    repeat =
                        if (viewModel?.flagRepeat?.value == true) tvRepeatSub.text.toString() else repeat
                }
                viewModel?.updateAlarmData()
                finish()
            }
            btnCancel.setOnClickListener {
                finish()
            }
            ivCalendar.setOnClickListener {
                initDate()
            }
        }
    }

    private fun initDate() {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dayOfWeek = SimpleDateFormat("EE").format(cal.time)
                val text =
                    if (year == Calendar.getInstance().get(Calendar.YEAR)) String.format("%02d월 %02d일 (%s)", month + 1, dayOfMonth, dayOfWeek)
                    else String.format("%04d년 %02d월 %02d일 (%s)", year, month + 1, dayOfMonth, dayOfWeek)
                binding.viewModel?.onDateClicked(text, false)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun initTime() {
        binding.apply {
            viewModel?.time?.observe(this@CreateAlarmActivity) { it ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val formatter =
                        DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                    val localTime = LocalTime.parse(it.toString(), formatter)
                    val localDateTime = LocalDateTime.of(LocalDate.now(), localTime)
                    val zoneId = ZoneId.systemDefault()
                    val date = Date.from(localDateTime.atZone(zoneId).toInstant())
                    cal.time = date
                } else {
                    val formatter = SimpleDateFormat("a hh:mm", Locale.KOREAN)
                    val date = formatter.parse(it)
                    cal.time = date
                }
                tpAlarmTime.hour = cal.get(Calendar.HOUR_OF_DAY)
                tpAlarmTime.minute = cal.get(Calendar.MINUTE)
                viewModel?.logLine(
                    "confirm Time",
                    "time = ${it}, hour = ${tpAlarmTime.hour}, minute = ${tpAlarmTime.minute}"
                )
                executePendingBindings()
            }
        }
    }
}