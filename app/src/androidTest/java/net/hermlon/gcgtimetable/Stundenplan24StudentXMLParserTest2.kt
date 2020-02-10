package net.hermlon.gcgtimetable

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.TimetableSource
import net.hermlon.gcgtimetable.database.TimetableSourceDao
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat

@RunWith(AndroidJUnit4::class)
class Stundenplan24StudentXMLParserTest2 {

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

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun fetchXML() = runBlocking {
        var source = TimetableSource(
            sourceName = "Test school",
            url = "https://www.stundenplan24.de/10000000/mobil")
        var repo = TimetableRepository(database)
        Log.d("Test", "Dadadaaaa")

        Log.d("Test", "GlobalScope")
        var day = SimpleDateFormat("dd.MM.yyyy").parse("05.02.2020")

        repo.fetch(source, day)
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