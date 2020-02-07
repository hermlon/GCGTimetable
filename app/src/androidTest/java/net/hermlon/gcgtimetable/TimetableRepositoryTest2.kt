package net.hermlon.gcgtimetable
/*
import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.TimetableProfileDao
import net.hermlon.gcgtimetable.database.TimetableSource
import net.hermlon.gcgtimetable.database.TimetableSourceDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class TimetableRepositoryTest {

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
        //timetableSourceDao = db.timetableSourceDao
        /*timetableSourceDao.insert(TimetableSource(
            sourceName = "Testschule",
            url = "https://www.stundenplan24.de/10000000/mobil"))*/
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



        var source = timetableSourceDao.get(1)
        var repo = TimetableRepository(db)
        if (source != null) {
            Log.d("Test", "das ist ein Test")
            runBlocking {
                var monday = Calendar.getInstance()
                monday.set(Calendar.DAY_OF_MONTH, 7)

                repo.fetch(source, monday.time)
                //Log.d("Test", "Antwort: ${result.code}: ${result.message} - ${result.request.url}")
                Log.d("Test", "Finished Fetch!!")
                assert(true)
            }
        }
        else {
            assert(false)
        }
    }
}*/