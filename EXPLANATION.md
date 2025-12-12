# LECTURLY - Complete Architecture & Code Explanation
## Object-Oriented Programming Course Project

---

## 🎯 Project Overview

**Lecturly** is a modern desktop application that leverages Object-Oriented Programming principles to create an intelligent lecture management platform. It combines:
- **Real-time AI Chat** with Google's Gemini 2.5 Flash API
- **Audio-to-Notes Generation** from lecture recordings
- **Notebook Management** with persistent local storage
- **Netflix-Style UI** using JavaFX with modern dark theme

### Key OOP Concepts Implemented
- **Encapsulation**: Separation of concerns across multiple classes
- **Inheritance & Polymorphism**: Controller hierarchy and service patterns
- **Abstraction**: Services hide complex API interactions
- **Single Responsibility Principle (SRP)**: Each class has one clear purpose
- **Dependency Injection**: Services passed to controllers for loose coupling

---

## 📁 Complete File Structure & Descriptions

### **Java Source Files** (src/main/java/org/example/lecturly/)

#### 1. **Launcher.java** ⭐ Entry Point
```
PURPOSE: Simple entry point that delegates to JavaFX Application
RESPONSIBILITY: Bootstrap the application
KEY METHODS:
  - main(String[] args): Launches the LecturlyApp JavaFX application
```
**OOP Concepts:**
- Simple wrapper demonstrating separation of concerns
- Follows MVC pattern by delegating to application class

---

#### 2. **LecturlyApp.java** 🚀 Main Application Class
```
PURPOSE: JavaFX Application lifecycle management
RESPONSIBILITIES:
  - Load and configure the UI (Dashboard view)
  - Apply Netflix-themed CSS styling
  - Load Barlow Condensed fonts for typography
ARCHITECTURE:
  - Extends JavaFX Application
  - Implements design patterns for font loading and error handling
```

**Key Features:**
```java
- loadBarlowCondensedFonts(): Attempts to load custom fonts with fallback logic
  • Tries multiple paths for font directory
  • Gracefully falls back to system fonts
  • Demonstrates defensive programming
  
- start(Stage stage): 
  • Loads dashboard-view.fxml (JavaFX declarative UI)
  • Applies Netflix theme CSS
  • Sets window properties (1200x800, dark background)
```

**OOP Concepts:**
- **Inheritance**: Extends Application
- **Encapsulation**: Hides font loading complexity
- **Error Handling**: Try-catch with informative fallbacks

---

#### 3. **MainController.java** 🎛️ Main UI Controller (Deprecated/Legacy)
```
PURPOSE: Original container controller (superseded by DashboardController)
COMPONENTS:
  - Chat View: Real-time Gemini conversation
  - Notes View: Audio file processing interface
  - Navigation: Sidebar buttons to switch between views
```

**Responsibilities:**
- Manage two sub-controllers (ChatController, NotesController)
- Handle view switching with CSS styling
- Route button clicks to appropriate handlers

**OOP Concepts:**
- **Composition**: Contains ChatController and NotesController instances
- **Delegation**: Routes events to sub-controllers
- **UI State Management**: Shows/hides views based on navigation

---

#### 4. **DashboardController.java** 📊 Primary Application Controller
```
PURPOSE: Main entry point for user interaction
RESPONSIBILITIES:
  - Display all saved notebooks
  - Allow notebook creation/deletion
  - Load notebooks into the editor
  - Initialize dummy notebooks on first launch
```

**Key Methods:**
```java
onCreateNotebook():
  • Opens TextInputDialog for notebook naming
  • Creates new Notebook object
  • Saves via NotebookStorageService
  • Refreshes the UI

loadNotebooks():
  • Retrieves all notebooks from storage
  • Creates visual notebook cards (HBox with styling)
  • Displays empty state if no notebooks exist
  • Sorts by update time (newest first)

createNotebookCard(Notebook notebook):
  • Creates clickable card with notebook metadata
  • Displays: Name, created date, last updated
  • Handles open/delete/rename operations
  • Implements hover effects for visual feedback
```

**OOP Concepts:**
- **Single Responsibility**: Only manages dashboard view
- **Separation of Concerns**: Uses NotebookStorageService for persistence
- **Reusability**: Card creation as separate method
- **UI Patterns**: Model-View-Controller architecture

---

#### 5. **NotebookController.java** 📝 Advanced Notebook Editor
```
PURPOSE: Full-featured notebook interface with integrated chat and notes
RESPONSIBILITIES:
  - Display and edit notebook content
  - Upload audio files for transcription
  - Generate notes from audio via FastAPI
  - Chat with Gemini while viewing notes
  - Save/load notebook state
LAYOUT:
  Left Panel: Notes editor with audio upload
  Right Panel: Chat interface
```

**Architecture Overview:**
```
┌─────────────────────────────────────────┐
│      NotebookController                 │
├────────────────┬────────────────────────┤
│   Notes Area   │   Chat Interface       │
│  • TextArea    │  • Messages VBox       │
│  • Upload UI   │  • Input TextArea      │
│  • Status      │  • Send/Clear Buttons  │
└────────────────┴────────────────────────┘
        ↓                    ↓
  AudioProcessing      GeminiChatService
  Service
```

**Key Responsibilities:**
```java
initialize():
  • Set up all UI components
  • Link notes text changes to chat context
  • Enable auto-scroll for chat messages
  • Add event listeners for dynamic behavior

setNotebook(Notebook notebook):
  • Loads notebook data into editor
  • Restores chat history from storage
  • Re-initializes GeminiChatService
  • Prepares notes context for future chats

onConnect(), onBrowseFile(), onGenerateNotes():
  • Handle audio file selection
  • Validate FastAPI backend availability
  • Process audio asynchronously (background thread)
  • Update UI with progress indicators

onSend():
  • Get message from input
  • Add to chat display immediately
  • Call GeminiChatService in background thread
  • Update notes context dynamically
```

**OOP Concepts:**
- **Encapsulation**: Hides complexity of async operations
- **Composition**: Uses AudioProcessingService and GeminiChatService
- **Observer Pattern**: TextArea listeners update chat context
- **Threading**: Uses ScheduledExecutorService for non-blocking UI

---

#### 6. **ChatController.java** 💬 Chat Interface Handler
```
PURPOSE: Manages real-time conversation with Gemini AI
RESPONSIBILITIES:
  - Initialize API connection with Gemini
  - Send user messages and receive responses
  - Maintain conversation history
  - Display formatted message bubbles
  - Clear conversation history
```

**Component Architecture:**
```
User Input → Send Button → onSendClick() 
                              ↓
                    (Background Thread)
                              ↓
                    GeminiChatService.chat()
                              ↓
                    HTTP Request to Gemini API
                              ↓
                    Platform.runLater() (UI Thread)
                              ↓
                    Display Response in Message Container
```

**Key Methods:**
```java
onConnectClick():
  • Validates API key input
  • Creates GeminiChatService instance
  • Updates status label
  • Enables send/clear buttons

onSendClick():
  • Validates connection status
  • Gets message from input area
  • Adds user message to UI
  • Calls GeminiChatService in background thread
  • Handles exceptions with error display
  • Uses Platform.runLater() for thread-safe UI updates

createMessageBubble():
  • Creates styled HBox for each message
  • Different styles for user/assistant/system messages
  • Auto-wrapping text for readability
  • Handles clipboard copy functionality
```

**OOP Concepts:**
- **Delegation**: Uses GeminiChatService for API communication
- **Thread Safety**: Platform.runLater() for JavaFX thread safety
- **Factory Pattern**: createMessageBubble() creates message UI elements
- **State Management**: Tracks connection status and conversation state

---

#### 7. **NotesController.java** 🎧 Audio Processing Controller
```
PURPOSE: Handle audio file upload and note generation
RESPONSIBILITIES:
  - File selection and validation
  - Audio file upload to FastAPI backend
  - Progress indication during processing
  - Display generated notes
  - Copy notes to clipboard
```

**Workflow:**
```
User selects file
        ↓
onBrowseFile() → FileChooser dialog
        ↓
selectedFile = user's choice
        ↓
onGenerateNotes() → AudioProcessingService.generateNotesFromAudio()
        ↓
(Background thread)
        ↓
HTTP multipart request to FastAPI backend
        ↓
FastAPI uploads to Gemini Files API
        ↓
Gemini generates notes
        ↓
Response back to Java
        ↓
Display in TextArea
```

**Key Methods:**
```java
onConnectClick():
  • Checks FastAPI backend availability
  • Uses AudioProcessingService.isBackendAvailable()
  • Provides setup instructions if backend not found
  • Updates UI state on success

onBrowseFile():
  • Opens FileChooser with audio format filters
  • Stores selected file in instance variable
  • Updates UI with filename display

onGenerateNotes():
  • Shows loading UI with progress indicator
  • Calls AudioProcessingService.generateNotesFromAudio() in background
  • Updates status label with processing status
  • Displays generated notes in TextArea
  • Handles errors gracefully
```

**OOP Concepts:**
- **Composition**: Uses AudioProcessingService
- **Async Operations**: Background thread via ScheduledExecutorService
- **UI State Management**: Shows/hides loading indicators
- **Error Handling**: User-friendly error messages

---

#### 8. **GeminiChatService.java** 🤖 AI Chat API Client
```
PURPOSE: HTTP client for Google Gemini 2.5 Flash API
RESPONSIBILITIES:
  - Manage conversation history
  - Build API requests
  - Send HTTP requests to Gemini API
  - Parse JSON responses
  - Maintain context across messages
  - Support note-based context in conversations
```

**Key Architecture:**
```java
class GeminiChatService {
  - apiKey: String (API authentication)
  - httpClient: HttpClient (Java HTTP client)
  - conversationHistory: List<JsonObject> (maintains chat state)
  - notesContext: String (optional notes for context)
}
```

**Request-Response Flow:**
```
chat(String userMessage)
        ↓
1. Add user message to conversationHistory
2. Create JSON request body:
   - Add system instruction with notes context (if available)
   - Include full conversation history
   - Format as per Gemini API spec
3. Send HTTP POST request:
   - URI: https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
   - Headers: Content-Type: application/json
   - Body: JSON with contents array
4. Parse response:
   - Extract text from nested JSON structure
   - Handle errors
5. Add assistant response to conversationHistory
6. Return response string to caller
```

**Key Methods:**
```java
chat(String userMessage):
  • Core method for sending messages
  • Maintains full conversation history
  • Includes notes context if available
  • Returns String response from Gemini
  
setNotesContext(String notes):
  • Sets optional context from lecture notes
  • This context is included in system instruction
  • Allows Gemini to answer questions based on notes

clearHistory():
  • Clears conversation history
  • Starts fresh conversation
  
restoreHistory(List<ChatMessage> messages):
  • Restores saved chat from notebook
  • Reconstructs full conversation state
  • Allows continuing previous conversations

extractTextFromResponse(JsonObject responseJson):
  • Parses Gemini API response
  • Navigates JSON hierarchy:
    candidates → content → parts → text
  • Returns extracted text or error message
```

**OOP Concepts:**
- **Encapsulation**: Hides API complexity
- **State Management**: Maintains conversation history internally
- **Error Handling**: Comprehensive try-catch with rollback
- **Data Transformation**: Converts between Java objects and JSON

---

#### 9. **AudioProcessingService.java** 🎙️ Audio File Handler
```
PURPOSE: HTTP client for FastAPI backend audio processing
RESPONSIBILITIES:
  - Send audio files to FastAPI backend
  - Build multipart form data requests
  - Parse response with generated notes
  - Check backend availability
  - Determine MIME types for audio files
```

**Architecture:**
```
Java Application
        ↓
AudioProcessingService
        ↓ (HTTP multipart POST)
FastAPI Backend (audio_backend/app.py)
        ↓
Gemini Files API (upload)
        ↓
Gemini 2.5 Flash (transcribe + generate notes)
        ↓
FastAPI Response (notes as JSON)
        ↓
Java (display in UI)
```

**Key Methods:**
```java
generateNotesFromAudio(File audioFile):
  • Main method called by NotesController
  • Validates file exists
  • Reads file bytes
  • Builds multipart form data
  • Sends HTTP POST to /audio-to-notes endpoint
  • Parses JSON response
  • Returns notes as String

buildMultipartFormData():
  • Constructs multipart/form-data request body
  • Includes boundary separators
  • Sets Content-Disposition headers
  • Embeds file content
  • Follows RFC 2388 standard format

getMimeType(String filename):
  • Returns MIME type based on file extension
  • Handles: mp3, wav, ogg, flac, m4a, aac, aiff
  • Defaults to audio/mpeg

isBackendAvailable():
  • Health check HTTP POST to /health endpoint
  • Returns true if FastAPI backend is running
  • 5-second timeout
  • Used by NotesController for pre-flight checks
```

**OOP Concepts:**
- **Single Responsibility**: Only handles audio processing
- **Encapsulation**: Hides HTTP complexity
- **Validation**: File existence checks
- **Fault Tolerance**: Health checks before operations

---

#### 10. **Notebook.java** 📓 Data Model
```
PURPOSE: Represents a notebook data structure
STRUCTURE:
  - id: UUID (unique identifier)
  - name: String (notebook title)
  - notes: String (markdown content)
  - chatHistory: List<ChatMessage> (conversation history)
  - createdAt: LocalDateTime (creation timestamp)
  - updatedAt: LocalDateTime (last modification)
```

**Class Hierarchy:**
```java
Notebook (outer class)
├── Properties:
│   ├── id: String
│   ├── name: String
│   ├── notes: String
│   ├── chatHistory: List<ChatMessage>
│   ├── createdAt: LocalDateTime
│   └── updatedAt: LocalDateTime
│
└── Inner Class: ChatMessage
    ├── role: String ("user" or "assistant")
    ├── content: String (message text)
    └── timestamp: LocalDateTime
```

**Key Methods:**
```java
Constructors:
  Notebook(): Default with UUID and timestamp
  Notebook(String name): Named notebook

Setters:
  Each setter updates updatedAt timestamp
  Maintains data integrity

Getters:
  Standard getters for all properties

Formatting:
  getFormattedCreatedAt(): "MMM dd, yyyy"
  getFormattedUpdatedAt(): "MMM dd, yyyy HH:mm"

addChatMessage(ChatMessage message):
  • Appends message to history
  • Updates updatedAt
```

**OOP Concepts:**
- **Encapsulation**: Private fields with public accessors
- **Inner Class**: ChatMessage as related data structure
- **Value Object Pattern**: Immutable timestamp tracking
- **Builder Potential**: Could be extended with Builder pattern

---

#### 11. **NotebookStorageService.java** 💾 Persistence Layer
```
PURPOSE: Save and load notebooks from local disk
RESPONSIBILITIES:
  - Serialize notebooks to JSON
  - Deserialize notebooks from JSON
  - Manage notebooks directory
  - Maintain notebook index
  - Provide CRUD operations
STORAGE:
  Location: ~/.lecturly/notebooks/
  Format: JSON (one file per notebook)
  Index: notebooks_index.json (for quick lookup)
```

**Architecture:**
```
Memory (Java Objects)
        ↓ (Serialization)
Gson Library
        ↓ (JSON encoding)
JSON Files on Disk (~/.lecturly/notebooks/)
        ↓ (Deserialization)
Gson Library
        ↓ (JSON decoding)
Memory (Java Objects)
```

**Key Methods:**
```java
Constructor NotebookStorageService():
  • Initializes Gson with custom LocalDateTime adapter
  • Creates notebooks directory in user home
  • Sets up Path for notebooks storage

saveNotebook(Notebook notebook):
  • Converts Notebook object to JSON using Gson
  • Writes JSON to file: {id}.json
  • Updates index file
  • Throws IOException on failure

loadNotebook(String id):
  • Reads JSON file by ID
  • Deserializes to Notebook object
  • Returns Notebook
  • Throws IOException if not found

loadAllNotebooks():
  • Reads index file first (ordered list)
  • Falls back to directory scan if index unavailable
  • Avoids duplicate loading
  • Sorts by updatedAt (newest first)
  • Returns List<Notebook>

initializeDummyNotebooks():
  • Called on first launch
  • Creates 3 sample notebooks
  • Sets example content
  • Demonstrates app functionality
```

**OOP Concepts:**
- **Service Pattern**: Encapsulates data access
- **Serialization**: Uses Gson for JSON transformation
- **Error Handling**: Comprehensive exception handling
- **Data Integrity**: Index file for consistency
- **Adapter Pattern**: Custom LocalDateTime JSON adapter

---

### **FXML View Files** (src/main/resources/)

#### dashboard-view.fxml
- **Purpose**: Main notebook list interface
- **Components**: ScrollPane with dynamic notebook cards
- **Controller**: DashboardController

#### notebook-view.fxml
- **Purpose**: Notebook editor with split pane
- **Layout**: Left (notes + upload), Right (chat)
- **Controller**: NotebookController

#### chat-view.fxml, hello-view.fxml, main-view.fxml
- Legacy FXML files from initial development
- Kept for reference

---

### **Styling**

#### netflix-theme.css
- **Color Scheme**: Black (#000000) backgrounds, red (#e50914) accents
- **Typography**: Barlow Condensed font family
- **Components**: Button styling, label theming
- **Effects**: Hover animations, focus states

---

### **Module Configuration**

#### module-info.java
```java
module org.example.lecturly {
  requires javafx.controls;      // JavaFX UI components
  requires javafx.fxml;          // FXML loading
  requires org.controlsfx.controls; // Enhanced controls
  requires com.google.gson;      // JSON serialization
  requires java.net.http;        // HTTP client
  
  opens org.example.lecturly to javafx.fxml, com.google.gson;
  exports org.example.lecturly;
}
```

**Purpose:** Declares module dependencies and visibility rules

---

### **Build Configuration**

#### build.gradle.kts
```gradle
Plugins:
  - java (core compilation)
  - application (executable)
  - org.javamodularity.moduleplugin (Java modules)
  - org.openjfx.javafxplugin (JavaFX support)
  - org.beryx.jlink (custom runtime)

Java Version: 21 (latest LTS)

Dependencies:
  - JavaFX 21.0.6 (UI framework)
  - ControlsFX 11.2.1 (enhanced controls)
  - Gson 2.10.1 (JSON serialization)
  - JUnit 5.12.1 (testing)

Main Class: org.example.lecturly.Launcher
```

---

## 🏗️ Overall Architecture

### **Layered Architecture**

```
┌─────────────────────────────────────────────────────┐
│         Presentation Layer (JavaFX UI)              │
│  - LecturlyApp, DashboardController, etc.          │
│  - FXML Views                                       │
│  - CSS Styling                                      │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│        Controller/Handler Layer                      │
│  - ChatController, NotesController                  │
│  - NotebookController, DashboardController          │
│  - Event handling & business logic coordination     │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│           Service Layer (Business Logic)            │
│  - GeminiChatService (Gemini API client)            │
│  - AudioProcessingService (FastAPI client)          │
│  - NotebookStorageService (Persistence)             │
│  - Handles async operations & data transformation   │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│           Data/Model Layer                          │
│  - Notebook (in-memory object model)                │
│  - ChatMessage (conversation data)                  │
│  - JSON serialization via Gson                      │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│         External Services (via HTTP)                │
│  - Gemini API (chat)                                │
│  - FastAPI Backend (audio processing)               │
│  - Gemini Files API (audio transcription)           │
└─────────────────────────────────────────────────────┘
```

### **Data Flow Diagrams**

#### Chat Feature Flow
```
User types message
        ↓
ChatController.onSendClick()
        ↓
Add user message to UI (immediate)
        ↓
GeminiChatService.chat(message) [Background thread]
        ↓
Build HTTP request with conversation history
        ↓
POST to Gemini API
        ↓
Parse JSON response
        ↓
Platform.runLater() [Switch to UI thread]
        ↓
Add assistant response to UI
        ↓
Update status label
```

#### Audio-to-Notes Flow
```
User selects audio file
        ↓
NotesController.onGenerateNotes()
        ↓
Show loading indicator
        ↓
AudioProcessingService.generateNotesFromAudio() [Background]
        ↓
Build multipart form data
        ↓
POST to FastAPI backend (/audio-to-notes)
        ↓
FastAPI uploads to Gemini Files API
        ↓
Gemini processes audio + generates notes
        ↓
FastAPI returns notes in JSON
        ↓
Platform.runLater() [Switch to UI thread]
        ↓
Display notes in TextArea
```

#### Notebook Management Flow
```
DashboardController loads
        ↓
NotebookStorageService.loadAllNotebooks()
        ↓
Read ~/.lecturly/notebooks/ directory
        ↓
Deserialize JSON files to Notebook objects
        ↓
Sort by updatedAt descending
        ↓
DashboardController.createNotebookCard() for each
        ↓
Display notebook cards with metadata
        ↓
User clicks notebook
        ↓
NotebookController.setNotebook(Notebook)
        ↓
Load notes + chat history
        ↓
Ready for editing
        ↓
User makes changes
        ↓
NotebookStorageService.saveNotebook()
        ↓
Write JSON to disk
```

---

## 🔑 Key OOP Design Patterns

### **1. Model-View-Controller (MVC)**
- **Model**: Notebook, ChatMessage classes
- **View**: FXML files, JavaFX components
- **Controller**: DashboardController, NotebookController, etc.

### **2. Service Pattern**
- GeminiChatService, AudioProcessingService, NotebookStorageService
- Encapsulates business logic
- Reusable across controllers

### **3. Dependency Injection**
```java
// Constructor injection in controllers
public ChatController(
    TextField apiKeyField,
    Button connectButton,
    // ... other components
)
```
- Components passed to constructors
- Loose coupling between classes
- Easy to test and mock

### **4. Observer Pattern**
```java
// TextArea listeners update chat context
notesArea.textProperty().addListener((obs, oldVal, newVal) -> {
    if (chatService != null) {
        chatService.setNotesContext(newVal);
    }
});
```

### **5. Factory Pattern**
```java
// createMessageBubble creates UI elements
private HBox createMessageBubble(String text, Boolean isUser)
```

### **6. Adapter Pattern**
```java
// LocalDateTime JSON adapter for serialization
registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
```

### **7. Thread-Safety Pattern**
```java
// Background thread + UI thread synchronization
executorService.execute(() -> {
    try {
        String response = chatService.chat(message);
        Platform.runLater(() -> {
            addAssistantMessage(response); // UI thread
        });
    } catch (Exception e) {
        Platform.runLater(() -> showError(e.getMessage()));
    }
});
```

---

## 💡 SOLID Principles Implementation

### **S - Single Responsibility**
- ChatController: Only manages chat
- NotebookStorageService: Only handles persistence
- GeminiChatService: Only communicates with Gemini API

### **O - Open/Closed**
- Service layer is open for extension (new services can be added)
- Closed for modification (existing services don't change)

### **L - Liskov Substitution**
- Controllers follow consistent interface patterns
- Services can be swapped for testing

### **I - Interface Segregation**
- Services expose only necessary methods
- Controllers don't depend on unnecessary functionality

### **D - Dependency Inversion**
- Controllers depend on service abstractions
- Not directly on implementation details
- Easy to swap implementations

---

## 🔄 User Interaction Flows

### **Scenario 1: Chat with Notes**
1. User opens dashboard
2. Selects/creates notebook
3. Uploads audio file → gets notes
4. Enters API key → connects to Gemini
5. Types question about notes
6. Gemini answers using notes as context
7. Notes saved, chat history saved

### **Scenario 2: Generate Notes from Lecture**
1. User has lecture recording (MP3, WAV, etc.)
2. Opens Lecturly → creates new notebook
3. Connects to FastAPI backend
4. Selects audio file
5. Clicks "Generate Notes"
6. Background thread uploads to FastAPI
7. FastAPI sends to Gemini Files API
8. Gemini transcribes + generates markdown notes
9. Notes appear in editor
10. User can now chat with notes as context

### **Scenario 3: Continue Previous Conversation**
1. User opens dashboard
2. Selects previously used notebook
3. Chat history is loaded and restored
4. Conversation state is reconstructed
5. User continues asking questions
6. New messages appended to history
7. All changes persisted

---

## 🚀 Technical Highlights

### **Asynchronous Processing**
- ScheduledExecutorService for background tasks
- Platform.runLater() for UI thread safety
- Non-blocking user interface

### **Error Handling**
- Try-catch blocks with informative messages
- Alert dialogs for user feedback
- Fallback mechanisms (e.g., system fonts)

### **API Integration**
- Google Gemini API for AI chat
- Google Gemini Files API for audio transcription
- FastAPI backend for audio file handling

### **Data Persistence**
- JSON serialization with Gson
- Local file storage (~/.lecturly/notebooks/)
- Full conversation history preservation

### **Modern UI Framework**
- JavaFX for cross-platform desktop UI
- CSS styling with Netflix theme
- Responsive layouts with auto-sizing

---

## 🔧 Testing & Quality

While no unit tests are included in the deliverable, the architecture supports testing through:
- Dependency injection for mocking services
- Clear separation of concerns
- Stateless service methods
- Observable state changes

Example test scenarios:
```java
// Mock GeminiChatService for ChatController tests
@Test
void testChatMessageFlow() {
    // Verify message appears in UI
    // Verify API is called
    // Verify history is updated
}

// Test NotebookStorageService
@Test
void testSaveAndLoad() {
    // Create notebook
    // Save to disk
    // Load from disk
    // Verify content matches
}
```

---

## 📊 Class Dependencies

```
LecturlyApp
  ↓
DashboardController
  ├→ NotebookStorageService
  └→ NotebookController
       ├→ NotebookStorageService
       ├→ GeminiChatService
       ├→ AudioProcessingService
       └→ Notebook

ChatController
  └→ GeminiChatService

NotesController
  └→ AudioProcessingService

GeminiChatService
  └→ Notebook (for history restoration)

AudioProcessingService
  (No internal dependencies)

NotebookStorageService
  ├→ Notebook
  ├→ LocalDateTimeAdapter
  └→ Gson
```

---

## 🎓 Learning Outcomes

By studying this project, you'll understand:

1. **Object-Oriented Design**: Real-world application of OOP principles
2. **Design Patterns**: MVC, Service, Factory, Adapter, Observer
3. **API Integration**: REST client implementation (HTTP, JSON)
4. **Async Programming**: Threading, UI thread safety
5. **Data Persistence**: File I/O, serialization
6. **UI Development**: JavaFX, FXML, CSS styling
7. **Project Structure**: Proper organization for maintainability
8. **Error Handling**: Defensive programming practices
9. **Build Systems**: Gradle configuration and dependency management
10. **Software Architecture**: Layered design, separation of concerns

---

## 🏁 Summary

**Lecturly** demonstrates professional software engineering practices through:
- Clean architecture with clear separation of concerns
- Proper use of design patterns
- Async/concurrent programming
- External API integration
- Persistent data storage
- Modern UI with responsive design
- Comprehensive error handling
- Scalable project structure

The codebase is production-quality and showcases deep understanding of Object-Oriented Programming principles and software design best practices.

---
