package net.hermlon.gcgtimetable.ui.timetable

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.domain.TimetableLesson
import net.hermlon.gcgtimetable.ui.timetable.TimetableDayAdapter.Companion.ARG_DATE
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TimetableDayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val timetableRepository: TimetableRepository
) : ViewModel() {
    private val date: LocalDate = savedStateHandle[ARG_DATE] ?: throw IllegalArgumentException("missing date")

    private val _timetable = MutableLiveData<Resource<TimetableDay>>()
    val timetable: LiveData<Resource<TimetableDay>> = _timetable

    val isLoading = Transformations.map(timetable) {
       it.status == ResourceStatus.LOADING
    }

    init {
        refresh()
    }

    fun refresh() {
        _timetable.value = Resource(ResourceStatus.LOADING)
        viewModelScope.launch {
            val testsource = DatabaseSource(0, "test", "https://www.stundenplan24.de/10000000/mobil", true, "sfa", "pasefsf")
            timetableRepository.refreshTimetable(_timetable, testsource, date)
            /*delay(500)
            val res = Resource<TimetableDay>(ResourceStatus.SUCCESS)
            res.data = TimetableDay(
                true,
                date,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                listOf(
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(2, "Deu", false, "Gel", false, "108V", false, null, 5),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(2, "Deu", false, "Gel", false, "108V", false, null, 5),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(1, "Mat", false, "Kra", false, "304", false, null, 3),
                    TimetableLesson(2, "Deu", false, "Gel", false, "108V", false, null, 5),
                    TimetableLesson(3, "Geo", false, "Bro", true, "108V", false, "Geo bei Herr Brode statt Frau Lange", 5),
                    TimetableLesson(2, "Deu", false, "Gel", false, "108V", false, null, 5),
                    TimetableLesson(3, "Geo", false, "Bro", true, "108V", false, "Geo bei Herr Brode statt Frau Lange", 5),
                    TimetableLesson(2, "Deu", false, "Gel", false, "108V", false, null, 5),
                    TimetableLesson(3, "Geo", false, "Bro", true, "108V", false, "Geo bei Herr Brode statt Frau Lange", 5),
                )
            )
            _timetable.value = res*/
        }
    }
}