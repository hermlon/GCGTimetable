package net.hermlon.gcgtimetable.ui.timetable

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.DatabaseLesson
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter.Companion.ARG_DATE
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import org.threeten.bp.LocalDate
import java.lang.IllegalArgumentException
import javax.inject.Inject

@HiltViewModel
class TimetableDayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    timetableRepository: TimetableRepository
) : ViewModel() {
    val date: LocalDate = savedStateHandle[ARG_DATE] ?: throw IllegalArgumentException("missing date")

    private val _timetable = timetableRepository.timetable
    val timetable: LiveData<List<DatabaseLesson>> = _timetable

    private val _fetchStatus = timetableRepository.fetchStatus
    val fetchStatus: LiveData<ResourceStatus> = _fetchStatus

    init {
        viewModelScope.launch {
            val testsource = DatabaseSource(0, "test", "https://www.stundenplan24.de/10000000/mobil", true, "sfa", "pasefsf")
            timetableRepository.refreshTimetable(testsource, date)
        }
    }
}