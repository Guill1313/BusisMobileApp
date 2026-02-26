package com.example.busisapp.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.busisapp.data.CarDto
import com.example.busisapp.data.DriverDto
import com.example.busisapp.data.JourneyDto
import com.example.busisapp.data.NoteDto
import com.example.busisapp.data.UserDto
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Composable function for the Notes screen.
 * Including Ui with a form-like to create notes and filter them with,
 * actual notes which can be interacted with, and a pull-to-refresh feature and a logout.
 *
 * @param onLogout Callback to be executed on logout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onLogout: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val cars by viewModel.cars.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val journeys by viewModel.journeys.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser = viewModel.currentUser

    var noteText by remember { mutableStateOf("") }
    var selectedCar by remember { mutableStateOf<CarDto?>(null) }
    var selectedDriver by remember { mutableStateOf<DriverDto?>(null) }
    var selectedJourney by remember { mutableStateOf<JourneyDto?>(null) }

    var noteToDelete by remember { mutableStateOf<NoteDto?>(null) }
    var noteToEdit by remember { mutableStateOf<NoteDto?>(null) }

    // Debounce for text filtering
    var debouncedText by remember { mutableStateOf("") }
    LaunchedEffect(noteText) {
        delay(500)
        debouncedText = noteText
    }

    // Snackbar for displaying errors
    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Filter notes based on selected filters
    val filteredNotes = remember(notes, selectedCar, selectedDriver, selectedJourney, debouncedText) {
        notes.filter { note ->
            val matchesCar = selectedCar == null || note.carID == selectedCar?.carID
            val matchesDriver = selectedDriver == null || note.targetID == selectedDriver?.userID || note.creatorID == selectedDriver?.userID
            val matchesJourney = selectedJourney == null || note.journeyID == selectedJourney?.journeyID
            val matchesText = debouncedText.isBlank() || note.text?.contains(debouncedText, ignoreCase = true) == true

            matchesCar && matchesDriver && matchesJourney && matchesText
        }
    }

    // Count unread notes for current user
    val unreadCount = filteredNotes.count { note ->
        val isForMe = if (currentUser?.role == "manager") note.targetID == null else note.targetID == currentUser?.userID
        !note.is_read && isForMe
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Notes Feed", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshNotes(isPullToRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Notes")
                    }
                    IconButton(onClick = { viewModel.logout(onComplete = onLogout) }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading && !isRefreshing) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshNotes(isPullToRefresh = true) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Create / Filter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    label = { Text("Note Text / Search") },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    maxLines = 4
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                CarDropdown(cars, selectedCar) { selectedCar = it }
                                Spacer(modifier = Modifier.height(8.dp))
                                DriverDropdown(drivers, selectedDriver) { selectedDriver = it }
                                Spacer(modifier = Modifier.height(8.dp))
                                JourneyDropdown(journeys, selectedJourney) { selectedJourney = it }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        viewModel.createNote(noteText, selectedDriver?.userID, selectedCar?.carID, selectedJourney?.journeyID)
                                        noteText = ""; selectedCar = null; selectedDriver = null; selectedJourney = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = noteText.isNotBlank()
                                ) { Text("Save Note") }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = "Unread notes ($unreadCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (filteredNotes.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No Notes",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No notes found.\nTry changing your filters or pull down to refresh.",
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(filteredNotes) { note ->
                            NoteCard(
                                note = note,
                                currentUser = currentUser,
                                onReadClick = { viewModel.markAsRead(note.noteID) },
                                onEditClick = { noteToEdit = note },
                                onDeleteClick = { noteToDelete = note }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            if (noteToDelete != null) {
                AlertDialog(
                    onDismissRequest = { noteToDelete = null },
                    title = { Text("Delete Note") },
                    text = { Text("Are you sure you want to delete this note?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteNote(noteToDelete!!.noteID)
                            noteToDelete = null
                        }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { noteToDelete = null }) { Text("Cancel") }
                    }
                )
            }

            if (noteToEdit != null) {
                EditNoteDialog(
                    note = noteToEdit!!,
                    cars = cars,
                    drivers = drivers,
                    journeys = journeys,
                    onDismiss = { noteToEdit = null },
                    onSave = { text, targetID, carID, journeyID ->
                        viewModel.editNote(noteToEdit!!.noteID, text, targetID, carID, journeyID)
                        noteToEdit = null
                    }
                )
            }
        }
    }
}

/**
 * Composable function for a single note card.
 *
 * @param note The note to display.
 * @param currentUser The currently logged-in user.
 * @param onReadClick Callback to be executed when the "Read" button is clicked.
 * @param onEditClick Callback to be executed when the "Edit" button is clicked.
 * @param onDeleteClick Callback to be executed when the "Delete" button is clicked.
 */
@Composable
fun NoteCard(
    note: NoteDto,
    currentUser: UserDto?,
    onReadClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isForMe = if (currentUser?.role == "manager") {
        note.targetID == null
    } else {
        note.targetID == currentUser?.userID
    }

    // Color based on read status
    val containerColor = when {
        isForMe && !note.is_read -> Color(0xFF90CAF9)
        isForMe && note.is_read -> Color(0xFFE3F2FD)
        !isForMe && !note.is_read -> Color(0xFFFFCC80)
        else -> Color(0xFFFFF3E0)
    }

    val fromText = "${note.creator?.firstName ?: "Unknown"} ${note.creator?.lastName ?: ""}".trim()
    val toText = if (note.target != null) {
        "${note.target.firstName} ${note.target.lastName}".trim()
    } else {
        "Company"
    }

    val isCreator = note.creatorID == currentUser?.userID
    val canRead = !note.is_read && isForMe && !isCreator
    val canDelete = currentUser?.role == "manager" || isCreator

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "From: $fromText", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(text = "To: $toText", style = MaterialTheme.typography.labelMedium)
                }
                Text(text = formatLocalTime(note.dateCreated), style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = note.text ?: "No content", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (note.target != null) AssistChip(onClick = {}, label = { Text("@${note.target.firstName}") })
                if (note.car != null) AssistChip(onClick = {}, label = { Text("Car: ${note.car.SPZ}") })
                if (note.journey != null) AssistChip(onClick = {}, label = { Text("Journey: ${note.journey.title}") })
            }

            if (note.is_read && note.dateRead != null) {
                Text(
                    text = "Note read at: ${formatLocalTime(note.dateRead)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (canRead) {
                    TextButton(onClick = onReadClick) { Text("Read", color = Color(0xFF0056b3)) }
                }
                if (isCreator) {
                    TextButton(onClick = onEditClick) { Text("Edit", color = Color.DarkGray) }
                }
                if (canDelete) {
                    TextButton(onClick = onDeleteClick) { Text("Delete", color = Color(0xFFB30000)) }
                }
            }
        }
    }
}

/**
 * Composable function for a dialog to edit a note. Functioning similarly to the basic "form" one.
 *
 * @param note The note to be edited.
 * @param cars List of available cars.
 * @param drivers List of available drivers.
 * @param journeys List of available journeys.
 * @param onDismiss Callback to be executed when the dialog is dismissed.
 * @param onSave Callback to be executed when the note is saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteDialog(
    note: NoteDto,
    cars: List<CarDto>,
    drivers: List<DriverDto>,
    journeys: List<JourneyDto>,
    onDismiss: () -> Unit,
    onSave: (String, Int?, Int?, Int?) -> Unit
) {
    var editNoteText by remember(note) { mutableStateOf(note.text ?: "") }
    var editSelectedCar by remember(note) { mutableStateOf(cars.find { it.carID == note.carID }) }
    var editSelectedDriver by remember(note) { mutableStateOf(drivers.find { it.userID == note.targetID }) }
    var editSelectedJourney by remember(note) { mutableStateOf(journeys.find { it.journeyID == note.journeyID }) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Editing note", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editNoteText,
                    onValueChange = { editNoteText = it },
                    label = { Text("Note Text") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))
                CarDropdown(cars, editSelectedCar) { editSelectedCar = it }
                Spacer(modifier = Modifier.height(8.dp))
                DriverDropdown(drivers, editSelectedDriver) { editSelectedDriver = it }
                Spacer(modifier = Modifier.height(8.dp))
                JourneyDropdown(journeys, editSelectedJourney) { editSelectedJourney = it }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(editNoteText, editSelectedDriver?.userID, editSelectedCar?.carID, editSelectedJourney?.journeyID) },
                        enabled = editNoteText.isNotBlank()
                    ) { Text("Save Changes") }
                }
            }
        }
    }
}

/**
 * Composable function for a dropdown menu with a "None" option for cars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDropdown(cars: List<CarDto>, selectedItem: CarDto?, onItemSelected: (CarDto?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedItem?.SPZ ?: "Select a Car (Optional)",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { onItemSelected(null); expanded = false })
            cars.forEach { car ->
                DropdownMenuItem(
                    text = { Text(car.SPZ) },
                    onClick = { onItemSelected(car); expanded = false }
                )
            }
        }
    }
}

/**
 * Composable function for a dropdown menu with a "None" option for drivers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDropdown(drivers: List<DriverDto>, selectedItem: DriverDto?, onItemSelected: (DriverDto?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedItem?.let { "${it.firstName} ${it.lastName}" } ?: "Select a Driver (Optional)",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { onItemSelected(null); expanded = false })
            drivers.forEach { driver ->
                DropdownMenuItem(
                    text = { Text("${driver.firstName} ${driver.lastName}") },
                    onClick = { onItemSelected(driver); expanded = false }
                )
            }
        }
    }
}

/**
 * Composable function for a dropdown menu with a "None" option for journeys.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyDropdown(journeys: List<JourneyDto>, selectedItem: JourneyDto?, onItemSelected: (JourneyDto?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedItem?.title ?: "Select a Journey (Optional)",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { onItemSelected(null); expanded = false })
            journeys.forEach { journey ->
                DropdownMenuItem(
                    text = { Text(journey.title) },
                    onClick = { onItemSelected(journey); expanded = false }
                )
            }
        }
    }
}

/**
 * Function for formatting a date and time string to a more readable format.
 *
 * @param utcDateString The UTC date and time string to be formatted.
 */
fun formatLocalTime(utcDateString: String?): String {
    if (utcDateString == null) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = inputFormat.parse(utcDateString)
        if (date != null) outputFormat.format(date) else "Invalid date"
    } catch (_: Exception) {
        "Invalid date format"
    }
}
