package com.grusie.miraclealarm.util

import com.grusie.miraclealarm.model.data.DayOfWeekProvider.daysOfWeek
import java.util.Calendar

fun timePickerToTimeString(hour: Int, minute: Int): String {
    val timeString: String
    val amPm = if (hour >= 12) "오후" else "오전"
    val displayHour = if (hour > 12) hour - 12 else hour

    timeString = String.format("$amPm %02d:%02d", displayHour, minute)

    return timeString
}

/**
 * 날짜 포맷
 * **/
fun Calendar.toDateFormat(): String {
    val year = this.get(Calendar.YEAR)
    val month = this.get(Calendar.MONTH)
    val day = this.get(Calendar.DAY_OF_MONTH)
    val dayOfWeek = this.get(Calendar.DAY_OF_WEEK)

    return if (year == Calendar.getInstance().get(Calendar.YEAR)) {
        String.format(
            "%02d월 %02d일 (%s)", month + 1, day, daysOfWeek[dayOfWeek - 1]
        )
    } else {
        String.format(
            "%04d년 %02d월 %02d일 (%s)", year, month + 1, day, daysOfWeek[dayOfWeek - 1]
        )
    }
}