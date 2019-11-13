package net.hermlon.gcgtimetable.ui

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("invisibleUnless")
fun invisibleUnless(view: View, dontHide: Boolean) {
    view.visibility = if (dontHide) View.VISIBLE else View.INVISIBLE
}