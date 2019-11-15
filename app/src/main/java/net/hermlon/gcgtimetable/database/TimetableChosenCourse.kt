package net.hermlon.gcgtimetable.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimetableChosenCourse(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val profileId: Long = 0L,
    val courseNr: Long = 0L
)