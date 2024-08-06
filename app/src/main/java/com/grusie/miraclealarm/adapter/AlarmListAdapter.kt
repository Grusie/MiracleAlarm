package com.grusie.miraclealarm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmListBinding
import com.grusie.miraclealarm.interfaces.AlarmListClickListener
import com.grusie.miraclealarm.model.data.AlarmUiModel
import com.grusie.miraclealarm.viewHolder.AlarmListViewHolder

class AlarmListAdapter(private val alarmListClickListener: AlarmListClickListener) :
    ListAdapter<AlarmUiModel, AlarmListViewHolder>(AlarmListDiffUtilCallback) {
    private var isDeleteMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmListViewHolder {
        val binding = ItemAlarmListBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_alarm_list, parent, false)
        )
        return AlarmListViewHolder(binding, alarmListClickListener)
    }

    override fun onBindViewHolder(holder: AlarmListViewHolder, position: Int) {
        holder.bind(getItem(position), isDeleteMode)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    private fun setModifyMode(isModifyMode: Boolean) {
        this.isDeleteMode = isModifyMode
    }
}
