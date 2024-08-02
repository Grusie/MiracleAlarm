package com.grusie.miraclealarm.model.data

import android.content.Context
import com.grusie.miraclealarm.R


object DayOfWeekProvider {
    lateinit var daysOfWeek: Array<String>

    fun initialize(context: Context) {
        daysOfWeek =
            context.resources.getStringArray(R.array.day_array)
    }
}