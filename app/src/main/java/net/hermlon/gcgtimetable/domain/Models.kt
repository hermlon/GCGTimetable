package net.hermlon.gcgtimetable.domain

data class TimetableLesson(
    val id: Long,
    //val dayId: Long
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