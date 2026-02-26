# Busis Mobile App

## Project Overview

This is a semestral project for the Mobile application programing NPRG056 course. The application is a native Android client for a mock Busis system, allowing users (drivers and managers) to communicate via a shared notes system. It features user authentication, session management, and full CRUD (Create, Read, Update, Delete) functionality for notes.

## Team Information

*   **Author:** Vilém Zahradník

## Core Features

*   **User Authentication:** Secure login screen for users to access the application.
*   **Session Persistence:** The user remains logged in for a reasonable period, with an automatic timeout after 10 minutes of inactivity.
*   **Notes Screen:** A central hub to view, create, and filter notes.
*   **CRUD Operations:**
    *   **Create:** Users can create new notes, optionally assigning them to a specific driver, car, or journey.
    *   **Read:** Notes are displayed in a list. Unread notes are highlighted. Users can mark notes as "read".
    *   **Update:** Users can edit their existing notes.
    *   **Delete:** Users can delete notes.
*   **Role-Based Logic:** The UI and available actions adapt based on the user's role (e.g., a manager sees all company notes, while a driver sees notes assigned to them). This is managed on application level as the backend is very basic, otherwise ideally backend would provide only accesible data for the given user to prevent potential for unauthorized actions.
*   **Pull-to-Refresh:** The notes list can be updated by pulling down and by refresh icon also.
*   **Filtering:** Users can filter the notes list by typing into text field, or by choosing from dropDownLists.

## Technical Stack & Architecture

*   **Language:** **Kotlin**.
*   **UI:** Built entirely with **Jetpack Compose**.
*   **Architecture:** Follows the **MVVM (Model-View-ViewModel)** pattern.
*   **Networking:** Uses **Retrofit** for type-safe HTTP requests to the backend API.
*   **Data Persistence:** Uses **DataStore** for persisting the user's authentication token and session data. Again the token contains a lot of information that normally the application should not really have access to and the application would rely on backend doing the heavy work, but since the backend is very basic, the application does most of the work. 
*   **Dependency Injection:** Uses **Hilt** to manage dependencies and decouple components.
*   **Asynchronous Operations:** Uses **Kotlin Coroutines** and **Flows** for managing background tasks and state.

## How to Build and Run

1.  Clone the repository: `git clone https://github.com/Guill1313/BusisMobileApp`
2.  Open the project in Android Studio.
3.  Let Gradle sync the project dependencies.
4.  Run the application on an Android emulator or a physical device.

**Note:** The application connects to a live backend at `https://app.bus-is.cz/mobile-app/`. No special setup is required for the API connection. The credentials and more information will be provided in an email.
