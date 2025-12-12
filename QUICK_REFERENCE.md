# LECTURLY Demo Day - Quick Reference Cards

## 🎯 PERSON 1 - Architecture (2-3 min)

### What to Say:
```
"Lecturly demonstrates a professional 5-layer architecture:
1. UI Layer (JavaFX + FXML)
2. Controller Layer (Dashboard, Notebook, Chat controllers)
3. Service Layer (GeminiChatService, AudioProcessingService, Storage)
4. Model Layer (Notebook, ChatMessage objects)
5. API Layer (Gemini, FastAPI, File I/O)

We used design patterns:
- MVC: Model (Notebook), View (FXML), Controller (DashboardController)
- Service: Encapsulates business logic
- Dependency Injection: Services passed to controllers
- Factory: Creating UI components
- Observer: Real-time data updates

SOLID Principles:
- Single Responsibility: Each class has one job
- Open/Closed: Can extend without modifying
- Liskov Substitution: Services are swappable
- Interface Segregation: Only expose needed methods
- Dependency Inversion: Depend on abstractions
```

### Key Code Points:
```java
// MVC in action
DashboardController {
  - Model: List<Notebook> notebooks
  - View: FXML layout
  - Controller: onCreateNotebook(), loadNotebooks()
}

// Dependency Injection
ChatController(TextField apiKeyField, Button connectButton, ...) {
  // Services passed in, not created here
  this.chatService = new GeminiChatService(apiKey);
}

// Layering
Controller → Service → External API
   (UI)    (Logic)    (External)
```

### Diagram to Show:
```
┌─────────────────────────────────┐
│   UI Layer (JavaFX FXML)        │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│  Controller Layer               │
│  Dashboard, Notebook, Chat      │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│  Service Layer (Business Logic) │
│  Chat, Audio, Storage Services  │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│  Model Layer (Data Objects)     │
│  Notebook, ChatMessage          │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│  API Layer (External Services)  │
│  Gemini, FastAPI, Files API     │
└─────────────────────────────────┘
```

---

## 💾 PERSON 2 - Backend Services (3-4 min)

### What to Say:
```
"Three core services handle all business logic:

1. GeminiChatService:
   - Maintains conversation history as List<JsonObject>
   - Each message: {"role": "user/model", "parts": [{"text": "..."}]}
   - Sends to Gemini API with full history
   - Supports notes context: system instruction prepended to API request
   - Returns parsed String response

2. AudioProcessingService:
   - Sends audio files to FastAPI backend
   - Builds multipart/form-data requests
   - Handles different audio formats (MP3, WAV, OGG, FLAC, etc.)
   - Health checks: /health endpoint
   - Returns generated notes as JSON response

3. NotebookStorageService:
   - Serializes Notebook to JSON with Gson
   - Stores in ~/.lecturly/notebooks/{id}.json
   - Maintains index for quick lookups
   - Deserializes on load
   - Custom LocalDateTime adapter for JSON dates

Data Model:
- Notebook: id, name, notes, chatHistory, timestamps
- ChatMessage: role, content, timestamp
```

### Key Code Points:
```java
// Conversation history flow
public String chat(String userMessage) {
  // 1. Add user to history
  conversationHistory.add({role: "user", content});
  
  // 2. Build API request
  requestBody.add("contents", conversationHistory);
  if (notesContext != null) {
    requestBody.add("systemInstruction", notes);
  }
  
  // 3. Send POST request
  response = httpClient.send(request);
  
  // 4. Parse response
  assistantResponse = extractText(response);
  
  // 5. Add to history
  conversationHistory.add({role: "model", content});
  
  return assistantResponse;
}

// Persistence
Notebook → Gson → JSON → File I/O
```

### Data Flow:
```
User Chat          Audio Upload       Save Notebook
  ↓                  ↓                   ↓
Message → History → Gemini           File → JSON
                      ↓                    ↓
                    Response ← Storage Service
```

---

## 🎨 PERSON 3 - Frontend & UI (2 min)

### What to Say:
```
"Four main controllers manage the UI:

1. DashboardController:
   - Entry point showing all notebooks
   - Create new notebook via TextInputDialog
   - Load notebooks and render cards
   - Click card to open notebook
   - Hover effects for visual feedback

2. NotebookController:
   - Split pane: Notes (left) + Chat (right)
   - Upload audio and generate notes
   - Chat with Gemini while viewing notes
   - Real-time context updates:
     When notes change → chatService.setNotesContext(notes)
   - Save/load from storage

3. ChatController:
   - Connect to Gemini API with key
   - Send/receive messages
   - Display formatted message bubbles
   - Different styles: user/assistant/system
   - Auto-scroll to latest message
   - Copy to clipboard

4. NotesController:
   - Browse and select audio file
   - Check backend availability
   - Show progress while processing
   - Display generated notes
   - Copy notes to clipboard

Design:
- Netflix dark theme: #000000 background, #e50914 red
- Barlow Condensed font
- Responsive layouts with auto-sizing
- Status labels with color (green/yellow/red)
- Disabled buttons based on workflow state
```

### UI States:
```
Not Connected:
  [API Key Input] [Connect Button] (enabled)
  
Connected:
  [API Key] (disabled) [Connected ✓] (status)
  [Chat Area] (enabled)
  [Send Button] (enabled)
  
Generating Notes:
  [Loading... 50%]
  [Cancel] (disabled)
```

---

## 🔌 PERSON 4 - API Integration (2 min)

### What to Say:
```
"Two main HTTP clients handle external APIs:

1. GeminiChatService HTTP Client:
   - Endpoint: generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
   - Method: POST
   - Auth: API key in URL parameter
   
   Request:
   {
     "systemInstruction": {
       "parts": [{"text": "Here are notes: [notes]. Use them to answer."}]
     },
     "contents": [
       {"role": "user", "parts": [{"text": "user message"}]},
       {"role": "model", "parts": [{"text": "response"}]}
     ]
   }
   
   Response:
   {
     "candidates": [{
       "content": {
         "parts": [{"text": "This is the AI response"}]
       }
     }]
   }

2. AudioProcessingService HTTP Client:
   - Endpoint: http://localhost:8000/audio-to-notes
   - Method: POST with multipart/form-data
   - Headers: Content-Type: multipart/form-data; boundary=...
   
   Body Format:
   --Boundary
   Content-Disposition: form-data; name="file"; filename="audio.mp3"
   Content-Type: audio/mpeg
   
   [binary audio data]
   --Boundary--
   
   Response:
   {
     "notes": "# Lecture Notes\n## Topic 1\n..."
   }

Error Handling:
- Check status codes (200-299)
- Parse error messages
- Show user-friendly alerts
- Rollback state on failure
```

### Request-Response Flow:
```
Java Code
   ↓
HttpClient.newHttpClient()
   ↓
HttpRequest.newBuilder()
  .uri()
  .header()
  .POST()
  .build()
   ↓
httpClient.send(request, BodyHandlers.ofString())
   ↓
HttpResponse<String>
   ↓
Gson.fromJson()
   ↓
Parse and return result
```

---

## 🎬 PERSON 5 - Live Demo (4-5 min)

### Demo Script:

**[0:00-0:30] Intro**
```
"Hi everyone! I'm going to show you Lecturly in action.
It's a smart lecture notes platform that combines AI with
note management. Let me walk you through the key features."
```

**[0:30-1:00] Dashboard**
```
"First, here's the dashboard. This shows all your notebooks.
Each notebook contains lecture notes and chat history.
Let me create a new one."

[Click Create Button]
"I'll name it 'Demo Notebook'"
[Type name, click OK]
"As you can see, it appears instantly in the list."
```

**[1:00-2:00] Open Notebook & Upload Audio**
```
"Now let me open this notebook to show the editor."
[Click on notebook]
"Here's the split-pane interface. Left side: notes with audio upload.
Right side: chat interface."

[Click Browse File]
"I'll select a pre-recorded lecture audio file."
[Select file]
"Here's a 10-minute lecture recording on Data Structures.
Let me generate notes from this."

[Click Generate Notes]
"This will process the audio, transcribe it, and generate
structured markdown notes using Gemini AI."
[Wait for completion - show loading indicator]
```

**[2:00-3:00] Notes Display & Chat**
```
"Perfect! Here are the generated notes. Notice they're in
markdown format with sections and bullet points - automatically
organized by the AI.

Now let me demonstrate the chat feature. I'll enter my API key
and ask questions about these notes."

[Enter API key]
[Type in chat: "What are the main data structures covered?"]
[Send message]
"Notice the message appears immediately as user message.
The AI is processing..."
[Response comes back]
"Great! The AI references the notes and provides specific answers.
Let me ask a follow-up:"

[Type: "Can you explain trees in more detail?"]
[Send and show response]
"The conversation continues, building on context. This is the
power of having your notes and an AI chat in one place."
```

**[3:00-3:30] Save & Persistence**
```
"Let me make a quick edit to the notes."
[Edit notes slightly]
"Now I'll save it."
[Click Save]
"The data is persisted locally. Let me go back to dashboard
and reopen this notebook to show it's saved."

[Click Back]
[Click on same notebook again]
"See? The notes are still there, and the chat history is preserved.
You can close and reopen notebooks without losing data."
```

**[3:30-4:00] Wrap up**
```
"To summarize what you saw:
1. Modern, clean UI built with JavaFX
2. Seamless audio processing (via FastAPI backend)
3. Smart note generation from audio
4. AI-powered chat with context awareness
5. Full persistence and state management

All of this is built on a clean, layered architecture
with proper OOP design patterns throughout.
Thank you!"
```

### Key Demo Tips:
```
✓ Pre-record or pre-test everything
✓ Have a backup audio file ready
✓ Test API key beforehand
✓ Keep demo fast-paced
✓ Point to UI elements as you speak
✓ Speak clearly and confidently
✓ Have backup screenshots just in case
```

---

## ❓ Quick Q&A Reference

### For Person 1:
```
Q: Why Java instead of Python?
A: "JavaFX provides excellent cross-platform desktop UI.
   Java's strong typing aligns with our OOP design.
   Also demonstrates modern Java capabilities (Java 21, modules)."

Q: How does composition help?
A: "Controllers use composition with services. DashboardController
   composes NotebookStorageService. This is loose coupling -
   easy to test and swap implementations."
```

### For Person 2:
```
Q: How is conversation preserved?
A: "Two-part solution: (1) In-memory List<JsonObject> for current
   chat, (2) Serialized in Notebook JSON for persistence.
   On load, we restore full history into memory."

Q: What if API fails?
A: "We catch exceptions and rollback. If chat() fails,
   we remove the user message from history before throwing exception.
   UI shows error dialog and lets user retry."
```

### For Person 3:
```
Q: How do you handle async operations in UI?
A: "We use ScheduledExecutorService for background tasks.
   When done, Platform.runLater() switches back to UI thread.
   This prevents blocking and freezing the interface."

Q: Why multiple controllers?
A: "Single Responsibility Principle. Each controller handles
   one view's logic. Dashboard manages notebooks,
   NotebookController manages the editor, etc."
```

### For Person 4:
```
Q: Why build custom HTTP client?
A: "We needed full control over request format, especially
   for multipart file uploads. Java's HttpClient is more flexible
   than libraries, no external dependencies beyond Gson."

Q: How is authentication handled?
A: "For Gemini API, we pass API key in URL parameter.
   For FastAPI, it reads key from environment variable.
   Both methods are stateless and scalable."
```

### For Person 5:
```
Q: What was the hardest part to implement?
A: "Probably the audio file upload with multipart/form-data.
   Had to manually construct the request body following RFC 2388
   specification, but once that worked, everything else was smooth."

Q: What would you improve?
A: "Three things: (1) Add unit tests throughout,
   (2) Database instead of file storage for scalability,
   (3) User authentication for multi-user support."
```

---

## ⏱️ Timing Checklist

```
0:00-1:00  ✓ Person 5: Welcome (1 min)
1:00-3:00  ✓ Person 1: Architecture (2 min)
3:00-6:00  ✓ Person 2: Services (3 min)
6:00-8:00  ✓ Person 3: UI (2 min)
8:00-10:00 ✓ Person 4: APIs (2 min)
10:00-14:00 ✓ Person 5: Demo (4 min)
14:00-15:00 ✓ All: Q&A (1 min)

BUFFER: 0 min (tight schedule - practice timing!)
```

---

## 🏆 Success Checklist

- [ ] All 5 people know their section
- [ ] Can explain concepts without reading notes
- [ ] Demo tested and working
- [ ] Smooth transitions between speakers
- [ ] Eye contact with audience
- [ ] Speaking clearly and confidently
- [ ] Answers prepared for common questions
- [ ] Positive body language
- [ ] Show enthusiasm for the project

You've got this! 🚀

---
