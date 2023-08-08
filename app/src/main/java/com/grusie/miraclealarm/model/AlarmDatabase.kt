package com.grusie.miraclealarm.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlarmData::class, AlarmTimeData::class], version = 2)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun alarmTimeDao(): AlarmTimeDao

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