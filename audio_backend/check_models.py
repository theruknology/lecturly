import httpx
import json
import os

key = os.environ.get('GEMINI_API_KEY') or 'AIzaSyDQKtMjXdTpsugGC9DB-1QQDB5HkMTQhrE'

print("Fetching available models...")
r = httpx.get(f'https://generativelanguage.googleapis.com/v1beta/models?key={key}')

if r.status_code == 200:
    data = r.json()
    print("\n✅ Available Models:\n")
    for model in data.get('models', []):
        name = model.get('name', '').replace('models/', '')
        display_name = model.get('displayName', name)
        supported_methods = model.get('supportedGenerationMethods', [])
        print(f"• {name}")
        print(f"  Display: {display_name}")
        print(f"  Methods: {', '.join(supported_methods)}\n")
else:
    print(f"❌ Error: {r.status_code}")
    print(r.text)
