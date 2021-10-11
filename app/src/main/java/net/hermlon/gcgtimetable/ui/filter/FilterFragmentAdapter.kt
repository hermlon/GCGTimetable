package net.hermlon.gcgtimetable.ui.filter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.hermlon.gcgtimetable.ui.filter.classname.ClassNameFilterFragment
import net.hermlon.gcgtimetable.ui.filter.courseid.CourseIdFilterFragment
import java.lang.IllegalArgumentException

class FilterFragmentAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> ClassNameFilterFragment()
            1 -> CourseIdFilterFragment()
            else -> {
                throw IllegalArgumentException("Position other than 0 or 1")
            }
        }
    }
}