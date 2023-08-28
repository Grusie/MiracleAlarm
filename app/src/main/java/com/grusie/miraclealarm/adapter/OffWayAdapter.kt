package com.grusie.miraclealarm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmValueListBinding
import com.grusie.miraclealarm.interfaces.GetSelectedItem
import com.grusie.miraclealarm.viewHolder.SingleDataViewHolder

class OffWayAdapter(
    private val listener: GetSelectedItem,
    private val offWayList: Array<String>
) : RecyclerView.Adapter<SingleDataViewHolder>() {
    lateinit var binding: ItemAlarmValueListBinding
    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleDataViewHolder {
        binding = ItemAlarmValueListBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alarm_value_list, parent, false)
        )
        return SingleDataViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return offWayList.size
    }

    override fun onBindViewHolder(holder: SingleDataViewHolder, position: Int) {
        holder.onBind(offWayList[position], listener, selectedPosition, position)
    }

    fun changeSelectedPosition(position: Int) {
        val prePosition = selectedPosition
        if (prePosition == position)
            return

        selectedPosition = position

        notifyItemChanged(prePosition)
        notifyItemChanged(selectedPosition)
    }
}
