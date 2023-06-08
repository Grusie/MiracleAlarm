package com.grusie.miraclealarm.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.grusie.miraclealarm.GetSelectedSound
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.SoundAdapter
import com.grusie.miraclealarm.databinding.ActivitySoundBinding
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import kotlin.properties.Delegates

class SoundActivity : AppCompatActivity(), GetSelectedSound {
    private lateinit var binding: ActivitySoundBinding
    private lateinit var soundArray: Array<String>
    private lateinit var adapter: SoundAdapter
    private var alarmId by Delegates.notNull<Int>()
    private var selectedItem: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sound)
        binding.viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.lifecycleOwner = this

        alarmId = intent.getIntExtra("alarmId", -1)
        selectedItem = intent.getStringExtra("detail")
        binding.viewModel?.initAlarmData(alarmId)
        soundArray = resources.getStringArray(R.array.sound_array)

        adapter = SoundAdapter(this, this, soundArray)
        adapter.selectedPosition = soundArray.indexOf(selectedItem)
        binding.lvAlarmSound.adapter = adapter

        binding.btnSoundSave.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("sound", selectedItem)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun getSelectedSound(item: String) {
        selectedItem = item
        binding.viewModel?.logLine("confirmSelectedItem", "$item, $selectedItem")
    }

}