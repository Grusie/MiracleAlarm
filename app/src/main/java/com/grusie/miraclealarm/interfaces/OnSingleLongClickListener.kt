package com.grusie.miraclealarm.interfaces

import android.os.SystemClock
import android.view.View

class OnSingleLongClickListener(
    private var interval: Int = INTERVAL_TIME,
    private var onSingleClick: (View) -> Unit,
) : View.OnLongClickListener {

    private var lastClickTime: Long = 0
    override fun onLongClick(view: View): Boolean {
        val elapsedRealtime = SystemClock.elapsedRealtime()
        if ((elapsedRealtime - lastClickTime) < interval) {
            return false
        }
        lastClickTime = elapsedRealtime
        onSingleClick(view)
        return true
    }

    companion object {
        private const val INTERVAL_TIME = 1000
    }
}
