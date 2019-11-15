package net.hermlon.gcgtimetable.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.*

@Entity
data class TimetableTimetable(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val profileId: Long = 0L,
    val updatedAt: Date = Date(),
    var day: Date = Date(),
    val information: String = ""
)

class TimetableTimetableAll(
    @Embedded
    val timetableTimetable: TimetableTimetable,
    @Relation(parentColumn = "id", entityColumn = "timetableId", entity = TimetableLesson::class)
    val lessons: List<TimetableLesson> = listOf()
)