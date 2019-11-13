package net.hermlon.gcgtimetable

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import net.hermlon.gcgtimetable.database.TimetableDatabase
import net.hermlon.gcgtimetable.database.TimetableProfile
import net.hermlon.gcgtimetable.database.TimetableProfileDao
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TimetableDatabaseTest {

    private lateinit var timetableProfileDao: TimetableProfileDao
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
        timetableProfileDao = db.timetableProfileDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetNight() {
        val timetableProfile = TimetableProfile(profileName = "Test Profil")
        timetableProfileDao.insert(timetableProfile)
        val profile = timetableProfileDao.get(1)
        assertEquals(profile?.profileName, "Test Profil")
    }
}