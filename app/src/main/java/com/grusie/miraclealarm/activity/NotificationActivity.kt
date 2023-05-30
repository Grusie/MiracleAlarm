package com.grusie.miraclealarm.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)

        binding.tvNotification.text = intent.getStringExtra("contentValue")
        setContentView(R.layout.activity_notification)
    }
}