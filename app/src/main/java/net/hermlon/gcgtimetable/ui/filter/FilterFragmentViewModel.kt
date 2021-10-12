package net.hermlon.gcgtimetable.ui.filter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.api.FilterRepository
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.FilterClassName
import net.hermlon.gcgtimetable.database.FilterCourse
import javax.inject.Inject

@HiltViewModel
class FilterFragmentViewModel @Inject constructor(private val filterRepository: FilterRepository, private val profileRepository: ProfileRepository, private val timetableRepository: TimetableRepository) : ViewModel() {

    var classNames = filterRepository.classNames
    var filterCourses = filterRepository.filterCourses

    init {
        viewModelScope.launch {
            filterRepository.updateClassNames(profileRepository.getDefaultProfile())
            filterRepository.updateFilterCourses(profileRepository.getDefaultProfile())
        }
    }

    fun onClickClassName(className: FilterClassName) {
        viewModelScope.launch {
            filterRepository.updateClassName(profileRepository.getDefaultProfile(), className)
            filterRepository.updateFilterCourses(profileRepository.getDefaultProfile())
        }
    }

    fun onClickFilterCourse(filterCourse: FilterCourse) {
        viewModelScope.launch {
            filterRepository.updateFilterCourse(profileRepository.getDefaultProfile(), filterCourse)
        }
    }

    fun onLeave() {
        // viewModelScope might be killed in the meantime
        GlobalScope.launch {
            profileRepository.getDefaultSource()?.let {
                timetableRepository.refreshAll(profileRepository.getDefaultProfile(),
                    it
                )
            }
        }
    }
}