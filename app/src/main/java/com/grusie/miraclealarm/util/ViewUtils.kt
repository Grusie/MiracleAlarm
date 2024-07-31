package com.grusie.miraclealarm.util

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.grusie.miraclealarm.R
import com.grusie.miraclealarm.interfaces.OnSingleClickListener
import com.grusie.miraclealarm.interfaces.OnSingleLongClickListener

fun View.makeSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .setBackgroundTint(context.getColor(R.color.white))
        .setTextColor(context.getColor(R.color.black_13131B))
        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE).show()
}

fun View.setOnSingleClickListener(onSingleClick: (View) -> Unit) {
    val onClick = OnSingleClickListener {
        onSingleClick(it)
    }
    setOnClickListener(onClick)
}

fun View.setOnSingleLongClickListener(onSingleLongClick: (View) -> Unit) {
    val onClick = OnSingleLongClickListener {
        onSingleLongClick(it)
    }
    setOnLongClickListener(onClick)
}