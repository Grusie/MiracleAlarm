package com.grusie.miraclealarm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmValueListBinding
import com.grusie.miraclealarm.interfaces.GetSelectedItem
import com.grusie.miraclealarm.viewHolder.SoundViewHolder

class SoundAdapter(
    val context: Context,
    private val listener: GetSelectedItem,
    private val soundList: Array<String>
) : RecyclerView.Adapter<SoundViewHolder>() {
    private lateinit var binding: ItemAlarmValueListBinding
    var selectedPosition: Int = -1
    var selectedSoundPosition: Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        binding = ItemAlarmValueListBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alarm_value_list, parent, false)
        )

        return SoundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        holder.bind(
            context,
            soundList[position],
            listener,
            position,
            selectedPosition,
            selectedSoundPosition
        )
    }

    override fun getItemCount(): Int {
        return soundList.size
    }

    fun changeSelectedPosition(selectFlag: Boolean, position: Int) {
        if (selectFlag) {
            val prePosition = selectedPosition
            if (prePosition == position)
                return

            selectedPosition = position

            notifyItemChanged(prePosition)
            notifyItemChanged(selectedPosition)
        } else {
            val prePosition = selectedSoundPosition
            if (prePosition == position)
                return

            selectedSoundPosition = position

            notifyItemChanged(prePosition)
            notifyItemChanged(selectedSoundPosition)
        }
    }
}

