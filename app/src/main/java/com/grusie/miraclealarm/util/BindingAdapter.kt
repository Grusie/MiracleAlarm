package com.grusie.miraclealarm.util

import android.widget.TextView
import androidx.databinding.BindingAdapter

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("leftTimeString")
    fun getTimeWithPattern(view: TextView, timeInMillis: Long?) {
        view.text =
            if (timeInMillis == null)
                view.context.getString(com.grusie.miraclealarm.R.string.str_turn_off_all)
            else Utils.createAlarmMessage(false, timeInMillis)
    }

}