package net.hermlon.gcgtimetable.ui.timetable

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAdjuster
import org.threeten.bp.temporal.TemporalAdjusters
import kotlin.math.abs


class TimetableDayAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // positionZero is 0 % 5 and therefore a monday
    private val dayZero = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val positionZero = 730000

    override fun getItemCount(): Int = 2 * positionZero

    override fun createFragment(position: Int): Fragment {
        val fragment = TimetableDayFragment()
        fragment.arguments = Bundle().apply {
            // pass date to fragment
            putSerializable(ARG_DATE, getDateByPosition(position))
        }
        return fragment
    }

    fun getPositionByDate(date: LocalDate): Int {
        var d = date
        // skip to next monday on weekend
        if(d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY) {
            d = date.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        }
        val offset = dayZero.until(d, ChronoUnit.DAYS).toInt()

        return if(offset >= 0) {
            positionZero + offset / 7 * 5 + offset % 7
        } else {
            positionZero + ((offset + 1) / 7 - 1) * 5 + 6 + (offset + 1) % 7
        }
    }

    fun getDateByPosition(position: Int): LocalDate {
        val offset = (position - positionZero).toLong()
        return if(offset >= 0) {
            dayZero.plusWeeks(offset / 5).plusDays(offset % 5)
        } else {
            dayZero.plusWeeks((offset + 1) / 5 - 1).plusDays(4 + ((offset + 1) % 5))
        }
    }

    companion object {
        const val ARG_DATE = "date"
    }
}