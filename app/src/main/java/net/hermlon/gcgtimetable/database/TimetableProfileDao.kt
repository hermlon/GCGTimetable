package net.hermlon.gcgtimetable.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.sql.Time

@Dao
interface TimetableProfileDao {

    @Insert
    fun insert(profile: TimetableProfile)

    @Update
    fun update(profile: TimetableProfile)

    @Query("SELECT * FROM TimetableProfile WHERE profileId = :key")
    fun get(key: Long): TimetableProfile?

    @Query("SELECT * FROM TimetableProfile ORDER BY profileId DESC")
    fun getAllProfiles(): LiveData<List<TimetableProfile>>
}