package com.example.busisapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.busisapp.data.SessionManager
import com.example.busisapp.ui.login.LoginScreen
import com.example.busisapp.ui.notes.NotesScreen
import com.example.busisapp.ui.theme.BusisAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MainActivity is the entry point of the application.
 *
 * It is responsible for setting up the UI and navigation.
 * Login tokens are saved for some reasonable time and kept saved while the application is in focus
 * allowing for automatic logout after longer inactivity (10 minutes).
 * Handles screen navigation and clears the backstack when needed.
 */
@AndroidEntryPoint // Enable Dependency Injection in this Activity via annotation
class MainActivity : ComponentActivity() {

    @Inject // Hilt inject for Dependency Injection
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Track when the entire app goes into the background for 10-minute timeout
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    lifecycleScope.launch {
                        sessionManager.updateActivityTimestamp()
                    }
                }
            }
        )

        setContent {
            BusisAppTheme {
                val navController = rememberNavController()
                    // Define Navigation Graph
                NavHost(navController = navController, startDestination = "login") {

                        /**
                         * Navigate to notes and pop "login" off the backstack
                         * so hitting the back button doesn't go back to "login"
                         */
                        composable("login") {
                            LoginScreen(
                                onNavigateToNotes = {
                                    navController.navigate("notes") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        /**
                         * Navigate to notes.
                         * When logout is clicked, navigate to "login" and clear the backstack
                         * so the user can't press the "back" button to return to notes
                         */
                        composable("notes") {
                            NotesScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                }
            }
        }
    }
}