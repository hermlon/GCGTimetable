package net.hermlon.gcgtimetable.ui.timetable

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.threeten.bp.LocalDate


class TimetableDayAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): Fragment {
        val fragment = TimetableDayFragment()
        fragment.arguments = Bundle().apply {
            // pass date to fragment
            putSerializable(ARG_DATE, LocalDate.now())
        }
        return fragment
    }

    fun getPositionByDate(date: LocalDate) {

    }

    fun getDateByPosition(position: Int) {

    }

    companion object {
        const val ARG_DATE = "date"
    }
}