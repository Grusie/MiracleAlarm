package com.grusie.miraclealarm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmVibrationBinding
import com.grusie.miraclealarm.function.GetSelectedItem
import com.grusie.miraclealarm.generated.callback.OnClickListener

class VibrationAdapter(
    private val context: Context,
    private val listener: GetSelectedItem,
    private val vibrationList: Array<String>
) : RecyclerView.Adapter<VibrationViewHolder>() {
    lateinit var binding: ItemAlarmVibrationBinding
    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VibrationViewHolder {
        binding = ItemAlarmVibrationBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alarm_vibration, parent, false)
        )

        return VibrationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return vibrationList.size
    }

    override fun onBindViewHolder(holder: VibrationViewHolder, position: Int) {
        holder.onBind(context, vibrationList[position], listener, selectedPosition, position)
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

class VibrationViewHolder(private val binding: ItemAlarmVibrationBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun onBind(context: Context, vibration: String, listener: GetSelectedItem, selectedPosition: Int, position: Int) {
        binding.vibration = vibration
        binding.checkedFlag = selectedPosition == position

        val clickListener = View.OnClickListener {
            listener.getSelectedItem(true, position)
        }

        binding.rbAlarmSound.setOnClickListener(clickListener)
        itemView.setOnClickListener(clickListener)
    }
}