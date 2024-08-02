package com.grusie.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.grusie.data.model.AlarmData
import com.grusie.data.model.AlarmTimeData
import com.grusie.data.model.AlarmTurnOffData

@Database(entities = [AlarmData::class, AlarmTimeData::class, AlarmTurnOffData::class], version = 4)
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
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // 마이그레이션 정의
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 예: database.execSQL("ALTER TABLE AlarmData ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 예: database.execSQL("ALTER TABLE AlarmData ADD COLUMN another_column TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 스키마 변경에 따른 SQL 문 추가
            }
        }
    }

}