package net.hermlon.gcgtimetable.di

import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class UiModule {

    @Provides
    @Singleton
    fun provideLessonRecycledViewPool(): RecyclerView.RecycledViewPool {
        return RecyclerView.RecycledViewPool()
    }
}