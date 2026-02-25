package com.example.busisapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// LOGIN DTOs
data class LoginRequest(val username: String, val password: String)
data class UserDto(val userID: Int, val companyID: String, val role: String, val firstName: String, val lastName: String)
data class LoginResponse(val status: String, val message: String?, val token: String?, val user: UserDto?)

// DROPDOWN DTOs
data class CarDto(val carID: Int, val companyID: Int, val SPZ: String)
data class CarsResponse(val status: String, val cars: List<CarDto> = emptyList())

data class JourneyDto(val journeyID: Int, val companyID: Int, val title: String)
data class JourneysResponse(val status: String, val journeys: List<JourneyDto> = emptyList())

data class DriverDto(val userID: Int, val firstName: String, val lastName: String)
data class DriversResponse(val status: String, val drivers: List<DriverDto> = emptyList())

// NOTE DTOs
data class NoteDto(
    val noteID: Int, val companyID: Int, val creatorID: Int, val targetID: Int?,
    val carID: Int?, val journeyID: Int?, val is_read: Boolean, val is_deleted: Boolean,
    val dateCreated: String, val dateRead: String?, val text: String?,
    val creator: UserDto?, val target: UserDto?, val car: CarDto?, val journey: JourneyDto?
)
data class NotesResponse(val status: String, val notes: List<NoteDto> = emptyList())

data class CreateNoteRequest(
    val companyID: Int, val creatorID: Int, val targetID: Int?,
    val carID: Int?, val journeyID: Int?, val text: String
)
data class CreateNoteResponse(val status: String, val message: String?, val note: NoteDto?)
data class BasicResponse(val status: String, val message: String?)

// RETROFIT INTERFACE
interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/companies/{companyID}/cars")
    suspend fun getCarsByCompany(@Path("companyID") companyID: String): CarsResponse

    @GET("api/companies/{companyID}/journeys")
    suspend fun getJourneysByCompany(@Path("companyID") companyID: String): JourneysResponse

    @GET("api/companies/{companyID}/drivers")
    suspend fun getDriversByCompany(@Path("companyID") companyID: String): DriversResponse

    @GET("api/companies/{companyID}/notes")
    suspend fun getNotesByCompany(@Path("companyID") companyID: String): NotesResponse

    @GET("api/targets/{targetID}/notes")
    suspend fun getNotesByTarget(@Path("targetID") targetID: Int): NotesResponse

    @GET("api/creators/{creatorID}/notes")
    suspend fun getNotesByCreator(@Path("creatorID") creatorID: Int): NotesResponse

    @POST("api/notes")
    suspend fun createNote(@Body request: CreateNoteRequest): CreateNoteResponse

    // --- NEW ENDPOINTS ---
    @PUT("api/notes/read/{id}")
    suspend fun markAsRead(@Path("id") id: Int): BasicResponse

    @PUT("api/notes/{id}")
    suspend fun editNote(@Path("id") id: Int, @Body request: CreateNoteRequest): CreateNoteResponse

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int): BasicResponse
}

/**
 * Singleton object for managing the Retrofit API service.
 */
object RetrofitClient {
    private const val BASE_URL = "https://app.bus-is.cz/mobile-app/"
    val apiService: ApiService by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
    }
}