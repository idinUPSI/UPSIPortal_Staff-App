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
            } catch (Exception e) {
                // Silently handle any initialization errors
            }
        });
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
