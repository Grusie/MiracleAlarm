package com.example.miraclealarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.miraclealarm.databinding.ActivityCreateAlarmBinding

class CreateAlarmActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateAlarmBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        binding.apply {
            btnSave.setOnClickListener {
                val intent = Intent(this@CreateAlarmActivity, MainActivity::class.java).apply {
                    val bundle = Bundle()
                    bundle.putParcelable(
                        Constant.KEY_ALARM_DATA,
                        AlarmData(10, "asdgfghghjgj", "16:30", "23-04-11", true, "", "", null, null)
                    )
                    putExtra(Constant.KEY_ALARM_DATA, bundle)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
            btnCancel.setOnClickListener {
                finish()
            }
        }
    }
}