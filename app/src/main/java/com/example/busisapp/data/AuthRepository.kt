package com.example.busisapp.data

import javax.inject.Inject

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun saveToken(token: String)
    suspend fun isSessionValid(): Boolean
    suspend fun getUserData(): UserDto?
    suspend fun logout()
}

/**
 * Implementation of the authentication repository.
 *
 * @param apiService The API service for making network requests.
 * @param sessionManager The session manager for managing user session.
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(request: LoginRequest): LoginResponse {
        return apiService.login(request)
    }

    override suspend fun saveToken(token: String) {
        sessionManager.saveToken(token)
    }

    override suspend fun isSessionValid(): Boolean {
        return sessionManager.isSessionValid()
    }

    override suspend fun getUserData(): UserDto? {
        return sessionManager.getUserData()
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }
}