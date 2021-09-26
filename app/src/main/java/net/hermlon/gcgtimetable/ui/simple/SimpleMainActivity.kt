package net.hermlon.gcgtimetable.ui.simple

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter
import net.hermlon.gcgtimetable.ui.timetable.WeekTabLayoutMediator
import java.text.DateFormatSymbols

class SimpleMainActivity : AppCompatActivity() {

    private lateinit var timetableDayAdapter: TimetableDayAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_main)

        timetableDayAdapter = TimetableDayAdapter(this)
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = timetableDayAdapter

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        WeekTabLayoutMediator(tabLayout, viewPager, 5) { tab, position ->
            tab.text = DateFormatSymbols.getInstance().shortWeekdays[position+2]
        }.attach()

        viewPager.setCurrentItem(50, false)
    }
}