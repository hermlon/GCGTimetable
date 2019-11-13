package net.hermlon.gcgtimetable.ui.profile.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

enum class LoginApiStatus { LOADING, ERROR_LOGIN, ERROR_URL, SUCCESS }

class LoginFragmentViewModel : ViewModel() {

    private val _status = MutableLiveData<LoginApiStatus>()
    val status: LiveData<LoginApiStatus>
        get() = _status

    val isLoading = Transformations.map(status) {
        status -> status == LoginApiStatus.LOADING
    }

    fun onLogin(schoolnr: String, username: String, passoword: String, isStudent: Boolean) {
        _status.value = LoginApiStatus.LOADING
    }

    private fun getAvailableClassFilters() {

    }
}