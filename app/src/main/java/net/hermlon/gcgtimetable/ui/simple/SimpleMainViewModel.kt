package net.hermlon.gcgtimetable.ui.simple

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.hermlon.gcgtimetable.api.FilterRepository
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.FilterClassName
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.util.Event
import org.threeten.bp.LocalDate
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

data class RefreshingDate(val isRefreshing: Boolean, val date: LocalDate)


@HiltViewModel
class SimpleMainViewModel @Inject constructor(private val profileRepository: ProfileRepository, private val timetableRepository: TimetableRepository, private val filterRepository: FilterRepository) : ViewModel() {

    val prominentClassName: LiveData<String?> = Transformations.map(filterRepository.classNames) {
        it.filter {
            it.whitelisted
        }.let { whitelistedClassNames ->
            return@map when(whitelistedClassNames.size) {
                0 -> null
                1 -> whitelistedClassNames.first().className
                else -> whitelistedClassNames.first().className
            }
        }
    }

    val isLoading = Transformations.map(timetableRepository.loadingCount) {
        it != 0
    }

    val noSourceAvailable = profileRepository.noSourceAvailable

    init {
        refreshTitle()
        viewModelScope.launch {
            // delete old days and lessons, this shouldn't slow down the start
            delay(60000)
            timetableRepository.deleteOldData()
        }
    }

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

    fun refreshTitle() {
        viewModelScope.launch {
            filterRepository.updateClassNames(profileRepository.getDefaultProfile())
        }
    }
}