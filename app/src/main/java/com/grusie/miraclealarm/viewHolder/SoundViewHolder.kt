package com.grusie.miraclealarm.viewHolder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.databinding.ItemAlarmValueListBinding
import com.grusie.miraclealarm.interfaces.GetSelectedItem
import com.grusie.miraclealarm.util.Utils

class SoundViewHolder(private val binding: ItemAlarmValueListBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        sound: String,
        listener: GetSelectedItem,
        position: Int,
        selectedPosition: Int,
        selectedSoundPosition: Int
    ) {

        binding.apply {
            checkedFlag = selectedPosition == position
            this.value = sound
            visible = true
            soundFlag = selectedSoundPosition == position

            val clickListener = View.OnClickListener {
                listener.getSelectedItem(true, position)
            }

            binding.rbAlarmSound.setOnClickListener(clickListener)
            itemView.setOnClickListener(clickListener)

            btnAlarmSound.setOnClickListener {
                if (!soundFlag!!) {
                    val playSound = Utils.getAlarmSound(context, sound)
                    Utils.stopAlarmSound(context)
                    Utils.playAlarmSound(context, playSound)
                } else {
                    Utils.stopAlarmSound(context)
                }
                soundFlag = !soundFlag!!
                listener.getSelectedItem(false, position)
            }
        }
    }
}