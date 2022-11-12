package net.hermlon.gcgtimetable

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import net.hermlon.gcgtimetable.api.ProfileRepository
import net.hermlon.gcgtimetable.api.Stundenplan24StudentXMLParser
import net.hermlon.gcgtimetable.api.TimetableRepository
import net.hermlon.gcgtimetable.database.*
import net.hermlon.gcgtimetable.network.Webservice
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@SmallTest
class TimetableDatabaseTest {

    private lateinit var database: TimetableDatabase
    private lateinit var webservice: Webservice
    private lateinit var timetableRepository: TimetableRepository
    private lateinit var profileRepository: ProfileRepository
    private var dayId = 0L

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDependencies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(context, TimetableDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()

        webservice = Webservice()
        timetableRepository = TimetableRepository(database, webservice)
        profileRepository = ProfileRepository(database)

        createDefaultProfile()
        createDefaultSource()
        parseDefaultData()
    }

    fun createDefaultProfile() {
        database.profileDao.insert(
            DatabaseProfile(
            1,
            "Default Profile",
            1,
            0
        )
        )
    }

    fun createDefaultSource() {
        database.sourceDao.insert(
            DatabaseSource(
                1,
                "Default Source",
                "https://www.stundenplan24.de/10000000/mobil",
                true,
                null,
                null
            )
        )
    }

    fun parseDefaultData() = runBlocking {
        val inputStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("student-test-data/gcg-example-1.xml")
        val parseResult = Stundenplan24StudentXMLParser().parse(inputStream)
        profileRepository.getDefaultSource()?.let { timetableRepository.updateDatabase(it, parseResult) }
        dayId = database.dayDao.getId(profileRepository.getDefaultSource()!!.id, LocalDate.of(2020, 2, 17))
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun getAvailableClassnames() = runBlocking {
        val classNames = database.courseDao.getClassNames()
        assertThat(classNames, containsInAnyOrder("11", "11/2", "11/3", "11/4", "12", "12/2", "12/3", "12/4", "05", "05/3", "05/2", "06", "06/2", "06/3", "07", "07/3", "07/2", "08", "08/3", "08/2", "09", "09/3", "09/2", "10", "10/3", "10/2"))
    }

    @Test
    fun getCourses() = runBlocking {
        val profile = profileRepository.getDefaultProfile()
        database.whitelistDao.whitelist(DatabaseClassNameWhitelist(profile.id, "10/3"))
        val res1 = database.lessonDao.getLessons(dayId, profile.id)
        assertThat(res1.size, `is`(12))
        val resStd1 = database.standardLessonDao.getStandardLessons(DayOfWeek.MONDAY.value, profile.id)
        assertThat(resStd1.size, `is`(10))

        database.blacklistDao.blacklist(DatabaseCourseIdBlacklist(profile.id, 369))
        val res2 = database.lessonDao.getLessons(dayId, profile.id)
        assertThat(res2.size, `is`(10))
        val resStd2 = database.standardLessonDao.getStandardLessons(DayOfWeek.MONDAY.value, profile.id)
        assertThat(resStd2.size, `is`(8))

        val courses = database.courseDao.getCourses(profile.id)
        assertThat(courses.filter {
            it.id == 369L
        }.first().blacklisted, `is`(true))

        courses.filter {
            it.id != 369L
        }.forEach {
            assertThat(it.blacklisted, `is`(false))
        }

        val classNames = database.courseDao.getFilterClassNames(profile.id)
        assertThat(classNames.filter {
            it.className == "10/3"
        }.first().whitelisted, `is`(true))

        classNames.filter {
            it.className != "10/3"
        }.forEach {
            assertThat(it.whitelisted, `is`(false))
        }
    }

    companion object {
        const val TAG = "TimetableDatabaseTest"
    }
}