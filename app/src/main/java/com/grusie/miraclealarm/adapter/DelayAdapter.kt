package com.grusie.miraclealarm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmValueListBinding
import com.grusie.miraclealarm.function.GetSelectedItem

class DelayAdapter(
    private val listener: GetSelectedItem,
    private val delayList: Array<String>
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
        return delayList.size
    }

    override fun onBindViewHolder(holder: DelayViewHolder, position: Int) {
        holder.onBind(delayList[position], listener, selectedPosition, position)
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

class DelayViewHolder(private val binding: ItemAlarmValueListBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun onBind(
        delay: String,
        listener: GetSelectedItem,
        selectedPosition: Int,
        position: Int
    ) {
        binding.value = delay
        binding.checkedFlag = selectedPosition == position
        binding.visible = false

        val clickListener = View.OnClickListener {
            listener.getSelectedItem(true, position)
        }

        binding.rbAlarmSound.setOnClickListener(clickListener)
        itemView.setOnClickListener(clickListener)
    }
}