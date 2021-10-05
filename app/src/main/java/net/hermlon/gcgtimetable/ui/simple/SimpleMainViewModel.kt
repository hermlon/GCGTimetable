package net.hermlon.gcgtimetable.ui.simple

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDate

data class RefreshingDate(val isRefreshing: Boolean, val date: LocalDate)


class SimpleMainViewModel : ViewModel() {

    private val _isRefreshing = MutableLiveData<RefreshingDate>()
    val isRefreshing: LiveData<RefreshingDate> = _isRefreshing

    fun setRefreshing(refreshing: RefreshingDate) {
        _isRefreshing.value = refreshing
    }
}