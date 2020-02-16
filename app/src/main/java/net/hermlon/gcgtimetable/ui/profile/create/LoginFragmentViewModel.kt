package net.hermlon.gcgtimetable.ui.profile.create

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.database.getDatabase
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.util.Resource

enum class LoginApiStatus { LOADING, ERROR_LOGIN, ERROR_URL, SUCCESS }

class LoginFragmentViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val timetableRepository = TimetableRepository(database)

    val status = Transformations.map(timetableRepository.fetchResult) {
        if(it is Resource.Success) {
            LoginApiStatus.SUCCESS
        }
        else {
            if (it is Resource.Loading) LoginApiStatus.LOADING
            else {
                if(it.message == "wrong username") {
                    LoginApiStatus.ERROR_LOGIN
                }
                else {
                    LoginApiStatus.ERROR_URL
                }
            }
        }
    }

    val isLoading = Transformations.map(status) {
        it == LoginApiStatus.LOADING
    }

    fun onLogin(schoolNr: String, username: String, password: String, isStudent: Boolean) {
        viewModelScope.launch {
            val source = TempSource(urlFromSchoolNr(schoolNr), isStudent, username, password)
            /* fetch latest timetable to test configuration */
            val result = timetableRepository.getTimetable(source, null)
            if(result is Resource.Success) {
                timetableRepository.addSource(source, "Stundenplan24: $schoolNr")
            }
        }
    }

    private fun urlFromSchoolNr(schoolNr: String): String {
        return "https://www.stundenplan24.de/$schoolNr/mobil"
    }

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}