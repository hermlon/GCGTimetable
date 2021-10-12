package net.hermlon.gcgtimetable.ui.simple

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.hermlon.gcgtimetable.api.FilterRepository
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.FilterClassName
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.util.Event
import org.threeten.bp.LocalDate
import javax.inject.Inject

data class RefreshingDate(val isRefreshing: Boolean, val date: LocalDate)


@HiltViewModel
class SimpleMainViewModel @Inject constructor(private val profileRepository: ProfileRepository, private val timetableRepository: TimetableRepository, private val filterRepository: FilterRepository) : ViewModel() {

    val isLoading = Transformations.map(timetableRepository.loadingCount) {
        it != 0
    }

    val noSourceAvailable = profileRepository.noSourceAvailable

    fun userRefresh(date: LocalDate) {
        viewModelScope.launch {
            profileRepository.getDefaultSource()?.let {
                timetableRepository.refreshTimetable(profileRepository.getDefaultProfile(), it, date)
            }
        }
    }

    fun setDefaultSource(source: TempSource) {
        viewModelScope.launch {
            profileRepository.setDefaultSource(source)
            profileRepository.getDefaultSource()?.let {
                timetableRepository.refreshAll(profileRepository.getDefaultProfile(),
                    it
                )
            }
        }
    }

    fun setFilter(filter: FilterClassName) {
        viewModelScope.launch {
            filterRepository.updateClassName(profileRepository.getDefaultProfile(), filter)
        }
    }
}