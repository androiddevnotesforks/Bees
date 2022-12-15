package com.iemkol.beez.di

import com.iemkol.beez.data.api.NSFWApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesNSFWApi():NSFWApi {
        return Retrofit.Builder()
            .baseUrl("https://nsfw3.p.rapidapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }
}