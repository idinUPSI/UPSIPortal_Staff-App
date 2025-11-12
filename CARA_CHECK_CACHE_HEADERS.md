# Cara Check Cache Headers dari Server UPSI

## 📍 Di Mana Setting `no-cache`?

Setting `Cache-Control: no-cache` **TIDAK** dalam codebase app ini. Ia di-set oleh **server UPSI** di:

### 1. **Server Configuration** (Web Server)
- **Apache**: `.htaccess` atau `httpd.conf`
- **Nginx**: `nginx.conf`
- **IIS**: `web.config`

### 2. **Application Code** (Backend)
- **PHP**: `header('Cache-Control: no-cache');`
- **ASP.NET**: `Response.Cache.SetCacheability(HttpCacheability.NoCache);`
- **Node.js**: `res.setHeader('Cache-Control', 'no-cache');`

### 3. **CDN/Proxy** (Jika ada)
- CloudFlare, AWS CloudFront, dll

## 🔍 Cara Check Cache Headers

### Method 1: Browser DevTools (Paling Mudah)
1. Buka `https://unistaff.upsi.edu.my` dalam browser
2. Tekan `F12` untuk buka DevTools
3. Pergi ke tab **Network**
4. Reload page (F5 atau Ctrl+R)
5. Klik pada request pertama (document - biasanya `unistaff.upsi.edu.my`)
6. Lihat tab **Headers** → scroll ke **Response Headers**
7. Cari `Cache-Control` atau `Expires` header

**Contoh hasil yang akan anda nampak:**
```
Cache-Control: no-store, no-cache, must-revalidate
Pragma: no-cache
Expires: Thu, 19 Nov 1981 08:52:00 GMT
```

### Method 2: Command Line

#### Windows PowerShell:
```powershell
# Method 1: Dapatkan semua headers
$response = Invoke-WebRequest -Uri "https://unistaff.upsi.edu.my" -Method Head
$response.Headers

# Method 2: Dapatkan specific header sahaja
$response = Invoke-WebRequest -Uri "https://unistaff.upsi.edu.my" -Method Head
Write-Host "Cache-Control:" $response.Headers['Cache-Control']
Write-Host "Expires:" $response.Headers['Expires']
Write-Host "Pragma:" $response.Headers['Pragma']
```

**Contoh output:**
```
Cache-Control: no-store, no-cache, must-revalidate
Expires: Thu, 19 Nov 1981 08:52:00 GMT
Pragma: no-cache
```

#### Windows CMD (jika ada curl):
```cmd
curl -I https://unistaff.upsi.edu.my
```

#### Linux/Mac:
```bash
curl -I https://unistaff.upsi.edu.my
```

**Hasil sebenar dari server UPSI (disemak pada 12 Nov 2025):**
```
HTTP/1.1 303 See Other
Cache-Control: no-store, no-cache, must-revalidate
Pragma: no-cache
Expires: Thu, 19 Nov 1981 08:52:00 GMT
```

### Method 3: Online Tools (Tanpa Install Software)
- **httpstatus.io**: https://httpstatus.io/ - Masukkan URL, dapatkan semua headers
- **webpagetest.org**: https://www.webpagetest.org/ - Test performance + headers
- **tools.keycdn.com/curl**: https://tools.keycdn.com/curl - Simulate curl command
- **cachecheck.net**: https://cachecheck.net/ - Khusus untuk check cache headers

**Cara guna:**
1. Buka salah satu website di atas
2. Masukkan URL: `https://unistaff.upsi.edu.my`
3. Klik "Check" atau "Test"
4. Lihat hasil - cari `Cache-Control` header

## 📋 Cache Headers Yang Biasa Ditemui

### Baik untuk Offline Mode:
```
Cache-Control: public, max-age=3600
Cache-Control: public, max-age=86400
Expires: Thu, 31 Dec 2025 23:59:59 GMT
```

### Tidak Baik untuk Offline Mode:
```
Cache-Control: no-cache
Cache-Control: no-store
Cache-Control: no-store, no-cache, must-revalidate  ← Server UPSI guna ini
Pragma: no-cache
```

**⚠️ HASIL SEMAKAN SERVER UPSI:**
Server UPSI (`unistaff.upsi.edu.my`) **memang set**:
- `Cache-Control: no-store, no-cache, must-revalidate`
- `Pragma: no-cache`
- `Expires: Thu, 19 Nov 1981 08:52:00 GMT` (expired date lama)

Ini bermakna server UPSI **tidak mahu** browser cache content mereka. Tapi app kita ada workaround dengan WebView cache mode.

## 🛠️ Workaround dalam App

App ini sudah ada workaround:

1. **WebView Cache Mode**: `LOAD_CACHE_ELSE_NETWORK`
   - Prioritize cache walaupun server set no-cache
   - WebView akan cache content secara automatik

2. **Force Cache**: App akan cache semua pages yang dilawati
   - Walaupun server set no-cache, WebView tetap cache untuk offline use

3. **Cache Size**: 50MB
   - Cukup untuk cache banyak pages

## ⚠️ Nota Penting

- **Server UPSI perlu set cache headers yang betul** untuk offline mode berfungsi dengan baik
- Jika server set `no-cache`, WebView masih boleh cache, tapi mungkin tidak sebaik jika server set cache headers yang betul
- Untuk best results, minta server UPSI set:
  ```
  Cache-Control: public, max-age=3600
  ```

## 📞 Siapa Perlu Contact?

Jika anda perlu ubah cache headers, contact:
- **Server Administrator UPSI**
- **IT Department UPSI**
- **Web Developer yang maintain portal UPSI**

Mereka perlu ubah server configuration atau application code untuk set cache headers yang betul.

