package com.grusie.miraclealarm.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.grusie.miraclealarm.databinding.ItemAlarmSoundBinding
import com.grusie.miraclealarm.function.Utils

interface GetSelectedSound {
    fun getSelectedSound(item: String)
}

class SoundAdapter(
    val context: Context,
    val listener: GetSelectedSound,
    private val soundList: Array<String>
) : BaseAdapter() {
    private lateinit var binding: ItemAlarmSoundBinding
    var selectedPosition: Int = -1
    var playPosition: Int = -1

    override fun getCount(): Int {
        return soundList.size
    }

    override fun getItem(position: Int): String {
        return soundList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val viewHolder: SoundViewHolder
        if (view == null) {
            binding = ItemAlarmSoundBinding.inflate(LayoutInflater.from(context))
            viewHolder = SoundViewHolder(binding)
            view = binding.root
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as SoundViewHolder
        }

        val sound = getItem(position)
        viewHolder.bind(sound, position)

        val isSelected = selectedPosition == position
        viewHolder.setSelected(isSelected)

        return view
    }

    private inner class SoundViewHolder(private val binding: ItemAlarmSoundBinding) {
        fun bind(sound: String, position: Int) {
            binding.sound = sound
            binding.flag = false

            // 선택 상태 업데이트
            val isSelected = selectedPosition == position
            setSelected(isSelected)

            val isPlayed = playPosition == position
            setPlayed(isPlayed)

            binding.root.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()

                setSelected(true)

                listener.getSelectedSound(binding.rbAlarmSound.text as String)
                Log.d("confirm clickEvent", "$position, $selectedPosition, ${binding.rbAlarmSound.isChecked}")
            }

            binding.btnAlarmSound.setOnClickListener {
                if(!(binding.flag as Boolean)) {
                    playPosition = position
                    notifyDataSetChanged()
                    val playSound = Utils.getAlarmSound(context, sound)
                    Utils.stopAlarmSound()
                    Utils.playAlarmSound(context, playSound)
                    binding.flag = true
                }else {
                    Utils.stopAlarmSound()
                    binding.flag = false
                }
            }
        }
        fun setSelected(isSelected: Boolean) {
            binding.rbAlarmSound.isChecked = isSelected
        }

        fun setPlayed(isPlayed: Boolean) {
            binding.flag = isPlayed
        }
    }
}