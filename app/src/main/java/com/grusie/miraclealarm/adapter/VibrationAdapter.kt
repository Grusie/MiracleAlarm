package com.grusie.miraclealarm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmValueListBinding
import com.grusie.miraclealarm.function.GetSelectedItem

class VibrationAdapter(
    private val listener: GetSelectedItem,
    private val vibrationList: Array<String>
) : RecyclerView.Adapter<VibrationViewHolder>() {
    lateinit var binding: ItemAlarmValueListBinding
    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VibrationViewHolder {
        binding = ItemAlarmValueListBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alarm_value_list, parent, false)
        )

        return VibrationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return vibrationList.size
    }

    override fun onBindViewHolder(holder: VibrationViewHolder, position: Int) {
        holder.onBind(vibrationList[position], listener, selectedPosition, position)
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

class VibrationViewHolder(private val binding: ItemAlarmValueListBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun onBind(
        vibration: String,
        listener: GetSelectedItem,
        selectedPosition: Int,
        position: Int
    ) {
        binding.value = vibration
        binding.checkedFlag = selectedPosition == position
        binding.visible = false

        val clickListener = View.OnClickListener {
            listener.getSelectedItem(true, position)
        }

        binding.rbAlarmSound.setOnClickListener(clickListener)
        itemView.setOnClickListener(clickListener)
    }
}