package net.hermlon.gcgtimetable

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.DatabaseLesson
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.TimetableSource
import net.hermlon.gcgtimetable.network.NetworkLesson
import net.hermlon.gcgtimetable.network.asDatabaseModel
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
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
    fun insertLessonAndGetById() = runBlockingTest {
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

        /*
        var source = TimetableSource(
            sourceName = "Test school",
            url = "https://www.stundenplan24.de/10000000/mobil")
        var repo = TimetableRepository(database)
        Log.d("Test", "Dadadaaaa")

        Log.d("Test", "GlobalScope")
        var day = SimpleDateFormat("dd.MM.yyyy").parse("05.02.2020")
*/
        //repo.fetch(source, day)
        Log.d("Test", "Fetched stuff")
        /*
        database.lessonDao.getLessons().observeForever { lessons ->
            Log.d("Test", "Lessons update!")
            Log.d("Test", lessons.toString())
            lessons.forEach {
                Log.d("Test", it.toString())
            }
        }*/
    }
}