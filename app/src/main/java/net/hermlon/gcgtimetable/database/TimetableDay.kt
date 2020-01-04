package net.hermlon.gcgtimetable.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.*

@Entity
data class TimetableDay(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sourceId: Long = 0L,
    val updatedAt: Date = Date(),
    var day: Date = Date(),
    val information: String = "",
    val lastRefresh: Date = Date()
)

/*
class TimetableTimetableAll(
    @Embedded
    val timetableTimetable: TimetableDay,
    @Relation(parentColumn = "id", entityColumn = "dayId", entity = TimetableLesson::class)
    val lessons: List<TimetableLesson> = listOf()
)*/