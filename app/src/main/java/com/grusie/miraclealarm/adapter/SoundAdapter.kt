package com.grusie.miraclealarm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.databinding.ItemAlarmSoundBinding
import com.grusie.miraclealarm.function.GetSelectedItem
import com.grusie.miraclealarm.function.Utils

class SoundAdapter(
    val context: Context,
    private val listener: GetSelectedItem,
    private val soundList: Array<String>
) : RecyclerView.Adapter<SoundViewHolder>() {
    private lateinit var binding: ItemAlarmSoundBinding
    var selectedPosition: Int = -1
    var selectedSoundPosition: Int = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        binding = ItemAlarmSoundBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_alarm_sound, parent, false)
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

class SoundViewHolder(private val binding: ItemAlarmSoundBinding) :
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
            this.sound = sound
            soundFlag = selectedSoundPosition == position

            itemView.setOnClickListener {
                listener.getSelectedItem(true, position)
            }
            rbAlarmSound.setOnClickListener {
                listener.getSelectedItem(true, position)
            }

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