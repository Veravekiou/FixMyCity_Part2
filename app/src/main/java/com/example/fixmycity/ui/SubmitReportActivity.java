package com.example.fixmycity.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.fixmycity.utils.CameraHelper;
import com.example.fixmycity.utils.ImageUploadHelper;

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
    private Button btnPickMapLocation, btnTakePhoto, btnSubmit;
    private TextView tvLocation;
    private ImageView ivPreview, ivMapPreview;
    private ProgressBar progressBar;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean locationSelected = false;
    private String locationAddress = "";
    private Uri selectedImageUri = null;
    private final AtomicInteger mapRequestCounter = new AtomicInteger(0);

    private ActivityResultLauncher<Intent> mapPickerLauncher;
    private CameraHelper cameraHelper;
    private ImageUploadHelper imageUploadHelper;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_report);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        btnPickMapLocation = findViewById(R.id.btnPickMapLocation);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvLocation = findViewById(R.id.tvLocation);
        ivPreview = findViewById(R.id.ivPreview);
        ivMapPreview = findViewById(R.id.ivMapPreview);
        progressBar = findViewById(R.id.progressBar);

        cameraHelper = new CameraHelper(this);
        imageUploadHelper = new ImageUploadHelper();
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

        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleMapPickerResult
        );

        btnPickMapLocation.setOnClickListener(v -> openMapPicker());
        ivMapPreview.setOnClickListener(v -> openMapPicker());
        btnTakePhoto.setOnClickListener(v -> {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.CAMERA},
                        CameraHelper.CAMERA_PERMISSION_REQUEST_CODE
                );
            } else {
                openCamera();
            }
        });

        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void openCamera() {
        Uri uri = cameraHelper.openCamera();
        if (uri != null) {
            selectedImageUri = uri;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = cameraHelper.getPhotoUri();
            ivPreview.setImageURI(selectedImageUri);
            Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
        }
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
        if (locationAddress.isEmpty()) {
            loadAddressForLocation(latitude, longitude);
        }
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

        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        if (selectedImageUri != null) {
            imageUploadHelper.uploadImage(selectedImageUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    saveReport(title, description, category, downloadUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    btnSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SubmitReportActivity.this,
                            "Image upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            saveReport(title, description, category, "");
        }
    }

    private void saveReport(String title, String description, String category, String imageUrl) {
        Report report = new Report(
                title,
                description,
                category,
                latitude,
                longitude,
                locationAddress,
                !imageUrl.isEmpty(),
                imageUrl,
                "Pending",
                System.currentTimeMillis(),
                "student@example.com"
        );

        reportRepository.saveReport(
                report,
                unused -> {
                    btnSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SubmitReportActivity.this,
                            "Report submitted successfully",
                            Toast.LENGTH_LONG).show();
                    returnToMainScreen();
                },
                e -> {
                    btnSubmit.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SubmitReportActivity.this,
                            "Save failed: " + e.getMessage(),
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CameraHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mapRequestCounter.incrementAndGet();
        super.onDestroy();
    }
}
