package net.hermlon.gcgtimetable.database

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.*
import net.hermlon.gcgtimetable.domain.TimetableDay
import net.hermlon.gcgtimetable.domain.TimetableLesson
import net.hermlon.gcgtimetable.util.Resource
import net.hermlon.gcgtimetable.util.ResourceStatus
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.sql.Time
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
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(source: DatabaseSource)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun update(source: DatabaseSource)

    @Transaction
    fun upsert(source: DatabaseSource) {
        try {
            insert(source)
        } catch(e: SQLiteConstraintException) {
            update(source)
        }
    }

    @Query("SELECT * FROM DatabaseSource WHERE id = :key")
    fun get(key: Long): DatabaseSource?

    @Query("SELECT * FROM DatabaseSource ORDER BY id DESC")
    fun getSources(): List<DatabaseSource>
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

    @Query("SELECT * FROM DatabaseDay WHERE sourceId = :sourceId AND date = :date")
    fun getByDate(sourceId: Long, date: LocalDate): DatabaseDay?

    @Query("SELECT * FROM DatabaseDay WHERE id = :key")
    fun get(key: Long): DatabaseDay?
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
    @Query("SELECT * FROM DatabaseLesson WHERE dayId = :dayId")
    fun getLessons(dayId: Long): List<DatabaseLesson>

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

    @Query("SELECT number, subject, teacher, room, courseId FROM " +
            "DatabaseStandardLesson INNER JOIN DatabaseCourse ON DatabaseStandardLesson.courseId = DatabaseCourse.id " +
            "WHERE dayOfWeek = :dayOfWeek")
    fun getStandardLessons(dayOfWeek: Int): List<EnrichedStandardLesson>
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