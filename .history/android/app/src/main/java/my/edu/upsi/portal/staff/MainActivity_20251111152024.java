package my.edu.upsi.portal.staff;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
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
    public void onStart() {
        super.onStart();
        requestNotificationPermissionIfNeeded();
        setupNetworkMonitoring();
        setupNativeUI();
    }

    private void setupNativeUI() {
        // Use a handler to ensure views are ready
        this.runOnUiThread(() -> {
            try {
                // Find views using resource IDs
                int offlineBannerId = getResources().getIdentifier("offline_banner", "id", getPackageName());
                int btnRefreshId = getResources().getIdentifier("btn_refresh", "id", getPackageName());
                int btnHomeId = getResources().getIdentifier("btn_home", "id", getPackageName());

                if (offlineBannerId != 0) {
                    offlineBanner = findViewById(offlineBannerId);
                }

                if (btnRefreshId != 0) {
                    Button btnRefresh = findViewById(btnRefreshId);
                    if (btnRefresh != null) {
                        btnRefresh.setOnClickListener(v -> {
                            if (getBridge() != null && getBridge().getWebView() != null) {
                                getBridge().getWebView().reload();
                            }
                        });
                    }
                }

                if (btnHomeId != 0) {
                    Button btnHome = findViewById(btnHomeId);
                    if (btnHome != null) {
                        btnHome.setOnClickListener(v -> {
                            if (getBridge() != null && getBridge().getWebView() != null) {
                                getBridge().getWebView().loadUrl(HOME_URL);
                            }
                        });
                    }
                }

                // Setup custom WebViewClient for error handling
                setupWebViewClient();
            } catch (Exception e) {
                // Silently handle any initialization errors
            }
        });
    }

    private void setupWebViewClient() {
        if (getBridge() != null && getBridge().getWebView() != null) {
            WebView webView = getBridge().getWebView();
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    // Hide offline banner when starting to load
                    if (offlineBanner != null) {
                        offlineBanner.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    
                    // Only handle main frame errors
                    if (request.isForMainFrame()) {
                        runOnUiThread(() -> {
                            // Show offline banner with error message
                            if (offlineBanner != null) {
                                offlineBanner.setText("Tiada sambungan internet. Sila semak rangkaian anda.");
                                offlineBanner.setVisibility(View.VISIBLE);
                            }
                            
                            // Load offline page
                            String offlineHtml = "<!DOCTYPE html>" +
                                "<html><head><meta charset='UTF-8'>" +
                                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                "<style>" +
                                "body { font-family: sans-serif; padding: 40px 20px; text-align: center; background: #f5f5f5; }" +
                                ".icon { font-size: 64px; margin-bottom: 20px; }" +
                                "h1 { color: #663399; font-size: 24px; margin-bottom: 10px; }" +
                                "p { color: #666; line-height: 1.6; margin-bottom: 20px; }" +
                                ".btn { background: #663399; color: white; border: none; padding: 12px 24px; " +
                                "border-radius: 4px; font-size: 16px; cursor: pointer; }" +
                                "</style></head><body>" +
                                "<div class='icon'>📡</div>" +
                                "<h1>Tiada Sambungan Internet</h1>" +
                                "<p>Sila semak sambungan internet anda dan cuba lagi.</p>" +
                                "<p>Pastikan WiFi atau data selular anda aktif.</p>" +
                                "<button class='btn' onclick='location.reload()'>Cuba Lagi</button>" +
                                "</body></html>";
                            
                            view.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null);
                        });
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // Page loaded successfully, ensure banner is hidden
                    if (offlineBanner != null && !url.equals("about:blank")) {
                        offlineBanner.setVisibility(View.GONE);
                    }
                }
            });
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
                });
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Tiada sambungan internet", Toast.LENGTH_SHORT).show();
                    if (offlineBanner != null) offlineBanner.setVisibility(View.VISIBLE);
                });
            }
        };

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback);
        } catch (Exception ignored) {}
    }
}
