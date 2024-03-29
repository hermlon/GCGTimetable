package net.hermlon.gcgtimetable.network

import net.hermlon.gcgtimetable.database.DatabaseCourse
import net.hermlon.gcgtimetable.database.DatabaseExam
import net.hermlon.gcgtimetable.database.DatabaseLesson
import net.hermlon.gcgtimetable.database.DatabaseStandardLesson
import net.hermlon.gcgtimetable.domain.TimetableLesson
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.*

data class NetworkParseResult(
    val courses: Set<NetworkCourse>,
    val lessons: Set<NetworkLesson>,
    val day: NetworkDay,
    val freeDays: Set<LocalDate>,
    val exams: Set<NetworkExam>,
    val standardLessons: Set<NetworkStandardLesson>
)

data class NetworkDay(
    var date: LocalDate,
    var updatedAt: LocalDateTime,
    var information: String?
)

data class NetworkCourse(
    val id: Long,
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
    val information: String?,
    val courseId: Long?
)

data class NetworkExam(
    val number: Int,
    val beginsAt: String,
    val length: Int,
    val information: String,
    val courseId: Long
)

data class NetworkStandardLesson(
    val number: Int,
    val courseId: Long,
    val room: String
)

fun Set<NetworkExam>.asDatabaseModel(dayId: Long): Array<DatabaseExam> {
    return map {
        DatabaseExam(
            dayId = dayId,
            number = it.number,
            beginsAt = it.beginsAt,
            length = it.length,
            information = it.information,
            courseId = it.courseId
        )
    }.toTypedArray()
}

fun Set<NetworkCourse>.asDatabaseModel(sourceId: Long): Array<DatabaseCourse> {
    return map {
        DatabaseCourse(
            id = it.id,
            sourceId = sourceId,
            className = it.className,
            teacher = it.teacher,
            subject = it.subject,
            name = it.name
        )
    }.toTypedArray()
}

fun Set<NetworkLesson>.asDatabaseModel(dayId: Long): Array<DatabaseLesson> {
    return map {
        DatabaseLesson(
            dayId = dayId,
            className = it.className,
            number = it.number,
            subject = it.subject,
            subjectChanged = it.subjectChanged,
            teacher = it.teacher,
            teacherChanged = it.teacherChanged,
            room = it.room,
            roomChanged = it.roomChanged,
            information = it.information,
            courseId = it.courseId
        )
    }.toTypedArray()
}

fun Set<NetworkStandardLesson>.asDatabaseModel(sourceId: Long, dayOfWeek: Int): Array<DatabaseStandardLesson> {
    return map {
        DatabaseStandardLesson(
            sourceId = sourceId,
            dayOfWeek = dayOfWeek,
            number = it.number,
            courseId = it.courseId,
            room = it.room
        )
    }.toTypedArray()
}