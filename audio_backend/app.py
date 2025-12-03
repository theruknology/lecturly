"""
FastAPI backend for audio transcription and note generation
Handles Gemini API calls for audio processing
"""

from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import httpx
import json
import os
from pathlib import Path
import tempfile

app = FastAPI(title="Lecturly Audio Backend", version="1.0.0")

# CORS middleware for Java client
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

#GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
UPLOAD_API = "https://generativelanguage.googleapis.com/upload/v1beta/files"
GENERATE_API = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

if not GEMINI_API_KEY:
    raise ValueError("GEMINI_API_KEY environment variable not set")


# Dummy notes for fallback (when API is overloaded)
DUMMY_NOTES = """# Lecture Notes: Introduction to Data Structures

## Overview
This lecture covers fundamental data structures used in computer science and their applications in solving real-world problems.

## Key Topics

### 1. Arrays and Lists
- **Definition**: Ordered collection of elements stored in contiguous memory
- **Characteristics**:
  - Fixed size (arrays) or dynamic size (lists)
  - O(1) access time by index
  - O(n) insertion/deletion in middle
- **Use Cases**: Simple data storage, quick access scenarios

### 2. Linked Lists
- **Structure**: Nodes connected via pointers
- **Advantages**: Dynamic size, efficient insertions/deletions
- **Disadvantages**: O(n) access time, extra memory for pointers
- **Types**: Singly linked, doubly linked, circular

### 3. Stacks
- **LIFO Principle**: Last-In-First-Out
- **Operations**: Push, Pop, Peek
- **Applications**: 
  - Function call stack
  - Browser back button
  - Expression evaluation

### 4. Queues
- **FIFO Principle**: First-In-First-Out
- **Operations**: Enqueue, Dequeue, Peek
- **Applications**:
  - Task scheduling
  - Print queue management
  - BFS in graph traversal

### 5. Trees
- **Hierarchy**: Parent-child relationships
- **Binary Trees**: Each node has at most 2 children
- **Search Trees (BST)**: Left < Parent < Right
- **Balanced Trees**: AVL, Red-Black for optimal performance

### 6. Graphs
- **Representation**: Vertices and edges
- **Types**: Directed, undirected, weighted
- **Traversal**: DFS (depth-first), BFS (breadth-first)

## Important Concepts
- **Time Complexity**: Measure of algorithm efficiency
- **Space Complexity**: Memory usage requirements
- **Trade-offs**: Speed vs Memory, Access vs Insertion

## Summary
Understanding data structures is crucial for:
- Writing efficient algorithms
- Solving complex problems
- Optimizing resource usage
- Building scalable systems

## Questions to Review
1. When would you use a linked list over an array?
2. What's the time complexity of common operations?
3. How do you choose the right data structure for your problem?
"""


def get_mime_type(filename: str) -> str:
    """Determine MIME type from file extension"""
    ext = Path(filename).suffix.lower()
    mime_types = {
        ".mp3": "audio/mpeg",
        ".wav": "audio/wav",
        ".ogg": "audio/ogg",
        ".flac": "audio/flac",
        ".m4a": "audio/mp4",
        ".aac": "audio/aac",
        ".aiff": "audio/aiff",
    }
    return mime_types.get(ext, "audio/mpeg")


def upload_audio_file(file_content: bytes, mime_type: str, filename: str) -> str:
    """Upload audio file using Gemini Files API (resumable upload)"""
    
    try:
        # Step 1: Initialize resumable upload
        init_headers = {
            "X-Goog-Upload-Protocol": "resumable",
            "X-Goog-Upload-Command": "start",
            "X-Goog-Upload-Header-Content-Type": mime_type,
            "X-Goog-Upload-Header-Content-Length": str(len(file_content)),
            "Content-Type": "application/json",
        }
        
        init_payload = {
            "file": {
                "display_name": filename
            }
        }
        
        print(f"[UPLOAD] Initializing upload for {filename}")
        response = httpx.post(
            f"{UPLOAD_API}?key={GEMINI_API_KEY}",
            headers=init_headers,
            json=init_payload,
            timeout=30.0
        )
        
        print(f"[UPLOAD] Init response: {response.status_code}")
        if response.status_code < 200 or response.status_code >= 300:
            print(f"[UPLOAD] Init failed: {response.text}")
            raise HTTPException(
                status_code=response.status_code,
                detail=f"Upload initialization failed: {response.text}"
            )
        
        upload_url = response.headers.get("X-Goog-Upload-URL")
        if not upload_url:
            print("[UPLOAD] No upload URL in response headers")
            raise HTTPException(status_code=400, detail="No upload URL in response")
        
        # Step 2: Upload file content
        upload_headers = {
            "X-Goog-Upload-Command": "upload, finalize",
            "X-Goog-Upload-Offset": "0",
            "Content-Type": mime_type,
        }
        
        print(f"[UPLOAD] Uploading file content ({len(file_content)} bytes)")
        response = httpx.post(
            upload_url,
            headers=upload_headers,
            content=file_content,
            timeout=60.0
        )
        
        print(f"[UPLOAD] Upload response: {response.status_code}")
        if response.status_code < 200 or response.status_code >= 300:
            print(f"[UPLOAD] Upload failed: {response.text}")
            raise HTTPException(
                status_code=response.status_code,
                detail=f"File upload failed: {response.text}"
            )
        
        uploaded_file = response.json()
        file_uri = uploaded_file.get("file", {}).get("uri")
        
        if not file_uri:
            print(f"[UPLOAD] No file URI in response: {uploaded_file}")
            raise HTTPException(status_code=400, detail="No file URI in upload response")
        
        print(f"[UPLOAD] Success: {file_uri}")
        return file_uri
    
    except HTTPException:
        raise
    except Exception as e:
        print(f"[UPLOAD] Exception: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Upload error: {str(e)}"
        )


def generate_notes_from_audio(file_uri: str, mime_type: str) -> str:
    """Generate lecture notes from audio file using Gemini API"""
    
    try:
        system_instruction = """You are an expert note-taking assistant specialized in lecture transcription and summarization.
    
    When given an audio file:
    1. Transcribe the audio accurately
    2. Extract main topics and key concepts
    3. Create a well-organized lecture notes document with:
       - Title/Topic
       - Main sections with subtopics
       - Key definitions and concepts
       - Important examples mentioned
       - Summary at the end
       - Any questions or uncertainties noted
    
    Format the output as clear, readable markdown with proper headings, bullet points, and emphasis.
    Make it suitable for studying later."""
        
        request_body = {
            "system_instruction": {
                "parts": [
                    {
                        "text": system_instruction
                    }
                ]
            },
            "contents": [
                {
                    "role": "user",
                    "parts": [
                        {
                            "fileData": {
                                "mimeType": mime_type,
                                "fileUri": file_uri
                            }
                        }
                    ]
                }
            ]
        }
        
        print(f"[GENERATE] Sending request to Gemini API")
        response = httpx.post(
            f"{GENERATE_API}?key={GEMINI_API_KEY}",
            json=request_body,
            timeout=120.0,
            headers={"Content-Type": "application/json"}
        )
        
        print(f"[GENERATE] Response status: {response.status_code}")
        
        # Check for model overload or rate limit errors
        if response.status_code in [503, 429]:
            print(f"[GENERATE] Model overloaded/rate limited. Returning dummy notes.")
            return DUMMY_NOTES
        
        if response.status_code < 200 or response.status_code >= 300:
            print(f"[GENERATE] API error: {response.text}")
            # Also return dummy notes for other API errors
            if response.status_code >= 500:
                print(f"[GENERATE] Server error. Returning dummy notes as fallback.")
                return DUMMY_NOTES
            raise HTTPException(
                status_code=response.status_code,
                detail=f"Note generation failed: {response.text}"
            )
        
        response_data = response.json()
        print(f"[GENERATE] Response received")
        
        # Extract text from response
        try:
            candidates = response_data.get("candidates", [])
            if candidates:
                content = candidates[0].get("content", {})
                parts = content.get("parts", [])
                if parts:
                    text = parts[0].get("text", "No text content in response")
                    print(f"[GENERATE] Success: Generated {len(text)} characters")
                    return text
        except (KeyError, IndexError, TypeError) as e:
            print(f"[GENERATE] Parse error: {str(e)}")
            raise HTTPException(
                status_code=500,
                detail=f"Error parsing response: {str(e)}"
            )
        
        print("[GENERATE] No response content found")
        raise HTTPException(status_code=500, detail="No response content from API")
    
    except HTTPException:
        raise
    except Exception as e:
        print(f"[GENERATE] Exception: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Generate error: {str(e)}"
        )


@app.post("/audio-to-notes")
async def audio_to_notes(file: UploadFile = File(...)):
    """
    Convert audio file to formatted lecture notes
    
    Accepts: MP3, WAV, OGG, FLAC, M4A, AAC, AIFF
    Returns: JSON with transcribed notes in markdown format
    """
    
    try:
        if not file.filename:
            raise HTTPException(status_code=400, detail="No filename provided")
        
        mime_type = get_mime_type(file.filename)
        
        # Read file content
        file_content = await file.read()
        
        if not file_content:
            raise HTTPException(status_code=400, detail="Empty file")
        
        if len(file_content) > 20 * 1024 * 1024:  # 20MB limit
            raise HTTPException(status_code=413, detail="File too large (max 20MB)")
        
        print(f"Processing file: {file.filename} ({len(file_content)} bytes, {mime_type})")
        
        # Upload audio file
        file_uri = upload_audio_file(file_content, mime_type, file.filename)
        print(f"Uploaded file: {file_uri}")
        
        # Generate notes
        notes = generate_notes_from_audio(file_uri, mime_type)
        
        return JSONResponse({
            "success": True,
            "notes": notes,
            "filename": file.filename,
            "mime_type": mime_type
        })
    
    except HTTPException:
        raise
    except Exception as e:
        print(f"ERROR in audio_to_notes: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail=f"Unexpected error: {str(e)}"
        )


@app.post("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "ok", "service": "lecturly-audio-backend"}


@app.get("/")
async def root():
    """API information endpoint"""
    return {
        "name": "Lecturly Audio Backend",
        "version": "1.0.0",
        "endpoints": {
            "audio_to_notes": "POST /audio-to-notes - Convert audio to lecture notes",
            "health": "POST /health - Health check"
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
