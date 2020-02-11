package net.hermlon.gcgtimetable

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.DatabaseLesson
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.TimetableSource
import net.hermlon.gcgtimetable.network.NetworkLesson
import net.hermlon.gcgtimetable.network.asDatabaseModel
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.greaterThan
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat

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
    fun insertLessonAndGetById() = runBlocking {
        // GIVEN - insert a task
        val lesson = NetworkLesson("7/1", 1, "Deu", false, "Kipp", false, "207", false, 235, null)
        val databaseLesson = setOf(lesson).asDatabaseModel(24)
        database.lessonDao.insertAll(*databaseLesson)

        // WHEN - Get the task by id from the database.u
        database.lessonDao.getLessons().observeForever {
            val loaded = it[0]
            // THEN - The loaded data contains the expected values
            assertThat<DatabaseLesson>(loaded, notNullValue())
            assertThat(loaded.className, `is`(lesson.className))
            assertThat(loaded.courseId, `is` (lesson.courseId))
            assertThat(loaded.dayId, `is` (24L))
            assertThat(loaded.information, `is` (lesson.information))
            assertThat(loaded.subject, `is` (lesson.subject))
            assertThat(loaded.subjectChanged, `is` (lesson.subjectChanged))
            assertThat(loaded.teacher, `is` (lesson.teacher))
            assertThat(loaded.teacherChanged, `is` (lesson.teacherChanged))
            assertThat(loaded.room, `is` (lesson.room))
            assertThat(loaded.roomChanged, `is` (lesson.roomChanged))
        }
    }

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun fetchXML() = runBlocking {
        var source = TimetableSource(
            sourceName = "Test school",
            url = "https://www.stundenplan24.de/10000000/mobil")
        var repo = TimetableRepository(database)

        // date null will fetch the latest
        repo.fetch(source, null)

        database.lessonDao.getLessons().observeForever { lessons ->
            assertThat(lessons.size, greaterThan(0))
        }
    }
}