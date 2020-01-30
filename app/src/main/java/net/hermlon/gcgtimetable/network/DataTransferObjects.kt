package net.hermlon.gcgtimetable.network

import java.util.*

data class NetworkParseResult(
    val courses: List<NetworkCourse>,
    val lessons: List<NetworkLesson>,
    val day: NetworkDay,
    val freeDays: List<Date>,
    val exams: List<NetworkExam>,
    val standardLessons: List<NetworkStandardLesson>
)

data class NetworkDay(
    var updatedAt: Date,
    var information: String
)

data class NetworkCourse(
    val courseId: Int,
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
    val courseId: Int?,
    val information: String
)

data class NetworkExam(
    val dayId: Int,
    val number: Int,
    val beginsAt: String,
    val length: Int,
    val information: String,
    val courseId: Int
)

data class NetworkStandardLesson(
    val className: String,
    val number: Int,
    val courseId: Int,
    val room: String
)