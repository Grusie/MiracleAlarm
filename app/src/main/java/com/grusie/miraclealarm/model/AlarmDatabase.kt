package com.grusie.miraclealarm.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlarmData::class, AlarmTimeData::class, AlarmTurnOffData::class], version = 3)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun alarmTimeDao(): AlarmTimeDao
    abstract fun alarmTurnOffDao(): AlarmTurnOffDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null
        var id = 0

        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "alarm_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }

}