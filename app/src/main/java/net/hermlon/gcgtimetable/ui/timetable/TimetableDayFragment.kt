package net.hermlon.gcgtimetable.ui.timetable

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter.Companion.ARG_OBJECT

class TimetableDayFragment : Fragment() {

    companion object {
        fun newInstance() = TimetableDayFragment()
    }

    private lateinit var viewModel: TimetableDayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.timetable_day_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TimetableDayViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            val textView: TextView = view.findViewById(R.id.demoText)
            textView.text = getInt(ARG_OBJECT).toString() + " mod 5: " + (getInt(ARG_OBJECT) % 5).toString()
        }
    }

}