package com.grusie.miraclealarm.adapter

import androidx.recyclerview.widget.DiffUtil
import com.grusie.miraclealarm.model.data.DayUiModel

object DayDiffUtilCallback: DiffUtil.ItemCallback<DayUiModel>() {
    override fun areItemsTheSame(oldItem: DayUiModel, newItem: DayUiModel): Boolean {
        return oldItem.day == newItem.day
    }

    override fun areContentsTheSame(oldItem: DayUiModel, newItem: DayUiModel): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: DayUiModel, newItem: DayUiModel): Any? {
        return oldItem.isSelected == newItem.isSelected
    }
}