package com.example.busisapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busisapp.data.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.busisapp.data.AuthRepository

/**
 * ViewModel for the Login screen with necessary dependencies injected and state handling.
 *
 * @param authRepository The injected AuthRepository for authentication operations.
 */
@HiltViewModel // Tells Hilt to manage this ViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCheckingSession = MutableStateFlow(true)
    val isCheckingSession: StateFlow<Boolean> = _isCheckingSession.asStateFlow()

    private val _navigateToNotes = MutableStateFlow(false)
    val navigateToNotes: StateFlow<Boolean> = _navigateToNotes.asStateFlow()

    init {
        // Check token right at launch
        viewModelScope.launch {
            if (authRepository.isSessionValid()) {
                _navigateToNotes.value = true // Go to Notes
            } else {
                _isCheckingSession.value = false // Display the form
            }
        }
    }

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    /**
     * Initiates the login process and tries to authenticate the user.
     *
     * @param onSuccess Callback to be executed on successful login.
     */
    fun login(onSuccess: () -> Unit) {
        val currentUsername = _username.value
        val currentPassword = _password.value

        if (currentUsername.isBlank() || currentPassword.isBlank()) {
            _errorMessage.value = "Username and password cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = authRepository.login(LoginRequest(currentUsername, currentPassword))

                if (response.status == "success" && response.token != null) {
                    authRepository.saveToken(response.token)
                    onSuccess()
                } else {
                    _errorMessage.value = response.message
                    _password.value = ""
                }
            } catch (_: Exception) {
                _errorMessage.value = "Network error or invalid credentials."
                _password.value = ""
            } finally {
                _isLoading.value = false
            }
        }
    }
}