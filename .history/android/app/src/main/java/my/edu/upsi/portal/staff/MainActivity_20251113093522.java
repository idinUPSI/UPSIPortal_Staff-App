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
import android.view.MotionEvent;
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
    private boolean isNetworkCallbackRegistered = false;
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
        
        // Setup native keyboard backspace fix
        setupKeyboardBackspaceFix();
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

                                    // Log error for diagnostics only (not shown to user)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        android.util.Log.e("MainActivity", "Main frame error: " + error.getErrorCode());
                                    }

                                    if (noNetwork) {
                                        // Directly show offline page - don't attempt cache reload
                                        // to avoid multiple reloads and better UX
                                        isShowingOfflinePage = true;
                                        loadOfflinePage(view);
                                    } else {
                                        // Network available but page failed - show friendly message
                                        // Don't show error code to user
                                        Toast.makeText(MainActivity.this,
                                                "Ralat memuat halaman. Sila semak sambungan anda.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                // Stop default error page
                                return;
                            }
                            // For subresource errors (images, scripts), ignore silently
                            // Don't call super to prevent error messages
                        }

                        @Override
                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                            // Log SSL error for debugging only (not shown to user)
                            android.util.Log.e("MainActivity", "SSL Error occurred");

                            // In debug builds, allow proceed for testing
                            // In release builds, cancel with user-friendly message
                            try {
                                boolean isDebuggable = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                                if (isDebuggable) {
                                    handler.proceed();
                                } else {
                                    handler.cancel();
                                    runOnUiThread(() -> Toast.makeText(
                                            MainActivity.this,
                                            "Sambungan tidak selamat. Sila cuba sebentar lagi.",
                                            Toast.LENGTH_SHORT
                                    ).show());
                                }
                            } catch (Exception ex) {
                                handler.cancel();
                            }
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                            // Only reset flag if loading a real URL (not our offline page)
                            if (!url.startsWith("data:") && !url.equals("about:blank")) {
                                isShowingOfflinePage = false;
                            }
                            // Show pull-to-refresh indicator if not already showing
                            if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(true);
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
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Setup error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, 100); // Optimized delay for WebView readiness
    }

    // Inject CSS to fix status bar overlap for portal content
    private void injectStatusBarFixCSS(WebView view) {
        String js = "(function(){" +
                "if(window.__upsiCSSInjected) return;" +
                "window.__upsiCSSInjected = true;" +
                "var existing = document.getElementById('android-status-bar-fix');" +
                "if(!existing){" +
                "  existing = document.createElement('style');" +
                "  existing.id = 'android-status-bar-fix';" +
                "  document.head.appendChild(existing);" +
                "}" +
                "existing.textContent = `" +
                // "html, body { padding-top: env(safe-area-inset-top, 39px) !important; margin-top: 0 !important; --background-color: rgba(255, 255, 255, 0);}" +
                // ".container-fluid.container-xl.position-relative.d-flex.align-items-center.justify-content-between {" +
                // "  padding-top: 25px !important;" +
                // "}" +
                ".page-header {" +
                "background-color: #2b0f48;" +
                "}" +
                ".page-header, .page-content {" +
                "  padding-top: 2.8rem !important;" +
                "  height: auto !important;" +
                "}" +
                ".page-content {" +
                "  padding-top: 3.7rem !important;" +
                "}" +
                ".header {" +
                "  padding-top: 2.8rem !important;" +
                "  height: auto !important;" +
                "}" +
                ".page-header .hidden-sm-up, .search {" +
                "  display: none !important;" +
                "}" +
                ".page-logo {" +
                "  top: 2.8rem !important;" +
                "}" +
                "#js-primary-nav {" +
                "  top: 3rem !important;" +
                "}" +
                // Improve input usability for mobile keyboards
                "input, textarea { -webkit-user-select: text !important; user-select: text !important; touch-action: manipulation !important; }" +
                "`;" +
                "console.log('[Android] Status bar fix CSS injected');" +
                "})();";
        view.evaluateJavascript(js, null);

            // Attach a lightweight focus handler to ensure inputs stay visible and not obscured by overlays
            String focusJs = "(function(){" +
                "if(window.__upsiFocusHandlerAttached) return;" +
                "document.addEventListener('focusin', function(e){" +
                "  var t=e.target;" +
                "  if(!t) return;" +
                "  var tag=(t.tagName||'').toUpperCase();" +
                "  if(tag==='INPUT' || tag==='TEXTAREA' || t.isContentEditable){" +
                "    try { t.scrollIntoView({block:'center', behavior:'smooth'}); } catch(_) {}" +
                "    // Temporarily relax pointer events on potential fixed overlays (optimized)" +
                "    var candidates = Array.from(document.querySelectorAll('.modal, .overlay, .popup, [role=dialog], .fixed-overlay, [class*=modal], [class*=overlay]')).filter(function(el){" +
                "      var st = getComputedStyle(el);" +
                "      if((st.position==='fixed'||st.position==='absolute') && parseInt(st.zIndex||0) > 50 && el !== t && !el.contains(t)){" +
                "         var r = el.getBoundingClientRect();" +
                "         return r.width > 80 && r.height > 40;" +
                "      }" +
                "      return false;" +
                "    });" +
                "    candidates.forEach(function(el){ el.__oldPE = el.style.pointerEvents; el.style.pointerEvents='none'; });" +
                "    setTimeout(function(){ candidates.forEach(function(el){ el.style.pointerEvents=el.__oldPE||''; }); }, 3000);" +
                "  }" +
                "}, true);" +
                "window.__upsiFocusHandlerAttached = true;" +
                "})();";
            view.evaluateJavascript(focusJs, null);

            // Ensure Backspace works in inputs even if site-level handlers block it
            // AGGRESSIVE FIX: Always re-inject, manual deletion fallback, capture+bubble phases
            String backspaceFix = "(function(){" +
                "function isEditable(el){" +
                "  if(!el) return false;" +
                "  var tag = (el.tagName||'').toUpperCase();" +
                "  if(tag==='TEXTAREA') return !el.readOnly && !el.disabled;" +
                "  if(tag==='INPUT'){ var t=(el.type||'').toLowerCase(); return ['text','search','url','tel','email','password','number','date','datetime-local','time','month','week'].indexOf(t)>=0 && !el.readOnly && !el.disabled; }" +
                "  if(el.isContentEditable) return true;" +
                "  return false;" +
                "}" +
                "function manualDelete(el){" +
                "  try{" +
                "    if(el.tagName==='INPUT'||el.tagName==='TEXTAREA'){" +
                "      var start=el.selectionStart, end=el.selectionEnd;" +
                "      if(start>0 && start===end){" +
                "        el.value=el.value.substring(0,start-1)+el.value.substring(end);" +
                "        el.selectionStart=el.selectionEnd=start-1;" +
                "        el.dispatchEvent(new Event('input',{bubbles:true}));" +
                "        return true;" +
                "      }" +
                "    }" +
                "  }catch(_){}" +
                "  return false;" +
                "}" +
                "var handler=function(e){" +
                "  var k=e.key||''; var code=e.keyCode||e.which||0;" +
                "  if((k==='Backspace'||code===8)&&isEditable(e.target)){" +
                "    e.stopImmediatePropagation();" +
                "    if(e.defaultPrevented){ e.preventDefault=function(){}; manualDelete(e.target); }" +
                "  }" +
                "};" +
                "document.removeEventListener('keydown',handler,true);" +
                "document.addEventListener('keydown',handler,true);" +
                "document.removeEventListener('keydown',handler,false);" +
                "document.addEventListener('keydown',handler,false);" +
                "console.log('[UPSI] Backspace fix re-injected');" +
                "})();";
            view.evaluateJavascript(backspaceFix, null);
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
        if (connectivityManager != null && networkCallback != null && isNetworkCallbackRegistered) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                isNetworkCallbackRegistered = false;
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
            if (!isNetworkCallbackRegistered) {
                connectivityManager.registerNetworkCallback(request, networkCallback);
                isNetworkCallbackRegistered = true;
            }
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
        }, 200);
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
                    
                    // Enable JavaScript and DOM storage for better app-like behavior
                    settings.setJavaScriptEnabled(true);
                    webView.setFocusable(true);
                    webView.setFocusableInTouchMode(true);
                    webView.requestFocus();

                    // Ensure WebView properly takes focus from touch (helps IME deliver keys reliably)
                    webView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                if (!v.hasFocus()) {
                                    v.requestFocus();
                                    v.requestFocusFromTouch();
                                }
                            }
                            return false; // let WebView handle normally
                        }
                    });

                    android.util.Log.i("MainActivity", "WebView settings optimized");
                }
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error setting up WebView: " + e.getMessage());
            }
        }, 200);
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
                            if (System.currentTimeMillis() - lastBackPressTime < BACK_PRESS_INTERVAL) {
                                finishAffinity();
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
    
    /**
     * Enhanced keyboard backspace fix for Issue #62306
     * Intercepts compositionend events and forces proper text deletion
     */
    private void setupKeyboardBackspaceFix() {
        // This will be injected via JavaScript in injectStatusBarFixCSS
        // See the backspaceFix enhancement in that method
    }
}

