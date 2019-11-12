package net.hermlon.gcgtimetable.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimetableProfile(

    @PrimaryKey(autoGenerate = true)
    var profileId: Long = 0L,
    var profileName: String = "",
    var url: String = "",
    var isStudent: Boolean = true,
    var username: String = "",
    var password: String = "",
    var classFilter: String = ""
)