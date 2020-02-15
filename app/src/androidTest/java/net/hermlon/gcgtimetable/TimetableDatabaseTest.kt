package net.hermlon.gcgtimetable

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.*
import net.hermlon.gcgtimetable.network.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.greaterThan
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class TimetableDatabaseTest {

    private lateinit var database: TimetableDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(context, TimetableDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertExamAndGetById() = runBlocking {
        val exam = NetworkExam(1, "7:30", 240, "turning a mouse into a snuff box", 342)
        val databaseExam = setOf(exam).asDatabaseModel(24)
        database.examDao.insertAll(*databaseExam)

        database.examDao.getExams().observeForever {
            val loaded = it[0]
            assertThat<DatabaseExam>(loaded, notNullValue())
            assertThat(loaded.dayId, `is`(24L))
            assertThat(loaded.courseId, `is`(342L))
        }
    }

    @Test
    fun insertCourseAndGetById() = runBlocking {
        val course = NetworkCourse(342, "7/1", "Severus Snape", "Defence Against the Dark Arts", "DATDA3")
        val databaseCourse = setOf(course).asDatabaseModel()
        database.courseDao.insertAll(*databaseCourse)

        database.courseDao.getCourses().observeForever {
            val loaded = it[0]
            assertThat<DatabaseCourse>(loaded, notNullValue())
            assertThat(loaded.id, `is`(342L))
            assertThat(loaded.teacher, `is`("Severus Snape"))
        }
    }

    @Test
    fun insertLessonAndGetById() = runBlocking {
        // GIVEN - insert a task
        val lesson = NetworkLesson(1, "Deu", false, "Kipp", false, "207", false, 235, null)
        val databaseLesson = setOf(lesson).asDatabaseModel(24)
        database.lessonDao.insertAll(*databaseLesson)

        // WHEN - Get the task by id from the database.u
        database.lessonDao.getLessons().observeForever {
            val loaded = it[0]
            // THEN - The loaded data contains the expected values
            assertThat<DatabaseLesson>(loaded, notNullValue())
            assertThat(loaded.courseId, `is`(lesson.courseId))
            assertThat(loaded.dayId, `is`(24L))
            assertThat(loaded.information, `is`(lesson.information))
            assertThat(loaded.subject, `is`(lesson.subject))
            assertThat(loaded.subjectChanged, `is`(lesson.subjectChanged))
            assertThat(loaded.teacher, `is`(lesson.teacher))
            assertThat(loaded.teacherChanged, `is`(lesson.teacherChanged))
            assertThat(loaded.room, `is`(lesson.room))
            assertThat(loaded.roomChanged, `is`(lesson.roomChanged))
        }
    }

    @Test
    fun insertStandardLessonAndGetByKey() = runBlocking {
        val stdLesson = NetworkStandardLesson(3, 34, "344")
        val dbStdLesson = setOf(stdLesson).asDatabaseModel(2)
        database.standardLessonDao.insertAll(*dbStdLesson)

        //TODO: some request to verify it worked
    }

    @Test
    fun upsertDay() = runBlocking {
        val commonDate = LocalDate.now()
        val day = DatabaseDay(0, 2, commonDate, LocalDateTime.now(), LocalDateTime.now(), null)
        database.dayDao.upsert(day)
        val day2 = DatabaseDay(0, 2, commonDate, LocalDateTime.now(), LocalDateTime.now(), "no school for today! yay!")
        val id = database.dayDao.upsert(day2)

        database.dayDao.get(id).observeForever {
            assertThat(it.information, `is`("no school for today! yay!"))
        }
    }

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun fetchXML() = runBlocking {
        var source = DatabaseSource(
            id = 0,
            sourceName = "Test school",
            url = "https://www.stundenplan24.de/10000000/mobil",
            isStudent = true)
        var repo = TimetableRepository(database)

        // date null will fetch the latest
        repo.fetch(source, null)

        database.lessonDao.getLessons().observeForever { lessons ->
            assertThat(lessons.size, greaterThan(0))
        }
        database.courseDao.getCourses().observeForever { courses ->
            assertThat(courses.size, greaterThan(0))
        }
    }
}