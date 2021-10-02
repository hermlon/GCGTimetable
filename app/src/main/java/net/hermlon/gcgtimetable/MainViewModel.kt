package net.hermlon.gcgtimetable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.api.TimetableRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(profileRepository: ProfileRepository) : ViewModel() {

    val profiles = profileRepository.profiles

}