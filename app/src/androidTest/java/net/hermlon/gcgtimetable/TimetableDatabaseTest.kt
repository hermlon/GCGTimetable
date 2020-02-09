package net.hermlon.gcgtimetable

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.TimetableLessonDao
import net.hermlon.gcgtimetable.database.TimetableProfile
import net.hermlon.gcgtimetable.database.TimetableProfileDao
import net.hermlon.gcgtimetable.network.NetworkLesson
import net.hermlon.gcgtimetable.network.asDatabaseModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TimetableDatabaseTest {

    private lateinit var db: TimetableDatabase

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

    @Test
    @Throws(Exception::class)
    fun insertAndGetLesson() {
        GlobalScope.launch(Dispatchers.Main) {
            db.lessonDao.insertAll(*setOf(
                NetworkLesson("7/1", 1, "Deu", false, "Kipp", false, "207", false, 235, information = "")
            ).asDatabaseModel(34))


            db.lessonDao.getLessons().observeForever {
                assertEquals(it[0].className, "7/1")
            }
        }
    }
}