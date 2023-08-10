package com.grusie.miraclealarm.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityOffWayCountBinding

class OffWayCountActivity : AppCompatActivity() {
    lateinit var binding: ActivityOffWayCountBinding
    var offWay: String? = null
    var offWayCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_off_way_count)
        initUi()
    }
    private fun initUi(){
        offWay = intent.getStringExtra("offWay")
        offWayCount = intent.getIntExtra("offWayCount", 0)

        binding.offWay = offWay

        val min = 5
        val max = 30
        val step = 5

        val values = (min..max step step).map { it.toString() }.toTypedArray()

        binding.npOffWayCount.apply {
            minValue = 0
            maxValue = values.size - 1
            displayedValues = values
            value = offWayCount

            setOnValueChangedListener { _, _, newVal ->
                offWayCount = newVal
            }
        }

        binding.btnSave.setOnClickListener {
            val intent = Intent(this, CreateAlarmActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra("offWay", offWay)
            intent.putExtra("offWayCount", offWayCount)

            startActivity(intent)
        }
    }
}