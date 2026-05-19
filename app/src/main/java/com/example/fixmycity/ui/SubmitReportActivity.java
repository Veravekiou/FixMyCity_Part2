package com.example.fixmycity.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.utils.CameraHelper;
import com.example.fixmycity.utils.ImageUploadHelper;
import com.example.fixmycity.utils.LocationHelper;

public class SubmitReportActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Spinner spCategory;
    private Button btnGetLocation, btnTakePhoto, btnSubmit;
    private TextView tvLocation;
    private ImageView ivPreview;
    private ProgressBar progressBar;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean locationSelected = false;
    private Uri selectedImageUri = null;

    private LocationHelper locationHelper;
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
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvLocation = findViewById(R.id.tvLocation);
        ivPreview = findViewById(R.id.ivPreview);
        progressBar = findViewById(R.id.progressBar);

        locationHelper = new LocationHelper(this);
        cameraHelper = new CameraHelper(this);
        imageUploadHelper = new ImageUploadHelper();
        reportRepository = new ReportRepository();

        String[] categories = {"Pothole", "Broken Streetlight", "Garbage", "Sidewalk Damage", "Vandalism"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        btnGetLocation.setOnClickListener(v -> {
            if (!locationHelper.hasLocationPermission()) {
                locationHelper.requestLocationPermission();
            } else {
                fetchLocation();
            }
        });

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

    private void fetchLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                latitude = lat;
                longitude = lng;
                locationSelected = true;
                tvLocation.setText("Lat: " + lat + ", Lng: " + lng);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SubmitReportActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
            Toast.makeText(this, "Please get location first", Toast.LENGTH_SHORT).show();
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
                    clearForm();
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

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        spCategory.setSelection(0);
        tvLocation.setText("Location not selected");
        ivPreview.setImageDrawable(null);

        latitude = 0.0;
        longitude = 0.0;
        locationSelected = false;
        selectedImageUri = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CameraHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}