package com.grusie.miraclealarm

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.grusie.miraclealarm.databinding.ItemAlarmSoundBinding

interface GetSelectedSound {
    fun getSelectedSound(item: String)
}

class SoundAdapter(
    val context: Context,
    val listener: GetSelectedSound,
    val soundList: Array<String>
) : BaseAdapter() {
    private lateinit var binding: ItemAlarmSoundBinding
    var selectedPosition: Int = -1

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

        return view
    }

    private inner class SoundViewHolder(private val binding: ItemAlarmSoundBinding) {
        fun bind(sound: String, position: Int) {
            binding.sound = sound
            binding.rbAlarmSound.isChecked = selectedPosition == position

            binding.root.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()

                binding.rbAlarmSound.isChecked = true
                listener.getSelectedSound(binding.rbAlarmSound.text as String)
                Log.d("confirm clickEvent", "$position, $selectedPosition, ${binding.rbAlarmSound.isChecked}")
            }
        }
    }
}