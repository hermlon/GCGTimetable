package net.hermlon.gcgtimetable.domain

data class TimetableLesson(
    val dayId: Long,
    val number: Int,
    val subject: String,
    val subjectChanged: Boolean = false,
    val teacher: String,
    val teacherChanged: Boolean = false,
    val room: String,
    val roomChanged: Boolean = false,
    val information: String?,
    val courseId: Long
)

data class TempSource(
    val url: String,
    val isStudent: Boolean,
    val username: String? = null,
    val password: String? = null
)