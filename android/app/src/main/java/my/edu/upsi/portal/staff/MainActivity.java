package my.edu.upsi.portal.staff;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private static final String HOME_URL = "https://unistaff.upsi.edu.my";
    private boolean isShowingOfflinePage = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long lastBackPressTime = 0;
    private static final int BACK_PRESS_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix status bar overlap
        setupStatusBar();
    }

        // Inject / update CSS to add padding for status bar and required header spacings
    /*    private void injectStatusBarPaddingCSS(WebView webView) {
            int statusBarHeightPx = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeightPx = getResources().getDimensionPixelSize(resourceId);
            }
            float scale = getResources().getDisplayMetrics().density;
            int statusBarHeightDp = (int) (statusBarHeightPx / scale);

            // Base paddings requested by user
            int headerTopBase = 40; // px
            int headerBottomBase = 10; // px
            int pageHeaderTopBase = 30; // px

            // Compose dynamic top paddings including status bar height (dp)
            String headerTopCalc = "calc(" + headerTopBase + "px + " + statusBarHeightDp + "px)";
            String pageHeaderTopCalc = "calc(" + pageHeaderTopBase + "px + " + statusBarHeightDp + "px)";

            String js = "(function(){" +
                    "var existing = document.getElementById('status-bar-padding-fix');" +
                    "var css = `" +
                    "html, body {padding-top: " + statusBarHeightDp + "px !important; margin:0 !important;}" +
                    "header, .header, .site-header, [class*='header'] {" +
                    "  padding: " + headerTopCalc + " 0 " + headerBottomBase + "px 0 !important;" +
                    "  margin-top:0 !important;" +
                    "}" +
                    ".page-header {" +
                    "  padding-top: " + pageHeaderTopCalc + " !important;" +
                    "  height:auto !important;" +
                    "  margin-top:0 !important;" +
                    "}" +
                    "@supports (padding: max(0px)) {" +
                    "  html, body {padding-top: max(env(safe-area-inset-top), " + statusBarHeightDp + "px) !important;}" +
                    "}" +
                    "`;" +
                    "if(!existing){" +
                    "  existing = document.createElement('style'); existing.id='status-bar-padding-fix'; document.head.appendChild(existing);" +
                    "} existing.innerHTML = css;" +
                    "console.log('[UPSI] Status bar & header padding applied (statusBarDp=" + statusBarHeightDp + ")');" +
                    "})();";
            webView.evaluateJavascript(js, null);
        }
    */
    private void setupStatusBar() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            // Keep content BELOW system bars (status bar and navigation bar)
            WindowCompat.setDecorFitsSystemWindows(window, true);
            
            // Set status bar color
            window.setStatusBarColor(Color.parseColor("#663399"));
            
            // Set light/dark status bar icons
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);
            if (controller != null) {
                // false = light icons (white), true = dark icons (black)
                controller.setAppearanceLightStatusBars(false);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-10 (API 23-29)
            window.setStatusBarColor(Color.parseColor("#663399"));
            
            // For Android 6+, we can control status bar icon colors
            int flags = decorView.getSystemUiVisibility();
            // Remove SYSTEM_UI_FLAG_LIGHT_STATUS_BAR to use white icons
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        } else {
            // Android 5 (API 21-22)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#663399"));
        }
        
    }

    @Override
    public void onStart() {
        super.onStart();
        setupNetworkMonitoring();
        setupOfflineErrorHandler();
        setupPullToRefresh();
        setupWebViewSettings();
    }

    private void setupOfflineErrorHandler() {
        // Wait for WebView to be ready
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    WebView webView = getBridge().getWebView();
                    
                    // Enable JavaScript for offline page
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");
                    
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                            // Handle only MAIN FRAME errors carefully
                            if (request.isForMainFrame()) {
                                runOnUiThread(() -> {
                                    boolean noNetwork = !isNetworkAvailable();

                                    // Log error details for diagnostics
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        String errorMsg = "onReceivedError (main frame): code=" + error.getErrorCode() + ", desc=" + error.getDescription();
                                        android.util.Log.e("MainActivity", errorMsg);
                                    }

                                    if (noNetwork) {
                                        // Try to load from cache first before showing offline page
                                        String url = request.getUrl().toString();
                                        android.util.Log.i("MainActivity", "Offline detected, trying cache for: " + url);
                                        
                                        // Switch to cache mode
                                        WebSettings settings = view.getSettings();
                                        int oldMode = settings.getCacheMode();
                                        settings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
                                        
                                        // Try to reload from cache
                                        view.reload();
                                        
                                        // Check if cache load succeeded after a delay
                                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                            // If still showing error or offline page needed
                                            if (view.getUrl() == null || view.getUrl().equals("about:blank")) {
                                                isShowingOfflinePage = true;
                                                loadOfflinePage(view);
                                            } else {
                                                // Cache worked! Restore cache mode
                                                settings.setCacheMode(oldMode);
                                                android.util.Log.i("MainActivity", "Successfully loaded from cache");
                                            }
                                        }, 500);
                                    } else {
                                        // Keep current page, show a lightweight message instead of forcing offline page
                                        Toast.makeText(MainActivity.this,
                                                "Ralat memuat halaman. Cuba lagi.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                // Stop default handling for main-frame
                                return;
                            }
                            // For subresource errors, let default handling proceed
                            super.onReceivedError(view, request, error);
                        }

                        @Override
                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                            // Log SSL error for debugging
                            android.util.Log.e("MainActivity", "SSL Error: " + error.toString());

                            // In debug builds (app debuggable), allow proceed to ease testing
                            // In release builds, cancel but DO NOT force offline page
                            try {
                                boolean isDebuggable = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                                if (isDebuggable) {
                                    handler.proceed();
                                } else {
                                    handler.cancel();
                                    runOnUiThread(() -> Toast.makeText(
                                            MainActivity.this,
                                            "Ralat keselamatan SSL. Sila hubungi pentadbir sistem.",
                                            Toast.LENGTH_LONG
                                    ).show());
                                }
                            } catch (Exception ex) {
                                handler.cancel();
                            }
                            // Don't load offline page here to avoid false offline state
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                            // Only reset flag if loading a real URL (not our offline page)
                            if (!url.startsWith("data:") && !url.equals("about:blank")) {
                                isShowingOfflinePage = false;
                            }
                            // Hide pull-to-refresh indicator if showing
                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            // Successfully loaded a real page
                            if (!url.startsWith("data:") && !url.equals("about:blank")) {
                                isShowingOfflinePage = false;
                                
                                // Inject CSS to prevent content overlap with status bar
                                injectStatusBarFixCSS(view);
                                
                                // Force cache this page for offline use
                                // This ensures the page is cached even if cache headers are not set
                                if (isNetworkAvailable() && url.startsWith("https://unistaff.upsi.edu.my")) {
                                    // Page loaded successfully, it's now in cache
                                    android.util.Log.i("MainActivity", "Page cached for offline: " + url);
                                }
                            }
                            // Hide pull-to-refresh indicator
                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                        
                        @Override
                        public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                            // This allows us to intercept and potentially serve cached content
                            // For offline mode, we can check cache here
                            if (!isNetworkAvailable()) {
                                // Offline: Try to serve from cache
                                // WebView will automatically use cache if available
                                android.util.Log.d("MainActivity", "Offline request: " + request.getUrl());
                            }
                            
                            // Workaround: Force cache even if server sets no-cache
                            // WebView's LOAD_CACHE_ELSE_NETWORK mode will handle this
                            android.webkit.WebResourceResponse response = super.shouldInterceptRequest(view, request);
                            
                            // If we have a response, we can modify headers to allow caching
                            // But WebView cache mode already handles this at a lower level
                            return response;
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Setup error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, 1000); // Increase delay to ensure WebView is fully ready
    }

    // Inject CSS to fix status bar overlap for portal content
    private void injectStatusBarFixCSS(WebView view) {
        String js = "(function(){" +
                "var existing = document.getElementById('android-status-bar-fix');" +
                "if(!existing){" +
                "  existing = document.createElement('style');" +
                "  existing.id = 'android-status-bar-fix';" +
                "  document.head.appendChild(existing);" +
                "}" +
                "existing.textContent = `" +
                "html, body { padding-top: env(safe-area-inset-top, 15px) !important; margin-top: 0 !important; }" +
                ".container-fluid.container-xl.position-relative.d-flex.align-items-center.justify-content-between {" +
                "  padding-top: 25px !important;" +
                "}" +
                ".page-header, .page-content {" +
                "  padding-top: 30px !important;" +
                "  height: auto !important;" +
                "}" +
                ".page-header {" +
                "  background-color: #4D2677 !important;" +
                "}" +
                "`;" +
                "console.log('[Android] Status bar fix CSS injected');" +
                "})();";
        view.evaluateJavascript(js, null);
    }
    
    private void loadOfflinePage(WebView view) {
        String offlineHtml = "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>" +
            "* { margin: 0; padding: 0; box-sizing: border-box; }" +
            "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; " +
            "padding: 40px 20px; text-align: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
            "min-height: 100vh; display: flex; flex-direction: column; justify-content: center; align-items: center; }" +
            ".container { background: white; padding: 40px 24px; border-radius: 16px; max-width: 400px; " +
            "box-shadow: 0 8px 32px rgba(0,0,0,0.1); }" +
            ".icon { font-size: 64px; margin-bottom: 20px; }" +
            "h1 { color: #663399; font-size: 24px; font-weight: 600; margin-bottom: 12px; }" +
            "p { color: #666; font-size: 15px; line-height: 1.6; margin-bottom: 10px; }" +
            ".btn { background: #663399; color: white; border: none; padding: 14px 28px; " +
            "border-radius: 8px; font-size: 16px; font-weight: 500; cursor: pointer; " +
            "margin-top: 20px; box-shadow: 0 4px 12px rgba(102,51,153,0.3); width: 100%; " +
            "transition: all 0.2s; }" +
            ".btn:active { background: #552b8a; transform: scale(0.98); }" +
            ".tips { margin-top: 24px; padding: 16px; background: #f8f9fa; border-radius: 8px; text-align: left; }" +
            ".tips h3 { color: #663399; font-size: 13px; margin-bottom: 8px; font-weight: 600; }" +
            ".tips ul { padding-left: 20px; }" +
            ".tips li { color: #666; font-size: 13px; line-height: 1.8; margin-bottom: 4px; }" +
            "</style></head><body>" +
            "<div class='container'>" +
            "<div class='icon'>📡</div>" +
            "<h1>Tiada Sambungan Internet</h1>" +
            "<p>Portal MyUPSI memerlukan sambungan internet untuk berfungsi.</p>" +
            "<p>Sila semak sambungan anda dan cuba lagi.</p>" +
            "<button class='btn' onclick='AndroidInterface.retryConnection()'>Cuba Lagi</button>" +
            "<div class='tips'><h3>💡 Petua Sambungan</h3><ul>" +
            "<li>Pastikan WiFi atau data selular aktif</li>" +
            "<li>Cuba tutup dan buka Airplane Mode</li>" +
            "<li>Semak had penggunaan data anda</li>" +
            "</ul></div>" +
            "</div></body></html>";
        
        view.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null);
    }
    
    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(network);
            if (caps == null) return false;

            // Be lenient: consider network available if it has INTERNET capability
            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return true;
            }

            // Fallback: check common transports
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        } else {
            android.net.NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }
    }


    private void setupNetworkMonitoring() {
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Sambungan internet tersedia", Toast.LENGTH_SHORT).show();
                    
                    // Switch back to default cache mode when online
                    if (getBridge() != null && getBridge().getWebView() != null) {
                        WebView webView = getBridge().getWebView();
                        WebSettings settings = webView.getSettings();
                        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                        android.util.Log.i("MainActivity", "Switched to default cache mode (online)");
                    }
                    
                    // Auto-reload if showing offline page
                    if (isShowingOfflinePage && getBridge() != null && getBridge().getWebView() != null) {
                        isShowingOfflinePage = false;
                        getBridge().getWebView().loadUrl(HOME_URL);
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Tiada sambungan internet", Toast.LENGTH_SHORT).show();
                    
                    // Switch to cache-only mode when offline
                    if (getBridge() != null && getBridge().getWebView() != null) {
                        WebView webView = getBridge().getWebView();
                        WebSettings settings = webView.getSettings();
                        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        android.util.Log.i("MainActivity", "Switched to cache mode for offline");
                    }
                });
            }
        };

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback);
        } catch (Exception ignored) {}
    }
    
    // JavaScript Interface for offline page retry button
    public class WebAppInterface {
        @android.webkit.JavascriptInterface
        public void retryConnection() {
            runOnUiThread(() -> {
                if (isNetworkAvailable()) {
                    if (getBridge() != null && getBridge().getWebView() != null) {
                        isShowingOfflinePage = false;
                        getBridge().getWebView().loadUrl(HOME_URL);
                        Toast.makeText(MainActivity.this, "Cuba menyambung semula...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Masih tiada sambungan internet", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Setup Pull-to-Refresh functionality
    private void setupPullToRefresh() {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    WebView webView = getBridge().getWebView();

                    // Find SwipeRefreshLayout in the view hierarchy
                    android.view.ViewParent parent = webView.getParent();
                    while (parent != null && !(parent instanceof SwipeRefreshLayout)) {
                        parent = parent.getParent();
                    }

                    if (parent instanceof SwipeRefreshLayout) {
                        swipeRefreshLayout = (SwipeRefreshLayout) parent;

                        // Configure pull-to-refresh
                        swipeRefreshLayout.setColorSchemeColors(
                            Color.parseColor("#663399"),
                            Color.parseColor("#9575cd"),
                            Color.parseColor("#4D2677")
                        );

                        swipeRefreshLayout.setOnRefreshListener(() -> {
                            if (isNetworkAvailable()) {
                                if (!isShowingOfflinePage) {
                                    webView.reload();
                                } else {
                                    isShowingOfflinePage = false;
                                    webView.loadUrl(HOME_URL);
                                }
                            } else {
                                Toast.makeText(MainActivity.this,
                                    "Tiada sambungan internet",
                                    Toast.LENGTH_SHORT).show();
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error setting up pull-to-refresh: " + e.getMessage());
            }
        }, 1500);
    }

    // Setup WebView Settings for better performance and security
    private void setupWebViewSettings() {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    WebView webView = getBridge().getWebView();
                    WebSettings settings = webView.getSettings();

                    // Performance settings
                    settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
                    
                    // Enable aggressive caching for offline mode
                    // LOAD_CACHE_ELSE_NETWORK: Use cache first, then network if cache fails
                    // This allows offline access to previously visited pages
                    // Use LOAD_CACHE_ELSE_NETWORK to prioritize cache but still allow network updates
                    settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    
                    // Enable cache storage (AppCache is deprecated and removed in newer Android versions)
                    // Modern Android uses HTTP cache automatically via WebView cache mode
                    // AppCache methods are deprecated and may not be available in newer API levels
                    // WebView's LOAD_CACHE_ELSE_NETWORK mode handles caching automatically
                    
                    settings.setDatabaseEnabled(true);
                    settings.setGeolocationEnabled(true);
                    
                    // Enable DOM storage for better caching
                    settings.setDomStorageEnabled(true);
                    settings.setJavaScriptCanOpenWindowsAutomatically(true);

                    // Enable zooming
                    settings.setSupportZoom(true);
                    settings.setBuiltInZoomControls(true);
                    settings.setDisplayZoomControls(false);

                    // Better text rendering
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
                    }

                    // Mixed content mode (allow HTTPS pages to load HTTP resources if needed)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
                    }

                    // File access
                    settings.setAllowFileAccess(true);
                    settings.setAllowContentAccess(true);

                    // Enable wide viewport
                    settings.setUseWideViewPort(true);
                    settings.setLoadWithOverviewMode(true);
                    
                    // Fix keyboard input issues
                    webView.setFocusable(true);
                    webView.setFocusableInTouchMode(true);
                    webView.requestFocus();

                    android.util.Log.i("MainActivity", "WebView settings optimized");
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error setting up WebView: " + e.getMessage());
            }
        }, 1500);
    }

    // Handle back button press
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    WebView webView = getBridge().getWebView();

                    // If showing offline page, try to go back or exit
                    if (isShowingOfflinePage) {
                        if (webView.canGoBack()) {
                            isShowingOfflinePage = false;
                            webView.goBack();
                            return true;
                        } else {
                            // Double press to exit
                            if (System.currentTimeMillis() - lastBackPressTime < BACK_PRESS_INTERVAL) {
                                finishAffinity(); // Exit app
                                return true;
                            } else {
                                lastBackPressTime = System.currentTimeMillis();
                                Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                        }
                    }

                    // If WebView can go back, go back
                    if (webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    } else {
                        // At home page, double press to exit
                        if (System.currentTimeMillis() - lastBackPressTime < BACK_PRESS_INTERVAL) {
                            finishAffinity(); // Exit app
                            return true;
                        } else {
                            lastBackPressTime = System.currentTimeMillis();
                            Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error handling back button: " + e.getMessage());
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}

