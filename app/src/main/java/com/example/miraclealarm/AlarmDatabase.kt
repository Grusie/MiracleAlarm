package com.example.miraclealarm

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [AlarmData::class], version = 1)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object{
        @Volatile
        private var INSTANCE: AlarmDatabase? = null
        var id = 0

        fun getDatabase(context: Context): AlarmDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, AlarmDatabase::class.java, "alarm_database").build()
                INSTANCE = instance
                instance
            }
        }

    }

}