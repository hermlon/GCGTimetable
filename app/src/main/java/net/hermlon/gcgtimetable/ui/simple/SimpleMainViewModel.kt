package net.hermlon.gcgtimetable.ui.simple

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.domain.TempSource
import org.threeten.bp.LocalDate
import javax.inject.Inject

data class RefreshingDate(val isRefreshing: Boolean, val date: LocalDate)


@HiltViewModel
class SimpleMainViewModel @Inject constructor(private val profileRepository: ProfileRepository) : ViewModel() {

    private val _isRefreshing = MutableLiveData<RefreshingDate>()
    val isRefreshing: LiveData<RefreshingDate> = _isRefreshing

    val noSourceAvailable = profileRepository.noSourceAvailable

    fun setRefreshing(refreshing: RefreshingDate) {
        _isRefreshing.value = refreshing
    }

    fun setDefaultSource(source: TempSource) {
        viewModelScope.launch {
            profileRepository.setDefaultSource(source)
        }
    }
}