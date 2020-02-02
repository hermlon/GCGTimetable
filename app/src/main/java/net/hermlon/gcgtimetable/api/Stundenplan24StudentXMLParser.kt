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

    lateinit var courses: MutableSet<NetworkCourse>
    lateinit var lessons: MutableSet<NetworkLesson>
    lateinit var standardLessons: MutableSet<NetworkStandardLesson>
    lateinit var exams: MutableSet<NetworkExam>

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): NetworkParseResult {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()

            var updatedAt = Date()
            var information = ""
            courses = mutableSetOf()
            lessons = mutableSetOf()
            standardLessons = mutableSetOf()
            exams = mutableSetOf()
            var freeDays: MutableSet<Date> = mutableSetOf()

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
                "Pl" -> readPlan(parser, className ?:
                    throw TimetableMissingInformationException("className unknown while parsing lessons"))
                "Klausuren" -> readExams(parser, className ?:
                    throw TimetableMissingInformationException("className unknown while parsing exams"))
                else -> skip(parser)
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readPlan(parser: XmlPullParser, className: String) {
        parser.require(XmlPullParser.START_TAG, ns, "Pl")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Std" -> readLesson(parser, className)
                else -> skip(parser)
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class, TimetableMissingInformationException::class)
    private fun readLesson(parser: XmlPullParser, className: String) {
        parser.require(XmlPullParser.START_TAG, ns, "Std")

        var number: Int? = null
        var subject: String? = null
        var subjectChanged: Boolean? = null
        var teacher: String? = null
        var teacherChanged: Boolean? = null
        var room: String? = null
        var roomChanged: Boolean? = null
        var courseId: Long? = null
        var information = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "St" -> number = readText(parser).toInt()
                "Nr" -> courseId = readText(parser).toLong()
                "If" -> information = readText(parser)
                "Fa" -> {
                    var prop = readLessonProperty(parser, "FaAe")
                    subject = prop.first
                    subjectChanged = prop.second
                }
                "Le" -> {
                    var prop = readLessonProperty(parser, "LeAe")
                    teacher = prop.first
                    teacherChanged = prop.second
                }
                "Ra" -> {
                    var prop = readLessonProperty(parser, "RaAe")
                    room = prop.first
                    roomChanged = prop.second
                }
                else -> skip(parser)
            }
        }

        if(number != null && subject != null && subjectChanged != null && teacher != null
            && teacherChanged != null && room != null && roomChanged != null) {
            lessons.add(NetworkLesson(
                className,
                number,
                subject,
                subjectChanged,
                teacher,
                teacherChanged,
                room,
                roomChanged,
                courseId,
                information
            ))
            /* All information to add a standard lesson is given, i. e. the room isn't changed
               and the lesson isn't a weird one which has no courseId (this can be the case e. g.
               lessons during exams where no normal class takes place) */
            if(!roomChanged && courseId != null) {
                standardLessons.add(
                    NetworkStandardLesson(
                        className,
                        number,
                        courseId,
                        room
                ))
            }
        }
        else {
            throw TimetableMissingInformationException("missing information while parsing lesson")
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLessonProperty(parser: XmlPullParser, attribute: String) : Pair<String, Boolean> {
        var changed = parser.getAttributeValue(ns, attribute) != null
        var content = readText(parser)
        return Pair(content, changed)
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
        var courseId = readText(parser).toLong()

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
    private fun readExams(parser: XmlPullParser, className: String) {
        parser.require(XmlPullParser.START_TAG, ns, "Klausuren")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "Klausur" -> readExam(parser, className)
                else -> skip(parser)
            }
        }
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readExam(parser: XmlPullParser, className: String) {
        parser.require(XmlPullParser.START_TAG, ns, "Klausur")

        var skip = false
        var number: Int? = null
        var beginsAt: String? = null
        var length: Int? = null
        var information = ""
        var courseId: Long? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "KlStunde" -> number = readText(parser).toInt()
                "KlBeginn" -> beginsAt = readText(parser)
                "KlDauer" -> length = readText(parser).toInt()
                "KlInfo" -> information = readText(parser)
                "KlKurs" -> {
                    var courseName = readText(parser)
                    /* when there is no real course name and the subject acts as a course name
                       it is only assumed to be unique for a class, therefore the second condition. */
                    var course = courses.find { it.name == courseName && it.className == className}
                    if(course != null) {
                        courseId = course.courseId
                    }
                    else {
                        /* A courseId which isn't found can occur since all exams for one year
                           are included in each class for that year. If the exams are for classes
                           which will be parsed later on the course name is still unknown.
                           The exam will at the latest be added once the class with the course it
                           refers to was parsed. */
                        skip = true
                    }
                }
                else -> skip(parser)
            }
        }

        if(!skip) {
            if(number != null && beginsAt != null && length != null && courseId != null) {
                exams.add(
                    NetworkExam(
                        className,
                        number,
                        beginsAt,
                        length,
                        information,
                        courseId
                    ))
            }
            else {
                throw TimetableMissingInformationException("missing information while parsing exam")
            }
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
    private fun readFreeDays(parser: XmlPullParser) : MutableSet<Date> {
        parser.require(XmlPullParser.START_TAG, ns, "FreieTage")

        var freeDays = mutableSetOf<Date>()

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