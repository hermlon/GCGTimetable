package net.hermlon.gcgtimetable.database.migrations

import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import net.hermlon.gcgtimetable.database.DatabaseLesson

class AutoMigrationSpec4to5 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        super.onPostMigrate(db)
        db.execSQL("DELETE FROM DatabaseLesson")
        db.execSQL("DELETE FROM DatabaseStandardLesson")
        db.execSQL("DELETE FROM DatabaseExam")
        db.execSQL("DELETE FROM DatabaseCourse")
        db.execSQL("DELETE FROM DatabaseDay")
    }
}