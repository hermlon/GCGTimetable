package net.hermlon.gcgtimetable

import androidx.test.platform.app.InstrumentationRegistry
import net.hermlon.gcgtimetable.api.Stundenplan24StudentXMLParser
import net.hermlon.gcgtimetable.network.NetworkParseResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException


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
        val result = parseXml("student-test-data/testschool-example-1.xml")

        assertThat(result.lessons.size, equalTo(229))
        assertThat(result.courses.size, equalTo(522))
        assertThat(result.exams.size, equalTo(0))
    }

    @Test
    fun gcgXml() {
        val result = parseXml("student-test-data/gcg-example-1.xml")

        assertThat(result.lessons.size, equalTo(204))
        assertThat(result.courses.size, equalTo(456))
        assertThat(result.exams.size, equalTo(4))
    }
}
