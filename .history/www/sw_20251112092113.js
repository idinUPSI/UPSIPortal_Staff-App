// MyUPSI Portal Staff Service Worker
const CACHE_NAME = 'upsi-portal-v1';
const ALLOWED_ORIGINS = ['https://unistaff.upsi.edu.my'];

// Cache essential files
const ASSETS = [
  '/',
  '/index.html',
    '/manifest.json',
    "/style.css",
];


// Install event
self.addEventListener('install', event => {
  console.log('Service Worker installing');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Opened cache');
        return cache.addAll(ASSETS);
      })
      .catch(error => {
        console.error('Cache installation failed:', error);
      })
  );
});

// Fetch event with security checks
self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);
  
  // Only handle same-origin or allowed origins
  if (url.origin === self.location.origin || ALLOWED_ORIGINS.includes(url.origin)) {
    event.respondWith(
      caches.match(event.request)
        .then(response => {
          // Return cached version or fetch from network
          if (response) {
            return response;
          }
          
          // Clone the request for security
          const fetchRequest = event.request.clone();
          
          return fetch(fetchRequest)
            .then(response => {
              // Check if valid response
              if (!response || response.status !== 200 || response.type !== 'basic') {
                return response;
              }
              
              // Clone the response for caching
              const responseToCache = response.clone();
              
              caches.open(CACHE_NAME)
                .then(cache => {
                  cache.put(event.request, responseToCache);
                })
                .catch(error => {
                  console.error('Cache put failed:', error);
                });
              
              return response;
            })
            .catch(error => {
              console.error('Fetch failed:', error);
              // Return offline page if available
              return caches.match('/index.html');
            });
        })
        .catch(error => {
          console.error('Cache match failed:', error);
          return fetch(event.request);
        })
    );
  }
});

// Activate event
self.addEventListener('activate', event => {
  console.log('Service Worker activating');
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

self.addEventListener("install", event => {
    event.waitUntil(
      caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS))
    );
  });
  
  self.addEventListener("fetch", event => {
    event.respondWith(
      caches.match(event.request).then(resp => resp || fetch(event.request))
    );
  });
