package net.hermlon.gcgtimetable.api

import android.text.format.DateFormat
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.TimetableSource
import net.hermlon.gcgtimetable.database.TimetableDay
import okhttp3.*
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.util.*

class TimetableRepository {

    // TODO: Inject with Dagger
    private  val client = OkHttpClient()

    suspend fun fetch(source: TimetableSource, date: Date) {
        // don't do this on Main Thread -> coroutines?

        val request = Request.Builder()
            .url(formatUrl(source.url, date, source.isStudent))
            .header("Authorization", Credentials.basic(source.username, source.password))
            .build()

        val response = client.newCall(request).await()

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val result: TimetableParseResult = response.body!!.byteStream().use { stream ->
            Stundenplan24StudentXMLParser().parse(stream)
        }
        // the date we fetched is the date of the result we got
        result.day.day = date
    }

    private fun formatUrl(url: String, date: Date, isStudent: Boolean): String {
        var datestring = DateFormat.format("yyyyMMdd", date)

        return if(isStudent) {
            "$url/mobdaten/PlanKl$datestring.xml"
        } else {
            "$url/mobdaten/PlanLe$datestring.xml"
        }

    }
}