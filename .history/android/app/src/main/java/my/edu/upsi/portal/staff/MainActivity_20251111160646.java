package my.edu.upsi.portal.staff;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private static final String HOME_URL = "https://unistaff.upsi.edu.my";
    private boolean isShowingOfflinePage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        requestNotificationPermissionIfNeeded();
        setupNetworkMonitoring();
        setupOfflineErrorHandler();
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
                            // ALWAYS handle main frame errors - prevent Android default error page
                            if (request.isForMainFrame()) {
                                runOnUiThread(() -> {
                                    isShowingOfflinePage = true;
                                    loadOfflinePage(view);
                                    
                                    // Debug: show error details
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        String errorMsg = "Error: " + error.getErrorCode() + " - " + error.getDescription();
                                        android.util.Log.e("MainActivity", errorMsg);
                                    }
                                });
                                // Don't call super - we always show our custom page
                                return;
                            }
                            super.onReceivedError(view, request, error);
                        }

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                            // Only reset flag if loading a real URL (not our offline page)
                            if (!url.startsWith("data:") && !url.equals("about:blank")) {
                                isShowingOfflinePage = false;
                            }
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            // Successfully loaded a real page
                            if (!url.startsWith("data:") && !url.equals("about:blank")) {
                                isShowingOfflinePage = false;
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Setup error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, 1000); // Increase delay to ensure WebView is fully ready
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
        
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && 
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
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

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
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
}
