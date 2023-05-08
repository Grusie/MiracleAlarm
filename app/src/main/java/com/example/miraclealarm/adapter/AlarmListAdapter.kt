package com.example.miraclealarm.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.miraclealarm.viewmodel.AlarmViewModel
import com.example.miraclealarm.activity.CreateAlarmActivity
import com.example.miraclealarm.R
import com.example.miraclealarm.databinding.ItemAlarmListBinding
import com.example.miraclealarm.model.AlarmData

class AlarmListAdapter(private val viewModel: AlarmViewModel) :
    RecyclerView.Adapter<AlarmListAdapter.AlarmListViewHolder>() {
    var alarmList: MutableList<AlarmData> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmListViewHolder {
        val binding = ItemAlarmListBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_alarm_list, parent, false)
        )
        return AlarmListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmListViewHolder, position: Int) {
        holder.bind(alarmList[position], viewModel)
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    inner class AlarmListViewHolder(private val binding: ItemAlarmListBinding) :
        ViewHolder(binding.root) {
        fun bind(alarm: AlarmData, viewModel: AlarmViewModel) {
            binding.viewModel = viewModel
            binding.alarm = alarm
            if(binding.alarm?.enabled == false){
                binding.tvAlarmTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.dark_gray))
                binding.tvAlarmDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.dark_gray))
                binding.tvAlarmTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.dark_gray))
            } else {
                binding.tvAlarmTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                binding.tvAlarmDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                binding.tvAlarmTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }
            binding.executePendingBindings()
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, CreateAlarmActivity::class.java)
                intent.putExtra("id", alarm.id)
                binding.root.context.startActivity(intent)
            }
        }
    }
}
