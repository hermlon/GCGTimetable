package net.hermlon.gcgtimetable.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.*
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.*

@Dao
interface ProfileDao {
    @Insert
    fun insert(profile: DatabaseProfile)

    @Query("SELECT * FROM DatabaseProfile ORDER BY position")
    fun getProfiles(): LiveData<List<DatabaseProfile>>
}

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
interface DayDao {

    @Insert
    fun insert(day: DatabaseDay): Long

    @Query("UPDATE DatabaseDay " +
            "SET updatedAt=:updatedAt, lastRefresh=:lastRefresh, information=:information " +
            "WHERE sourceId = :sourceId AND date = :date")
    fun update(sourceId: Long, date: LocalDate, updatedAt: LocalDateTime, lastRefresh: LocalDateTime, information: String?): Int

    @Transaction
    fun upsert(day: DatabaseDay): Long {
        return if(update(day.sourceId, day.date, day.updatedAt, day.lastRefresh, day.information) == 0) {
            // no update, entity therefore doesn't exist yet, create one
            insert(day)
        } else {
            //TODO: unsure whether this is good practice

            // we did an update, id must be queried for
            getId(day.sourceId, day.date)
        }
    }

    @Query("SELECT id FROM DatabaseDay WHERE sourceId = :sourceId AND date = :date")
    fun getId(sourceId: Long, date: LocalDate): Long

    @Query("SELECT * FROM DatabaseDay WHERE id = :key")
    fun get(key: Long): LiveData<DatabaseDay>
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM DatabaseCourse")
    fun getCourses(): LiveData<List<DatabaseCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg courses: DatabaseCourse)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM DatabaseLesson")
    fun getLessons(): LiveData<List<DatabaseLesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg lessons: DatabaseLesson)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM DatabaseExam")
    fun getExams(): LiveData<List<DatabaseExam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg exams: DatabaseExam)
}

@Dao
interface StandardLessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg stdLessons: DatabaseStandardLesson)
}

@Database(entities = [
    DatabaseExam::class,
    DatabaseCourse::class,
    DatabaseLesson::class,
    DatabaseStandardLesson::class,
    DatabaseSource::class,
    DatabaseDay::class,
    DatabaseProfile::class
], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TimetableDatabase : RoomDatabase() {

    abstract val examDao: ExamDao
    abstract val courseDao: CourseDao
    abstract val lessonDao: LessonDao
    abstract val standardLessonDao: StandardLessonDao
    abstract val sourceDao: SourceDao
    abstract val dayDao: DayDao
    abstract val profileDao: ProfileDao
}