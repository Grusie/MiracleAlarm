package com.grusie.miraclealarm.viewHolder

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmListBinding
import com.grusie.miraclealarm.interfaces.AlarmListClickListener
import com.grusie.miraclealarm.model.data.AlarmUiModel
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.util.setOnSingleClickListener
import com.grusie.miraclealarm.util.setOnSingleLongClickListener

class AlarmListViewHolder(
    private val binding: ItemAlarmListBinding,
    private val alarmListClickListener: AlarmListClickListener
) : ViewHolder(binding.root) {
    fun bind(
        alarmUiModel: AlarmUiModel,
        isDeleteMode: Boolean
    ) {
        binding.alarm = alarmUiModel

        val isDarkMode = Utils.isDarkModeEnabled(itemView.context)
        val textColorRes = if (binding.alarm?.enabled == true) {
            if (isDarkMode) R.color.white else R.color.black
        } else {
            R.color.dark_gray
        }

        binding.apply {
            tvAlarmTime.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))
            tvAlarmTitle.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))
            tvAlarmDate.setTextColor(ContextCompat.getColor(itemView.context, textColorRes))

            binding.isDeleteMode = isDeleteMode
        }

        itemView.setOnSingleClickListener {
            alarmListClickListener.alarmOnClickListener(alarmUiModel)
        }

        itemView.setOnSingleLongClickListener {
            alarmListClickListener.alarmOnLongClickListener(alarmUiModel)
        }

        binding.swAlarm.setOnSingleClickListener {
            alarmListClickListener.changeAlarmEnable(alarmUiModel)
        }
    }
}