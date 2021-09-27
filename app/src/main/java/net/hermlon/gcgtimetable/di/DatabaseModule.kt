package net.hermlon.gcgtimetable.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.hermlon.gcgtimetable.database.TimetableDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): TimetableDatabase {
        return Room.databaseBuilder(context,
            TimetableDatabase::class.java,
            "timetable_database")
            //TODO: Remove this in production and write migrations instead!
            .fallbackToDestructiveMigration()
            .build()
    }
}