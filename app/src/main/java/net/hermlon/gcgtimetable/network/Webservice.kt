package net.hermlon.gcgtimetable.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.hermlon.gcgtimetable.api.Stundenplan24StudentXMLParser
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.xmlpull.v1.XmlPullParserException
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.lang.Exception
import java.net.UnknownHostException

class Webservice {

    private val client = OkHttpClient()

    suspend fun fetch(source: TempSource, date: LocalDate?): Resource<NetworkParseResult> {
        var requestBuild = Request.Builder()
            .url(formatUrl(source.url, date, source.isStudent))
        if(source.username != null && source.password != null) {
            requestBuild = requestBuild.header("Authorization", Credentials.basic(source.username, source.password))
        }
        val request = requestBuild.build()

        try {
            val response = client.newCall(request).await()

            if (!response.isSuccessful) {
                Log.e("TimetableRepository", "Error fetching data: " + response.headers + (response.body?.string() ?: ""))
                return when (response.code) {
                    401 -> Resource(ResourceStatus.ERROR_AUTH)
                    404 -> Resource(ResourceStatus.ERROR_NOT_FOUND)
                    else -> Resource(ResourceStatus.ERROR)
                }
            }

            /* The parsing of the input steam mustn't happen on Main Thread, because it is slow
            * and more importantly will trigger Android's NetworkOnMainThreadException. Dispatchers.Default
            * could be used because parsing is CPU heavy, but I'm unsure to which extend the network request
            * is still running while accessing the input stream. During the network call OkHttp takes care
            * of main-safety by itself. */
            return withContext(Dispatchers.IO) {
                try {
                    val parsedResult = Stundenplan24StudentXMLParser().parse(response.body!!.byteStream())
                    Resource(ResourceStatus.SUCCESS, parsedResult)
                } catch (e: XmlPullParserException) {
                    Log.e("TimetableRepository", e.toString() + e.stackTraceToString())
                    Resource(ResourceStatus.ERROR)
                } catch (e: IOException) {
                    Log.e("TimetableRepository", e.toString() + e.stackTraceToString())
                    Resource(ResourceStatus.ERROR)
                }
            }
        } catch(e: UnknownHostException) {
            return Resource(ResourceStatus.ERROR_OFFLINE)
        } catch(e: Exception) {
            Log.e("TimetableRepository", e.toString() + e.stackTraceToString())
            return Resource(ResourceStatus.ERROR)
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