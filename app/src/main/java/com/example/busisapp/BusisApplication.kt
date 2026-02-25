package com.example.busisapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The main application class for the BusisApp.
 *
 * This class is annotated with `@HiltAndroidApp` to enable Hilt for dependency injection
 * throughout the application.
 */
@HiltAndroidApp
class BusisApplication : Application()
