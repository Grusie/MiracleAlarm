package com.grusie.miraclealarm.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alarm_table")      //알람 테이블
data class AlarmData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String = "",                //알람 제목
    var time: String = "",                 //알람 시간
    var holiday: Boolean = false,          //공휴일 알람 허용 여부
    var date: String = "",                 //알람 날짜 or 요일
    var dateRepeat: Boolean = false,       //알람 요일 반복 여부
    var enabled: Boolean = true,           //알람 사용 여부
    var sound: String = "탁상시계 알람소리",      //알람 소리
    var volume: Int = 1,                   //알람 볼륨
    var vibrate: String = "Basic call",    //알람 진동
    var off_way: String = "흔들어서 끄기", //알람 끄는 방법
    var delay: String = "5분",             //미루기
    var flagSound: Boolean = true,         //알람 소리 사용 여부
    var flagVibrate: Boolean = true,       //알람 진동 사용 여부
    var flagOffWay: Boolean = true,        //알람 끄는 방법 사용 여부
    var flagDelay: Boolean = false,        //알람 미루기 사용 여부
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(time)
        parcel.writeByte(if (holiday) 1 else 0)
        parcel.writeString(date)
        parcel.writeByte(if (dateRepeat) 1 else 0)
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeString(sound)
        parcel.writeInt(volume)
        parcel.writeString(vibrate)
        parcel.writeString(off_way)
        parcel.writeString(delay)
        parcel.writeByte(if (flagSound) 1 else 0)
        parcel.writeByte(if (flagVibrate) 1 else 0)
        parcel.writeByte(if (flagOffWay) 1 else 0)
        parcel.writeByte(if (flagDelay) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlarmData> {
        override fun createFromParcel(parcel: Parcel): AlarmData {
            return AlarmData(parcel)
        }

        override fun newArray(size: Int): Array<AlarmData?> {
            return arrayOfNulls(size)
        }
    }
}
