package com.grusie.miraclealarm.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.activity.CreateAlarmActivity
import com.grusie.miraclealarm.databinding.ItemAlarmListBinding
import com.grusie.miraclealarm.model.data.AlarmData
import com.grusie.miraclealarm.util.Utils
import com.grusie.miraclealarm.viewmodel.AlarmViewModel

class AlarmListAdapter(
    private val context: Context,
    private val viewModel: AlarmViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<AlarmListAdapter.AlarmListViewHolder>() {
    var alarmList: MutableList<AlarmData> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmListViewHolder {
        val binding = ItemAlarmListBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_alarm_list, parent, false)
        )
        return AlarmListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmListViewHolder, position: Int) {
        val alarm = alarmList[position]
        holder.binding.cbAlarmSelect.isChecked =
            holder.binding.viewModel?.modifyList?.value?.contains(alarm) ?: false
        holder.bind(context, alarm, viewModel, lifecycleOwner)
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    inner class AlarmListViewHolder(val binding: ItemAlarmListBinding) : ViewHolder(binding.root) {
        fun bind(
            context: Context,
            alarm: AlarmData,
            viewModel: AlarmViewModel,
            lifecycleOwner: LifecycleOwner
        ) {
            binding.viewModel = viewModel
            binding.alarm = alarm

            val isDarkMode = Utils.isDarkModeEnabled(context)
            val textColorRes = if (binding.alarm?.enabled == true) {
                if (isDarkMode) R.color.white else R.color.black
            } else {
                R.color.dark_gray
            }

            binding.apply {
                tvAlarmTime.setTextColor(ContextCompat.getColor(context, textColorRes))
                tvAlarmTitle.setTextColor(ContextCompat.getColor(context, textColorRes))
                tvAlarmDate.setTextColor(ContextCompat.getColor(context, textColorRes))
            }

            binding.viewModel?.modifyMode?.observe(lifecycleOwner) {
                if (it) binding.cbAlarmSelect.visibility =
                    View.VISIBLE else binding.cbAlarmSelect.visibility = View.GONE
            }

            itemView.setOnClickListener {
                if (binding.viewModel?.modifyMode?.value == false) {
                    val intent = Intent(itemView.context, CreateAlarmActivity::class.java)
                    intent.putExtra("id", alarm.id)
                    binding.root.context.startActivity(intent)
                } else {
                    binding.cbAlarmSelect.isChecked = !binding.cbAlarmSelect.isChecked
                }
            }

            itemView.setOnLongClickListener {
                if (binding.viewModel?.modifyMode?.value == false) {
                    binding.viewModel?.changeModifyMode()
                    binding.cbAlarmSelect.isChecked = true
                }
                true
            }
        }
    }
}
