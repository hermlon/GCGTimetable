package net.hermlon.gcgtimetable.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class TimetableSource(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sourceName: String = "",
    val url: String = "",
    val isStudent: Boolean = true,
    val username: String = "",
    val password: String = ""
)