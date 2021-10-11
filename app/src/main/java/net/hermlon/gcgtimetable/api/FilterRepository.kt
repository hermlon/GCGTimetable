package net.hermlon.gcgtimetable.api;

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.database.DatabaseClassNameWhitelist
import net.hermlon.gcgtimetable.database.DatabaseProfile
import net.hermlon.gcgtimetable.database.FilterClassName
import net.hermlon.gcgtimetable.database.TimetableDatabase;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class FilterRepository @Inject constructor(private val database: TimetableDatabase) {

    private val _classNames = MutableLiveData<List<FilterClassName>>()
    val classNames: LiveData<List<FilterClassName>> = _classNames

    suspend fun updateClassNames(profile: DatabaseProfile) {
        var result: List<FilterClassName>
        withContext(Dispatchers.IO) {
            result = database.courseDao.getFilterClassNames(profile.id)
        }
        _classNames.value = result
    }

    suspend fun updateClassName(profile: DatabaseProfile, className: FilterClassName) {
        withContext(Dispatchers.IO) {
            if(className.whitelisted) {
                database.whitelistDao.whitelist(DatabaseClassNameWhitelist(profile.id, className.className))
            } else {
                database.whitelistDao.delete(DatabaseClassNameWhitelist(profile.id, className.className))
            }
        }
        updateClassNames(profile)
    }
}