package com.example.miraclealarm

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.miraclealarm.databinding.ItemAlarmListBinding

class AlarmListAdapter(private val viewModel: AlarmViewModel) :
    RecyclerView.Adapter<AlarmListAdapter.AlarmListViewHolder>() {
    var alarmList: MutableList<AlarmData> = arrayListOf()
/*
        set(value) {
            field.clear()
            field.addAll(value.sortedBy { it.time })
            notifyDataSetChanged()
        }
*/

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
            binding.executePendingBindings()
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, CreateAlarmActivity::class.java)
                intent.putExtra("id", alarm.id)
                binding.root.context.startActivity(intent)
            }
        }
    }
}
