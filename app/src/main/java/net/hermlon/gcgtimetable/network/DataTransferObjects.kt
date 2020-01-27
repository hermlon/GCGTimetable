package net.hermlon.gcgtimetable.network

import java.util.*

data class NetworkParseResult(
    val lessons: List<NetworkTimetableLesson>,
    val day: NetworkTimetableDay,
    val freeDays: List<Date>
)

data class NetworkTimetableLesson(
    val number: Int,
    val subject: String,
    val subjectChanged: Boolean = false,
    val subjectNormal: String,
    val courseName: String,
    val teacher: String,
    val teacherChanged: Boolean = false,
    val teacherNormal: String,
    val room: String,
    val roomChanged: Boolean = false,
    val information: String,
    val courseNr: Long,
    val className: String
)

data class NetworkTimetableDay(
    val updatedAt: Date,
    val information: String
)