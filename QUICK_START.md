# 🚀 Quick Start Guide - Lecturly

## What is Lecturly?

A **Netflix-style lecture intelligence platform** that:
- 💬 Chat with Gemini 2.5 Flash AI in real-time
- 📝 Convert lecture audio → beautifully formatted notes in seconds
- 🎨 Modern dark theme with red (#e50914) Netflix accents
- ⚡ Fast, reliable, professional interface

## Getting Started in 3 Steps

### Step 1: Get Your API Keys

#### 🔑 Gemini API Key (Required for Chat & Note Formatting)
1. Visit https://ai.google.dev/gemini-api/docs/api-key
2. Click "Get API Key"
3. Select or create a project
4. Copy your key
5. **Paste it in Lecturly Chat section under "Enter your Gemini API key"**

#### 🔑 OpenAI API Key (Required for Audio Transcription)
1. Visit https://platform.openai.com/account/api-keys
2. Click "Create new secret key"
3. Copy your key
4. **Paste it in Lecturly Notes section (same field as Gemini key for now)**

**Note**: For now, use the same key field. Future versions can use separate fields.

### Step 2: Launch the App

```bash
cd /path/to/lecturly
./gradlew run
```

Or run the JAR file if already built.

### Step 3: Start Using

#### Chat Section:
```
1. Enter Gemini API key
2. Click "Connect"
3. Type your message
4. Press Enter or click "Send"
5. Watch the Netflix-style chat happen! 💬
```

#### Notes Section:
```
1. Enter Gemini API key (uses both Gemini + Whisper)
2. Click "Connect"
3. Click "Browse Files" 
4. Select MP3/WAV/OGG/FLAC/M4A audio file
5. Click "Generate Notes"
6. Wait 1-3 minutes (depending on audio length)
7. Get beautiful markdown notes! 📝
8. Click "Copy Notes" to copy to clipboard
```

## Supported Audio Formats

| Format | Extension | Quality | Speed |
|--------|-----------|---------|-------|
| MP3 | .mp3 | Great | Fast |
| WAV | .wav | Excellent | Fast |
| OGG | .ogg | Good | Fast |
| FLAC | .flac | Best | Fast |
| M4A | .m4a | Good | Fast |

**Recommended**: MP3 or WAV (best balance of quality & file size)

## Features

### 💬 Chat Features
- ✅ Real-time messaging with Gemini 2.5 Flash
- ✅ Conversation history maintained
- ✅ Syntax highlighting for code
- ✅ Clear conversation button
- ✅ Connection status indicator

### 📝 Notes Features
- ✅ Audio file upload browser
- ✅ Real-time transcription with Whisper
- ✅ AI-powered note formatting
- ✅ Copy-to-clipboard
- ✅ Editable notes field
- ✅ Professional markdown output

### 🎨 UI Features
- ✅ Netflix-inspired dark theme
- ✅ Red accent color (#e50914)
- ✅ Smooth animations
- ✅ Responsive layout
- ✅ Professional typography
- ✅ Status indicators

## Example Workflows

### Workflow 1: Chat with Gemini
```
YOU:     "Explain quantum entanglement like I'm 5"
GEMINI:  "[Detailed, kid-friendly explanation]"
YOU:     "What about in physics terms?"
GEMINI:  "[Professional physics explanation]"
```

### Workflow 2: Create Lecture Notes
```
1. Record your lecture to MP3 (use Voice Memos app)
2. Open Lecturly → Notes section
3. Upload the MP3
4. Wait for transcription + formatting
5. Get organized lecture notes with:
   - Key topics
   - Definitions
   - Examples
   - Summary
   - Review questions
```

### Workflow 3: Study Guide Generation
```
1. Download lecture recording
2. Upload to Lecturly
3. Get automatic study guide
4. Share or export
5. Use for review
```

## Cost Breakdown

### Per Session Costs

| Service | Cost | Details |
|---------|------|---------|
| Chat (100 messages) | ~$0.10 | ~1000 tokens × $0.000075 |
| 30-min lecture transcription | ~$0.60 | ~30 min × $0.02/min |
| Note formatting | ~$0.05 | ~2000 tokens × $0.000075 |
| **Total (1 lecture)** | **~$0.75** | Very affordable! |

### Free Trial Strategy
- OpenAI: Start with $5 free credit (good for ~200 min of audio)
- Google: Free tier (good for ~1000 calls to Gemini)

## Tips & Tricks

### 💡 For Better Transcriptions
- Use clear, high-quality audio
- Avoid heavy background noise
- Speak clearly and at normal pace
- Record in English or major languages (Whisper supports 99 languages)

### 📚 For Better Notes
- Long lectures (30+ min) get more comprehensive notes
- Technical lectures benefit from review questions
- Notes are in markdown - export to PDF if needed

### 💰 Cost Optimization
- Combine multiple short clips into one submission
- Use during off-peak hours if possible
- Monitor OpenAI usage dashboard regularly

### ⚡ Performance Tips
- Close other apps to free up memory
- Use WiFi for faster uploads
- First-time setup may take 2-3 minutes
- Subsequent uses are faster

## Troubleshooting

### Chat Not Working
**Problem**: "Failed to connect"
**Solution**: 
- Check Gemini API key is correct
- Verify you have internet connection
- Try refreshing the app

### Notes Not Generating
**Problem**: "Error generating notes"
**Solution**:
- Check both API keys are valid
- Verify audio file is supported format
- Ensure file is not corrupted
- Try with a different audio file

### White Background in Chat
**Problem**: Chat area looks white/broken
**Solution**: This was a known bug - you should have the black background now!

### Slow Performance
**Problem**: App is taking too long
**Solution**:
- Longer audio = longer processing
- Check internet speed
- Restart the app
- Try again later

## Support Resources

- **Documentation**: See `WHISPER_API_SETUP.md`
- **Implementation Details**: See `IMPLEMENTATION_SUMMARY.md`
- **Code Changes**: See `BEFORE_AFTER.md`
- **OpenAI Help**: https://help.openai.com
- **Google Gemini Docs**: https://ai.google.dev

## Next Steps

After you get it working:

1. **Try the chat feature first** - makes sure your API key works
2. **Test with a short 1-min audio clip** - verify transcription works
3. **Use for your next lecture** - full workflow
4. **Adjust note format** - edit the prompt in code if needed

## Advanced Users

### Custom Note Format
Edit `AudioProcessingService.java` line ~73-87 to customize the note format:
```java
instruction.addProperty("text", "YOUR_CUSTOM_PROMPT_HERE");
```

### Use Different Models
Replace "gemini-2.5-flash" with:
- `gemini-1.5-pro` (more powerful, slightly more expensive)
- `gemini-1.5-flash` (faster, cheaper)

### Separate API Keys
Edit `NotesController.java` to add a second API key field for Whisper if you want.

---

**You're all set!** 🎉 

Start with the Chat feature to verify everything works, then try the Notes feature. Enjoy your new lecture intelligence platform!

**Questions?** Check the documentation files or review the code - it's well-commented!
