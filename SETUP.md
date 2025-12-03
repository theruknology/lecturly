# Lecturly Setup Guide

## Architecture

```
┌─────────────┐         HTTP          ┌──────────────┐         Gemini API       ┌────────────┐
│   JavaFX    │  ◄────────────────►   │   FastAPI    │  ◄─────────────────────► │  Gemini    │
│   Desktop   │   (multipart audio)   │   Backend    │  (Files API + Generate) │  API       │
│   App       │                       │   (Python)   │                        │            │
└─────────────┘                       └──────────────┘                        └────────────┘
  - Chat                                - Audio upload
  - File Browser                        - Note generation
  - Display notes                       - API key handling
  - Better fonts
```

## Setup Steps

### 1. Java/JavaFX Application

```bash
# Build and run the application
cd lecturly
./gradlew run
```

The application will start and look for the FastAPI backend.

### 2. FastAPI Backend (Python)

**Important:** This must be running for the Notes feature to work.

#### Install Dependencies
```bash
cd audio_backend
pip install -r requirements.txt
```

#### Set Gemini API Key

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY = "your-api-key-here"
```

**Windows (CMD):**
```cmd
set GEMINI_API_KEY=your-api-key-here
```

**Linux/Mac:**
```bash
export GEMINI_API_KEY=your-api-key-here
```

#### Start the Server
```bash
python app.py
```

Server starts at `http://localhost:8000`

### 3. Barlow Font (Optional but Recommended)

For best typography, the app uses the **Barlow** font. To add it:

1. Download Barlow font files from: https://fonts.google.com/specimen/Barlow
2. Download the TTF or OTF files (all weights: Regular, 500, Bold)
3. Create a `fonts/` directory in the project:
   ```bash
   mkdir fonts
   ```
4. Copy the font files into `fonts/`:
   ```
   fonts/
   ├── Barlow-Regular.ttf
   ├── Barlow-Medium.ttf
   └── Barlow-Bold.ttf
   ```

5. Update `HelloApplication.java` to load fonts (optional - currently uses fallback):
   ```java
   // Fonts will be loaded from fonts/ directory if available
   ```

**Note:** If font files aren't provided, the app falls back to system fonts (Segoe UI, system defaults) - it will still look great!

## Usage

### Chat Feature
1. Enter your Gemini API key
2. Click "Connect"
3. Type messages and press "Send" (or Shift+Enter for new lines)
4. AI responses appear with full context

### Notes Feature
1. **Make sure FastAPI backend is running** (see Step 2 above)
2. Click the "📝 Notes" button
3. Click "Connect" (verifies backend availability)
4. Click "📁 Browse Files" to select an audio file
5. Click "🚀 Generate Notes" to process
6. Notes appear in markdown format
7. Click "📋 Copy Notes" to copy to clipboard

## File Structure

```
lecturly/
├── src/main/
│   ├── java/org/example/lecturly/
│   │   ├── HelloApplication.java       (Entry point)
│   │   ├── MainController.java         (Navigation/orchestration)
│   │   ├── ChatController.java         (Chat UI/logic)
│   │   ├── NotesController.java        (Notes UI/logic)
│   │   ├── GeminiChatService.java      (Chat API calls)
│   │   └── AudioProcessingService.java (FastAPI client)
│   └── resources/
│       └── org/example/lecturly/
│           ├── main-view.fxml         (Main UI layout)
│           └── hello-view.fxml        (Legacy)
│
├── audio_backend/
│   ├── app.py                 (FastAPI server - MUST RUN SEPARATELY)
│   ├── requirements.txt        (Python dependencies)
│   └── README.md              (Backend documentation)
│
├── fonts/                      (Optional - Barlow font files)
│   ├── Barlow-Regular.ttf
│   ├── Barlow-Medium.ttf
│   └── Barlow-Bold.ttf
│
├── build.gradle.kts           (Gradle build config)
├── settings.gradle.kts
└── README.md                  (This file)
```

## Troubleshooting

### FastAPI Backend Not Available
- Make sure Python backend is running: `python audio_backend/app.py`
- Verify API key is set: `echo $GEMINI_API_KEY`
- Check port 8000 is not in use: `netstat -ano | findstr :8000`

### Audio Processing Fails
- Check backend logs for API errors
- Verify file is under 20MB
- Supported formats: MP3, WAV, OGG, FLAC, M4A, AAC, AIFF

### Font Not Displaying Correctly
- System fallback fonts (Segoe UI) are being used
- To enable Barlow: provide font files in `fonts/` directory

### Chat Not Working
- Verify Gemini API key is valid
- Check internet connection
- Try refreshing by clicking Connect again

## API Keys

You need a **Google Gemini API key** (free tier available):
1. Go to https://aistudio.google.com/apikey
2. Create a new API key
3. Use it in the application

## Technology Stack

- **Frontend:** JavaFX 21 (Desktop UI)
- **Backend:** FastAPI (Python REST API)
- **AI Model:** Gemini 2.5 Flash
- **Audio:** Gemini Files API
- **Fonts:** Barlow (Google Fonts)
- **Build:** Gradle with Kotlin DSL

## Features

✅ Real-time chat with Gemini 2.5 Flash
✅ Conversation context preservation
✅ Audio-to-notes generation
✅ Dark mode Netflix-style UI
✅ Professional typography (Barlow font)
✅ Markdown formatted output
✅ Copy notes to clipboard
✅ Health checks for backend
✅ Clean separation of concerns

## Performance Notes

- Chat responses: ~2-5 seconds per message
- Audio processing: Depends on audio length (typically 1-3 minutes per hour of audio)
- Recommended audio file size: Under 10MB for faster processing
- Maximum file size: 20MB (Gemini API limit)

## Future Improvements

- [ ] Save chat history to file
- [ ] Export notes as PDF
- [ ] Support for video files
- [ ] Custom note formatting templates
- [ ] Dark mode toggle
- [ ] Theme customization
- [ ] Keyboard shortcuts guide
- [ ] Multi-language support
