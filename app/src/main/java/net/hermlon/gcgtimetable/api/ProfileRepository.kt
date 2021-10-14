package net.hermlon.gcgtimetable.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.database.DatabaseProfile
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.asDomainModel
import net.hermlon.gcgtimetable.domain.Profile
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.util.Event
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val database: TimetableDatabase) {

    //val profiles: LiveData<List<Profile>> = Transformations.map(database.profileDao.getProfiles()) {
    //    it.asDomainModel()
    //}

    var cachedDefaultSource: DatabaseSource? = null
    var cachedDefaultProfile: DatabaseProfile? = null

    private val _noSourceAvailable = MutableLiveData<Event<Boolean>>(Event(false))

    val noSourceAvailable: LiveData<Event<Boolean>> = _noSourceAvailable

    fun resetNoSourceAvailable() {
        _noSourceAvailable.value = Event(false)
    }

    suspend fun getDefaultSource(): DatabaseSource? {
        if(cachedDefaultSource == null) {
            withContext(Dispatchers.IO) {
               cachedDefaultSource = database.sourceDao.get(DEFAULT_SOURCE_ID)
            }
            if(cachedDefaultSource == null) {
                // read from shared preferences or ask for setup
                // only set if it isn't already known there is no default source
                if(!noSourceAvailable.value!!.peekContent()) {
                    _noSourceAvailable.value = Event(true)
                }
            }
        }
        return cachedDefaultSource
    }

    suspend fun getDefaultProfile(): DatabaseProfile {
        if(cachedDefaultProfile == null) {
            withContext(Dispatchers.IO) {
                cachedDefaultProfile = database.profileDao.get(DEFAULT_PROFILE_ID)
                if(cachedDefaultProfile == null) {
                    database.profileDao.insert(
                        DatabaseProfile(
                            DEFAULT_PROFILE_ID,
                            "Default Profile",
                            DEFAULT_SOURCE_ID,
                            0
                        )
                    )
                    cachedDefaultProfile = database.profileDao.get(DEFAULT_PROFILE_ID)
                }
            }
        }
        return cachedDefaultProfile!!
    }

    suspend fun setDefaultSource(source: TempSource) {
        withContext(Dispatchers.IO) {
            // delete all old lessons
            database.dayDao.deleteAll()
            database.blacklistDao.deleteAll()
            database.whitelistDao.deleteAll()
            database.courseDao.deleteAll()
            database.examDao.deleteAll()
            database.standardLessonDao.deleteAll()
            database.lessonDao.deleteAll()
            database.sourceDao.upsert(DatabaseSource(Companion.DEFAULT_SOURCE_ID, "default source", source.url, source.isStudent, source.username, source.password))
        }
        resetNoSourceAvailable()
    }

    companion object {
        const val DEFAULT_SOURCE_ID = 1L
        const val DEFAULT_PROFILE_ID = 1L
    }
}