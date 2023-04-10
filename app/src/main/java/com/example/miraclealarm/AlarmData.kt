package com.example.miraclealarm

import android.os.Parcel
import android.os.Parcelable


data class AlarmData(
    val id : Int,
    var title : String?,
    var time : String,
    var date : String,
    var flag : Boolean,
    var sound : String,
    var vibrate : String,
    var off_way : String?,
    var repeat : String?
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(time)
        parcel.writeString(date)
        parcel.writeByte(if (flag) 1 else 0)
        parcel.writeString(sound)
        parcel.writeString(vibrate)
        parcel.writeString(off_way)
        parcel.writeString(repeat)
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
