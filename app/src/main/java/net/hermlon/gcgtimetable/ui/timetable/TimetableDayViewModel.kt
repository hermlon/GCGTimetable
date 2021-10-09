package net.hermlon.gcgtimetable.ui.timetable

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.domain.TempSource
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
    private val timetableRepository: TimetableRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    val date: LocalDate = savedStateHandle[ARG_DATE] ?: throw IllegalArgumentException("missing date")

    private val _timetable = timetableRepository.getTimetableLiveData(date)
    val timetable: LiveData<Resource<TimetableDay>> = _timetable

    val isLoading = Transformations.map(timetable) {
       it.status == ResourceStatus.LOADING
    }

    init {
        refresh()
    }

    fun tryRefresh() {
        if(isLoading.value == null || !isLoading.value!!) {
           refresh()
        }
    }

    fun refresh() {
        _timetable.value = Resource(ResourceStatus.LOADING)
        viewModelScope.launch {
            profileRepository.getDefaultSource()?.let {
                timetableRepository.refreshTimetable(it, date)
            }
            if(profileRepository.getDefaultSource() == null) {
                _timetable.value = Resource(ResourceStatus.ERROR)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timetableRepository.clearTimetableLiveData(date)
    }
}