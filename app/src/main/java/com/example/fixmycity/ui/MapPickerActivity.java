package com.example.fixmycity.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity {

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_ADDRESS = "extra_address";

    private static final double DEFAULT_LATITUDE = 37.9838;
    private static final double DEFAULT_LONGITUDE = 23.7275;

    private WebView pickLocationMap;
    private TextView tvPickedLocation;
    private Button btnConfirmLocation;

    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress = "";
    private boolean hasSelectedLocation = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        tvPickedLocation = findViewById(R.id.tvPickedLocation);
        pickLocationMap = findViewById(R.id.pickLocationMap);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LONGITUDE)) {
            selectedLatitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0);
            selectedLongitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0);
            selectedAddress = intent.getStringExtra(EXTRA_ADDRESS);
            if (selectedAddress == null) {
                selectedAddress = "";
            }
            hasSelectedLocation = true;
        }

        btnConfirmLocation.setEnabled(hasSelectedLocation);
        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
        showPickedLocationText();

        WebSettings settings = pickLocationMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        pickLocationMap.setWebViewClient(new WebViewClient());
        pickLocationMap.addJavascriptInterface(new MapBridge(), "AndroidLocationPicker");
        pickLocationMap.loadDataWithBaseURL(
                "https://fixmycity.local/",
                buildMapHtml(),
                "text/html",
                "UTF-8",
                null
        );
    }

    private String buildMapHtml() {
        double centerLat = hasSelectedLocation ? selectedLatitude : DEFAULT_LATITUDE;
        double centerLng = hasSelectedLocation ? selectedLongitude : DEFAULT_LONGITUDE;
        int zoom = hasSelectedLocation ? 17 : 13;
        String markerScript = hasSelectedLocation
                ? "setPickedLocation(" + selectedLatitude + "," + selectedLongitude + ", false);"
                : "";

        return "<!doctype html>"
                + "<html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'>"
                + "<style>html,body,#map{height:100%;margin:0;} .leaflet-control-attribution{font-size:10px;}</style>"
                + "</head><body>"
                + "<div id='map'></div>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<script>"
                + "var map=L.map('map').setView([" + centerLat + "," + centerLng + "]," + zoom + ");"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{"
                + "maxZoom:19,attribution:'OpenStreetMap'}).addTo(map);"
                + "var marker=null;"
                + "function setPickedLocation(lat,lng,notify){"
                + "if(marker){marker.setLatLng([lat,lng]);}else{marker=L.marker([lat,lng]).addTo(map);}"
                + "map.setView([lat,lng],17);"
                + "if(notify){AndroidLocationPicker.onLocationSelected(String(lat),String(lng));}"
                + "}"
                + "map.on('click',function(e){setPickedLocation(e.latlng.lat,e.latlng.lng,true);});"
                + markerScript
                + "</script></body></html>";
    }

    private void handleLocationSelected(double lat, double lng) {
        selectedLatitude = lat;
        selectedLongitude = lng;
        selectedAddress = "";
        hasSelectedLocation = true;
        btnConfirmLocation.setEnabled(true);
        tvPickedLocation.setText("Location selected on map");
        loadAddressForLocation(lat, lng);
    }

    private void loadAddressForLocation(double lat, double lng) {
        new Thread(() -> {
            String resolvedAddress = "";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    resolvedAddress = addresses.get(0).getAddressLine(0);
                }
            } catch (IOException ignored) {
                resolvedAddress = "";
            }

            String finalAddress = resolvedAddress;
            runOnUiThread(() -> {
                selectedAddress = finalAddress;
                showPickedLocationText();
            });
        }).start();
    }

    private void showPickedLocationText() {
        if (!selectedAddress.isEmpty()) {
            tvPickedLocation.setText(selectedAddress);
        } else if (hasSelectedLocation) {
            tvPickedLocation.setText("Location selected on map");
        } else {
            tvPickedLocation.setText("Tap the exact point of the problem on the map");
        }
    }

    private void confirmLocation() {
        if (!hasSelectedLocation) {
            Toast.makeText(this, "Select a point on the map first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra(EXTRA_LATITUDE, selectedLatitude);
        result.putExtra(EXTRA_LONGITUDE, selectedLongitude);
        result.putExtra(EXTRA_ADDRESS, selectedAddress);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onDestroy() {
        pickLocationMap.destroy();
        super.onDestroy();
    }

    private class MapBridge {
        @JavascriptInterface
        public void onLocationSelected(String latitude, String longitude) {
            try {
                double lat = Double.parseDouble(latitude);
                double lng = Double.parseDouble(longitude);
                runOnUiThread(() -> handleLocationSelected(lat, lng));
            } catch (NumberFormatException ignored) {
                runOnUiThread(() -> Toast.makeText(
                        MapPickerActivity.this,
                        "Could not read selected location",
                        Toast.LENGTH_SHORT
                ).show());
            }
        }
    }
}
