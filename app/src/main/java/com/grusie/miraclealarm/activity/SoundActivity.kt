package com.grusie.miraclealarm.activity

import android.app.Activity
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.grusie.miraclealarm.GetSelectedSound
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.SoundAdapter
import com.grusie.miraclealarm.databinding.ActivitySoundBinding
import com.grusie.miraclealarm.function.HeadsetReceiver
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.viewmodel.AlarmViewModel
import kotlin.math.max

class SoundActivity : AppCompatActivity(), GetSelectedSound,
    HeadsetReceiver.HeadsetConnectionListener {
    private lateinit var binding: ActivitySoundBinding
    private lateinit var soundArray: Array<String>
    private lateinit var adapter: SoundAdapter
    private var selectedItem: String? = null
    private lateinit var am: AudioManager
    private var volume: Int = 0
    private var maxVolume: Int = 100
    private val headsetReceiver = HeadsetReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sound)
        binding.viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.lifecycleOwner = this
        am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager


        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            Utils.createPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            Utils.createPermission(android.Manifest.permission.BLUETOOTH)
            Utils.createPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
        }

        selectedItem = intent.getStringExtra("param1")

        soundArray = resources.getStringArray(R.array.sound_array)

        adapter = SoundAdapter(this, this, soundArray)
        adapter.selectedPosition = soundArray.indexOf(selectedItem)
        binding.lvAlarmSound.adapter = adapter

        volume = intent.getIntExtra("param2", 0)

        headsetCheck()

        binding.sbSoundVolume.setProgress(volume, true)
        binding.sbSoundVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                volume = p1
                am.getStreamVolume(AudioManager.STREAM_MUSIC)
                am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * p1 / 100,
                    AudioManager.FLAG_SHOW_UI
                )
                Log.d("confirm volume", "$volume")
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        binding.btnSoundSave.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("sound", selectedItem)
            resultIntent.putExtra("volume", volume)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.btnSoundCancel.setOnClickListener {
            finish()
        }
    }

    private fun headsetCheck() {
        // BroadcastReceiver 등록
        val intentFilter = IntentFilter()
        headsetReceiver.setConnectionListener(this)
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(headsetReceiver, intentFilter)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isConnected =
            audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn

        handleHeadsetConnection(isConnected)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(headsetReceiver)
    }

    override fun getSelectedSound(item: String) {
        selectedItem = item
        binding.viewModel?.logLine("confirmSelectedItem", "$item, $selectedItem")
    }

    override fun onHeadsetConnected(isConnected: Boolean) {
        handleHeadsetConnection(isConnected)
    }

    private fun handleHeadsetConnection(isConnected: Boolean) {
        if (isConnected) {
            maxVolume = 75
            if(binding.sbSoundVolume.progress > maxVolume){
                binding.sbSoundVolume.progress = maxVolume
            }else{
                am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volume / 100,
                    AudioManager.FLAG_SHOW_UI
                )
            }
            binding.sbSoundVolume.max = maxVolume
            Toast.makeText(this, "이어폰 착용으로 최대 소리가 줄어듭니다.", Toast.LENGTH_SHORT).show()
        } else {
            maxVolume = 100
            binding.sbSoundVolume.max = maxVolume
        }
    }
}