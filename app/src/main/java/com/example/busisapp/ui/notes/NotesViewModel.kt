package com.example.busisapp.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busisapp.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.busisapp.data.AuthRepository
import com.example.busisapp.data.NotesRepository

/**
 * ViewModel for the Notes screen with necessary dependencies injected and state handling.
 * Supporting states, API calls, and error handling.
 *
 * @param notesRepository The injected NotesRepository for managing notes.
 * @param authRepository The injected AuthRepository for authentication operations.
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteDto>>(emptyList())
    val notes: StateFlow<List<NoteDto>> = _notes.asStateFlow()

    private val _cars = MutableStateFlow<List<CarDto>>(emptyList())
    val cars: StateFlow<List<CarDto>> = _cars.asStateFlow()

    private val _drivers = MutableStateFlow<List<DriverDto>>(emptyList())
    val drivers: StateFlow<List<DriverDto>> = _drivers.asStateFlow()

    private val _journeys = MutableStateFlow<List<JourneyDto>>(emptyList())
    val journeys: StateFlow<List<JourneyDto>> = _journeys.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorEvents = Channel<String>()
    val errorEvents = _errorEvents.receiveAsFlow()

    var currentUser: UserDto? = null
        private set

    private val _noteText = MutableStateFlow("")
    val noteText: StateFlow<String> = _noteText.asStateFlow()
    fun updateNoteText(text: String) { _noteText.value = text }

    private val _selectedCar = MutableStateFlow<CarDto?>(null)
    val selectedCar: StateFlow<CarDto?> = _selectedCar.asStateFlow()
    fun updateSelectedCar(car: CarDto?) { _selectedCar.value = car }

    private val _selectedDriver = MutableStateFlow<DriverDto?>(null)
    val selectedDriver: StateFlow<DriverDto?> = _selectedDriver.asStateFlow()
    fun updateSelectedDriver(driver: DriverDto?) { _selectedDriver.value = driver }

    private val _selectedJourney = MutableStateFlow<JourneyDto?>(null)
    val selectedJourney: StateFlow<JourneyDto?> = _selectedJourney.asStateFlow()
    fun updateSelectedJourney(journey: JourneyDto?) { _selectedJourney.value = journey }

    init {
        loadInitialData()
    }

    /**
     * Loads initial data, including cars, drivers, and journeys.
     * Called when the ViewModel is initialized.
     */
    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            currentUser = authRepository.getUserData()

            val user = currentUser
            if (user != null) {
                try {
                    // dropDownList data
                    val carsDeferred = async { notesRepository.getCarsByCompany(user.companyID) }
                    val driversDeferred = async { notesRepository.getDriversByCompany(user.companyID) }
                    val journeysDeferred = async { notesRepository.getJourneysByCompany(user.companyID) }

                    _cars.value = carsDeferred.await().cars
                    _drivers.value = driversDeferred.await().drivers
                    _journeys.value = journeysDeferred.await().journeys

                    refreshNotes()
                } catch (_: Exception) {
                    _errorEvents.send("Failed to load initial data.")
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Refreshes the notes list.
     * Called when the user triggers a pull-to-refresh event, or after a successful API call.
     *
     * @param isPullToRefresh Indicates if the refresh is triggered by a pull-to-refresh event.
     */
    fun refreshNotes(isPullToRefresh: Boolean = false) {
        val user = currentUser ?: return
        viewModelScope.launch {
            if (isPullToRefresh) _isRefreshing.value = true
            try {
                // difference in roles, decision made app-wise, normally would be made in backend
                if (user.role == "manager") {
                    _notes.value = notesRepository.getNotesByCompany(user.companyID).notes
                } else if (user.role == "driver") {
                    val createdNotes = notesRepository.getNotesByCreator(user.userID).notes
                    val targetedNotes = notesRepository.getNotesByTarget(user.userID).notes

                    _notes.value = (createdNotes + targetedNotes)
                        .distinctBy { it.noteID }
                        .sortedByDescending { it.dateCreated }
                }
            } catch (_: Exception) {
                _errorEvents.send("Failed to refresh notes. Check your connection.")
            } finally {
                if (isPullToRefresh) _isRefreshing.value = false
            }
        }
    }

    /**
     * Creates a new note with the given parameters.
     * Parameter-less as ViewModel holds the states.
     *
     */
    fun createNote() {
        val user = currentUser ?: return
        viewModelScope.launch {
            try {
                val req = CreateNoteRequest(
                    companyID = user.companyID.toInt(),
                    creatorID = user.userID,
                    targetID = _selectedDriver.value?.userID,
                    carID = _selectedCar.value?.carID,
                    journeyID = _selectedJourney.value?.journeyID,
                    text = _noteText.value
                )
                if (notesRepository.createNote(req).status == "success") {
                    // Clear the form after successful creation
                    _noteText.value = ""
                    _selectedCar.value = null
                    _selectedDriver.value = null
                    _selectedJourney.value = null

                    refreshNotes()
                } else {
                    _errorEvents.send("Failed to create note on the server.")
                }
            } catch (_: Exception) {
                _errorEvents.send("Network error while creating note.")
            }
        }
    }

    /**
     * Marks a note as read.
     *
     * @param noteID The ID of the note to be marked as read.
     */
    fun markAsRead(noteID: Int) {
        viewModelScope.launch {
            try {
                if (notesRepository.markAsRead(noteID).status == "success") refreshNotes()
            } catch (_: Exception) {
                _errorEvents.send("Failed to mark note as read.")
            }
        }
    }

    /**
     * Deletes a note.
     *
     * @param noteID The ID of the note to be deleted.
     */
    fun deleteNote(noteID: Int) {
        viewModelScope.launch {
            try {
                if (notesRepository.deleteNote(noteID).status == "success") refreshNotes()
            } catch (_: Exception) {
                _errorEvents.send("Failed to delete note.")
            }
        }
    }

    /**
     * Edits a note with the given parameters.
     *
     * @param noteID The ID of the note to be edited.
     * @param text The new text content of the note.
     * @param targetID The new ID of the target user for the note (nullable).
     * @param carID The new ID of the car associated with the note (nullable).
     * @param journeyID The new ID of the journey associated with the note (nullable).
     */
    fun editNote(noteID: Int, text: String, targetID: Int?, carID: Int?, journeyID: Int?) {
        val user = currentUser ?: return
        viewModelScope.launch {
            try {
                val req = CreateNoteRequest(user.companyID.toInt(), user.userID, targetID, carID, journeyID, text)
                if (notesRepository.editNote(noteID, req).status == "success") refreshNotes()
            } catch (_: Exception) {
                _errorEvents.send("Failed to save edits.")
            }
        }
    }

    /**
     * Logs out the user by clearing the session and executing the provided callback.
     *
     * @param onComplete Callback to be executed after clearing the session.
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}