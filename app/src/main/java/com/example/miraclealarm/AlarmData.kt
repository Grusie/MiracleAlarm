package com.example.miraclealarm

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alarm_table")
data class AlarmData(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    var title : String = "",
    var time : String = "",
    var holiday : Boolean = false,
    var date : String = "",
    var enabled : Boolean = true,
    var sound : String = "Homecoming",
    var vibrate : String = "Basic call",
    var off_way : String = "흔들어서 끄기",
    var repeat : String = "5분,3회",
    var flagSound : Boolean = true,
    var flagVibrate : Boolean = true,
    var flagOffWay : Boolean = true,
    var flagRepeat : Boolean = false,
)
