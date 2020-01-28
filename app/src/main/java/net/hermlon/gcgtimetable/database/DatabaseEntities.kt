package net.hermlon.gcgtimetable.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.hermlon.gcgtimetable.domain.TimetableLesson

@Entity
data class DatabaseTimetableLesson constructor(
    @PrimaryKey
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
}