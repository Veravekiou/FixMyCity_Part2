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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.domain.ReportFactory;
import com.example.fixmycity.domain.ReportFormData;
import com.example.fixmycity.domain.ReportFormValidator;
import com.example.fixmycity.domain.ReportFormValidator.ValidationResult;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.utils.CameraHelper;
import com.example.fixmycity.utils.Constants;
import com.example.fixmycity.utils.ImageUploadHelper;
import com.example.fixmycity.utils.LocationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
    private Button btnPickMapLocation, btnUseCurrentLocation, btnTakePhoto, btnSubmit;
    private TextView tvLocation, tvLocationStatus, tvCategoryStep, tvLocationStep, tvPhotoStep,
            tvDetailsStep, tvSubmitStep;
    private View viewCategoryStep, viewDetailsStep, viewLocationStep, viewPhotoStep, viewSubmitStep;
    private ImageView ivPreview, ivMapPreview;
    private LinearLayout layoutImagePlaceholder;
    private BottomNavigationView bottomNavigation;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean locationSelected = false;
    private String locationAddress = "";
    private Uri selectedImageUri = null;
    private final AtomicInteger mapRequestCounter = new AtomicInteger(0);

    private ActivityResultLauncher<Intent> mapPickerLauncher;
    private CameraHelper cameraHelper;
    private ImageUploadHelper imageUploadHelper;
    private LocationHelper locationHelper;
    private ReportRepository reportRepository;
    private ReportFormValidator formValidator;
    private ReportFactory reportFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_report);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        btnPickMapLocation = findViewById(R.id.btnPickMapLocation);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvLocation = findViewById(R.id.tvLocation);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvCategoryStep = findViewById(R.id.tvCategoryStep);
        tvLocationStep = findViewById(R.id.tvLocationStep);
        tvPhotoStep = findViewById(R.id.tvPhotoStep);
        tvDetailsStep = findViewById(R.id.tvDetailsStep);
        tvSubmitStep = findViewById(R.id.tvSubmitStep);
        viewCategoryStep = findViewById(R.id.viewCategoryStep);
        viewDetailsStep = findViewById(R.id.viewDetailsStep);
        viewLocationStep = findViewById(R.id.viewLocationStep);
        viewPhotoStep = findViewById(R.id.viewPhotoStep);
        viewSubmitStep = findViewById(R.id.viewSubmitStep);
        ivPreview = findViewById(R.id.ivPreview);
        ivMapPreview = findViewById(R.id.ivMapPreview);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        cameraHelper = new CameraHelper(this);
        imageUploadHelper = new ImageUploadHelper();
        locationHelper = new LocationHelper(this);
        reportRepository = new ReportRepository();
        formValidator = new ReportFormValidator();
        reportFactory = new ReportFactory();
        showMapPlaceholder();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_category,
                Constants.REPORT_CATEGORIES
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                styleCategoryOption(view, position == 0);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                styleCategoryOption(view, position == 0);
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.item_spinner_category_dropdown);
        spCategory.setAdapter(adapter);

        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleMapPickerResult
        );

        btnPickMapLocation.setOnClickListener(v -> openMapPicker());
        btnUseCurrentLocation.setOnClickListener(v -> {
            if (!locationHelper.hasLocationPermission()) {
                locationHelper.requestLocationPermission();
            } else {
                fetchCurrentLocation();
            }
        });
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

        setupProgressWatchers();
        updateProgressIndicators();
        BottomNavigationHelper.setup(this, bottomNavigation, View.NO_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationHelper.syncSelectedItem(bottomNavigation, View.NO_ID);
    }

    private void openCamera() {
        Uri uri = cameraHelper.openCamera();
        if (uri != null) {
            selectedImageUri = uri;
        } else {
            Toast.makeText(this, R.string.submit_camera_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = cameraHelper.getPhotoUri();
            ivPreview.setImageURI(selectedImageUri);
            layoutImagePlaceholder.setVisibility(View.GONE);
            updateProgressIndicators();
            Toast.makeText(this, R.string.submit_photo_captured, Toast.LENGTH_SHORT).show();
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
        tvLocationStatus.setText(R.string.submit_location_ready);
        tvLocationStatus.setBackgroundResource(R.drawable.bg_location_selected);
        tvLocationStatus.setTextColor(getColor(R.color.primary_dark));
        tvLocation.setText(locationAddress.isEmpty()
                ? getString(R.string.submit_location_format, latitude, longitude)
                : locationAddress);

        if (locationAddress.isEmpty()) {
            loadAddressForLocation(latitude, longitude);
        }

        loadStaticMap(latitude, longitude, 17);
        updateProgressIndicators();
    }

    private void fetchCurrentLocation() {
        btnUseCurrentLocation.setEnabled(false);
        tvLocationStatus.setText(R.string.submit_location_loading);

        locationHelper.getCurrentLocation(new LocationHelper.ReportLocationCallback() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                btnUseCurrentLocation.setEnabled(true);
                latitude = lat;
                longitude = lng;
                locationAddress = "";
                locationSelected = true;

                tvLocationStatus.setText(R.string.submit_location_ready);
                tvLocationStatus.setBackgroundResource(R.drawable.bg_location_selected);
                tvLocationStatus.setTextColor(getColor(R.color.primary_dark));
                tvLocation.setText(getString(R.string.submit_location_format, latitude, longitude));
                loadAddressForLocation(latitude, longitude);
                loadStaticMap(latitude, longitude, 17);
                updateProgressIndicators();
            }

            @Override
            public void onLocationFailed(Exception exception) {
                btnUseCurrentLocation.setEnabled(true);
                Toast.makeText(
                        SubmitReportActivity.this,
                        R.string.submit_current_location_failed,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void styleCategoryOption(TextView view, boolean isPlaceholder) {
        view.setTextColor(getColor(isPlaceholder ? R.color.text_secondary : R.color.text_primary));
        view.setTextSize(isPlaceholder ? 14 : 15);
        view.setTypeface(null, isPlaceholder ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
    }

    private void setupProgressWatchers() {
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateProgressIndicators();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateProgressIndicators();
            }
        });

        TextWatcher detailsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProgressIndicators();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op.
            }
        };

        etTitle.addTextChangedListener(detailsWatcher);
        etDescription.addTextChangedListener(detailsWatcher);
    }

    private void updateProgressIndicators() {
        setStepState(tvCategoryStep, viewCategoryStep, isCategorySelected());
        setStepState(tvDetailsStep, viewDetailsStep, areDetailsEntered());
        setStepState(tvLocationStep, viewLocationStep, locationSelected);
        setStepState(tvPhotoStep, viewPhotoStep, selectedImageUri != null);
    }

    private boolean isCategorySelected() {
        Object selectedCategory = spCategory.getSelectedItem();
        return selectedCategory != null
                && !Constants.CATEGORY_PLACEHOLDER.equals(selectedCategory.toString());
    }

    private boolean areDetailsEntered() {
        return !isBlank(etTitle.getText().toString())
                && !isBlank(etDescription.getText().toString());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void submitReport() {
        ReportFormData formData = getFormData();
        ValidationResult validationResult = formValidator.validate(formData);

        if (!validationResult.isValid()) {
            showValidationError(validationResult);
            return;
        }

        setSubmittingState(true);

        if (selectedImageUri != null) {
            imageUploadHelper.uploadImage(selectedImageUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    saveReport(formData, downloadUrl);
                }

                @Override
                public void onFailure(Exception e) {
                    setSubmittingState(false);
                    Toast.makeText(SubmitReportActivity.this,
                            getString(R.string.submit_image_upload_failed_detail, e.getMessage()),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            saveReport(formData, "");
        }
    }

    private void saveReport(ReportFormData formData, String imageUrl) {
        Report report = reportFactory.createFromForm(formData, imageUrl);

        reportRepository.saveReport(
                report,
                unused -> {
                    setSubmittingState(false);
                    showSuccessDialog();
                    clearForm();
                },
                e -> {
                    setSubmittingState(false);
                    Toast.makeText(SubmitReportActivity.this,
                            getRepositoryErrorMessage(e),
                            Toast.LENGTH_LONG).show();
                }
        );
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.submit_success)
                .setMessage(R.string.submit_success_message)
                .setPositiveButton(R.string.dialog_ok, null)
                .show();
    }

    private void markStepCompleted(TextView stepView) {
        setStepState(stepView, getStepLine(stepView), true);
    }

    private void markStepPending(TextView stepView) {
        setStepState(stepView, getStepLine(stepView), false);
    }

    private void setStepState(TextView stepView, View stepLine, boolean isCompleted) {
        stepView.setTextColor(getColor(isCompleted ? R.color.primary_dark : R.color.text_secondary));

        if (stepLine != null) {
            stepLine.setBackgroundResource(isCompleted
                    ? R.drawable.bg_step_line_active
                    : R.drawable.bg_step_line_inactive);
        }
    }

    private View getStepLine(TextView stepView) {
        if (stepView == tvCategoryStep) {
            return viewCategoryStep;
        }

        if (stepView == tvDetailsStep) {
            return viewDetailsStep;
        }

        if (stepView == tvLocationStep) {
            return viewLocationStep;
        }

        if (stepView == tvPhotoStep) {
            return viewPhotoStep;
        }

        if (stepView == tvSubmitStep) {
            return viewSubmitStep;
        }

        return null;
    }

    private void setSubmittingState(boolean isSubmitting) {
        btnSubmit.setEnabled(!isSubmitting);
        btnSubmit.setText(isSubmitting
                ? getString(R.string.submit_report_loading)
                : getString(R.string.submit_report));

        if (isSubmitting) {
            markStepCompleted(tvSubmitStep);
        } else {
            markStepPending(tvSubmitStep);
        }
    }

    private ReportFormData getFormData() {
        return new ReportFormData(
                etTitle.getText().toString(),
                etDescription.getText().toString(),
                spCategory.getSelectedItem().toString(),
                latitude,
                longitude,
                locationAddress,
                locationSelected,
                selectedImageUri
        );
    }

    private void showValidationError(ValidationResult validationResult) {
        switch (validationResult.getField()) {
            case CATEGORY:
            case LOCATION:
                Toast.makeText(this, validationResult.getMessage(), Toast.LENGTH_SHORT).show();
                break;
            case TITLE:
                etTitle.setError(validationResult.getMessage());
                break;
            case DESCRIPTION:
                etDescription.setError(validationResult.getMessage());
                break;
            default:
                Toast.makeText(this, getString(R.string.submit_check_form), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private String getRepositoryErrorMessage(Exception exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            return getString(R.string.submit_save_failed);
        }

        return getString(R.string.submit_save_failed_detail, detail);
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        spCategory.setSelection(0);
        tvLocationStatus.setText(getString(R.string.submit_location_missing));
        tvLocationStatus.setBackgroundResource(R.drawable.bg_preview);
        tvLocationStatus.setTextColor(getColor(R.color.text_secondary));
        tvLocation.setText(getString(R.string.submit_location_missing_note));
        ivPreview.setImageDrawable(null);
        layoutImagePlaceholder.setVisibility(View.VISIBLE);
        markStepPending(tvLocationStep);
        markStepPending(tvPhotoStep);
        markStepPending(tvSubmitStep);

        latitude = 0.0;
        longitude = 0.0;
        locationAddress = "";
        locationSelected = false;
        selectedImageUri = null;
        showMapPlaceholder();
        updateProgressIndicators();
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
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mapRequestCounter.incrementAndGet();
        super.onDestroy();
    }
}
