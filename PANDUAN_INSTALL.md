# 📱 Panduan Lengkap: Build Aplikasi Sistem Idin

Panduan step-by-step untuk membina aplikasi Android dari website UPSI.

---

## 📋 Langkah 1: Install Software Yang Diperlukan

### A. Install Java Development Kit (JDK)
1. Download **JDK 17** dari: https://adoptium.net/
2. Pilih "Latest LTS Release" → Windows → x64 → .msi installer
3. Install dengan setting default (klik Next sahaja)
4. Verify installation:
   ```powershell
   java -version
   ```
   Sepatutnya keluar: `openjdk version "17.x.x"`

### B. Install Android Studio
1. Download dari: https://developer.android.com/studio
2. Run installer dan pilih:
   - ✅ Android SDK
   - ✅ Android SDK Platform
   - ✅ Android Virtual Device
3. Semasa setup wizard:
   - Pilih "Standard" installation
   - Tunggu download Android SDK selesai (boleh ambil 15-30 minit)
4. Selepas install, buka Android Studio dan:
   - Go to: **Tools → SDK Manager**
   - Tab **SDK Platforms**: Tick ✅ **Android 13.0 (API Level 33)**
   - Tab **SDK Tools**: Pastikan ada:
     - ✅ Android SDK Build-Tools
     - ✅ Android SDK Command-line Tools
     - ✅ Android Emulator
     - ✅ Android SDK Platform-Tools
   - Klik "Apply" dan tunggu download selesai

### C. Install Node.js
1. Download dari: https://nodejs.org/
2. Pilih versi LTS (contoh: 20.x.x)
3. Install dengan setting default
4. Verify:
   ```powershell
   node -version
   npm -version
   ```

### D. Setup Environment Variables
1. Cari lokasi Android SDK (biasanya: `C:\Users\[YourName]\AppData\Local\Android\Sdk`)
2. Buka **System Properties** → **Environment Variables**
3. Tambah atau edit:
   - Variable: `ANDROID_HOME`
   - Value: `C:\Users\[YourName]\AppData\Local\Android\Sdk`
4. Edit `Path` variable, tambah:
   - `%ANDROID_HOME%\platform-tools`
   - `%ANDROID_HOME%\tools`
   - `%ANDROID_HOME%\tools\bin`
5. Restart PowerShell dan verify:
   ```powershell
   echo $env:ANDROID_HOME
   adb version
   ```

---

## 📦 Langkah 2: Install Dependencies Aplikasi

Buka PowerShell dalam folder projek ini dan run:

```powershell
# Install dependencies
npm install

# Sync projek dengan Android
npm run build
```

**Nota**: Kalau ada error, cuba:
```powershell
npm install --legacy-peer-deps
```

---

## 🔧 Langkah 3: Build Aplikasi

### Pilihan A: Build dan Test Dalam Android Studio (Disyorkan untuk pemula)

1. Buka Android Studio
2. Pilih **Open Project**
3. Navigate ke folder: `\DevTools\UPSIPortal-App\android`
4. Tunggu Gradle sync selesai (first time akan ambil masa)
5. Kalau ada error "SDK not found":
   - File → Project Structure → SDK Location
   - Set Android SDK location
6. Setup emulator atau sambung phone:
   
   **Emulator**:
   - Tools → Device Manager
   - Create Device → Pilih Pixel 5 atau lain-lain
   - System Image: Pilih API Level 33 (Android 13)
   - Finish dan start emulator

   **Physical Phone**:
   - Enable Developer Options (tap Build Number 7 kali)
   - Enable USB Debugging
   - Sambung phone dengan USB

7. Klik butang ▶️ **Run** (hijau) di toolbar
8. Pilih device (emulator atau phone)
9. Tunggu build dan app akan auto-install & run

### Pilihan B: Build APK File Untuk Install Manual

```powershell
# Navigate ke folder android
cd android

# Build debug APK
.\gradlew assembleDebug

# APK akan ada di: android\app\build\outputs\apk\debug\app-debug.apk
```

Install APK:
- Transfer file ke phone
- Buka file dan install (allow "Unknown sources" kalau perlu)

### Pilihan C: Build Release APK (Untuk production)

```powershell
cd android
.\gradlew assembleRelease
```

**Nota**: Release build perlukan signing key. Untuk testing, guna debug build sahaja.

---

## 🧪 Langkah 4: Test Aplikasi

Apabila app running:

### Features Yang Patut Test:
- ✅ Splash screen muncul 2 saat
- ✅ Website UPSI load dalam app
- ✅ Navigation dalam website berfungsi
- ✅ Butang refresh dan home berfungsi
- ✅ Status bar warna biru
- ✅ Connection status (cuba matikan WiFi)
- ✅ Back button navigate dalam website
- ✅ Notifications (perlu setup server-side)

### Common Issues:

**Website tidak load:**
- Check internet connection
- Pastikan URL betul dalam capacitor.config.json
- Check Android permissions dalam Manifest

**App crash:**
- Check Logcat dalam Android Studio (View → Tool Windows → Logcat)
- Look for error messages

**Slow loading:**
- Normal untuk first time load
- Website UPSI mungkin slow

---

## 🔄 Langkah 5: Update Aplikasi (Untuk masa hadapan)

Bila anda buat perubahan pada code:

```powershell
# 1. Sync perubahan ke Android
npm run build

# 2. Rebuild dalam Android Studio atau:
cd android
.\gradlew assembleDebug
```

---

## 🎨 Customization Guide

### Tukar Warna Theme:
Edit `\www\index.html`:
```css
/* Line ~17 - Theme color */
#1976D2  →  [warna baru]
```

### Tukar Logo/Name:
- Logo: Edit `.splash-logo` dalam index.html
- Name: Edit `capacitor.config.json` → `appName`

### Tukar Icon:
1. Buat icon PNG (512x512px)
2. Guna tool: https://icon.kitchen/
3. Download dan replace files dalam `android\app\src\main\res\mipmap-*`

---

## 📝 File Structure Penting

```
UPSIPortal-App/
├── www/
│   └── index.html          ← Website/UI utama
├── android/
│   └── app/
│       └── src/main/
│           ├── AndroidManifest.xml  ← Permissions & settings
│           └── java/.../MainActivity.java  ← App entry point
├── capacitor.config.json   ← App configuration
└── package.json            ← Dependencies
```

---

## 🆘 Troubleshooting

### Error: "SDK not found"
```powershell
# Set ANDROID_HOME
$env:ANDROID_HOME = "C:\Users\[YourName]\AppData\Local\Android\Sdk"
```

### Error: "Gradle build failed"
1. Buka Android Studio
2. File → Invalidate Caches → Invalidate and Restart
3. Build → Clean Project
4. Build → Rebuild Project

### Error: "Unable to locate adb"
- Pastikan Android SDK platform-tools installed
- Restart PowerShell after setting environment variables

### App crashes immediately:
1. Check Logcat untuk error message
2. Verify all permissions dalam AndroidManifest.xml
3. Pastikan targetSdkVersion compatible

---

## 📞 Sokongan

Kalau ada masalah:
1. Check error message dalam Logcat (Android Studio)
2. Copy full error message
3. Search error di Google atau Stack Overflow
4. Atau tanya saya untuk troubleshoot!

---

## ✅ Checklist Lengkap

Sebelum build, pastikan:
- [ ] JDK 17 installed
- [ ] Android Studio installed
- [ ] Android SDK API Level 33 downloaded
- [ ] ANDROID_HOME environment variable set
- [ ] Node.js installed
- [ ] `npm install` berjaya
- [ ] `npm run build` berjaya
- [ ] Emulator setup atau phone connected
- [ ] Website URL betul dalam config

**Selamat mencuba! 🚀**

Aplikasi ini akan berikan user experience seperti native app dengan:
- Native splash screen
- Status bar styling
- Offline detection
- Push notifications ready
- Smooth navigation
- Professional UI/UX
