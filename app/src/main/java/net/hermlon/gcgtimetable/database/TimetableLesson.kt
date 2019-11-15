package net.hermlon.gcgtimetable.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity
data class TimetableLesson(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timetableId: Long = 0L,
    val number: Int = 0,
    val subject: String = "",
    val subjectChanged: Boolean = false,
    val subjectNormal: String = "",
    val courseName: String = "",
    val teacher: String = "",
    val teacherChanged: Boolean = false,
    val teacherNormal: String = "",
    val room: String = "",
    val roomChanged: Boolean = false,
    val information: String = "",
    val courseNr: Long = 0L,
    val className: String = ""
)

@Entity
data class TimetableNormalLesson(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sourceId: Long = 0L,
    val number: Int = 0,
    val subject: String = "",
    val courseName: String = "",
    val teacher: String = "",
    val room: String = "",
    val courseNr: Long = 0L,
    val dayOfWeek: Int = 0,
    val className: String = ""
)