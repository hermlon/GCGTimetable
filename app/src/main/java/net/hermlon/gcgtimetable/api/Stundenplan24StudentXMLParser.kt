package net.hermlon.gcgtimetable.api

import android.util.Log
import android.util.Xml
import net.hermlon.gcgtimetable.database.TimetableTimetable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val ns: String? = null

class Stundenplan24StudentXMLParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): TimetableTimetable {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()

            var freeDays: MutableList<Date> = mutableListOf()
            var updatedAt: Date = Date()
            var additionalInfo: String = ""

            parser.require(XmlPullParser.START_TAG, ns, "VpMobil")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "Kopf" -> updatedAt = readUpdatedAt(parser)
                    "FreieTage" -> freeDays = readFreeDays(parser)
                    "ZusatzInfo" -> additionalInfo = readAdditionalInfo(parser)
                    else -> skip(parser)
                }
            }

            Log.d("StudentXMLParser", "UpdatedAt: ${updatedAt.toString()}")
            Log.d("StudentXMLParser", "ZusatzInfo: ${additionalInfo}")

            return TimetableTimetable(
                updatedAt = updatedAt,
                information = additionalInfo
            )
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readUpdatedAt(parser: XmlPullParser): Date {
        parser.require(XmlPullParser.START_TAG, ns, "Kopf")

        var date: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "zeitstempel" -> date = readText(parser)
                else -> skip(parser)
            }
        }
        return SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.US).parse(date)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readAdditionalInfo(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "ZusatzInfo")

        var info: String = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "ZiZeile" -> info += readText(parser) + "\n"
                else -> skip(parser)
            }
        }
        return info
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readFreeDays(parser: XmlPullParser) : MutableList<Date> {
        parser.require(XmlPullParser.START_TAG, ns, "FreieTage")

        var freeDays = mutableListOf<Date>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "ft" -> freeDays.add(readDate(parser))
                else -> skip(parser)
            }
        }
        return freeDays
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDate(parser: XmlPullParser): Date {
        var date = readText(parser)
        return SimpleDateFormat("yyMMdd").parse(date)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

}