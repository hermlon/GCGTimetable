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
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.network.Webservice
import net.hermlon.gcgtimetable.network.asDatabaseModel
import net.hermlon.gcgtimetable.util.Resource
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

    val profiles: LiveData<List<Profile>> = Transformations.map(database.profileDao.getProfiles()) {
        it.asDomainModel()
    }

    val dayCache: HashMap<LocalDate, MutableLiveData<Resource<TimetableDay>>> = HashMap()

    val sourceId = 0L

    fun getTimetable(date: LocalDate): LiveData<Resource<TimetableDay>> {
        dayCache[date] = MutableLiveData()
        dayCache[date]!!.value = Resource(ResourceStatus.LOADING)
        return dayCache[date]!!
    }

    suspend fun refreshTimetable(source: DatabaseSource, date: LocalDate) {
        dayCache[date]!!.value = Resource(ResourceStatus.LOADING)
        // read old data from database
        val oldData = getTimetableDay(sourceId, date)
        if(oldData != null) {
            dayCache[date]!!.value = Resource(ResourceStatus.LOADING, oldData)
        }
        // fetch new data from server
        val res = webservice.fetch(source.asTempSource(), date)
        if(res.status == ResourceStatus.SUCCESS && res.data != null && date.isEqual(res.data!!.day.date)) {
            updateDatabase(source, res.data!!)
            dayCache[date]!!.value = Resource(ResourceStatus.SUCCESS, getTimetableDay(sourceId, date))
        } else {
            // make sure to show an error if 2nd and 3rd of the three if conditions fails
            dayCache[date]!!.value = Resource(if(res.status == ResourceStatus.SUCCESS) ResourceStatus.ERROR else res.status)
        }
    }

    private suspend fun getTimetableDay(sourceId: Long, date: LocalDate): TimetableDay? {
        return withContext(Dispatchers.IO) {
            val day = database.dayDao.getByDate(sourceId, date) ?: return@withContext null
            var lessons = database.lessonDao.getLessons(day.id).asDomainModel()
            var isStandard = false
            if(lessons.isEmpty()) {
                isStandard = true
                lessons = database.standardLessonDao.getStandardLessons(date.dayOfWeek.value).asDomainModel()
                Log.d("TimetableRepository", lessons.toString())
            }
            TimetableDay(
                isStandard = isStandard,
                date = day.date,
                updatedAt = day.updatedAt,
                lastRefresh = day.lastRefresh,
                information = day.information,
                lessons = lessons
            )
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