# Cara Test dan Verify Perubahan Design

## ⚠️ Nota Penting

Saya (AI) **TIDAK DAPAT** melihat paparan sebenar app atau portal UPSI. Semua perubahan dibuat berdasarkan:
- Masalah yang anda laporkan
- Best practices untuk mobile development
- Code yang ada dalam codebase

**Oleh itu, anda perlu test pada device sebenar untuk verify perubahan betul atau tidak.**

## 📱 Cara Test Perubahan

### 1. **Test Splash Screen Logo**
Selepas install APK baru:

**Check:**
- [ ] Logo berada di tengah-tengah screen (vertically centered)
- [ ] Logo tidak terke bawah
- [ ] Logo tidak terke atas
- [ ] Logo tidak terpotong oleh status bar

**Jika masih tidak betul:**
- Ambil screenshot dan hantar kepada saya
- Beritahu kedudukan sebenar (terke bawah berapa px, atau terke atas)
- Saya akan adjust CSS dengan nilai yang tepat

### 2. **Test Header Bar Portal**
Selepas portal dimuat:

**Check:**
- [ ] Header bar tidak bertindih dengan status bar
- [ ] Header bar tidak terlalu tinggi (ada ruang kosong yang banyak)
- [ ] Header bar tidak terlalu rendah (content terpotong)
- [ ] Content dalam portal tidak terpotong

**Jika masih tidak betul:**
- Ambil screenshot dan hantar kepada saya
- Beritahu masalah spesifik:
  - Header masih bertindih? Berapa px?
  - Header terlalu tinggi? Berapa px?
  - Header terlalu rendah? Berapa px?

### 3. **Test pada Device Berbeza**
Test pada:
- [ ] Phone dengan notch (iPhone X style)
- [ ] Phone tanpa notch (standard Android)
- [ ] Phone dengan different screen sizes
- [ ] Different Android versions

## 🔧 Cara Adjust Berdasarkan Feedback

### Jika Logo Masih Terke Bawah:
```css
/* Dalam index.html, adjust .logo */
.logo {
  margin-top: -20px; /* atau nilai lain */
}
```

### Jika Header Bar Masih Bertindih:
```css
/* Dalam inject.js atau MainActivity, increase padding */
header {
  padding-top: calc(env(safe-area-inset-top, 24px) + 10px) !important;
}
```

### Jika Header Bar Terlalu Tinggi:
```css
/* Kurangkan padding */
header {
  padding-top: calc(env(safe-area-inset-top, 24px) - 5px) !important;
}
```

## 📸 Screenshot untuk Debug

Ambil screenshot:
1. Splash screen (logo position)
2. Portal header bar (status bar overlap)
3. Portal content (terpotong atau tidak)

Hantar screenshot kepada saya dengan:
- Device model
- Android version
- Masalah spesifik (berapa px terke bawah/atas, dll)

## 🎯 Workflow yang Disyorkan

1. **Install APK baru** dari GitHub Actions
2. **Test pada device sebenar**
3. **Ambil screenshot** jika ada masalah
4. **Beritahu saya masalah spesifik** dengan nilai yang tepat (berapa px)
5. **Saya akan adjust** dengan nilai yang tepat
6. **Repeat** sehingga perfect

## 💡 Tips

- Gunakan **Android Studio Layout Inspector** untuk check exact pixel values
- Gunakan **Chrome DevTools** (jika test dalam browser) untuk inspect elements
- Test pada **multiple devices** untuk ensure compatibility

## 📞 Feedback Format

Apabila beritahu masalah, gunakan format ini:

```
Masalah: Logo terke bawah
Device: Samsung Galaxy S21
Android: 13
Nilai: Kira-kira 30px terke bawah dari tengah
Screenshot: [attach]
```

Atau:

```
Masalah: Header bar masih bertindih
Device: Pixel 6
Android: 14
Nilai: Header bertindih dengan status bar kira-kira 10px
Screenshot: [attach]
```

Dengan maklumat ini, saya boleh adjust dengan nilai yang tepat!

