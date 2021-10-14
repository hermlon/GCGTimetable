package net.hermlon.gcgtimetable.ui.profile.create

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.util.Event
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import javax.inject.Inject

@HiltViewModel
class LoginFragmentViewModel @Inject constructor(private var timetableRepository: TimetableRepository, private var profileRepository: ProfileRepository) : ViewModel() {

    private val _status = MutableLiveData<ResourceStatus>()
    val status: LiveData<ResourceStatus> = _status

    private val _success = MutableLiveData<Event<Boolean>>(Event(false))
    val success: LiveData<Event<Boolean>> = _success

    val isLoading = Transformations.map(status) {
        it == ResourceStatus.LOADING
    }

    fun onLogin(schoolNr: String, username: String, password: String, isStudent: Boolean) {
        viewModelScope.launch {
            _status.value = ResourceStatus.LOADING
            val source = TempSource(urlFromSchoolNr(schoolNr), isStudent, username, password)
            val res = timetableRepository.testSource(source)
            if(res == ResourceStatus.SUCCESS) {
                profileRepository.setDefaultSource(source)
                _success.value = Event(true)
            } else {
                _status.value = res
            }
        }
    }

    private fun urlFromSchoolNr(schoolNr: String): String {
        return "https://www.stundenplan24.de/$schoolNr/mobil"
    }

    fun resetNoSourceAvailable() = profileRepository.resetNoSourceAvailable()
}