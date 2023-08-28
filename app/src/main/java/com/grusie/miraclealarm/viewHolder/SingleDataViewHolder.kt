package com.grusie.miraclealarm.viewHolder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.databinding.ItemAlarmValueListBinding
import com.grusie.miraclealarm.interfaces.GetSelectedItem

class SingleDataViewHolder(private val binding: ItemAlarmValueListBinding) :
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