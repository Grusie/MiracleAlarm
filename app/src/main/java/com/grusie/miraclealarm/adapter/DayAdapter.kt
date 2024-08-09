package com.grusie.miraclealarm.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.grusie.miraclealarm.model.data.DayUiModel
import com.grusie.miraclealarm.viewHolder.DayViewHolder

class DayAdapter(private val dayClickListener: (DayUiModel) -> Unit) :
    ListAdapter<DayUiModel, DayViewHolder>(DayDiffUtilCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        return DayViewHolder.from(parent, dayClickListener)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}