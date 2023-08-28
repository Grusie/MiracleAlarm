package com.grusie.miraclealarm.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.grusie.miraclealarm.interfaces.HeadsetConnectionListener

class HeadsetReceiver : BroadcastReceiver() {
    private var listener: HeadsetConnectionListener? = null

    fun setConnectionListener(listener: HeadsetConnectionListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> {
                val isConnected = intent.getIntExtra("state", 0) == 1
                listener?.onHeadsetConnected(isConnected)
            }

            BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    BluetoothProfile.EXTRA_STATE,
                    BluetoothProfile.STATE_DISCONNECTED
                )
                if (state == BluetoothHeadset.STATE_CONNECTED || state == BluetoothHeadset.STATE_DISCONNECTED) {
                    val isConnected = state == BluetoothAdapter.STATE_CONNECTED
                    listener?.onHeadsetConnected(isConnected)
                }
            }
        }
    }
}