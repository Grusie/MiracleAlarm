package com.grusie.miraclealarm.function

import android.app.Application

class ApplicationController : Application() {
    override fun onTerminate() {
        super.onTerminate()
        deleteDatabaseFile()
    }

    private fun deleteDatabaseFile() {
        val databaseName = "alarm_database.db" // 데이터베이스 파일 이름
        val context = applicationContext
        context.deleteDatabase(databaseName)
    }
}