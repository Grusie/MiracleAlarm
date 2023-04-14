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
    var flag : Boolean = true,
    var sound : String = "",
    var vibrate : String = "",
    var off_way : String = "",
    var repeat : String = ""
)
