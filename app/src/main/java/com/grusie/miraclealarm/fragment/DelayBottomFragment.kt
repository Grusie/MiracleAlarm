package com.grusie.miraclealarm.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.adapter.DelayAdapter
import com.grusie.miraclealarm.databinding.FragmentDelayBottomBinding
import com.grusie.miraclealarm.function.GetSelectedItem

class DelayBottomFragment : BottomSheetDialogFragment(), GetSelectedItem {
    var binding: FragmentDelayBottomBinding? = null
    private var selectedItem: String? = null
    private lateinit var adapter: DelayAdapter
    private lateinit var delayArray: Array<String>
    private lateinit var listener: OnDelayDataPassListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDelayBottomBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnDelayDataPassListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delayArray = resources.getStringArray(R.array.delay_array)
        selectedItem = arguments?.getString("delay")

        adapter = DelayAdapter(this, delayArray)
        binding?.rvAlarmDelay?.adapter = adapter
        binding?.rvAlarmDelay?.layoutManager = LinearLayoutManager(requireContext())
        adapter.selectedPosition = delayArray.indexOf(selectedItem)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun getSelectedItem(selectFlag: Boolean, position: Int) {
        selectedItem = delayArray[position]
        adapter.changeSelectedPosition(position)
        listener.onDelayDataPass(selectedItem)
        dismiss()
    }
}

interface OnDelayDataPassListener {
    fun onDelayDataPass(data: String?)
}