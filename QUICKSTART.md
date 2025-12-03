# Quick Start Guide - Lecturly

## 🚀 Get Started in 5 Minutes

### Step 1: Get Your Gemini API Key (1 min)
1. Go to https://aistudio.google.com/apikey
2. Click "Create API Key"
3. Copy the key

### Step 2: Start the Backend (2 min)
```bash
# Open a Terminal/PowerShell and run:
cd lecturly/audio_backend
pip install -r requirements.txt
set GEMINI_API_KEY=your-api-key-here
python app.py
```

✅ You should see: `INFO: Application startup complete`

### Step 3: Run the App (1 min)
```bash
# Open another Terminal/PowerShell and run:
cd lecturly
./gradlew run
```

✅ The Lecturly window opens!

### Step 4: Use It! (1 min)

#### **Chat Tab**
1. Paste your Gemini API key
2. Click "Connect"
3. Type a message and hit "Send"

#### **Notes Tab**
1. Click "Connect" (checks backend)
2. Click "📁 Browse Files"
3. Select an MP3/WAV audio file
4. Click "🚀 Generate Notes"
5. ✨ Notes appear in markdown!

---

## 📁 Required Folder Structure

```
lecturly/
├── audio_backend/          ← Start Python here first
│   ├── app.py
│   └── requirements.txt
└── ...
```

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| Backend not found | Make sure Python is running in terminal 1 |
| API Key Invalid | Double-check key from aistudio.google.com |
| Audio file size too large | Keep under 20MB (recommended: <10MB) |
| Chat not responding | Wait a few seconds, Gemini can be slow |
| Font looks different | Barlow font not installed (fallback used) |

---

## 🎨 Optional: Better Fonts

Download and add Barlow font for premium look:
1. Get fonts from: https://fonts.google.com/specimen/Barlow
2. Create `lecturly/fonts/` folder
3. Add TTF files there
4. Restart the app

---

## 💡 Tips

- Use markdown formatting in notes for better readability
- Copy notes and paste into Word/Notion
- Keep backend window open while using app
- Chat context is saved during your session
- Maximum audio: 9.5 hours per file

---

## 📚 Full Documentation

See `SETUP.md` for detailed setup and troubleshooting.

Enjoy! 🎬
