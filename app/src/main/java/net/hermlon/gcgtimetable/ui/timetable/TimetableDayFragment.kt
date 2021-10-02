package net.hermlon.gcgtimetable.ui.timetable

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter.Companion.ARG_DATE
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import org.threeten.bp.format.DateTimeFormatter
import java.lang.IllegalArgumentException

@AndroidEntryPoint
class TimetableDayFragment : Fragment() {

    private lateinit var viewModel: TimetableDayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentId = requireArguments().getSerializable(ARG_DATE).toString()
        viewModel = ViewModelProvider(this).get(fragmentId, TimetableDayViewModel::class.java)
        return inflater.inflate(R.layout.timetable_day_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.timetable.observe(viewLifecycleOwner) {
            val textView = view.findViewById<TextView>(R.id.demoText)
            var text = it.status.toString()
            if(it.data != null) {
                text += " " + it.data!!.lastRefresh.toString()
            }
            textView.text = viewModel.date.dayOfWeek.toString() + "\n\n" + viewModel.date.toString() + " " + text
        }
    }
}