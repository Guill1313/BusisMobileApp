package com.example.busisapp.di

import android.content.Context
import com.example.busisapp.data.ApiService
import com.example.busisapp.data.AuthRepository
import com.example.busisapp.data.AuthRepositoryImpl
import com.example.busisapp.data.NotesRepository
import com.example.busisapp.data.NotesRepositoryImpl
import com.example.busisapp.data.RetrofitClient
import com.example.busisapp.data.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Support for Dependency Injection.
 */
@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused") // Suppress warnings as they are beings used by DI
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return RetrofitClient.apiService
    }

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService, sessionManager: SessionManager): AuthRepository {
        return AuthRepositoryImpl(apiService, sessionManager)
    }

    @Provides
    @Singleton
    fun provideNotesRepository(apiService: ApiService): NotesRepository {
        return NotesRepositoryImpl(apiService)
    }
}