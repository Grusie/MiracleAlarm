package com.example.miraclealarm.activity

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miraclealarm.AlarmDateAdapter
import com.example.miraclealarm.R
import com.example.miraclealarm.databinding.ActivityCreateAlarmBinding
import com.example.miraclealarm.function.AlarmNotiReceiver
import com.example.miraclealarm.viewmodel.AlarmViewModel
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
    private lateinit var alarmCal: Calendar
    private lateinit var currCal: Calendar
    private lateinit var alarmManager : AlarmManager
    private lateinit var receiverIntent : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        setContentView(binding.root)
        initUi()
    }

    private fun initUi() {
        alarmId = intent.getIntExtra("id", -1)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        receiverIntent = Intent(this, AlarmNotiReceiver::class.java)

        cal = Calendar.getInstance()
        currCal = Calendar.getInstance()

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

        alarmViewModel.initAlarmData(alarmId)

        alarmCal = Calendar.getInstance()

        binding.viewModel?.timePickerToTime(
            binding.tpAlarmTime.hour,
            binding.tpAlarmTime.minute
        )

        binding.apply {
            val date = resources.getStringArray(R.array.array_date)

            val adapter = AlarmDateAdapter(alarmViewModel, date, this@CreateAlarmActivity)
            rvAlarmDate.adapter = adapter
            rvAlarmDate.layoutManager = LinearLayoutManager(this@CreateAlarmActivity, LinearLayoutManager.HORIZONTAL, false)

            val currYear = currCal.get(Calendar.YEAR)
            currCal.add(Calendar.DAY_OF_YEAR, 1)

            viewModel?.date?.observe(this@CreateAlarmActivity) {
                viewModel?.logLine("confirm day_of_week", it)
                if (it.isNullOrEmpty()) {
                    if (currCal.get(Calendar.YEAR) != currYear)
                        viewModel?.onDateClicked(
                            String.format(
                                "%04d년 %02d월 %02d일 (%s)",
                                currCal.get(Calendar.YEAR),
                                currCal.get(Calendar.MONTH) + 1,
                                currCal.get(Calendar.DAY_OF_MONTH),
                                viewModel?.daysOfWeek?.get(currCal.get(Calendar.DAY_OF_WEEK) - 1)
                            ), false
                        )
                    else
                        viewModel?.onDateClicked(
                            String.format(
                                "%02d월 %02d일 (%s)",
                                currCal.get(Calendar.MONTH) + 1,
                                currCal.get(Calendar.DAY_OF_MONTH),
                                viewModel?.daysOfWeek?.get(currCal.get(Calendar.DAY_OF_WEEK) - 1)
                            ), false
                        )
                }
                if (!viewModel?.alarm?.value?.dateRepeat!!) {
                    viewModel?.logLine("confirm initDate3", it)
                    alarmCal = viewModel?.dateToCal(it, alarmCal)!!
                }
                viewModel?.logLine("confirm initDate", it)
                viewModel?.logLine("confirm initDate", "${alarmCal.time}, ${alarmCal.get(Calendar.YEAR)}, ${alarmCal.get(Calendar.MONTH)+1}, ${alarmCal.get(Calendar.DAY_OF_MONTH)}")
            }

            tpAlarmTime.setOnTimeChangedListener { _, hour, minute ->
                viewModel?.timePickerToTime(hour, minute)
            }

            initTime()

            btnSave.setOnClickListener {
                saveAlarm()
            }
            btnCancel.setOnClickListener {
                finish()
            }
            ivCalendar.setOnClickListener {
                initDate()
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
                vibrate =
                    if (viewModel?.flagVibe?.value == true) tvVibeSub.text.toString() else vibrate
                off_way =
                    if (viewModel?.flagOffWay?.value == true) tvOffWaySub.text.toString() else off_way
                repeat =
                    if (viewModel?.flagRepeat?.value == true) tvRepeatSub.text.toString() else repeat
            }
            viewModel?.updateAlarmData()

            viewModel?.getAlarmTime()!!.forEach{ alarmTime ->
                setAlarm(alarmTime)
            }
            //viewModel?.logLine("confirm alarmDate", "${viewModel?.getAlarmTime()!!}")
            viewModel?.getAlarmTime()!!.forEach{ alarmTime ->
                viewModel?.logLine(
                    "confirm alarmDate",
                    SimpleDateFormat("yyyy MM dd, hh:mm:ss (EE)").format(alarmTime.time)
                )
            }
        }
        finish()
    }

    private fun setAlarm(alarmTime: Calendar) {
        receiverIntent.putExtra(
            "content",
            "${alarmTime.time} 알람"
        )

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            binding.viewModel?.alarm?.value?.id!!,
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // AlarmClockInfo 생성
        val alarmClockInfo = AlarmManager.AlarmClockInfo(alarmTime.timeInMillis, pendingIntent)

        // 알람 설정
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        binding.viewModel?.logLine("알람 설정", "${alarmTime.time} 알람이 설정됨")
    }

    private fun initDate() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                alarmCal.set(Calendar.YEAR, year)
                alarmCal.set(Calendar.MONTH, month)
                alarmCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                alarmCal.add(Calendar.DAY_OF_YEAR, 1)
                val dayOfWeek = SimpleDateFormat("EE").format(alarmCal.time)
                val text =
                    if (year == Calendar.getInstance()
                            .get(Calendar.YEAR)
                    ) String.format("%02d월 %02d일 (%s)", month + 1, dayOfMonth, dayOfWeek)
                    else String.format(
                        "%04d년 %02d월 %02d일 (%s)",
                        year,
                        month + 1,
                        dayOfMonth,
                        dayOfWeek
                    )
                binding.viewModel?.onDateClicked(text, false)
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
                cal.time = date
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