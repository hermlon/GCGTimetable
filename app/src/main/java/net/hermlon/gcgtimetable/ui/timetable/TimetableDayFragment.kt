package net.hermlon.gcgtimetable.ui.timetable

import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import net.hermlon.gcgtimetable.R
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.ui.simple.RefreshingDate
import net.hermlon.gcgtimetable.ui.simple.SimpleMainViewModel
import net.hermlon.gcgtimetable.util.ResourceStatus
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.w3c.dom.Text
import java.text.DateFormatSymbols
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

        val cardView: MaterialCardView = view.findViewById(R.id.timetable_day_cardview)
        val errorText: TextView = view.findViewById(R.id.timetable_error_text)
        val errorImage: ImageView = view.findViewById(R.id.timetable_error_image)
        val errorView: LinearLayout = view.findViewById(R.id.timetable_day_error_view)

        val filterButton: MaterialButton = view.findViewById(R.id.button_check_filter)
        filterButton.setOnClickListener {
            findNavController().navigate(R.id.fragment_filter)
        }

        var lastData: TimetableDay? = null

        viewModel.timetable.observe(viewLifecycleOwner, { timetable ->
            timetable.data?.let {
                lastData = it
            }
            if(lastData != null) {
                errorView.visibility = View.GONE
                cardView.visibility = View.VISIBLE

                if(lastData!!.lessons.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    errorText.text = getString(R.string.timetable_check_filter)
                    errorImage.setImageResource(R.drawable.ic_baseline_filter_list)
                    filterButton.visibility = View.VISIBLE
                    errorView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    adapter.submitList(lastData!!.lessons)
                }

                if(lastData!!.information != null) {
                    dayInformation.text = lastData!!.information
                    dayInformation.visibility = View.VISIBLE
                } else {
                    dayInformation.visibility = View.GONE
                }

                if(lastData!!.isStandard) {
                    lastUpdate.text = getString(R.string.as_every_weekday, DateFormatSymbols.getInstance().weekdays[lastData!!.date.dayOfWeek.value+1])
                } else {
                    // works as long as you are in the same timezone as the dates are written in
                    lastUpdate.text = DateUtils.getRelativeDateTimeString(context,
                        lastData!!.updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS * 2, DateUtils.FORMAT_SHOW_WEEKDAY).toString().replaceFirstChar { it.lowercase() }
                }
            } else {
                filterButton.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                cardView.visibility = View.GONE
                recyclerView.visibility = View.GONE
                when(timetable.status) {
                    ResourceStatus.ERROR_NOT_FOUND -> {
                        errorText.text = getString(R.string.timetable_error_not_found)
                        errorImage.setImageResource(R.drawable.ic_baseline_landscape)
                    }
                    ResourceStatus.ERROR_OFFLINE -> {
                        errorText.text = getString(R.string.timetable_error_offline)
                        errorImage.setImageResource(R.drawable.ic_baseline_cloud_off)
                    }
                    ResourceStatus.LOADING -> {}
                    else -> {
                        errorText.text = getString(R.string.timetable_error_unknown)
                        errorImage.setImageResource(R.drawable.ic_baseline_error)
                    }
                }
            }
        })
    }
}