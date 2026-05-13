package com.example.fixmycity.ui;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.fixmycity.utils.Constants;
import com.example.fixmycity.utils.LocationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SubmitReportActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Spinner spCategory;
    private Button btnGetLocation, btnPickImage, btnSubmit;
    private TextView tvLocation;
    private ImageView ivPreview;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean locationSelected = false;
    private Uri selectedImageUri = null;

    private LocationHelper locationHelper;
    private ActivityResultLauncher<String> imagePickerLauncher;
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
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvLocation = findViewById(R.id.tvLocation);
        ivPreview = findViewById(R.id.ivPreview);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        locationHelper = new LocationHelper(this);
        reportRepository = new ReportRepository();
        formValidator = new ReportFormValidator();
        reportFactory = new ReportFactory();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Constants.REPORT_CATEGORIES
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

        btnGetLocation.setOnClickListener(v -> {
            if (!locationHelper.hasLocationPermission()) {
                locationHelper.requestLocationPermission();
            } else {
                fetchLocation();
            }
        });

        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSubmit.setOnClickListener(v -> submitReport());

        BottomNavigationHelper.setup(this, bottomNavigation, R.id.navNewReport);
    }

    private void fetchLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                latitude = lat;
                longitude = lng;
                locationSelected = true;
                tvLocation.setText(getString(R.string.submit_location_format, lat, lng));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SubmitReportActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReport() {
        ReportFormData formData = getFormData();
        ValidationResult validationResult = formValidator.validate(formData);

        if (!validationResult.isValid()) {
            showValidationError(validationResult);
            return;
        }

        setSubmittingState(true);

        Report report = reportFactory.createFromForm(formData);

        reportRepository.saveReport(
                report,
                unused -> {
                    setSubmittingState(false);
                    Toast.makeText(SubmitReportActivity.this,
                            getString(R.string.submit_success),
                            Toast.LENGTH_LONG).show();
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

    private void setSubmittingState(boolean isSubmitting) {
        btnSubmit.setEnabled(!isSubmitting);
        btnSubmit.setText(isSubmitting
                ? getString(R.string.submit_report_loading)
                : getString(R.string.submit_report));
    }

    private ReportFormData getFormData() {
        return new ReportFormData(
                etTitle.getText().toString(),
                etDescription.getText().toString(),
                spCategory.getSelectedItem().toString(),
                latitude,
                longitude,
                locationSelected,
                selectedImageUri
        );
    }

    private void showValidationError(ValidationResult validationResult) {
        switch (validationResult.getField()) {
            case TITLE:
                etTitle.setError(validationResult.getMessage());
                break;
            case DESCRIPTION:
                etDescription.setError(validationResult.getMessage());
                break;
            case LOCATION:
                Toast.makeText(this, validationResult.getMessage(), Toast.LENGTH_SHORT).show();
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
        tvLocation.setText(getString(R.string.submit_location_missing));
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
                Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
