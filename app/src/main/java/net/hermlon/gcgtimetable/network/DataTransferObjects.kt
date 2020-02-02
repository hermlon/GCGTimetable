package net.hermlon.gcgtimetable.network

import net.hermlon.gcgtimetable.database.DatabaseLesson
import java.util.*

data class NetworkParseResult(
    val courses: Set<NetworkCourse>,
    val lessons: Set<NetworkLesson>,
    val day: NetworkDay,
    val freeDays: Set<Date>,
    val exams: Set<NetworkExam>,
    val standardLessons: Set<NetworkStandardLesson>
)

data class NetworkDay(
    var updatedAt: Date,
    var information: String
)

data class NetworkCourse(
    val courseId: Long,
    val className: String,
    val teacher: String,
    val subject: String,
    val name: String
)

data class NetworkLesson(
    val className: String,
    val number: Int,
    val subject: String,
    val subjectChanged: Boolean = false,
    val teacher: String,
    val teacherChanged: Boolean = false,
    val room: String,
    val roomChanged: Boolean = false,
    val courseId: Long?,
    val information: String
)

data class NetworkExam(
    val className: String,
    val number: Int,
    val beginsAt: String,
    val length: Int,
    val information: String,
    val courseId: Long
)

data class NetworkStandardLesson(
    val className: String,
    val number: Int,
    val courseId: Long,
    val room: String
)

fun Set<NetworkLesson>.asDatabaseModel(dayId: Long): Array<DatabaseLesson> {
    return map {
        DatabaseLesson(
            dayId = dayId,
            number = it.number,
            subject = it.subject,
            subjectChanged = it.subjectChanged,
            teacher = it.teacher,
            teacherChanged = it.teacherChanged,
            room = it.room,
            roomChanged = it.roomChanged,
            information = it.information,
            courseId = it.courseId,
            className = it.className
        )
    }.toTypedArray()
}