package net.hermlon.gcgtimetable.api

import android.util.Log
import android.util.Xml
import net.hermlon.gcgtimetable.database.TimetableDay
import net.hermlon.gcgtimetable.network.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.Collections.list

private val ns: String? = null

class TimetableMissingInformationException(message: String) : Exception(message)

class Stundenplan24StudentXMLParser {

    lateinit var courses: MutableList<NetworkCourse>

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): NetworkParseResult {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()

            var updatedAt: Date = Date()
            var information: String = ""
            courses = mutableListOf()
            var lessons: MutableList<NetworkLesson> = mutableListOf()
            var exams: MutableList<NetworkExam> = mutableListOf()
            var standardLessons: MutableList<NetworkStandardLesson> = mutableListOf()
            var freeDays: MutableList<Date> = mutableListOf()

            parser.require(XmlPullParser.START_TAG, ns, "VpMobil")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "Kopf" -> updatedAt = readUpdatedAt(parser)
                    "FreieTage" -> freeDays = readFreeDays(parser)
                    "ZusatzInfo" -> information = readAdditionalInfo(parser)
                    "Klassen" -> readClasses(parser)
                    else -> skip(parser)
                }
            }

            //Log.d("StudentXMLParser", "UpdatedAt: ${updatedAt.toString()}")
            //Log.d("StudentXMLParser", "ZusatzInfo: ${information}")
            //Log.d("StudentXMLParser", "Klassen: ${lessons.toString()}")

            return NetworkParseResult(
                courses,
                lessons,
                NetworkDay(updatedAt, information),
                freeDays,
                exams,
                standardLessons
            )
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readClasses(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, ns, "Klassen")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Kl" -> readClass(parser)
                else -> skip(parser)
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readClass(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, ns, "Kl")

        var className: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Kurz" -> className = readText(parser)
                "Unterricht" -> readCourses(parser, className ?:
                    throw TimetableMissingInformationException("className unknown while parsing course"))
                /*"Unterricht" -> lessons += readCourses(parser).map {
                    it.
                }*/
                else -> skip(parser)
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCourses(parser: XmlPullParser, className: String) {
        parser.require(XmlPullParser.START_TAG, ns, "Unterricht")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Ue" -> readCourse(parser, className)
                else -> skip(parser)
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCourse(parser: XmlPullParser, className: String) {
        parser.require(XmlPullParser.START_TAG, ns, "Ue")
        parser.nextTag()
        parser.require(XmlPullParser.START_TAG, ns, "UeNr")

        var teacher = parser.getAttributeValue(ns, "UeLe")
        var subject = parser.getAttributeValue(ns, "UeFa")
        var name = parser.getAttributeValue(ns, "UeGr")
        if (name == null) {
            name = subject
        }
        var courseId = readText(parser).toInt()

        courses.add(NetworkCourse(
            courseId,
            className,
            teacher,
            subject,
            name
        ))

        // the closing tag of <Ue>
        parser.nextTag()
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