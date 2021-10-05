package net.hermlon.gcgtimetable.ui.timetable

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.domain.TimetableDay

@AndroidEntryPoint
class TimetableDayFragment : Fragment() {

    private val viewModel: TimetableDayViewModel by viewModels()

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