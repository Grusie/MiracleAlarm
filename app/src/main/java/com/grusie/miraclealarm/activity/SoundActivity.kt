package com.grusie.miraclealarm.activity

import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.SoundAdapter
import com.grusie.miraclealarm.databinding.ActivitySoundBinding
import com.grusie.miraclealarm.function.GetSelectedItem
import com.grusie.miraclealarm.function.HeadsetReceiver
import com.grusie.miraclealarm.function.Utils
import com.grusie.miraclealarm.function.Utils.Companion.audioFocus
import com.grusie.miraclealarm.function.Utils.Companion.changeVolume
import com.grusie.miraclealarm.function.Utils.Companion.hasAudioFocus

class SoundActivity : AppCompatActivity(), GetSelectedItem,
    HeadsetReceiver.HeadsetConnectionListener {
    private lateinit var binding: ActivitySoundBinding
    private lateinit var soundArray: Array<String>
    private lateinit var adapter: SoundAdapter
    private var selectedItem: String? = null
    private var volume: Int = 0
    private val headsetReceiver = HeadsetReceiver()
    private var isConnected = false
    private var changeVolumeFlag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sound)
        initUi()
    }

    private fun initUi() {
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
        binding.rvAlarmSound.adapter = adapter
        binding.rvAlarmSound.layoutManager = LinearLayoutManager(this)

        volume = intent.getIntExtra("param2", 0)

        binding.sbSoundVolume.setProgress(volume, true)

        Utils.initVolume(this)
        headsetCheck()

        binding.sbSoundVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                volume = p1
                if (!hasAudioFocus)
                    audioFocus(this@SoundActivity)
                changeVolume(this@SoundActivity, volume, isConnected)
                changeVolumeFlag = true
                binding.sbSoundVolume.setProgress(volume, true)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        binding.btnSave.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("sound", selectedItem)
            resultIntent.putExtra("volume", volume)
            setResult(Const.RESULT_CODE_SOUND, resultIntent)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        changeVolume(this@SoundActivity, null, isConnected)
        Utils.stopAlarmSound(this@SoundActivity)
    }

    private fun headsetCheck() {
        // BroadcastReceiver 등록
        val intentFilter = IntentFilter()
        headsetReceiver.setConnectionListener(this)
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(headsetReceiver, intentFilter)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isConnected =
            audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn

        if (isConnected) Toast.makeText(this, "이어폰 착용으로 최대 소리가 줄어듭니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(headsetReceiver)
        } catch (e: Exception) {
        }
    }

    override fun onHeadsetConnected(isConnected: Boolean) {
        this.isConnected = isConnected
        Toast.makeText(this, "이어폰 착용으로 최대 소리가 줄어듭니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun getSelectedItem(selectFlag: Boolean, position: Int) {
        selectedItem = soundArray[position]
        if (!selectFlag && changeVolumeFlag) {
            changeVolume(this, volume, isConnected)
            changeVolumeFlag = false
        }
        adapter.changeSelectedPosition(selectFlag, position)
    }
}