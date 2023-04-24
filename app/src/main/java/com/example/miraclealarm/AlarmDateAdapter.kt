package com.example.miraclealarm

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.miraclealarm.databinding.ItemAlarmDateBinding

class AlarmDateAdapter(private val context: Context, private val data: Array<String>) :
    RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlarmDateBinding.inflate(LayoutInflater.from(context))
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

}
class ViewHolder(private val binding: ItemAlarmDateBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(data: String, position: Int){

        val checkedColor = ContextCompat.getColor(binding.root.context,R.color.blue)
        val color = when(position){
            0 -> {
                ContextCompat.getColor(binding.root.context,R.color.red)
            }
            6 -> {
                ContextCompat.getColor(binding.root.context,R.color.light_blue)
            }
            else -> {
                ContextCompat.getColor(binding.root.context,R.color.black)
            }
        }
        val colorSelector = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)), intArrayOf(checkedColor,color))
        binding.cbItemDate.text = data

        binding.cbItemDate.setTextColor(colorSelector)
    }
}