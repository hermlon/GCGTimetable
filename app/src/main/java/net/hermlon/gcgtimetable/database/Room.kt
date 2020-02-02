package net.hermlon.gcgtimetable.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TimetableLessonDao {
    @Query("select * from DatabaseLesson")
    fun getLessons(): LiveData<List<DatabaseLesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg lessons: DatabaseLesson)

}

@Database(entities = [
    DatabaseLesson::class
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TimetableDatabase : RoomDatabase() {

    abstract val lessonDao: TimetableLessonDao
    //abstract val timetableProfileDao: TimetableProfileDao
    //abstract val timetableSourceDao: TimetableSourceDao
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