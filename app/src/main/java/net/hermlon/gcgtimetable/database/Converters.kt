package net.hermlon.gcgtimetable.database

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToDate(date: LocalDate): String? {
        return date.toString()
    }
}
