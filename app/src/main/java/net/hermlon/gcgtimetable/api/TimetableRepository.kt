package net.hermlon.gcgtimetable.api

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Deferred
import net.hermlon.gcgtimetable.database.TimetableLesson
import net.hermlon.gcgtimetable.database.TimetableSource
import okhttp3.*
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.*

class TimetableRepository {

    // TODO: Inject with Dagger
    private  val client = OkHttpClient()

    suspend fun fetch(source: TimetableSource, date: Date) {
        Log.d("TimetableRepository", "Fetch Class")

        // don't do this on Main Thread -> coroutines?

        val request = Request.Builder()
            .url(formatUrl(source.url, date, source.isStudent)).build()

        val response = client.newCall(request).await()

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        Log.d("TimetableRepository", "Response: ${response.body?.string()}")
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