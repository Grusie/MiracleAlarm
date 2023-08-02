package com.grusie.miraclealarm.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.grusie.miraclealarm.Const
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.VibrationAdapter
import com.grusie.miraclealarm.databinding.ActivityVibrationBinding
import com.grusie.miraclealarm.function.GetSelectedItem
import com.grusie.miraclealarm.function.Utils

class VibrationActivity : AppCompatActivity(), GetSelectedItem{
    lateinit var binding: ActivityVibrationBinding
    lateinit var vibrationArray: Array<String>
    private var selectedItem :String? = null
    private lateinit var adapter: VibrationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_vibration)
        initUi()
    }

    private fun initUi() {
        vibrationArray = resources.getStringArray(R.array.vibration_array)
        selectedItem = intent.getStringExtra("param1")
        adapter = VibrationAdapter(this, this, vibrationArray)

        binding.rvAlarmSound.adapter = adapter
        binding.rvAlarmSound.layoutManager = LinearLayoutManager(this)
        adapter.selectedPosition = vibrationArray.indexOf(selectedItem)

        binding.btnSave.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("vibration", selectedItem)
            setResult(Const.RESULT_CODE_VIBRATION, resultIntent)
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
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