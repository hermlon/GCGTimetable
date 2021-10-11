package net.hermlon.gcgtimetable

import androidx.test.platform.app.InstrumentationRegistry
import net.hermlon.gcgtimetable.api.Stundenplan24StudentXMLParser
import net.hermlon.gcgtimetable.network.NetworkParseResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class Stundenplan24ParserTest {

    fun parseXml(path: String): NetworkParseResult {
        var sxmlp = Stundenplan24StudentXMLParser()

        val inputStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open(path)
        return sxmlp.parse(inputStream)
    }

    @Test
    fun testschoolXml() {
        val result = parseXml("sampledata/student-test-data/testschool-example-1.xml")

        assertThat(result.lessons.size, `is`(229))
        assertThat(result.courses.size, `is`(522))
        assertThat(result.exams.size, `is`(0))
        assertThat(result.day.date, `is`(LocalDate.of(2020, 2, 5)))
        assertThat(result.day.updatedAt, `is`(LocalDateTime.of(2020, 2, 5, 9, 39)))
    }

    @Test
    fun gcgXml() {
        val result = parseXml("sampledata/student-test-data/gcg-example-1.xml")

        assertThat(result.lessons.size, `is`(204))
        assertThat(result.courses.size, `is`(456))
        assertThat(result.exams.size, `is`(4))
        assertThat(result.day.date, `is`(LocalDate.of(2020, 2, 17)))
        assertThat(result.day.updatedAt, `is`(LocalDateTime.of(2020, 2, 7, 11, 42)))
    }
}
