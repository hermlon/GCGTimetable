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
import net.hermlon.gcgtimetable.network.asDatabaseModel
import net.hermlon.gcgtimetable.util.Resource
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import ru.gildor.coroutines.okhttp.await

class TimetableRepository(private val database: TimetableDatabase) {

    // TODO: Inject with Dagger
    private  val client = OkHttpClient()

    val fetchResult = MutableLiveData<Resource<NetworkParseResult>>()

    val profiles: LiveData<List<Profile>> = Transformations.map(database.profileDao.getProfiles()) {
        it.asDomainModel()
    }

    suspend fun getTimetable(source: TempSource, date: LocalDate?): Resource<NetworkParseResult> {
        fetchResult.value = Resource.Loading()
        val result = fetch(source, date)
        fetchResult.value = result
        return result
    }

    suspend fun refreshTimetableDay(source: DatabaseSource, date: LocalDate?) {
        val res = fetch(source.asTempSource(), date)
        if(res is Resource.Success && res.data != null) {
            updateDatabase(source, res.data)
        }
        else {
            throw Exception(res.message)
        }

    }

    suspend fun fetch(source: TempSource, date: LocalDate?): Resource<NetworkParseResult> {

        var requestBuild = Request.Builder()
            .url(formatUrl(source.url, date, source.isStudent))
        if(source.username != null && source.password != null) {
            requestBuild = requestBuild.header("Authorization", Credentials.basic(source.username, source.password))
        }
        val request = requestBuild.build()

        val response = client.newCall(request).await()

        if (!response.isSuccessful) {
            Log.e("TimetableRepository", response.toString())
            return when (response.code) {
                // TODO: somehow make these strings a constant somewhere
                401 -> Resource.Error("wrong username")
                else -> Resource.Error("wrong url")
            }
        }

        /* The parsing of the input steam mustn't happen on Main Thread, because it is slow
        * and more importantly will trigger Android's NetworkOnMainThreadException. Dispatchers.Default
        * could be used because parsing is CPU heavy, but I'm unsure to which extend the network request
        * is still running while accessing the input stream. During the network call OkHttp takes care
        * of main-safety by itself. */
        return withContext(Dispatchers.IO) {
            Resource.Success(Stundenplan24StudentXMLParser().parse(response.body!!.byteStream()))
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

    private fun formatUrl(url: String, date: LocalDate?, isStudent: Boolean): String {
        return if(date != null) {
            var datestring = date.format(DateTimeFormatter.BASIC_ISO_DATE)
            if(isStudent) {
                "$url/mobdaten/PlanKl$datestring.xml"
            } else {
                "$url/mobdaten/PlanLe$datestring.xml"
            }
        } else {
            //if(isStudent) {
                "$url/mobdaten/Klassen.xml"
            //} else {
            // TODO: implement default for teachers
            //}
        }
    }
}