package net.hermlon.gcgtimetable.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import java.lang.IllegalArgumentException
import java.util.logging.Filter

@AndroidEntryPoint
class FilterFragment : Fragment() {

    private val viewModel: FilterFragmentViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
        toolbar.setupWithNavController(navController)
        toolbar.setOnMenuItemClickListener {
            it.onNavDestinationSelected(navController)
        }

        val filterFragmentAdapter = FilterFragmentAdapter(requireActivity())
        val viewPager: ViewPager2 = view.findViewById(R.id.pager_filter)
        viewPager.adapter = filterFragmentAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout_filter)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.filter_class_names)
                1 -> getString(R.string.filter_course_ids)
                else -> throw IllegalArgumentException("Position other than 0 or 1")
            }
        }.attach()
    }

    private val onDestinationChanged = NavController.OnDestinationChangedListener { _, destination, _ ->
        if(destination.id != R.id.fragment_filter) {
            viewModel.onLeave()
        }
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(onDestinationChanged)
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(onDestinationChanged)
    }
}