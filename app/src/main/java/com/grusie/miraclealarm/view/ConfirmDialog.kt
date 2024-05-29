package com.grusie.miraclealarm.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.grusie.miraclealarm.databinding.DialogConfirmBinding

class ConfirmDialog(
    private val title: String,
    private val content: String,
    private val positiveButton: String,
    private val negativeButton: String,
    private val positiveCallback: () -> Unit,
    private val negativeCallback: () -> Unit
) : DialogFragment() {
    private val binding: DialogConfirmBinding by lazy { DialogConfirmBinding.inflate(layoutInflater) }
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = screenWidth - (40 * displayMetrics.density).toInt()
            val layoutParams = dialog.window?.attributes
            layoutParams?.apply {
                this.width = width
                gravity = Gravity.CENTER
            }
            dialog.setCanceledOnTouchOutside(false)
            dialog.window?.attributes = layoutParams
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.title = title
        binding.content = content
        binding.positive = positiveButton
        binding.negative = negativeButton

        binding.tvBtnCancel.setOnClickListener {
            negativeCallback()
            dialog?.dismiss()
        }
        binding.tvBtnConfirm.setOnClickListener {
            dialog?.dismiss()
            positiveCallback()
        }

        return binding.root
    }

    companion object {
        const val CONFIRM_DIALOG = "confirm_dialog"
    }
}