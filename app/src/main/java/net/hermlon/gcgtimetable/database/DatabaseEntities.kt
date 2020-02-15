package net.hermlon.gcgtimetable.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.hermlon.gcgtimetable.domain.TimetableLesson
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Entity
data class DatabaseSource constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val sourceName: String,
    val url: String,
    val isStudent: Boolean,
    val username: String? = null,
    val password: String? = null
)

@Entity(
    indices = [Index(value = ["sourceId", "date"], unique = true)])
data class DatabaseDay constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val sourceId: Long,
    val date: LocalDate,
    val updatedAt: LocalDateTime,
    val lastRefresh: LocalDateTime,
    val information: String?
)

@Entity
data class DatabaseCourse constructor(
    @PrimaryKey
    val id: Long,
    val className: String,
    val teacher: String,
    val subject: String,
    val name: String
)

@Entity(primaryKeys = ["dayId", "number", "courseId"])
data class DatabaseLesson constructor(
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

@Entity(primaryKeys = ["dayId", "number", "courseId"])
data class DatabaseExam constructor(
    val dayId: Long,
    val number: Int,
    val beginsAt: String,
    val length: Int,
    val information: String,
    val courseId: Long
)

@Entity(primaryKeys = ["dayOfWeek", "number", "courseId"])
data class DatabaseStandardLesson constructor(
    val dayOfWeek: Int,
    val number: Int,
    val courseId: Long,
    val room: String
)

/*
fun List<DatabaseTimetableLesson>.asDomainModel(): List<TimetableLesson> {
    return map {
        TimetableLesson(
            id = it.id,
            //dayId = it.dayId
            number = it.number,
            subject = it.subject,
            subjectChanged = it.subjectChanged,
            subjectNormal = it.subjectNormal,
            courseName = it.courseName,
            teacher = it.teacher,
            teacherChanged = it.teacherChanged,
            teacherNormal = it.teacherNormal,
            room = it.room,
            roomChanged = it.roomChanged,
            information = it.information,
            courseNr = it.courseNr,
            className = it.className
        )
    }
}*/