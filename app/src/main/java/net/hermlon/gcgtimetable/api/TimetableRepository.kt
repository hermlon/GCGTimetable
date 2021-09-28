package net.hermlon.gcgtimetable.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.database.*
import net.hermlon.gcgtimetable.domain.Profile
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.network.Webservice
import net.hermlon.gcgtimetable.network.asDatabaseModel
import net.hermlon.gcgtimetable.util.ResourceStatus
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import ru.gildor.coroutines.okhttp.await
import java.lang.Exception
import javax.inject.Inject

class TimetableRepository @Inject constructor(private val database: TimetableDatabase, private val webservice: Webservice) {

    val fetchStatus = MutableLiveData<ResourceStatus>()

    val profiles: LiveData<List<Profile>> = Transformations.map(database.profileDao.getProfiles()) {
        it.asDomainModel()
    }

    val timetable: LiveData<List<DatabaseLesson>> = database.lessonDao.getLessons()

    suspend fun refreshTimetable(source: DatabaseSource, date: LocalDate?) {
        fetchStatus.value = ResourceStatus.LOADING
        val res = webservice.fetch(source.asTempSource(), date)
        if(res.status == ResourceStatus.SUCCESS && res.data != null) {
            updateDatabase(source, res.data!!)
            fetchStatus.value = ResourceStatus.SUCCESS
        } else {
            fetchStatus.value = res.status
        }
    }

    suspend fun addSource(source: TempSource, name: String) {
        withContext(Dispatchers.IO) {
            database.sourceDao.insert(DatabaseSource(0, name, source.url, source.isStudent, source.username, source.password))
        }
    }

    private suspend fun updateDatabase(source: DatabaseSource, newData: NetworkParseResult) {
        withContext(Dispatchers.IO) {
            val dayId = database.dayDao.upsert(DatabaseDay(
                0,
                source.id,
                newData.day.date,
                newData.day.updatedAt,
                /* last time the xml file was fetched and parsed, i. e. now */
                LocalDateTime.now(),
                newData.day.information))
            database.lessonDao.insertAll(*newData.lessons.asDatabaseModel(dayId))
            database.courseDao.insertAll(*newData.courses.asDatabaseModel())
            database.examDao.insertAll(*newData.exams.asDatabaseModel(dayId))
            database.standardLessonDao.insertAll(*newData.standardLessons.asDatabaseModel(newData.day.date.dayOfWeek.value))
        }
    }
}