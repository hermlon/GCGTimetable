package net.hermlon.gcgtimetable.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.asDomainModel
import net.hermlon.gcgtimetable.domain.Profile
import net.hermlon.gcgtimetable.domain.TempSource
import javax.inject.Inject

class ProfileRepository @Inject constructor(private val database: TimetableDatabase){

    val profiles: LiveData<List<Profile>> = Transformations.map(database.profileDao.getProfiles()) {
        it.asDomainModel()
    }

    suspend fun addSource(source: TempSource, name: String) {
        withContext(Dispatchers.IO) {
            database.sourceDao.insert(DatabaseSource(0, name, source.url, source.isStudent, source.username, source.password))
        }
    }
}