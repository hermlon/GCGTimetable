package net.hermlon.gcgtimetable.database

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.*
import net.hermlon.gcgtimetable.database.migrations.AutoMigrationSpec4to5
import net.hermlon.gcgtimetable.network.NetworkParseResult
import net.hermlon.gcgtimetable.network.asDatabaseModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Dao
interface ProfileDao {
    @Insert
    fun insert(profile: DatabaseProfile)

    @Query("SELECT * FROM DatabaseProfile ORDER BY position")
    fun getProfiles(): List<DatabaseProfile>

    @Query("SELECT * FROM DatabaseProfile WHERE id = :profileId")
    fun get(profileId: Long): DatabaseProfile?
}

@Dao
interface WhitelistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun whitelist(classNameWhitelist: DatabaseClassNameWhitelist)

    @Delete
    fun delete(classNameWhitelist: DatabaseClassNameWhitelist)

    @Query("DELETE FROM DatabaseClassNameWhitelist")
    fun deleteAll()
}

@Dao
interface BlacklistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun blacklist(courseIdBlacklist: DatabaseCourseIdBlacklist)

    @Delete
    fun delete(courseIdBlacklist: DatabaseCourseIdBlacklist)

    @Query("DELETE FROM DatabaseCourseIdBlacklist")
    fun deleteAll()
}

@Dao
interface SourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(source: DatabaseSource)

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

    @Query("DELETE FROM DatabaseDay WHERE date <= :date")
    fun deleteOlderThan(date: LocalDate)

    @Query("DELETE FROM DatabaseDay")
    fun deleteAll()
}

@Dao
interface CourseDao {
    @Query("SELECT id, className, teacher, subject, name, " +
            "id IN $BLACKLISTED_COURSE_IDS_SUBQUERY AS blacklisted " +
            "FROM DatabaseCourse WHERE className IN $WHITELISTED_CLASS_NAMES_SUBQUERY " +
            "AND sourceId = $SOURCE_ID_SUBQUERY " +
            "ORDER BY className, subject ASC")
    fun getCourses(profileId: Long): List<FilterCourse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg courses: DatabaseCourse)

    @Query("SELECT DISTINCT className, className IN $WHITELISTED_CLASS_NAMES_SUBQUERY AS whitelisted FROM DatabaseCourse " +
            "WHERE sourceId = $SOURCE_ID_SUBQUERY ORDER BY className ASC")
    fun getFilterClassNames(profileId: Long): List<FilterClassName>

    @Query("DELETE FROM DatabaseCourse")
    fun deleteAll()
}

@Dao
interface LessonDao {
    @Query("SELECT L.id, L.dayId, L.className, L.number, L.subject, L.subjectChanged, L.teacher, " +
            "L.teacherChanged, L.room, L.roomChanged, L.information, L.courseId " +
            "FROM DatabaseLesson AS L " +
            "LEFT JOIN DatabaseCourse ON L.courseId = DatabaseCourse.id " +
            "WHERE (DatabaseCourse.className IS NULL OR L.className = DatabaseCourse.className) " +
            "AND dayId = :dayId $LESSON_FILTER_QUERY ORDER BY L.number, DatabaseCourse.className ASC")
    fun getLessons(dayId: Long, profileId: Long): List<DatabaseLesson>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg lessons: DatabaseLesson)

    @Query("DELETE FROM DatabaseLesson WHERE dayId = :dayId")
    fun deleteDay(dayId: Long)

    @Query("DELETE FROM DatabaseLesson")
    fun deleteAll()
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM DatabaseExam")
    fun getExams(): List<DatabaseExam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg exams: DatabaseExam)

    @Query("DELETE FROM DatabaseExam")
    fun deleteAll()
}

@Dao
interface StandardLessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg stdLessons: DatabaseStandardLesson)

    @Query("SELECT number, subject, teacher, room, courseId FROM " +
            "DatabaseStandardLesson INNER JOIN DatabaseCourse ON DatabaseStandardLesson.courseId = DatabaseCourse.id " +
            "WHERE dayOfWeek = :dayOfWeek $STD_LESSON_FILTER_QUERY AND DatabaseStandardLesson.sourceId = $SOURCE_ID_SUBQUERY AND DatabaseCourse.sourceId = $SOURCE_ID_SUBQUERY ORDER BY number ASC")
    fun getStandardLessons(dayOfWeek: Int, profileId: Long): List<EnrichedStandardLesson>

    @Query("DELETE FROM DatabaseStandardLesson WHERE sourceId = :sourceId AND dayOfWeek = :dayOfWeek AND NOT courseId IN (:courseIds)")
    fun deleteExcept(sourceId: Long, dayOfWeek: Int, courseIds: List<Long>)

    @Query("DELETE FROM DatabaseStandardLesson")
    fun deleteAll()
}

@Database(entities = [
    DatabaseExam::class,
    DatabaseCourse::class,
    DatabaseLesson::class,
    DatabaseStandardLesson::class,
    DatabaseSource::class,
    DatabaseDay::class,
    DatabaseClassNameWhitelist::class,
    DatabaseCourseIdBlacklist::class,
    DatabaseProfile::class
], version = 5, exportSchema = true, autoMigrations = [
    AutoMigration(from = 4, to = 5, spec = AutoMigrationSpec4to5::class)
])
@TypeConverters(Converters::class)
abstract class TimetableDatabase : RoomDatabase() {
    abstract val examDao: ExamDao
    abstract val courseDao: CourseDao
    abstract val lessonDao: LessonDao
    abstract val standardLessonDao: StandardLessonDao
    abstract val sourceDao: SourceDao
    abstract val dayDao: DayDao
    abstract val whitelistDao: WhitelistDao
    abstract val blacklistDao: BlacklistDao
    abstract val profileDao: ProfileDao
}

const val BLACKLISTED_COURSE_IDS_SUBQUERY = "(SELECT courseId FROM DatabaseCourseIdBlacklist WHERE profileId = :profileId)"
const val WHITELISTED_CLASS_NAMES_SUBQUERY = "(SELECT className FROM DatabaseClassNameWhitelist WHERE profileId = :profileId)"

const val LESSON_FILTER_QUERY = "AND L.className IN " +
            "(SELECT className FROM DatabaseClassNameWhitelist WHERE profileId = :profileId) " +
            "AND (courseId IS NULL OR courseId NOT IN " + BLACKLISTED_COURSE_IDS_SUBQUERY + ")"

const val STD_LESSON_FILTER_QUERY = "AND className IN " +
        "(SELECT className FROM DatabaseClassNameWhitelist WHERE profileId = :profileId) " +
        "AND (courseId IS NULL OR courseId NOT IN " + BLACKLISTED_COURSE_IDS_SUBQUERY + ")"

const val SOURCE_ID_SUBQUERY = "(SELECT sourceId FROM DatabaseProfile WHERE DatabaseProfile.id = :profileId)"