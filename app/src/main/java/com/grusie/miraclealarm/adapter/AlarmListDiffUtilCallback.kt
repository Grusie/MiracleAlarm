package com.grusie.miraclealarm.adapter

import androidx.recyclerview.widget.DiffUtil
import com.grusie.miraclealarm.model.data.AlarmUiModel

object AlarmListDiffUtilCallback : DiffUtil.ItemCallback<AlarmUiModel>() {
    override fun areItemsTheSame(oldItem: AlarmUiModel, newItem: AlarmUiModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AlarmUiModel, newItem: AlarmUiModel): Boolean {
        return oldItem == newItem
    }
}