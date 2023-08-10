package com.grusie.miraclealarm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmValueListBinding
import com.grusie.miraclealarm.function.GetSelectedItem

class OffWayAdapter(
    private val listener: GetSelectedItem,
    private val offWayList: Array<String>
) : RecyclerView.Adapter<DelayViewHolder>() {
    lateinit var binding: ItemAlarmValueListBinding
    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DelayViewHolder {
        binding = ItemAlarmValueListBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alarm_value_list, parent, false)
        )
        return DelayViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return offWayList.size
    }

    override fun onBindViewHolder(holder: DelayViewHolder, position: Int) {
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

class OffWayViewHolder(private val binding: ItemAlarmValueListBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun onBind(
        offWay: String,
        listener: GetSelectedItem,
        selectedPosition: Int,
        position: Int
    ) {
        binding.value = offWay
        binding.checkedFlag = selectedPosition == position
        binding.visible = false

        val clickListener = View.OnClickListener {
            listener.getSelectedItem(true, position)
        }

        binding.rbAlarmSound.setOnClickListener(clickListener)
        itemView.setOnClickListener(clickListener)
    }
}