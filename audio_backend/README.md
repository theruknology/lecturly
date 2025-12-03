# Lecturly Audio Backend

A FastAPI server that handles audio transcription and lecture note generation using Google's Gemini API.

## Setup

### 1. Install Python dependencies
```bash
cd audio_backend
pip install -r requirements.txt
```

### 2. Set API Key
```bash
# On Windows (PowerShell)
$env:GEMINI_API_KEY = "your-api-key-here"

# On Windows (CMD)
set GEMINI_API_KEY=your-api-key-here

# On Linux/Mac
export GEMINI_API_KEY=your-api-key-here
```

### 3. Run the server
```bash
python app.py
```

The server will start on `http://localhost:8000`

## API Endpoints

### POST /audio-to-notes
Convert audio file to formatted lecture notes.

**Request:**
- Content-Type: `multipart/form-data`
- Body: Audio file (MP3, WAV, OGG, FLAC, M4A, AAC, AIFF)

**Response:**
```json
{
  "success": true,
  "notes": "# Lecture Title\n\n## Main Topics...",
  "filename": "lecture.mp3",
  "mime_type": "audio/mpeg"
}
```

### POST /health
Health check endpoint.

**Response:**
```json
{
  "status": "ok",
  "service": "lecturly-audio-backend"
}
```

### GET /
API information endpoint.

## Usage from Java

```java
// 1. Read audio file
File audioFile = new File("lecture.mp3");

// 2. Create multipart request
HttpRequest request = HttpRequest.newBuilder()
    .uri(new URI("http://localhost:8000/audio-to-notes"))
    .header("Content-Type", "multipart/form-data")
    .POST(HttpRequest.BodyPublishers.ofFile(audioFile))
    .build();

// 3. Send request
HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

// 4. Parse response
JsonObject result = gson.fromJson(response.body(), JsonObject.class);
String notes = result.get("notes").getAsString();
```

## Supported Audio Formats
- MP3 (audio/mpeg)
- WAV (audio/wav)
- OGG (audio/ogg)
- FLAC (audio/flac)
- M4A (audio/mp4)
- AAC (audio/aac)
- AIFF (audio/aiff)

## File Size Limits
- Maximum: 20MB
- Recommended: Under 10MB for faster processing

## Notes
- The server handles resumable uploads for better reliability
- Gemini 2.5 Flash model processes audio with high accuracy
- Generated notes are formatted in Markdown
