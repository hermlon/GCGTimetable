package net.hermlon.gcgtimetable.network

import java.util.*

data class NetworkParseResult(
    val courses: List<NetworkCourse>,
    val lessons: List<NetworkLesson>,
    val day: NetworkDay,
    val freeDays: List<Date>
)

data class NetworkDay(
    val updatedAt: Date,
    val information: String
)

data class NetworkCourse(
    val courseId: Int,
    val dayId: Int,
    val teacher: String,
    val subject: String,
    val name: String
)

data class NetworkLesson(
    val className: String,
    val number: Int,
    val dayId: Int,
    val subject: String,
    val subjectChanged: Boolean = false,
    val teacher: String,
    val teacherChanged: Boolean = false,
    val room: String,
    val roomChanged: Boolean = false,
    val courseId: Int,
    val information: String
)