package com.grusie.miraclealarm.viewHolder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemDayBinding
import com.grusie.miraclealarm.model.data.DayUiModel

class DayViewHolder(
    private val binding: ItemDayBinding,
    private val dayClickListener: (DayUiModel) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(dayUiModel: DayUiModel) {
        binding.dayUiModel = dayUiModel

        binding.cbDate1.setTextColor(
            itemView.context.getColor(
                when (dayUiModel.day) {
                    "일" -> R.color.red
                    "토" -> R.color.light_blue
                    else -> R.color.black_13131B
                }
            )
        )
        itemView.setOnClickListener { dayClickListener(dayUiModel) }
    }

    companion object {
        fun from(parent: ViewGroup, dayClickListener: (DayUiModel) -> Unit): DayViewHolder {
            return DayViewHolder(
                ItemDayBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                dayClickListener
            )
        }
    }
}