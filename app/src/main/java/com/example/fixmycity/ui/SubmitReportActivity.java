package com.example.fixmycity.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.model.Report;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class SubmitReportActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Spinner spCategory;
    private Button btnPickMapLocation, btnPickImage, btnSubmit;
    private TextView tvLocation;
    private ImageView ivPreview, ivMapPreview;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean locationSelected = false;
    private String locationAddress = "";
    private Uri selectedImageUri = null;
    private final AtomicInteger mapRequestCounter = new AtomicInteger(0);

    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> mapPickerLauncher;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_report);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        btnPickMapLocation = findViewById(R.id.btnPickMapLocation);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvLocation = findViewById(R.id.tvLocation);
        ivPreview = findViewById(R.id.ivPreview);
        ivMapPreview = findViewById(R.id.ivMapPreview);

        reportRepository = new ReportRepository();
        showMapPlaceholder();

        String[] categories = {"Pothole", "Broken Streetlight", "Garbage", "Sidewalk Damage", "Vandalism"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreview.setImageURI(uri);
                    }
                }
        );

        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleMapPickerResult
        );

        btnPickMapLocation.setOnClickListener(v -> openMapPicker());
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        ivMapPreview.setOnClickListener(v -> openMapPicker());

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void handleMapPickerResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }

        Intent data = result.getData();
        latitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, 0.0);
        longitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, 0.0);
        locationAddress = data.getStringExtra(MapPickerActivity.EXTRA_ADDRESS);
        if (locationAddress == null) {
            locationAddress = "";
        }

        locationSelected = true;
        tvLocation.setText(locationAddress.isEmpty() ? "Location selected on map" : locationAddress);
        loadStaticMap(latitude, longitude, 17);
    }

    private void submitReport() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }

        if (!locationSelected) {
            Toast.makeText(this, "Please choose a location on the map first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasImage = selectedImageUri != null;
        String localImageUri = selectedImageUri != null ? selectedImageUri.toString() : "";

        Toast.makeText(this, "Validation passed", Toast.LENGTH_SHORT).show();

        btnSubmit.setEnabled(false);

        Report report = new Report(
                title,
                description,
                category,
                latitude,
                longitude,
                locationAddress,
                hasImage,
                localImageUri,
                "Pending",
                System.currentTimeMillis(),
                "student@example.com"
        );

        reportRepository.saveReport(
                report,
                unused -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(SubmitReportActivity.this,
                            "Report submitted successfully",
                            Toast.LENGTH_LONG).show();
                    returnToMainScreen();
                },
                e -> {
                    btnSubmit.setEnabled(true);
                    e.printStackTrace();
                    Toast.makeText(SubmitReportActivity.this,
                            "Save failed: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
        );
    }

    private void returnToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        spCategory.setSelection(0);
        tvLocation.setText("Location not selected");
        ivPreview.setImageDrawable(null);

        latitude = 0.0;
        longitude = 0.0;
        locationAddress = "";
        locationSelected = false;
        selectedImageUri = null;
        showMapPlaceholder();
    }

    private void loadAddressForLocation(double lat, double lng) {
        new Thread(() -> {
            String resolvedAddress = "";
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    resolvedAddress = address.getAddressLine(0);
                }
            } catch (IOException ignored) {
                resolvedAddress = "";
            }

            String finalAddress = resolvedAddress;
            runOnUiThread(() -> {
                locationAddress = finalAddress;
                if (!locationAddress.isEmpty()) {
                    tvLocation.setText(locationAddress);
                } else {
                    tvLocation.setText("Location selected on map");
                }
            });
        }).start();
    }

    private void loadStaticMap(double lat, double lng, int zoom) {
        int requestId = mapRequestCounter.incrementAndGet();
        new Thread(() -> {
            try {
                String marker = URLEncoder.encode("color:red|" + lat + "," + lng, "UTF-8");
                String url = "https://maps.googleapis.com/maps/api/staticmap"
                        + "?center=" + lat + "," + lng
                        + "&zoom=" + zoom
                        + "&size=640x360"
                        + "&scale=2"
                        + "&maptype=roadmap"
                        + "&markers=" + marker
                        + "&key=" + getString(R.string.google_maps_key);

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                try (InputStream inputStream = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null && requestId == mapRequestCounter.get()) {
                        runOnUiThread(() -> ivMapPreview.setImageBitmap(bitmap));
                    }
                } finally {
                connection.disconnect();
                }
            } catch (IOException ignored) {
                if (requestId == mapRequestCounter.get()) {
                    runOnUiThread(() -> ivMapPreview.setBackgroundColor(Color.LTGRAY));
                }
            }
        }).start();
    }

    private void showMapPlaceholder() {
        mapRequestCounter.incrementAndGet();
        ivMapPreview.setImageDrawable(null);
        ivMapPreview.setBackgroundColor(Color.LTGRAY);
    }

    private void openMapPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        if (locationSelected) {
            intent.putExtra(MapPickerActivity.EXTRA_LATITUDE, latitude);
            intent.putExtra(MapPickerActivity.EXTRA_LONGITUDE, longitude);
            intent.putExtra(MapPickerActivity.EXTRA_ADDRESS, locationAddress);
        }
        mapPickerLauncher.launch(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapRequestCounter.incrementAndGet();
    }
}
