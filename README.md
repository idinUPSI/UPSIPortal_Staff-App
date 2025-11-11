# MyUPSI Portal Staff

Aplikasi Android rasmi yang membungkus portal staf UPSI (https://unistaff.upsi.edu.my) dengan pengalaman seperti aplikasi native.

## 🚀 Features

- ✅ Native splash screen dengan branding UPSI
- ✅ Status bar styling
- ✅ Offline detection
- ✅ Push notifications ready
- ✅ Smooth navigation & loading states
- ✅ Responsive design untuk semua screen sizes

## 📱 Platform

- Android (Capacitor 7)
- Target SDK: API Level 34 (Android 14) atau mengikut konfigurasi projek

## 🛠️ Technology Stack

- **Framework**: Capacitor 7
- **Language**: HTML, CSS, JavaScript
- **Build**: GitHub Actions (CI/CD)
- **Plugins**: 
  - Push Notifications
  - Status Bar
  - Splash Screen
  - Network Status

## 📦 Installation

### Prasyarat

- Node.js 20 LTS
- Java 21 (Temurin/Adoptium)
- Git

### Setup

```powershell
# Clone repository
git clone https://github.com/idinUPSI/UPSIPortal_Staff-App.git
cd UPSIPortal-App

# Install dependencies
npm ci

# Sync web assets to Android
npm run build
```

## 🔨 Build APK

### Menggunakan GitHub Actions (Automatik)

1. Push code to GitHub:
  ```powershell
   git push origin main
   ```

2. Wait for build to complete (~5-10 minutes)

3. Download APK from GitHub Actions tab

### Bina Secara Lokal (Jika perlu)

Memerlukan Android Studio & SDK. Rujuk `PANDUAN_INSTALL.md` untuk butiran.

## 📖 Documentation

- [PANDUAN_INSTALL.md](PANDUAN_INSTALL.md) - Complete setup guide (Bahasa Malaysia)
- [GitHub Actions](.github/workflows/build-android.yml) - CI/CD configuration

## 🚀 Development Workflow

```bash
# 1. Edit code
code www/index.html

# 2. Test locally (preview in browser if possible)

# 3. Commit changes
git add .
git commit -m "Your message"

# 4. Push to GitHub (auto-builds APK)
git push origin main

# 5. Download APK from GitHub Actions
# 6. Install & test on device
```

## 📂 Project Structure

```
UPSIPortal-App/
├── www/                    # Web assets
│   ├── index.html         # Main app file
│   ├── manifest.json      # PWA manifest
│   └── assets/            # Images, icons, etc
├── android/               # Android native project
├── .github/
│   └── workflows/         # GitHub Actions CI/CD
├── capacitor.config.json  # Capacitor configuration
└── package.json           # Dependencies
```

## 🔧 Konfigurasi

### App Configuration
Edit `capacitor.config.json`:
- `appId`: Nama pakej Android. Terkini: `my.edu.upsi.portal.staff`
- `appName`: Nama paparan aplikasi. Terkini: `MyUPSI Portal Staff`
- `server.url`: URL portal UPSI

### Theme & Styling
Edit `www/index.html`:
- Warna tema (kini `#663399`)
- Teks splash screen
- Tajuk header

## 🔔 Notifikasi (Firebase Cloud Messaging)

Untuk mengaktifkan notifikasi push:

1. Bina projek Firebase dan tambah app Android dengan package name tepat: `my.edu.upsi.portal.staff`.
2. Muat turun `google-services.json` dan letak di `android/app/google-services.json`.
3. Build semula app. Plugin Google services akan diaktifkan secara automatik jika fail tersebut wujud.
4. Di app, token FCM akan dilog (rujuk konsol). Hantar token ke server anda untuk pengurusan pengkapsulan notifikasi.

Perubahan masa hadapan yang memerlukan anda ulang langkah Firebase:
- Menukar package name/applicationId → perlu buat Android app baharu di Firebase & guna fail `google-services.json` yang baharu.
- Menukar SHA-1/keystore untuk release signing → kemas kini di Firebase (Project settings → App integrity).
- Menukar project Firebase → semestinya perlu fail baharu.

Perubahan yang TIDAK memerlukan integrasi semula:
- Tukar warna/tema, nama paparan (appName), kandungan web, URL server, atau kemaskini kod HTML/CSS/JS biasa.

## 📝 License

Private project - UPSI Internal Use

## 👨‍💻 Author

UPSI Development Team

---

**Built with ❤️ for Universiti Pendidikan Sultan Idris**
