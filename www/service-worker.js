// Service Worker for MyUPSI Portal Staff
// Version-based caching with network-first strategy

// Debug mode - set to false in production to disable console logs
const DEBUG_MODE = false;

function debugLog(...args) {
    if (DEBUG_MODE) {
        console.log(...args);
    }
}

function debugError(...args) {
    if (DEBUG_MODE) {
        console.error(...args);
    }
}

const CACHE_VERSION = 'v1.0.0';
const CACHE_NAME = `myupsi-portal-${CACHE_VERSION}`;
const OFFLINE_CACHE = `myupsi-offline-${CACHE_VERSION}`;

// Assets to cache for offline use
const OFFLINE_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/assets/icons/icon-192x192.png',
  '/assets/icons/icon-512x512.png'
];

// Install event - cache offline assets
self.addEventListener('install', (event) => {
  debugLog('[ServiceWorker] Installing...');
  event.waitUntil(
    caches.open(OFFLINE_CACHE)
      .then((cache) => {
        debugLog('[ServiceWorker] Caching offline assets');
        return cache.addAll(OFFLINE_ASSETS);
      })
      .then(() => {
        debugLog('[ServiceWorker] Skip waiting');
        return self.skipWaiting();
      })
      .catch((error) => {
        debugError('[ServiceWorker] Install failed:', error);
      })
  );
});

// Activate event - clean up old caches
self.addEventListener('activate', (event) => {
  debugLog('[ServiceWorker] Activating...');
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((cacheName) => {
            // Delete old caches that don't match current version
            return cacheName.startsWith('myupsi-') &&
                   cacheName !== CACHE_NAME &&
                   cacheName !== OFFLINE_CACHE;
          })
          .map((cacheName) => {
            debugLog('[ServiceWorker] Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          })
      );
    })
    .then(() => {
      debugLog('[ServiceWorker] Claiming clients');
      return self.clients.claim();
    })
  );
});

// Fetch event - network-first strategy with cache fallback
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Skip non-GET requests
  if (request.method !== 'GET') {
    return;
  }

  // Skip chrome-extension and other schemes
  if (!url.protocol.startsWith('http')) {
    return;
  }

  // Note: Service Worker can only cache same-origin requests
  // For cross-origin requests (like unistaff.upsi.edu.my), WebView cache is used instead
  
  // Network-first strategy for same-origin requests
  event.respondWith(
    fetch(request)
      .then((response) => {
        // Clone the response before caching
        const responseToCache = response.clone();

        // Cache successful responses (only for same-origin)
        if (response.status === 200 && response.type === 'basic') {
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(request, responseToCache).catch(err => {
              debugLog('[ServiceWorker] Cache put failed (may be cross-origin):', request.url);
            });
          });
        }

        return response;
      })
      .catch((error) => {
        debugLog('[ServiceWorker] Fetch failed, trying cache:', request.url);

        // Try to get from cache
        return caches.match(request)
          .then((cachedResponse) => {
            if (cachedResponse) {
              debugLog('[ServiceWorker] Serving from cache:', request.url);
              return cachedResponse;
            }

            // If no cached response and it's a navigation request, return offline page
            if (request.mode === 'navigate') {
              return caches.match('/index.html');
            }

            // For other requests, return error
            // Note: For cross-origin requests, WebView will handle cache automatically
            throw error;
          });
      })
  );
});

// Message event - handle messages from clients
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }

  if (event.data && event.data.type === 'CLEAR_CACHE') {
    event.waitUntil(
      caches.keys().then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            if (cacheName.startsWith('myupsi-')) {
              return caches.delete(cacheName);
            }
          })
        );
      })
    );
  }
});

// Handle push notifications
self.addEventListener('push', (event) => {
  debugLog('[ServiceWorker] Push received:', event);

  const options = {
    body: event.data ? event.data.text() : 'Anda mempunyai notifikasi baru',
    icon: '/assets/icons/icon-192x192.png',
    badge: '/assets/icons/icon-72x72.png',
    vibrate: [200, 100, 200],
    tag: 'myupsi-notification',
    requireInteraction: false
  };

  event.waitUntil(
    self.registration.showNotification('MyUPSI Portal', options)
  );
});

// Handle notification click
self.addEventListener('notificationclick', (event) => {
  debugLog('[ServiceWorker] Notification clicked:', event);
  event.notification.close();

  event.waitUntil(
    clients.openWindow('/')
  );
});

debugLog('[ServiceWorker] Loaded');
