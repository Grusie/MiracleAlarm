package com.example.miraclealarm

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        setContentView(binding.root)
        initUi()

    }

    private fun initUi() {
        alarmId = intent.getIntExtra("id", -1)
        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        binding.lifecycleOwner = this
        binding.viewModel = alarmViewModel

        if (!alarmViewModel.instanceFlag) {
            alarmViewModel.initAlarmData(alarmId)
            binding.viewModel?.timePickerToTime(binding.tpAlarmTime.hour, binding.tpAlarmTime.minute)
        }

        binding.apply {
            val date = resources.getStringArray(R.array.array_date)

            val adapter = AlarmDateAdapter(alarmViewModel,date, this@CreateAlarmActivity)
            rvAlarmDate.adapter = adapter
            rvAlarmDate.layoutManager =
                LinearLayoutManager(this@CreateAlarmActivity, LinearLayoutManager.HORIZONTAL, false)

            tpAlarmTime.setOnTimeChangedListener { timePicker, hour, minute ->
                viewModel?.timePickerToTime(hour, minute)
            }

            viewModel?.time?.observe(this@CreateAlarmActivity) { it ->
                val cal = Calendar.getInstance()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val formatter =
                        DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                    val localTime =
                        LocalTime.parse(it.toString(), formatter)
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

            btnSave.setOnClickListener {
                viewModel?.alarm?.value?.apply {
                    title = etAlarmTitle.text.toString()
                    holiday = viewModel?.flagHoliday?.value == true
                    flagSound = viewModel?.flagSound?.value == true
                    flagVibrate = viewModel?.flagVibe?.value == true
                    flagOffWay = viewModel?.flagOffWay?.value == true
                    flagRepeat = viewModel?.flagRepeat?.value == true

                    time = viewModel?.time?.value.toString()
                    this.date = viewModel?.sortDate()!!

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
        }
    }
}