package net.hermlon.gcgtimetable.api

import android.text.format.DateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.database.DatabaseDay
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.DatabaseSource
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.network.asDatabaseModel
import okhttp3.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.util.*

class TimetableRepository(private val database: TimetableDatabase) {

    // TODO: Inject with Dagger
    private  val client = OkHttpClient()

    suspend fun fetch(source: DatabaseSource, date: LocalDate?) {
        // don't do this on Main Thread -> coroutines?

        var requestBuild = Request.Builder()
            .url(formatUrl(source.url, date, source.isStudent))
        if(source.username != null && source.password != null) {
            requestBuild = requestBuild.header("Authorization", Credentials.basic(source.username, source.password))
        }
        val request = requestBuild.build()

        val response = client.newCall(request).await()

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val result: NetworkParseResult = response.body!!.byteStream().use { stream ->
            Stundenplan24StudentXMLParser().parse(stream)
        }
        // the date we fetched is the date of the result we got
        //result.day.day = date

        withContext(Dispatchers.IO) {
            val dayId = database.dayDao.upsert(DatabaseDay(
                0,
                source.id,
                result.day.date,
                result.day.updatedAt,
                /* last time the xml file was fetched and parsed, i. e. now */
                LocalDateTime.now(),
                result.day.information))
            database.lessonDao.insertAll(*result.lessons.asDatabaseModel(dayId))
            database.courseDao.insertAll(*result.courses.asDatabaseModel())
            database.examDao.insertAll(*result.exams.asDatabaseModel(dayId))
            database.standardLessonDao.insertAll(*result.standardLessons.asDatabaseModel(result.day.date.dayOfWeek.value))
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