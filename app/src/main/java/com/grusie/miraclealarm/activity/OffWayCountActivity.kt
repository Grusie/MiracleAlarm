package com.grusie.miraclealarm.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ActivityOffWayCountBinding

class OffWayCountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOffWayCountBinding
    private var offWay: String? = null
    private var offWayCount = 0
    private lateinit var offWayArray: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_off_way_count)
        initUi()
    }

    private fun initUi() {
        offWayArray = resources.getStringArray(R.array.off_way_array)
        offWay = intent.getStringExtra("offWay")
        offWayCount = intent.getIntExtra("offWayCount", 0)

        binding.offWay = offWay
        val min: Int
        val max: Int
        val step: Int

        when (offWay) {
            offWayArray[1] -> {
                min = 1
                max = 7
                step = 1
            }

            offWayArray[2] -> {
                min = 2
                max = 20
                step = 2
            }

            else -> {
                min = 10
                max = 70
                step = 5
            }
        }

        val values = (min..max step step).map { it.toString() }.toTypedArray()

        binding.npOffWayCount.apply {
            minValue = 1
            maxValue = values.size
            displayedValues = values

            val index = values.indexOf(offWayCount.toString())
            value = if (index != -1) {
                index + 1
            } else {
                displayedValues[values.size / 2].toInt()
            }
            offWayCount = displayedValues[value - 1].toInt()

            setOnValueChangedListener { _, _, newVal ->
                offWayCount = displayedValues[newVal - 1].toInt()
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