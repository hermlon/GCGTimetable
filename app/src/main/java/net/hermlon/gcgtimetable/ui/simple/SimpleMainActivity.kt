package net.hermlon.gcgtimetable.ui.simple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter
import net.hermlon.gcgtimetable.ui.timetable.WeekTabLayoutMediator
import org.threeten.bp.LocalDate
import java.text.DateFormatSymbols

@AndroidEntryPoint
class SimpleMainActivity : AppCompatActivity() {

    private val viewModel: SimpleMainViewModel by viewModels()

    private lateinit var timetableDayAdapter: TimetableDayAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_main)

        timetableDayAdapter = TimetableDayAdapter(this)
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = timetableDayAdapter
        viewPager.offscreenPageLimit = 1

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        WeekTabLayoutMediator(tabLayout, viewPager, 5) { tab, position ->
            tab.text = DateFormatSymbols.getInstance().shortWeekdays[position+2]
        }.attach()

        viewPager.setCurrentItem(timetableDayAdapter.getPositionByDate(LocalDate.now()), false)

        val swipeRefresh: SwipeRefreshLayout = findViewById(R.id.refresh_timetable)
        swipeRefresh.setOnRefreshListener {
            Log.d("REF", "setRefreshing true")
            viewModel.setRefreshing(RefreshingDate(true, timetableDayAdapter.getDateByPosition(viewPager.currentItem)))
        }
        viewModel.isRefreshing.observe(this, {
            Log.d("REF", "is refreshing " + it.toString())
            if(it.date == timetableDayAdapter.getDateByPosition(viewPager.currentItem)) {
                swipeRefresh.isRefreshing = it.isRefreshing
            }
        })

        viewModel.noSourceAvailable.observe(this, {
            if(it) {
                val oldLogin = getOldLogin()
                if(oldLogin != null) {
                    Toast.makeText(this, "Found old profile", Toast.LENGTH_SHORT).show()
                    viewModel.setDefaultSource(oldLogin)
                    viewModel.setRefreshing(RefreshingDate(true, timetableDayAdapter.getDateByPosition(viewPager.currentItem)))
                } else {
                    // launch profile configuration activity
                    Toast.makeText(this, "No old profile found", Toast.LENGTH_SHORT).show()
                }
            }
        })

        /*viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // overwrite date
                //viewModel.setRefreshing(RefreshingDate(swipeRefresh.isRefreshing, timetableDayAdapter.getDateByPosition(position)))
            }
        })*/
    }

    private fun getOldLogin(): TempSource? {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val schoolnum = settings.getString("schoolnr", null)
        if (schoolnum != null) {
            var grade = settings.getString("grade", "") + "/" + settings.getString("subclass", "")
            return TempSource(
                url = "https://www.stundenplan24.de/$schoolnum/mobil",
                isStudent = true,
                username = settings.getString("username", null),
                password = settings.getString("password", null)
            )
        } else {
            return null
        }
    }
}