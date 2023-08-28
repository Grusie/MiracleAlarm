package com.grusie.miraclealarm.activity

import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.SoundAdapter
import com.grusie.miraclealarm.databinding.ActivitySoundBinding
import com.grusie.miraclealarm.interfaces.GetSelectedItem
import com.grusie.miraclealarm.receiver.HeadsetReceiver
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.Utils.Companion.audioFocus
import com.grusie.miraclealarm.util.Utils.Companion.changeVolume
import com.grusie.miraclealarm.util.Utils.Companion.hasAudioFocus
import com.grusie.miraclealarm.interfaces.HeadsetConnectionListener

class SoundActivity : AppCompatActivity(), GetSelectedItem,
    HeadsetConnectionListener {
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

        Utils.createBlueToothPermission(this) { headsetCheck() }

        selectedItem = intent.getStringExtra("param1")

        soundArray = resources.getStringArray(R.array.sound_array)

        setSupportActionBar(binding.icToolbar.tbTitle)
        binding.icToolbar.title = getString(R.string.sound_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        adapter = SoundAdapter(this, this, soundArray)
        adapter.selectedPosition = soundArray.indexOf(selectedItem)
        binding.rvAlarmSound.adapter = adapter
        binding.rvAlarmSound.layoutManager = LinearLayoutManager(this)

        volume = intent.getIntExtra("param2", 0)

        binding.sbSoundVolume.setProgress(volume, true)

        Utils.initVolume(this)

        binding.sbSoundVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                volume = p1
                if (!hasAudioFocus)
                    audioFocus(this@SoundActivity)
                changeVolume(this@SoundActivity, volume, isConnected)
                changeVolumeFlag = true
                //binding.sbSoundVolume.setProgress(volume, true)
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

        if (isConnected) Toast.makeText(
            this,
            getString(R.string.str_headset_connected),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(headsetReceiver)
        } catch (e: Exception) {
            Log.e("confirm registerReceiverError", e.stackTraceToString())
        }
    }

    override fun onHeadsetConnected(isConnected: Boolean) {
        this.isConnected = isConnected
        Toast.makeText(this, getString(R.string.str_headset_connected), Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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