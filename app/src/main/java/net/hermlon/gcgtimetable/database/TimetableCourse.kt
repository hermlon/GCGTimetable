package net.hermlon.gcgtimetable.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimetableCourse(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val courseName: String,
    val profileId: Long
)