package com.example.busisapp.data

import javax.inject.Inject

/**
 * Repository interface for managing notes.
 */
interface NotesRepository {
    suspend fun getCarsByCompany(companyID: String): CarsResponse
    suspend fun getJourneysByCompany(companyID: String): JourneysResponse
    suspend fun getDriversByCompany(companyID: String): DriversResponse
    suspend fun getNotesByCompany(companyID: String): NotesResponse
    suspend fun getNotesByTarget(targetID: Int): NotesResponse
    suspend fun getNotesByCreator(creatorID: Int): NotesResponse
    suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse
    suspend fun markAsRead(id: Int): BasicResponse
    suspend fun editNote(id: Int, request: CreateNoteRequest): CreateNoteResponse
    suspend fun deleteNote(id: Int): BasicResponse
}

/**
 * Implementation of the notes' repository.
 *
 * @param apiService The API service for making network requests.
 */
class NotesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NotesRepository {
    override suspend fun getCarsByCompany(companyID: String) = apiService.getCarsByCompany(companyID)
    override suspend fun getJourneysByCompany(companyID: String) = apiService.getJourneysByCompany(companyID)
    override suspend fun getDriversByCompany(companyID: String) = apiService.getDriversByCompany(companyID)
    override suspend fun getNotesByCompany(companyID: String) = apiService.getNotesByCompany(companyID)
    override suspend fun getNotesByTarget(targetID: Int) = apiService.getNotesByTarget(targetID)
    override suspend fun getNotesByCreator(creatorID: Int) = apiService.getNotesByCreator(creatorID)
    override suspend fun createNote(request: CreateNoteRequest) = apiService.createNote(request)
    override suspend fun markAsRead(id: Int) = apiService.markAsRead(id)
    override suspend fun editNote(id: Int, request: CreateNoteRequest) = apiService.editNote(id, request)
    override suspend fun deleteNote(id: Int) = apiService.deleteNote(id)
}