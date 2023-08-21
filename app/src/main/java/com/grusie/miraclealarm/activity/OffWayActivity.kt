package com.grusie.miraclealarm.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.OffWayAdapter
import com.grusie.miraclealarm.databinding.ActivityOffWayBinding
import com.grusie.miraclealarm.function.GetSelectedItem
import com.grusie.miraclealarm.function.Utils

class OffWayActivity : AppCompatActivity(), GetSelectedItem {
    private lateinit var binding: ActivityOffWayBinding
    private lateinit var offWayArray: Array<String>
    private var selectedItem: String? = null
    private var offWayCount: Int = 0
    private lateinit var adapter: OffWayAdapter
    private var defaultCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_off_way)
        initUi()
    }

    private fun initUi() {
        offWayArray = resources.getStringArray(R.array.off_way_array)
        selectedItem = intent.getStringExtra("param1")
        defaultCount = when (selectedItem) {
            offWayArray[1] -> 3
            offWayArray[2] -> 10
            else -> 30
        }

        offWayCount = intent.getIntExtra("param2", defaultCount)

        adapter = OffWayAdapter(this, offWayArray)
        binding.icToolbar.title = "끄는 방법"
        setSupportActionBar(binding.icToolbar.tbTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.rvAlarmSound.adapter = adapter
        binding.rvAlarmSound.layoutManager = LinearLayoutManager(this)
        adapter.selectedPosition = offWayArray.indexOf(selectedItem)
    }

    override fun getSelectedItem(selectFlag: Boolean, position: Int) {
        selectedItem = offWayArray[position]
        adapter.changeSelectedPosition(position)

        val intent = Intent(this, OffWayCountActivity::class.java)
        intent.putExtra("offWay", selectedItem)
        intent.putExtra("offWayCount", offWayCount)
        startActivity(intent)
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

    override fun onStop() {
        super.onStop()
        Utils.stopVibrator()
    }
}