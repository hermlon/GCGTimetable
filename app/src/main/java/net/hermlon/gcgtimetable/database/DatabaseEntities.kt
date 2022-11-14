package net.hermlon.gcgtimetable.database

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import net.hermlon.gcgtimetable.domain.Profile
import net.hermlon.gcgtimetable.domain.TempSource
import net.hermlon.gcgtimetable.domain.TimetableLesson
import net.hermlon.gcgtimetable.network.NetworkStandardLesson
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@Entity(indices = [Index(value = ["sourceId"])])
    /*foreignKeys = [ForeignKey(entity = DatabaseSource::class, parentColumns = ["id"], childColumns = ["sourceId"])])*/
data class DatabaseProfile constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val sourceId: Long,
    val position: Int
)

@Entity(primaryKeys = ["profileId", "className"],
    foreignKeys = [ForeignKey(entity = DatabaseProfile::class, parentColumns = ["id"], childColumns = ["profileId"])])
data class DatabaseClassNameWhitelist constructor(
    val profileId: Long,
    val className: String
)

@Entity(primaryKeys = ["profileId", "courseId"],
    foreignKeys = [ForeignKey(entity = DatabaseProfile::class, parentColumns = ["id"], childColumns = ["profileId"])])
data class DatabaseCourseIdBlacklist constructor(
    val profileId: Long,
    val courseId: Long
)

@Entity
data class DatabaseSource constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val sourceName: String,
    val url: String,
    val isStudent: Boolean,
    val username: String? = null,
    val password: String? = null
)

@Entity(indices = [Index(value = ["sourceId", "date"], unique = true)],
    foreignKeys = [ForeignKey(entity = DatabaseSource::class, parentColumns = ["id"], childColumns = ["sourceId"])])
data class DatabaseDay constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val sourceId: Long,
    val date: LocalDate,
    val updatedAt: LocalDateTime,
    val lastRefresh: LocalDateTime,
    val information: String?
)

@Entity(indices = [Index(value = ["sourceId"])],
    primaryKeys = ["id", "sourceId"],
    foreignKeys = [ForeignKey(entity = DatabaseSource::class, parentColumns = ["id"], childColumns = ["sourceId"])])
data class DatabaseCourse constructor(
    val id: Long,
    @ColumnInfo(defaultValue = "1")
    val sourceId: Long,
    val className: String,
    val teacher: String,
    val subject: String,
    val name: String
)

data class FilterCourse(
    val id: Long,
    val className: String,
    val teacher: String,
    val subject: String,
    val name: String,
    val blacklisted: Boolean
)

data class FilterClassName(
    val className: String,
    val whitelisted: Boolean
)

@Entity(indices = [Index(value = ["dayId", "number"])],
    foreignKeys = [ForeignKey(onDelete = CASCADE, entity = DatabaseDay::class, parentColumns = ["id"], childColumns = ["dayId"])])
data class DatabaseLesson constructor(
    @PrimaryKey(autoGenerate = true)
    // this is only for migrating
    @ColumnInfo(defaultValue = "-1")
    val id: Long = 0L,
    val dayId: Long,
    val number: Int,
    val subject: String,
    val subjectChanged: Boolean = false,
    val teacher: String,
    val teacherChanged: Boolean = false,
    val room: String,
    val roomChanged: Boolean = false,
    val information: String?,
    val courseId: Long? = null
)

@Entity(primaryKeys = ["dayId", "number", "courseId"],
    foreignKeys = [ForeignKey(onDelete = CASCADE, entity = DatabaseDay::class, parentColumns = ["id"], childColumns = ["dayId"])])
data class DatabaseExam constructor(
    val dayId: Long,
    val number: Int,
    val beginsAt: String,
    val length: Int,
    val information: String,
    val courseId: Long
)

@Entity(primaryKeys = ["sourceId", "dayOfWeek", "number", "courseId"],
    foreignKeys = [ForeignKey(entity = DatabaseSource::class, parentColumns = ["id"], childColumns = ["sourceId"])])
data class DatabaseStandardLesson constructor(
    @ColumnInfo(defaultValue = "1")
    val sourceId: Long,
    val dayOfWeek: Int,
    val number: Int,
    val courseId: Long,
    val room: String
)

data class EnrichedStandardLesson(
    val number: Int,
    val subject: String,
    val teacher: String,
    val room: String,
    val courseId: Long
)

@JvmName("asDomainModelDatabaseLesson")
fun List<DatabaseLesson>.asDomainModel(): List<TimetableLesson> {
    return map {
        TimetableLesson(
            number = it.number,
            subject = it.subject,
            subjectChanged = it.subjectChanged,
            teacher = it.teacher,
            teacherChanged = it.teacherChanged,
            room = it.room,
            roomChanged = it.roomChanged,
            information = it.information,
            courseId = it.courseId
        )
    }
}

@JvmName("asDomainModelEnrichedStandardLesson")
fun List<EnrichedStandardLesson>.asDomainModel(): List<TimetableLesson> {
    return map{
        TimetableLesson(
            number = it.number,
            subject = it.subject,
            teacher = it.teacher,
            room = it.room,
            courseId = it.courseId
        )
    }
}

/*fun List<NetworkStandardLesson>.asDomainModel(dayOfWeek: Int): Array<DatabaseStandardLesson> {
    return map {
        DatabaseStandardLesson(
            dayOfWeek = dayOfWeek,
            number = it.number,
            courseId = it.courseId,
            room = it.room
        )
    }.toTypedArray()
}*/

fun DatabaseSource.asTempSource(): TempSource {
    return TempSource(
        url,
        isStudent,
        username,
        password
    )
}

fun List<DatabaseProfile>.asDomainModel(): List<Profile> {
    return map {
        Profile(
            it.id,
            it.name,
            it.sourceId,
            it.position
        )
    }
}