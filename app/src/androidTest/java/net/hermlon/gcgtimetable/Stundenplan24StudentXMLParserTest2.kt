package net.hermlon.gcgtimetable

import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class Stundenplan24StudentXMLParserTest2 {

    private lateinit var timetableSourceDao: TimetableSourceDao
    private lateinit var db: TimetableDatabase
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, TimetableDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    @Throws(Exception::class)
    fun fetchXML() {
        var source = TimetableSource(
            sourceName = "Test school",
            url = "https://www.stundenplan24.de/10000000/mobil")
        var repo = TimetableRepository(db)
        if (source != null) {
            runBlocking {
                var day = SimpleDateFormat("dd.MM.yyyy").parse("05.02.2020")

                repo.fetch(source, day)
            }
            GlobalScope.launch(Dispatchers.Main) {
                db.lessonDao.getLessons().observeForever { lessons ->
                    Log.d("Test", "Lessons update!")
                    Log.d("Test", lessons.toString())
                    lessons.forEach {
                        Log.d("Test", it.toString())
                    }
                }
            }

            assert(true)
        }
        else {
            assert(false)
        }
    }
}