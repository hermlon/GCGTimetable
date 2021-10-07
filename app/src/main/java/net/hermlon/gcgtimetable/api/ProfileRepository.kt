package net.hermlon.gcgtimetable.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.asDomainModel
import net.hermlon.gcgtimetable.domain.Profile
import net.hermlon.gcgtimetable.domain.TempSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val database: TimetableDatabase){

    val profiles: LiveData<List<Profile>> = Transformations.map(database.profileDao.getProfiles()) {
        it.asDomainModel()
    }

    var cachedDefaultSource: DatabaseSource? = null

    private val _noSourceAvailable = MutableLiveData<Boolean>(false)
    val noSourceAvailable: LiveData<Boolean> = _noSourceAvailable

    suspend fun getDefaultSource(): DatabaseSource? {
        _noSourceAvailable.value = false
        if(cachedDefaultSource == null) {
            withContext(Dispatchers.IO) {
               cachedDefaultSource = database.sourceDao.get(Companion.DEFAULT_SOURCE_ID)
            }
            if(cachedDefaultSource == null) {
                // read from shared preferences or ask for setup
                _noSourceAvailable.value = true
            }
        }
        return cachedDefaultSource
    }

    suspend fun setDefaultSource(source: TempSource) {
        withContext(Dispatchers.IO) {
            database.sourceDao.upsert(DatabaseSource(Companion.DEFAULT_SOURCE_ID, "default source", source.url, source.isStudent, source.username, source.password))
        }
    }

    companion object {
        const val DEFAULT_SOURCE_ID = 1L
    }
}