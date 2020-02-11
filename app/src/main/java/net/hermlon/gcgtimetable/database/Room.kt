package net.hermlon.gcgtimetable.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SourceDao {
    @Insert
    fun insert(source: DatabaseSource)

    @Update
    fun update(source: DatabaseSource)

    @Query("SELECT * FROM DatabaseSource WHERE id = :key")
    fun get(key: Long): LiveData<DatabaseSource>

    @Query("SELECT * FROM DatabaseSource ORDER BY id DESC")
    fun getSources(): LiveData<List<DatabaseSource>>
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM DatabaseLesson")
    fun getLessons(): LiveData<List<DatabaseLesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg lessons: DatabaseLesson)

}

@Database(entities = [
    DatabaseLesson::class,
    DatabaseSource::class
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TimetableDatabase : RoomDatabase() {

    abstract val lessonDao: LessonDao
    abstract val sourceDao: SourceDao
}

private lateinit var INSTANCE: TimetableDatabase

fun getDatabase(context: Context): TimetableDatabase {
    synchronized(TimetableDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                TimetableDatabase::class.java,
                "timetable_database").build()
        }
    }
    return INSTANCE
}