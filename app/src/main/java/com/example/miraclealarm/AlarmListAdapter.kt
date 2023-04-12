package com.example.miraclealarm

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.miraclealarm.databinding.ItemAlarmListBinding

class AlarmListAdapter : RecyclerView.Adapter<AlarmListAdapter.AlarmListViewHolder>() {
    var alarmList: MutableList<AlarmData> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmListViewHolder {
        return AlarmListViewHolder(
            ItemAlarmListBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.item_alarm_list, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: AlarmListViewHolder, position: Int) {
        holder.binding.apply {
            tvAlarmTitle.text = alarmList[position].title
            tvAlarmTime.text = alarmList[position].time
            tvAlarmDate.text = alarmList[position].date
            swAlarm.isChecked = alarmList[position].flag
        }
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    inner class AlarmListViewHolder(val binding: ItemAlarmListBinding) : ViewHolder(binding.root)
}