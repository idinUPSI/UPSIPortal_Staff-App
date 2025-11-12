# 🚀 Cara Debug Cepat - Live Reload

## Method 1: Chrome Remote Debugging (Paling Mudah!)

### Setup:
1. **Enable USB Debugging** di phone:
   - Settings → About Phone → Tap "Build Number" 7 kali
   - Settings → Developer Options → Enable "USB Debugging"

2. **Sambung phone ke komputer** dengan USB

3. **Buka Chrome** di komputer, pergi ke:
   ```
   chrome://inspect
   ```

4. **Pilih device** anda dan klik "inspect" pada app

### Kebaikan:
- ✅ Edit CSS live dan tengok result instantly
- ✅ Console log untuk debug JavaScript
- ✅ Inspect element seperti biasa
- ✅ Network monitoring
- ✅ Tiada perlu rebuild APK!

---

## Method 2: Live Reload dengan HTTP Server

### Setup Sekali Sahaja:

1. **Dapatkan IP komputer**:
   ```powershell
   ipconfig
   ```
   Cari IPv4 Address (contoh: `192.168.1.100`)

2. **Edit `capacitor.config.dev.json`**:
   - Tukar `YOUR_IP_HERE` dengan IP komputer anda
   - Contoh: `"url": "http://192.168.1.100:8100"`

3. **Copy config untuk development**:
   ```powershell
   copy capacitor.config.dev.json capacitor.config.json
   ```

4. **Sync dan build sekali sahaja**:
   ```powershell
   npm run build
   cd android
   .\gradlew assembleDebug
   ```

5. **Install APK** di phone (sekali sahaja)

### Cara Guna:

1. **Start HTTP server** (biarkan running):
   ```powershell
   npm run dev
   ```

2. **Edit fail** dalam `www/` folder

3. **Refresh app** di phone (swipe down atau reopen)

4. **Tengok perubahan instantly!**

### Bila Dah Siap Development:

Tukar balik ke production config:
```powershell
copy capacitor.config.json capacitor.config.dev.json
git restore capacitor.config.json
npm run build
```

---

## Method 3: Browser Testing (Untuk UI Sahaja)

Untuk test UI/CSS cepat (tanpa native features):

```powershell
npm run dev
```

Buka browser: `http://localhost:8100`

**Nota**: Native features (status bar, splash screen, etc) tak akan berfungsi.

---

## Tips:

- **Method 1** (Chrome Remote Debugging) adalah paling bagus untuk debug CSS issues
- **Method 2** (Live Reload) bagus untuk development berterusan
- **Method 3** (Browser) bagus untuk test UI cepat

## Troubleshooting:

**Phone tak detect di chrome://inspect:**
- Pastikan USB Debugging enabled
- Cuba tukar USB cable atau port
- Restart ADB: `adb kill-server` then `adb start-server`

**Live reload tak berfungsi:**
- Pastikan phone dan komputer dalam WiFi yang sama
- Check firewall tidak block port 8100
- Verify IP address betul dalam config
