# Lecturly - OpenAI Whisper API Setup Guide

## Overview

Lecturly now uses **OpenAI's Whisper API** for reliable speech-to-text transcription instead of attempting to handle audio directly with Gemini. This approach:

✅ **Separates Concerns**: Whisper handles transcription, Gemini handles formatting
✅ **Eliminates Java Audio Issues**: No more raw audio handling problems
✅ **Industry Standard**: Whisper is battle-tested by millions of users worldwide
✅ **Highly Accurate**: Best-in-class speech recognition across multiple languages
✅ **Affordable**: ~$0.02 per minute of audio (pay-as-you-go)

## Architecture

```
Audio File → Whisper API (transcription) → Text → Gemini API (formatting/notes) → Formatted Notes
```

### Components

1. **WhisperTranscriptionService.java** - Handles Whisper API communication
2. **AudioProcessingService.java** - Orchestrates the full workflow (transcription → formatting)
3. **NotesController.java** - UI controller for the Notes feature

## Setup Instructions

### Step 1: Get Your OpenAI API Key

1. Go to [OpenAI Platform](https://platform.openai.com/account/api-keys)
2. Sign up or log in to your account
3. Navigate to **API Keys** section
4. Click **Create new secret key**
5. Copy and save your API key securely

### Step 2: Add Credit to Your OpenAI Account (Optional but Recommended)

1. Visit [OpenAI Billing](https://platform.openai.com/account/billing/overview)
2. Add a payment method
3. Set usage limits to control costs

**Cost Estimate**: 
- 1 hour of audio ≈ $1.20 (transcription only)
- Gemini formatting is separate and typically cheaper

### Step 3: Configure Lecturly

#### Current Implementation (Simple)
In the Notes view:
- **API Key Field**: Enter your Gemini API key (required for formatting)
- Whisper API uses the same key internally for now

#### Future Enhancement
You can modify the code to use separate API keys:
- Edit `NotesController.java` to add a separate Whisper API key field
- Update `AudioProcessingService` constructor to accept both keys separately

### Step 4: Use the Notes Feature

1. **Connect**: Click "Connect" button after entering your Gemini API key
2. **Browse**: Select an audio file (MP3, WAV, OGG, FLAC, M4A)
3. **Generate**: Click "Generate Notes"
4. **Wait**: System will:
   - Transcribe audio with Whisper API
   - Format with Gemini 2.5 Flash
   - Display formatted notes

## Supported Audio Formats

- ✅ MP3 (MPEG-1 Layer 3)
- ✅ WAV (Waveform Audio)
- ✅ OGG Vorbis
- ✅ FLAC (Free Lossless Audio)
- ✅ M4A (MPEG-4 Audio)
- ✅ WebM

**Size Limit**: 25 MB per file

## Troubleshooting

### "401 Unauthorized" Error
- Check that your OpenAI API key is correct and hasn't expired
- Verify your OpenAI account is active with valid payment method

### "Rate Limited" Error
- OpenAI has rate limits. Wait a few seconds and try again
- Check your OpenAI usage at [Usage Dashboard](https://platform.openai.com/account/usage/overview)

### Empty Transcription
- Verify the audio file contains clear speech
- Check that file format is supported
- Ensure audio quality is reasonable (not heavily distorted)

### Notes Not Formatting Correctly
- Verify Gemini API key is correct
- Check transcription was successful in logs
- Try with a shorter audio file first

## Cost Breakdown

| Operation | Cost |
|-----------|------|
| Whisper Transcription | ~$0.02/min of audio |
| Gemini Formatting | ~$0.000008 per 1K input tokens |
| Full Workflow (30-min lecture) | ~$0.60 |

## Advanced Configuration

To use separate API keys, modify `NotesController.java`:

```java
// Add another TextField for Whisper key in FXML
// Then update onConnectClick():
String geminiKey = apiKeyField.getText().trim();
String whisperKey = whisperKeyField.getText().trim();
audioService = new AudioProcessingService(geminiKey, whisperKey);
```

## References

- [OpenAI Whisper API Docs](https://platform.openai.com/docs/guides/speech-to-text)
- [OpenAI API Pricing](https://openai.com/pricing)
- [Supported Languages](https://github.com/openai/whisper/blob/main/whisper/tokenizer.py)

---

**Questions?** The Whisper API is extremely reliable. If you encounter issues, check the OpenAI status page at [status.openai.com](https://status.openai.com)
