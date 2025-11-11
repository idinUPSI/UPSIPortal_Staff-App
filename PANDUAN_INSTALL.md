# 📱 Panduan Lengkap: Build Aplikasi MyUPSI Portal Staff

Panduan langkah demi langkah untuk membina aplikasi Android yang membungkus portal staf UPSI menggunakan Capacitor.

---

## 📋 Langkah 1: Install Software Yang Diperlukan

### A. Install Java Development Kit (JDK)
1. Download **JDK 21** (Temurin) dari: https://adoptium.net/ (versi LTS baharu yang disokong Gradle & Capacitor 7)
2. Pilih "Latest LTS Release" → Windows → x64 → .msi installer
3. Install dengan setting default (klik Next sahaja)
4. Verify installation:
   ```powershell
   java -version
   ```
   Sepatutnya keluar: `openjdk version "21.x.x"`

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
2. Pilih versi LTS (20.x.x)
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

## 📦 Langkah 2: Pasang Dependencies Aplikasi

Buka PowerShell dalam folder projek ini dan run:

```powershell
# Install dependencies
npm ci

# Sync projek dengan Android
npm run build
```

**Nota**: `npm ci` memastikan pemasangan konsisten berdasarkan `package-lock.json`.

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

### Pilihan B: Build APK Debug Untuk Pasang Manual

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

### Pilihan C: Build Release APK (Untuk produksi)

```powershell
cd android
.\gradlew assembleRelease
```

**Nota**: Release build perlukan signing key. Untuk testing, guna debug build sahaja.

---

## 🧪 Langkah 4: Uji Aplikasi

Apabila app running:

### Features Yang Patut Test:
Senarai semak:
- ✅ Splash screen muncul
- ✅ Portal UPSI dimuat
- ✅ Navigasi dalam portal berfungsi
- ✅ Butang refresh & home berfungsi
- ✅ Status bar warna ungu (#663399)
- ✅ Offline banner muncul bila tiada internet
- ✅ Back button kembali dalam sejarah laman
- ✅ Persediaan notifikasi (belum Firebase, akan diaktifkan kemudian)

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

## 🔄 Langkah 5: Kemas Kini Aplikasi (Masa Hadapan)

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

### Tukar Warna Tema:
Edit `\www\index.html`:
```css
/* Meta theme-color & StatusBar */
#663399  →  [warna baru]
```

### Tukar Logo/Nama:
- Logo: Edit `.splash-logo` dalam `www/index.html`
- Nama paparan: Edit `capacitor.config.json` → `appName`
- appId (JANGAN ubah selepas produksi kecuali perlu – perubahan memerlukan Firebase app baharu)

### Tukar Ikon:
1. Sediakan icon PNG (1024x1024 asas / 512x512 minimum)
2. Guna tool seperti https://icon.kitchen/ atau `npx @capacitor/assets generate`
3. Ganti fail dalam `android\app\src\main\res\mipmap-*` & semak adaptive icon jika perlu

---

## 📝 Struktur Fail Penting

```
UPSIPortal-App/
├── www/
│   └── index.html          ← UI shell (WebView + splash + header)
├── android/
│   └── app/
│       └── src/main/
│           ├── AndroidManifest.xml  ← Permissions & settings
│           └── java/.../MainActivity.java  ← App entry point
├── capacitor.config.json   ← App configuration
└── package.json            ← Dependencies
```

---

## 🆘 Penyelesaian Masalah

### Ralat: "SDK not found"
```powershell
# Set ANDROID_HOME
$env:ANDROID_HOME = "C:\Users\[YourName]\AppData\Local\Android\Sdk"
```

### Ralat: "Gradle build failed"
1. Buka Android Studio
2. File → Invalidate Caches → Invalidate and Restart
3. Build → Clean Project
4. Build → Rebuild Project

### Ralat: "Unable to locate adb"
- Pastikan Android SDK platform-tools installed
- Restart PowerShell after setting environment variables

### App crash sebaik dibuka:
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

## ✅ Senarai Semak Lengkap

Sebelum build, pastikan:
- [ ] JDK 21 installed
- [ ] Android Studio installed
- [ ] Android SDK API Level 33 downloaded
- [ ] ANDROID_HOME environment variable set
- [ ] Node.js installed
- [ ] `npm install` berjaya
- [ ] `npm run build` berjaya
- [ ] Emulator setup atau phone connected
- [ ] Website URL betul dalam config

## 🔔 Integrasi Firebase (Notifikasi Push)

Langkah ringkas aktivasi FCM:
1. Buka Firebase Console → Create Project (atau gunakan sedia ada).
2. Tambah Android app dengan package name: `my.edu.upsi.portal.staff`.
3. Muat turun `google-services.json` dan letak di `android/app/google-services.json`.
4. Jalankan `npm run build` dan bina APK. Token FCM akan dipaparkan di log (boleh hantar ke server).

Perubahan yang memerlukan anda ulang integrasi Firebase:
- Tukar package name (applicationId).
- Tukar keystore / SHA-1 untuk ciri yang perlukan verifikasi (contoh: Sign-In). 
- Tukar projek Firebase baharu.

Perubahan yang TIDAK memerlukan integrasi semula:
- Ubah warna tema, nama paparan (appName), kandungan UI HTML/CSS/JS,
- Kemaskini versi plugin tanpa menukar applicationId.

**Nota**: Pastikan hanya satu appId final digunakan sebelum produksi untuk elak perlu buat semula app di Firebase.

**Selamat mencuba! 🚀**

Aplikasi ini menyediakan pengalaman hampir native:
- Splash screen
- Status bar berwarna tema
- Offline detection
- Struktur siap untuk push notifications
- Navigasi lancar
- UI/UX ringkas dan jelas
