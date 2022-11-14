package net.hermlon.gcgtimetable.domain

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

data class TimetableDay(
    val isStandard: Boolean,
    val date: LocalDate,
    val updatedAt: LocalDateTime,
    val lastRefresh: LocalDateTime,
    val information: String?,
    val lessons: List<TimetableLesson>
)

data class TimetableLesson(
    val number: Int,
    val subject: String,
    val subjectChanged: Boolean = false,
    val teacher: String,
    val teacherChanged: Boolean = false,
    val room: String,
    val roomChanged: Boolean = false,
    val information: String? = null,
    val courseId: Long?
)

data class TempSource(
    val url: String,
    val isStudent: Boolean,
    val username: String? = null,
    val password: String? = null
)

data class Profile constructor(
    val id: Long,
    val name: String,
    val sourceId: Long,
    val position: Int
)