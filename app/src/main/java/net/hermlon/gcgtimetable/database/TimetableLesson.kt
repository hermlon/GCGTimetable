package net.hermlon.gcgtimetable.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimetableLesson(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timetableId: Long = 0L,
    val number: Int = 0,
    val subject: String = "",
    val subjectChanged: Boolean = false,
    val subjectNormal: String = "",
    val teacher: String = "",
    val teacherChanged: Boolean = false,
    val teacherNormal: String = "",
    val room: String = "",
    val roomChanged: Boolean = false,
    val course: String = "",
    val information: String = ""
)