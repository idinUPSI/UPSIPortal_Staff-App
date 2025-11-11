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
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private TextView offlineBanner;
    private static final String HOME_URL = "https://unistaff.upsi.edu.my";
    private boolean isShowingOfflinePage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BridgeActivity will inflate its default layout with WebView
        // We'll add our custom header and banner programmatically as overlays
    }

    @Override
    public void onStart() {
        super.onStart();
        requestNotificationPermissionIfNeeded();
        setupNetworkMonitoring();
        
        // Delay UI setup to ensure WebView is ready
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            addCustomUI();
            setupWebViewClient();
        }, 500);
    }

    private void addCustomUI() {
        try {
            // Get the root view (FrameLayout from BridgeActivity)
            ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
            if (rootView == null) return;
            
            // Create vertical container for header + banner
            LinearLayout overlayContainer = new LinearLayout(this);
            overlayContainer.setOrientation(LinearLayout.VERTICAL);
            overlayContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            
            // Create header bar
            LinearLayout headerBar = createHeaderBar();
            overlayContainer.addView(headerBar);
            
            // Create offline banner
            offlineBanner = createOfflineBanner();
            overlayContainer.addView(offlineBanner);
            
            // Add overlay to root view
            rootView.addView(overlayContainer);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private LinearLayout createHeaderBar() {
        LinearLayout headerBar = new LinearLayout(this);
        headerBar.setOrientation(LinearLayout.HORIZONTAL);
        headerBar.setBackgroundColor(Color.parseColor("#663399"));
        headerBar.setGravity(Gravity.CENTER_VERTICAL);
        
        int padding = dpToPx(12);
        headerBar.setPadding(padding, padding, padding, padding);
        headerBar.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(56)
        ));
        
        // Title
        TextView title = new TextView(this);
        title.setText("MyUPSI Portal Staff");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
        );
        title.setLayoutParams(titleParams);
        headerBar.addView(title);
        
        // Refresh button
        Button btnRefresh = new Button(this);
        btnRefresh.setText("↻");
        btnRefresh.setTextColor(Color.WHITE);
        btnRefresh.setBackgroundColor(Color.TRANSPARENT);
        btnRefresh.setAllCaps(false);
        btnRefresh.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    getBridge().getWebView().reload();
                }
            } else {
                Toast.makeText(MainActivity.this, "Tiada sambungan internet", Toast.LENGTH_SHORT).show();
            }
        });
        headerBar.addView(btnRefresh);
        
        // Home button
        Button btnHome = new Button(this);
        btnHome.setText("⌂");
        btnHome.setTextColor(Color.WHITE);
        btnHome.setBackgroundColor(Color.TRANSPARENT);
        btnHome.setAllCaps(false);
        btnHome.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    getBridge().getWebView().loadUrl(HOME_URL);
                }
            } else {
                Toast.makeText(MainActivity.this, "Tiada sambungan internet", Toast.LENGTH_SHORT).show();
            }
        });
        headerBar.addView(btnHome);
        
        return headerBar;
    }
    
    private TextView createOfflineBanner() {
        TextView banner = new TextView(this);
        banner.setText("Tiada sambungan internet");
        banner.setTextColor(Color.WHITE);
        banner.setBackgroundColor(Color.parseColor("#FF9800"));
        banner.setGravity(Gravity.CENTER);
        banner.setTextSize(14);
        int padding = dpToPx(12);
        banner.setPadding(padding, padding, padding, padding);
        banner.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        banner.setVisibility(View.GONE);
        return banner;
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void setupWebViewClient() {
        if (getBridge() != null && getBridge().getWebView() != null) {
            WebView webView = getBridge().getWebView();
            
            // Enable JavaScript (required for offline page interaction)
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            
            // Add JavaScript interface for retry button
            webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");
            
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    
                    // Don't hide banner if loading offline page
                    if (!url.startsWith("data:")) {
                        isShowingOfflinePage = false;
                        if (offlineBanner != null) {
                            offlineBanner.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    
                    // Only handle main frame errors
                    if (request.isForMainFrame()) {
                        runOnUiThread(() -> {
                            isShowingOfflinePage = true;
                            
                            // Show offline banner with error message
                            if (offlineBanner != null) {
                                offlineBanner.setText("Tiada sambungan internet. Sila semak rangkaian anda.");
                                offlineBanner.setVisibility(View.VISIBLE);
                            }
                            
                            // Load improved offline page with proper retry mechanism
                            String offlineHtml = "<!DOCTYPE html>" +
                                "<html><head><meta charset='UTF-8'>" +
                                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                "<style>" +
                                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; " +
                                "padding: 60px 20px; text-align: center; background: #f5f5f5; min-height: 100vh; " +
                                "display: flex; flex-direction: column; justify-content: center; align-items: center; }" +
                                ".icon { font-size: 80px; margin-bottom: 24px; opacity: 0.8; }" +
                                "h1 { color: #663399; font-size: 26px; font-weight: 600; margin-bottom: 16px; }" +
                                "p { color: #666; font-size: 16px; line-height: 1.6; margin-bottom: 12px; max-width: 400px; }" +
                                ".btn { background: #663399; color: white; border: none; padding: 14px 32px; " +
                                "border-radius: 8px; font-size: 16px; font-weight: 500; cursor: pointer; " +
                                "margin-top: 20px; box-shadow: 0 2px 8px rgba(102,51,153,0.3); " +
                                "transition: all 0.2s; }" +
                                ".btn:active { background: #552b8a; transform: scale(0.98); }" +
                                ".tips { margin-top: 32px; padding: 16px; background: white; border-radius: 8px; " +
                                "max-width: 400px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                                ".tips h3 { color: #663399; font-size: 14px; margin-bottom: 8px; }" +
                                ".tips ul { text-align: left; color: #666; font-size: 14px; line-height: 1.8; }" +
                                "</style></head><body>" +
                                "<div class='icon'>📡</div>" +
                                "<h1>Tiada Sambungan Internet</h1>" +
                                "<p>Portal MyUPSI memerlukan sambungan internet untuk berfungsi.</p>" +
                                "<p>Sila semak sambungan anda dan cuba lagi.</p>" +
                                "<button class='btn' onclick='AndroidInterface.retryConnection()'>Cuba Lagi</button>" +
                                "<div class='tips'><h3>💡 Tips:</h3><ul>" +
                                "<li>Pastikan WiFi atau data selular aktif</li>" +
                                "<li>Cuba toggle Airplane Mode</li>" +
                                "<li>Semak penggunaan data</li>" +
                                "</ul></div>" +
                                "</body></html>";
                            
                            view.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null);
                        });
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    
                    // Page loaded successfully
                    if (!url.startsWith("data:") && !url.equals("about:blank")) {
                        isShowingOfflinePage = false;
                        if (offlineBanner != null) {
                            offlineBanner.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
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
        if (Build.VERSION.SDK_INT >= 33) { // Android 13+
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
                    if (offlineBanner != null) offlineBanner.setVisibility(View.GONE);
                    
                    // Auto-reload if showing offline page
                    if (isShowingOfflinePage && getBridge() != null && getBridge().getWebView() != null) {
                        getBridge().getWebView().loadUrl(HOME_URL);
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Tiada sambungan internet", Toast.LENGTH_SHORT).show();
                    if (offlineBanner != null && !isShowingOfflinePage) {
                        offlineBanner.setVisibility(View.VISIBLE);
                    }
                });
            }
        };

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback);
        } catch (Exception ignored) {}
    }
    
    // JavaScript Interface for offline page
    public class WebAppInterface {
        @android.webkit.JavascriptInterface
        public void retryConnection() {
            runOnUiThread(() -> {
                if (isNetworkAvailable()) {
                    // Network is back, reload the portal
                    if (getBridge() != null && getBridge().getWebView() != null) {
                        getBridge().getWebView().loadUrl(HOME_URL);
                        Toast.makeText(MainActivity.this, "Cuba menyambung semula...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Still no network
                    Toast.makeText(MainActivity.this, "Masih tiada sambungan internet", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
