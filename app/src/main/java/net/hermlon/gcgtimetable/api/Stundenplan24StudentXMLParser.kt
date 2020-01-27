package net.hermlon.gcgtimetable.api

import android.util.Log
import android.util.Xml
import net.hermlon.gcgtimetable.database.TimetableDay
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.network.NetworkTimetableDay
import net.hermlon.gcgtimetable.network.NetworkTimetableLesson
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.list

private val ns: String? = null

class Stundenplan24StudentXMLParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): NetworkParseResult {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()

            var lessons: MutableList<NetworkTimetableLesson> = mutableListOf()
            var updatedAt: Date = Date()
            var information: String = ""
            var freeDays: MutableList<Date> = mutableListOf()

            parser.require(XmlPullParser.START_TAG, ns, "VpMobil")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "Kopf" -> updatedAt = readUpdatedAt(parser)
                    "FreieTage" -> freeDays = readFreeDays(parser)
                    "Klassen" -> lessons = readClasses(parser)
                    "ZusatzInfo" -> information = readAdditionalInfo(parser)
                    else -> skip(parser)
                }
            }

            Log.d("StudentXMLParser", "UpdatedAt: ${updatedAt.toString()}")
            Log.d("StudentXMLParser", "ZusatzInfo: ${information}")
            Log.d("StudentXMLParser", "Klassen: ${lessons.toString()}")

            return NetworkParseResult(
                lessons,
                NetworkTimetableDay(
                    updatedAt,
                    information
                ),
                freeDays
            )
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readClasses(parser: XmlPullParser) : MutableList<NetworkTimetableLesson> {
        parser.require(XmlPullParser.START_TAG, ns, "Klassen")

        val lessons: MutableList<NetworkTimetableLesson> = mutableListOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Kl" -> lessons += readClass(parser)
                else -> skip(parser)
            }
        }
        return lessons
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readClass(parser: XmlPullParser) : MutableList<NetworkTimetableLesson> {
        parser.require(XmlPullParser.START_TAG, ns, "Kl")

        var name = ""
        val lessons: MutableList<NetworkTimetableLesson> = mutableListOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Kurz" -> name = readText(parser)
                "Unterricht" -> lessons += readCourses(parser).map {
                    it.
                }
                else -> skip(parser)
            }
        }
        return lessons
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCourses(parser: XmlPullParser) : MutableList<NetworkTimetableLesson> {
        parser.require(XmlPullParser.START_TAG, ns, "Unterricht")

        val lessons: MutableList<NetworkTimetableLesson> = mutableListOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Ue" -> lessons.add(readCourse(parser))
                else -> skip(parser)
            }
        }
        return lessons
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCourse(parser: XmlPullParser) : NetworkTimetableLesson {
        parser.require(XmlPullParser.START_TAG, ns, "Ue")
        parser.nextTag()
        parser.require(XmlPullParser.START_TAG, ns, "UeNr")

        var teacher = parser.getAttributeValue(ns, "UeLe")
        var subject = parser.getAttributeValue(ns, "UeFa")
        var courseName = parser.getAttributeValue(ns, "UeGr")
        if (courseName == null) {
            courseName = subject
        }
        var courseNr = readText(parser).toLong()

        // the closing tag of <Ue>
        parser.nextTag()
        return NetworkTimetableLesson(
            number =
        )
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