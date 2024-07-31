package com.grusie.miraclealarm.interfaces

import android.os.SystemClock
import android.view.View

class OnSingleClickListener(
    private var interval: Int = INTERVAL_TIME,
    private var onSingleClick: (View) -> Unit,
) : View.OnClickListener {

    private var lastClickTime: Long = 0
    override fun onClick(view: View) {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if ((elapsedRealtime - lastClickTime) < interval) {
            return
        }
        lastClickTime = elapsedRealtime
        onSingleClick(view)
    }

    companion object {
        private const val INTERVAL_TIME = 600
    }
}

