package com.grusie.miraclealarm.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.VibrationAdapter
import com.grusie.miraclealarm.databinding.ActivityVibrationBinding
import com.grusie.miraclealarm.function.GetSelectedItem
import com.grusie.miraclealarm.function.Utils

class VibrationActivity : AppCompatActivity(), GetSelectedItem {
    lateinit var binding: ActivityVibrationBinding
    private lateinit var vibrationArray: Array<String>
    private var selectedItem: String? = null
    private lateinit var adapter: VibrationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vibration)
        initUi()
    }

    private fun initUi() {
        vibrationArray = resources.getStringArray(R.array.vibration_array)
        selectedItem = intent.getStringExtra("param1")
        adapter = VibrationAdapter(this, vibrationArray)

        setSupportActionBar(binding.icToolbar.tbTitle)
        binding.icToolbar.title = "진동"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.rvAlarmSound.adapter = adapter
        binding.rvAlarmSound.layoutManager = LinearLayoutManager(this)
        adapter.selectedPosition = vibrationArray.indexOf(selectedItem)

        binding.btnSave.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("vibration", selectedItem)
            setResult(Const.RESULT_CODE_VIBRATION, resultIntent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSelectedItem(selectFlag: Boolean, position: Int) {
        selectedItem = vibrationArray[position]
        Utils.startVibrator(this, Utils.getVibrationEffect(this, selectedItem!!), -1)
        adapter.changeSelectedPosition(position)
    }

    override fun onStop() {
        super.onStop()
        Utils.stopVibrator()
    }
}