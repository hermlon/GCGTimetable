package net.hermlon.gcgtimetable.api

import android.util.Log
import android.util.Xml
import net.hermlon.gcgtimetable.database.TimetableDay
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

private val ns: String? = null

class Stundenplan24StudentXMLParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): TimetableParseResult {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()

            var freeDays: MutableList<Date> = mutableListOf()
            var updatedAt: Date = Date()
            var additionalInfo: String = ""
            var classes: MutableMap<String, MutableMap<Long, Course>> = mutableMapOf()

            parser.require(XmlPullParser.START_TAG, ns, "VpMobil")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "Kopf" -> updatedAt = readUpdatedAt(parser)
                    "FreieTage" -> freeDays = readFreeDays(parser)
                    "Klassen" -> classes = readClasses(parser)
                    "ZusatzInfo" -> additionalInfo = readAdditionalInfo(parser)
                    else -> skip(parser)
                }
            }

            Log.d("StudentXMLParser", "UpdatedAt: ${updatedAt.toString()}")
            Log.d("StudentXMLParser", "ZusatzInfo: ${additionalInfo}")
            Log.d("StudentXMLParser", "Klassen: ${classes.toString()}")

            return TimetableParseResult(
                classes,
                TimetableDay(
                    updatedAt = updatedAt,
                    information = additionalInfo
                ),
                freeDays
            )
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readClass(parser: XmlPullParser) : MutableMap<String, MutableMap<Long, Course>> {
        parser.require(XmlPullParser.START_TAG, ns, "Kl")

        var name = ""
        val courses: MutableMap<Long, Course> = mutableMapOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Kurz" -> name = readText(parser)
                "Unterricht" -> courses += readCourses(parser)
                else -> skip(parser)
            }
        }
        return mutableMapOf(name to courses)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCourse(parser: XmlPullParser) : Course {
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
        return Course(teacher = teacher, subject = subject, courseNr = courseNr, courseName = courseName)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCourses(parser: XmlPullParser) : MutableMap<Long, Course> {
        parser.require(XmlPullParser.START_TAG, ns, "Unterricht")

        var courses: MutableMap<Long, Course> = mutableMapOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Ue" -> {
                    var course = readCourse(parser)
                    courses[course.courseNr] = course
                }
                else -> skip(parser)
            }
        }
        return courses
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readClasses(parser: XmlPullParser) : MutableMap<String, MutableMap<Long, Course>> {
        parser.require(XmlPullParser.START_TAG, ns, "Klassen")

        val classes: MutableMap<String, MutableMap<Long, Course>> = mutableMapOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Kl" -> classes += readClass(parser)
                else -> skip(parser)
            }
        }
        return classes
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

data class Course (
    var teacher: String,
    var subject: String,
    var courseName: String,
    var courseNr: Long
)

data class TimetableParseResult (
    var classes: MutableMap<String, MutableMap<Long, Course>>,
    var day: TimetableDay,
    var freeDays: MutableList<Date>
)