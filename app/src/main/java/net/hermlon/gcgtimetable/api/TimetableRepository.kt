package net.hermlon.gcgtimetable.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.database.*
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.network.Webservice
import net.hermlon.gcgtimetable.network.asDatabaseModel
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(private val database: TimetableDatabase, private val webservice: Webservice) {

    private val _loadingCount = MutableLiveData<Int>(0)
    val loadingCount: LiveData<Int> = _loadingCount

    private val _timetableDays = HashMap<LocalDate, MutableLiveData<Resource<TimetableDay>>>()

    suspend fun refreshTimetable(profile: DatabaseProfile, source: DatabaseSource, date: LocalDate) {
        val liveData = getTimetableLiveData(date)
        startLoad()
        liveData.value = Resource(ResourceStatus.LOADING)
        // read old data from database
        val oldData = getTimetableDay(profile, date)
        if(oldData != null) {
            liveData.value = Resource(ResourceStatus.LOADING, oldData)
        }
        // fetch new data from server
        val res = webservice.fetch(source.asTempSource(), date)
        if(res.status == ResourceStatus.SUCCESS && date.isEqual(res.data!!.day.date)) {
            updateDatabase(source, res.data!!)
            liveData.value = Resource(ResourceStatus.SUCCESS, getTimetableDay(profile, date))
        } else {
            // make sure to show an error if 2nd condition of the if conditions fails
            liveData.value = Resource(if(res.status == ResourceStatus.SUCCESS) ResourceStatus.ERROR else res.status)
        }
        endLoad()
    }

    suspend fun refreshAll(profile: DatabaseProfile, source: DatabaseSource) {
        withContext(Dispatchers.Main) {
            startLoad()
            _timetableDays.keys.forEach {
                refreshTimetable(profile, source, it)
            }
            endLoad()
        }
    }

    suspend fun testSource(source: TempSource): ResourceStatus {
        // date is null to fetch Klassen.xml (latest available timetable)
        return webservice.fetch(source, null).status
    }

    fun getTimetableLiveData(date: LocalDate): MutableLiveData<Resource<TimetableDay>> {
        return _timetableDays.getOrPut(date, { MutableLiveData(Resource(ResourceStatus.LOADING)) })
    }

    fun clearTimetableLiveData(date: LocalDate) {
        _timetableDays.remove(date)
    }

    private fun startLoad() {
        if(_loadingCount.value == null) {
            _loadingCount.value = 1
        } else {
            _loadingCount.value = _loadingCount.value!! + 1
        }
    }

    private fun endLoad() {
        if(_loadingCount.value == null) {
            _loadingCount.value = 0
        } else {
            _loadingCount.value = _loadingCount.value!! - 1
        }
    }

    private suspend fun getTimetableDay(profile: DatabaseProfile, date: LocalDate): TimetableDay? {
        return withContext(Dispatchers.IO) {
            val day = database.dayDao.getByDate(profile.sourceId, date) ?: return@withContext null
            var lessons = database.lessonDao.getLessons(day.id, profile.id).asDomainModel()
            var isStandard = false
            if(lessons.isEmpty()) {
                isStandard = true
                lessons = database.standardLessonDao.getStandardLessons(date.dayOfWeek.value, profile.id).asDomainModel()
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

    suspend fun updateDatabase(source: DatabaseSource, newData: NetworkParseResult) {
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