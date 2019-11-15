package net.hermlon.gcgtimetable.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.sql.Time

@Dao
interface TimetableSourceDao {

    @Insert
    fun insert(source: TimetableSource)

    @Update
    fun update(source: TimetableSource)

    @Query("SELECT * FROM TimetableSource WHERE id = :key")
    fun get(key: Long): TimetableSource?

    @Query("SELECT * FROM TimetableSource ORDER BY id DESC")
    fun getAllSources(): LiveData<List<TimetableSource>>
}