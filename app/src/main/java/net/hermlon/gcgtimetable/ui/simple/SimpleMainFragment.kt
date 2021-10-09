package net.hermlon.gcgtimetable.ui.simple

import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter
import net.hermlon.gcgtimetable.ui.timetable.WeekTabLayoutMediator
import org.threeten.bp.LocalDate
import java.text.DateFormatSymbols

@AndroidEntryPoint
class SimpleMainFragment : Fragment() {

    private val viewModel: SimpleMainViewModel by viewModels()

    private lateinit var timetableDayAdapter: TimetableDayAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
        toolbar.setupWithNavController(navController, AppBarConfiguration(setOf(R.id.simpleMainFragment)))
        toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.fragment_filter -> {
                    navController.navigate(R.id.fragment_filter)
                    true
                }
                R.id.stundenplan24LoginFragment -> {
                    navController.navigate(R.id.stundenplan24LoginFragment)
                    true
                }
                else -> false
            }
            // in the future use this directly, check on transitions and animation (back arrow sometimes shows up, sometimes doesn't, creates flickering)
            //it.onNavDestinationSelected(navController)
        }

        timetableDayAdapter = TimetableDayAdapter(requireActivity())
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = timetableDayAdapter
        viewPager.offscreenPageLimit = 1

        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        WeekTabLayoutMediator(tabLayout, viewPager, 5) { tab, position ->
            tab.text = DateFormatSymbols.getInstance().shortWeekdays[position+2]
        }.attach()

        viewPager.setCurrentItem(timetableDayAdapter.getPositionByDate(LocalDate.now()), false)

        val swipeRefresh: SwipeRefreshLayout = view.findViewById(R.id.refresh_timetable)
        swipeRefresh.setOnRefreshListener {
            viewModel.userRefresh(timetableDayAdapter.getDateByPosition(viewPager.currentItem))
        }

        viewModel.isLoading.observe(viewLifecycleOwner, {
            swipeRefresh.isRefreshing = it
        })

        viewModel.noSourceAvailable.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { noSource ->
                if(noSource) {
                    val oldLogin = getOldLogin()
                    if (oldLogin != null) {
                        viewModel.setDefaultSource(oldLogin)
                    } else {
                        // launch profile configuration activity
                        findNavController().navigate(R.id.action_login_s24)
                    }
                }
            }
        })
    }

    /**
     * reads legacy login data from v1 of GCGTimetable stored in SharedPreferences
     */
    private fun getOldLogin(): TempSource? {
        val settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
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