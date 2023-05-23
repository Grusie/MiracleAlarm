package com.example.miraclealarm.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.miraclealarm.viewmodel.AlarmViewModel
import com.example.miraclealarm.activity.CreateAlarmActivity
import com.example.miraclealarm.R
import com.example.miraclealarm.databinding.ItemAlarmListBinding
import com.example.miraclealarm.model.AlarmData

class AlarmListAdapter(private val viewModel: AlarmViewModel, private val lifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<AlarmListAdapter.AlarmListViewHolder>() {
    var alarmList: MutableList<AlarmData> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmListViewHolder {
        val binding = ItemAlarmListBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_alarm_list, parent, false)
        )
        return AlarmListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmListViewHolder, position: Int) {
        val alarm = alarmList[position]
        holder.binding.cbAlarmSelect.isChecked = holder.binding.viewModel?.modifyList?.value?.contains(alarm) ?: false
        holder.bind(alarm, viewModel, lifecycleOwner)
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    inner class AlarmListViewHolder(val binding: ItemAlarmListBinding) :
        ViewHolder(binding.root) {
        fun bind(alarm: AlarmData, viewModel: AlarmViewModel, lifecycleOwner: LifecycleOwner) {
            binding.viewModel = viewModel
            binding.alarm = alarm
            if(binding.alarm?.enabled == false){
                binding.tvAlarmTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.dark_gray))
                binding.tvAlarmDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.dark_gray))
                binding.tvAlarmTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.dark_gray))
            } else {
                binding.tvAlarmTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                binding.tvAlarmDate.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                binding.tvAlarmTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }
            binding.executePendingBindings()

            binding.viewModel?.modifyMode?.observe(lifecycleOwner){
                if(it) binding.cbAlarmSelect.visibility = View.VISIBLE else binding.cbAlarmSelect.visibility = View.GONE
            }
            
            val checkBoxListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    binding.viewModel?.modifyList?.value!!.add(alarm)
                }
                else{
                    binding.viewModel?.modifyList?.value!!.remove(alarm)
                }
                binding.viewModel?.logLine("confirm clickListener", "${alarm}, ${binding.viewModel?.modifyList?.value}")
            }

            binding.cbAlarmSelect.setOnCheckedChangeListener(checkBoxListener)

            itemView.setOnClickListener {
                if(binding.viewModel?.modifyMode?.value == false) {
                    val intent = Intent(itemView.context, CreateAlarmActivity::class.java)
                    intent.putExtra("id", alarm.id)
                    binding.root.context.startActivity(intent)
                }else {
                    binding.cbAlarmSelect.isChecked = !binding.cbAlarmSelect.isChecked
                }
            }

            itemView.setOnLongClickListener {
                if(binding.viewModel?.modifyMode?.value == false){
                    binding.viewModel?.modifyMode?.value = true

                    binding.viewModel?.modifyList?.value!!.add(alarm)
                    binding.cbAlarmSelect.isChecked = true
                }
                true
            }
        }
    }
}
