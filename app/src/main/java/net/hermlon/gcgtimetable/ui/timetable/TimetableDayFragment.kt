package net.hermlon.gcgtimetable.ui.timetable

import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.ui.simple.RefreshingDate
import net.hermlon.gcgtimetable.ui.simple.SimpleMainViewModel
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.w3c.dom.Text
import javax.inject.Inject

@AndroidEntryPoint
class TimetableDayFragment : Fragment() {

    private val viewModel: TimetableDayViewModel by viewModels()

    @Inject lateinit var sharedPool: RecyclerView.RecycledViewPool

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timetable_day, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val recyclerView: RecyclerView = view.findViewById(R.id.lessons_recycler_view)
        val adapter = LessonListAdapter()
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(context)
        (recyclerView.layoutManager as LinearLayoutManager).initialPrefetchItemCount = 8
        recyclerView.setRecycledViewPool(sharedPool)

        val dayInformation: TextView = view.findViewById(R.id.timetable_day_information)
        val lastUpdate: TextView = view.findViewById(R.id.timetable_last_update)

        var lastData: TimetableDay? = null

        viewModel.timetable.observe(viewLifecycleOwner, { timetable ->
            timetable.data?.let {
                lastData = it
            }
            if(lastData != null) {
                //if(lastData!!.lessons.isEmpty()) {
                //    status.visibility = View.VISIBLE
                //    status.text = "nothing today (or maybe check filter)"
                //} else {
                //    status.visibility = View.GONE
                //}
                adapter.submitList(lastData!!.lessons)
                if(lastData!!.information != null) {
                    dayInformation.text = lastData!!.information
                    dayInformation.visibility = View.VISIBLE
                } else {
                    dayInformation.visibility = View.GONE
                }

                // works as long as you are in the same timezone as the dates are written in
                lastUpdate.text = DateUtils.getRelativeDateTimeString(context,
                    lastData!!.updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0)
            } else {
                //status.visibility = View.VISIBLE
                //status.text = timetable.status.toString()
            }
        })
    }
}