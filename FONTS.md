# 🎨 Barlow Font Setup

The Lecturly application uses **Barlow** font from Google Fonts for a modern, professional look.

## Current Status

✅ **The app works without font files!**

The application currently:
- Uses Barlow if available
- Falls back to Segoe UI (excellent alternative)
- Finally falls back to system defaults
- All look great - no action required

## Adding Barlow Font Files (Optional Enhancement)

If you want the exact Barlow font for premium typography:

### Option 1: Automatic Download (Recommended)

Python script to download Barlow font:

```bash
cd lecturly/fonts
python download_fonts.py
```

### Option 2: Manual Download

1. Go to https://fonts.google.com/specimen/Barlow
2. Click "Download Family"
3. Extract the ZIP file
4. Copy these TTF files to `lecturly/fonts/`:
   - `Barlow-Regular.ttf` (weight 400)
   - `Barlow-Medium.ttf` (weight 500)
   - `Barlow-Bold.ttf` (weight 700)
   - `Barlow-SemiBold.ttf` (weight 600)

### Option 3: Via Package Manager

**Windows (Chocolatey):**
```bash
choco install fonts-barlow
```

**Mac (Homebrew):**
```bash
brew install --cask font-barlow
```

**Linux:**
```bash
sudo apt-get install fonts-barlow
```

## File Structure

```
lecturly/
├── fonts/                          (Create this folder)
│   ├── Barlow-Regular.ttf         (400 weight)
│   ├── Barlow-Medium.ttf          (500 weight)
│   ├── Barlow-SemiBold.ttf        (600 weight)
│   └── Barlow-Bold.ttf            (700 weight)
├── src/
│   └── main/
│       └── resources/
│           └── org/example/lecturly/
│               └── main-view.fxml
└── ...
```

## Font Usage in UI

The FXML file (`main-view.fxml`) specifies fonts with fallback chain:

```xml
style="-fx-font-family: 'Barlow', 'Segoe UI', 'Arial', sans-serif;"
```

### Font Sizes Used

- **Logo/Titles:** 20pt, Bold
- **Section Headers:** 16pt, Bold
- **Labels:** 14-11pt, Regular
- **Body Text:** 12-13pt, Regular
- **Small Text:** 9-10pt, Regular

## Font Weights

| Element | Weight | Usage |
|---------|--------|-------|
| Navigation button (active) | 700 (Bold) | Connect, Send, Browse |
| Headers | 600 (SemiBold) | Section titles |
| Normal text | 500 (Medium) | Input fields, labels |
| Regular text | 400 (Regular) | Body text, messages |

## Benefits of Barlow Font

✨ Modern and clean
✨ Excellent readability
✨ Professional appearance
✨ Multiple weights available
✨ Free and open-source
✨ Google Fonts certified

## If You Don't Add Fonts

No problem! The app uses system fallbacks:
- **Windows:** Segoe UI (excellent!)
- **Mac:** San Francisco
- **Linux:** DejaVu Sans (good)

All fallbacks look professional and are fully readable.

## Font License

Barlow is licensed under the **Open Font License (OFL)**
- Free for personal and commercial use
- Attribution appreciated but not required
- Modifications allowed
- Learn more: https://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL

## Testing Fonts

To verify fonts are loaded:

1. Run the application
2. Check the UI text in:
   - Chat input area
   - Notes section headers
   - Button labels

If text looks crisp and modern → Barlow is loaded
If text looks system-like → Fallback fonts are used (still looks good!)

## Issues?

If fonts don't load:
1. Ensure font files are in correct location: `lecturly/fonts/`
2. Use correct file names (case-sensitive on some systems)
3. Restart the application
4. Check font files are valid TTF format

## Download Links

- **Official:** https://fonts.google.com/specimen/Barlow
- **GitHub:** https://github.com/jpt/barlow

---

**TL;DR:** 
- ✅ App works beautifully without font files
- 🎨 Optional: Add TTF files to `fonts/` folder for exact look
- 💯 Both options look professional and modern
