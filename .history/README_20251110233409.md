# Sistem Idin - UPSI Portal App

Aplikasi mobile Android untuk mengakses portal UPSI Staff (https://unistaff.upsi.edu.my) dengan pengalaman native app.

## 🚀 Features

- ✅ Native splash screen dengan branding UPSI
- ✅ Status bar styling
- ✅ Offline detection
- ✅ Push notifications ready
- ✅ Smooth navigation & loading states
- ✅ Responsive design untuk semua screen sizes

## 📱 Platform

- Android (Capacitor 7)
- Target SDK: API Level 33 (Android 13)

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

### Prerequisites

- Node.js 18+ 
- Git

### Setup

```bash
# Clone repository
git clone <your-repo-url>
cd UPSIPortal-App

# Install dependencies
npm install

# Sync with Android
npm run build
```

## 🔨 Build APK

### Using GitHub Actions (Automatic)

1. Push code to GitHub:
   ```bash
   git push origin main
   ```

2. Wait for build to complete (~5-10 minutes)

3. Download APK from GitHub Actions tab

### Local Build (If needed)

Requires Android Studio & SDK. See `PANDUAN_INSTALL.md` for details.

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

## 🔧 Configuration

### App Configuration
Edit `capacitor.config.json`:
- `appId`: Android package name
- `appName`: Display name
- `server.url`: Backend URL

### Theme & Styling
Edit `www/index.html`:
- Colors (search for `#1976D2`)
- Splash screen text
- Header title

## 📝 License

Private project - UPSI Internal Use

## 👨‍💻 Author

UPSI Development Team

---

**Built with ❤️ for Universiti Pendidikan Sultan Idris**
