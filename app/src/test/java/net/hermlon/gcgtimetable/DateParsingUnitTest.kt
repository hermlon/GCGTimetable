package net.hermlon.gcgtimetable

import net.hermlon.gcgtimetable.api.Stundenplan24StudentXMLParser
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.threeten.bp.LocalDate


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DateParsingUnitTest {
    @Test
    fun dateParsing() {
        val date = Stundenplan24StudentXMLParser.parseRequiredLocalDate("Mittwoch, 05. Februar 2020",
            Stundenplan24StudentXMLParser.TIMETABLE_DATE_FROMAT)
        assertThat(date, `is`(LocalDate.of(2020, 2, 5)))
    }
}
