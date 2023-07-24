package com.grusie.miraclealarm.activity

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
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.GetSelectedSound
import com.grusie.miraclealarm.adapter.SoundAdapter
import com.grusie.miraclealarm.databinding.ActivitySoundBinding
import com.grusie.miraclealarm.function.HeadsetReceiver
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.viewmodel.AlarmViewModel

class SoundActivity : AppCompatActivity(), GetSelectedSound,
    HeadsetReceiver.HeadsetConnectionListener {
    private lateinit var binding: ActivitySoundBinding
    private lateinit var soundArray: Array<String>
    private lateinit var adapter: SoundAdapter
    private var selectedItem: String? = null
    private var volume: Int = 0
    private val headsetReceiver = HeadsetReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sound)
        binding.viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]
        binding.lifecycleOwner = this

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

        Utils.initVolume(this)
        headsetCheck()

        binding.sbSoundVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                volume = p1
                Utils.changeVolume(this@SoundActivity, volume)
                binding.sbSoundVolume.setProgress(volume, true)
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
            setResult(Const.RESULT_CODE_SOUND, resultIntent)
            finish()
        }

        binding.btnSoundCancel.setOnClickListener {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        adapter.playPosition = -1
        Utils.changeVolume(this@SoundActivity, null)
        Utils.stopAlarmSound()
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

        volume = Utils.handleHeadsetConnection(this, isConnected, volume)
        changeProgressValue(isConnected, volume)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(headsetReceiver)
        }catch (e:Exception){ }
    }

    override fun getSelectedSound(item: String) {
        selectedItem = item
        binding.viewModel?.logLine("confirmSelectedItem", "$item, $selectedItem")
    }

    override fun onHeadsetConnected(isConnected: Boolean) {
        volume = Utils.handleHeadsetConnection(this, isConnected, volume)
        changeProgressValue(isConnected, volume)
    }

    private fun changeProgressValue(isConnected: Boolean, volume: Int){
        binding.sbSoundVolume.max = if(isConnected) 75 else 100
        Log.d("confirm max", "$isConnected, $volume, ${binding.sbSoundVolume.max}")
        binding.sbSoundVolume.setProgress(volume, true)
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}