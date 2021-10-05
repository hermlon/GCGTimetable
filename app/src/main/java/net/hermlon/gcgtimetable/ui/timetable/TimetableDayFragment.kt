package net.hermlon.gcgtimetable.ui.timetable

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.ui.simple.RefreshingDate
import net.hermlon.gcgtimetable.ui.simple.SimpleMainViewModel
import net.hermlon.gcgtimetable.util.ResourceStatus

@AndroidEntryPoint
class TimetableDayFragment(private val sharedPool: RecyclerView.RecycledViewPool) : Fragment() {

    private val viewModel: TimetableDayViewModel by viewModels()
    private val activityViewModel: SimpleMainViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timetable_day_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val status: TextView = view.findViewById(R.id.timetable_status)

        val recyclerView: RecyclerView = view.findViewById(R.id.lessons_recycler_view)
        val adapter = LessonListAdapter()
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(context)
        (recyclerView.layoutManager as LinearLayoutManager).initialPrefetchItemCount = 8
        recyclerView.setRecycledViewPool(sharedPool)

        var lastData: TimetableDay? = null

        viewModel.timetable.observe(viewLifecycleOwner, { timetable ->
            timetable.data?.let {
                lastData = it
            }
            if(lastData != null){
                status.visibility = View.GONE
                adapter.submitList(lastData!!.lessons)
            } else {
                status.visibility = View.VISIBLE
                status.text = timetable.status.toString()
            }
        })

        activityViewModel.isRefreshing.observe(viewLifecycleOwner, {
            if(it.isRefreshing && it.date == viewModel.date) {
               Log.d("REF", "try timetable refresh")
               viewModel.tryRefresh()
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {
            Log.d("REF", "set refreshing from fragment " + it.toString())
            activityViewModel.setRefreshing(RefreshingDate(it, viewModel.date))
        })
    }

    fun updateOldLogin() {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        viewModel.setOldProfile(
            "https://www.stundenplan24.de/" + settings.getString("schoolnr", "10000000") +"/mobil",
            settings.getString("username", null),
            settings.getString("password", null),
            settings.getString("grade", "") + "/" + settings.getString("subclass", "")
        )
    }
}