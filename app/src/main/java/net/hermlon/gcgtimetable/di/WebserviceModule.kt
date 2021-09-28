package net.hermlon.gcgtimetable.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.hermlon.gcgtimetable.network.Webservice
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class WebserviceModule {

    @Provides
    @Singleton
    fun provideWebservice(): Webservice {
        return Webservice()
    }
}