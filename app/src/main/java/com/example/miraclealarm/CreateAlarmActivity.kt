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

        alarmViewModel.initAlarmData(alarmId)

        binding.lifecycleOwner = this
        binding.apply {
            val date = resources.getStringArray(R.array.array_date)

            val adapter = AlarmDateAdapter(this@CreateAlarmActivity, date)
            rvAlarmDate.adapter = adapter
            rvAlarmDate.layoutManager = LinearLayoutManager(this@CreateAlarmActivity, LinearLayoutManager.HORIZONTAL, false)


            viewModel = alarmViewModel


            viewModel?.timePickerToTime(tpAlarmTime.hour, tpAlarmTime.minute)
            tpAlarmTime.setOnTimeChangedListener { timePicker, hour, minute ->
                viewModel?.timePickerToTime(hour, minute)
            }

            viewModel?.alarm?.observe(this@CreateAlarmActivity) { it ->
                if (it != null) {
                    viewModel?.logLine("alarm Object : ", "$it")
                    if (it.time.isNotEmpty()) {
                        val cal = Calendar.getInstance()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val formatter =
                                DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
                            val localTime =
                                LocalTime.parse(it.time.toString(), formatter)
                            val localDateTime = LocalDateTime.of(LocalDate.now(), localTime)
                            val zoneId = ZoneId.systemDefault()
                            val date = Date.from(localDateTime.atZone(zoneId).toInstant())
                            cal.time = date
                        } else {
                            val formatter = SimpleDateFormat("a hh:mm", Locale.KOREAN)
                            val date = it.time.let { formatter.parse(it) }
                            cal.time = date
                        }
                        tpAlarmTime.hour = cal.get(Calendar.HOUR_OF_DAY)
                        tpAlarmTime.minute = cal.get(Calendar.MINUTE)
                    }
                    executePendingBindings()
                }
            }

            btnSave.setOnClickListener {
                viewModel?.alarm?.value?.apply {
                    title = etAlarmTitle.text.toString()
                    //date = lvAlarmDate
                    holiday = viewModel?.flagHoliday?.value == true
                    flag = false
                    sound =
                        if (binding.viewModel?.flagSound?.value == true) tvSoundSub.text.toString() else ""
                    vibrate =
                        if (binding.viewModel?.flagVibe?.value == true) tvVibeSub.text.toString() else ""
                    off_way =
                        if (binding.viewModel?.flagOffWay?.value == true) tvOffWaySub.text.toString() else ""
                    repeat =
                        if (binding.viewModel?.flagRepeat?.value == true) tvRepeatSub.text.toString() else ""
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