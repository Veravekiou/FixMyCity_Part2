package com.example.fixmycity.ui;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SubmitReportActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Spinner spCategory;
    private Button btnGetLocation, btnPickImage, btnSubmit;
    private TextView tvLocation, tvLocationStatus, tvCategoryStep, tvLocationStep, tvPhotoStep,
            tvDetailsStep, tvSubmitStep;
    private ImageView ivPreview;
    private LinearLayout layoutImagePlaceholder;
    private BottomNavigationView bottomNavigation;

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
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvCategoryStep = findViewById(R.id.tvCategoryStep);
        tvLocationStep = findViewById(R.id.tvLocationStep);
        tvPhotoStep = findViewById(R.id.tvPhotoStep);
        tvDetailsStep = findViewById(R.id.tvDetailsStep);
        tvSubmitStep = findViewById(R.id.tvSubmitStep);
        ivPreview = findViewById(R.id.ivPreview);
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        locationHelper = new LocationHelper(this);
        reportRepository = new ReportRepository();
        formValidator = new ReportFormValidator();
        reportFactory = new ReportFactory();

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

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreview.setImageURI(uri);
                        layoutImagePlaceholder.setVisibility(View.GONE);
                        markStepCompleted(tvPhotoStep);
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

        markStepCompleted(tvCategoryStep);
        markStepCompleted(tvDetailsStep);
        BottomNavigationHelper.setup(this, bottomNavigation, R.id.navNewReport);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationHelper.syncSelectedItem(bottomNavigation, R.id.navNewReport);
    }

    private void fetchLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                latitude = lat;
                longitude = lng;
                locationSelected = true;
                tvLocationStatus.setText(getString(R.string.submit_location_ready));
                tvLocationStatus.setBackgroundResource(R.drawable.bg_location_selected);
                tvLocationStatus.setTextColor(getColor(R.color.primary_dark));
                tvLocation.setText(getString(R.string.submit_location_format, lat, lng));
                markStepCompleted(tvLocationStep);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SubmitReportActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void styleCategoryOption(TextView view, boolean isPlaceholder) {
        view.setTextColor(getColor(isPlaceholder ? R.color.text_secondary : R.color.text_primary));
        view.setTextSize(isPlaceholder ? 14 : 15);
        view.setTypeface(null, isPlaceholder ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
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
        stepView.setBackgroundResource(R.drawable.bg_step_active);
        stepView.setTextColor(getColor(R.color.white));
    }

    private void markStepPending(TextView stepView) {
        stepView.setBackgroundResource(R.drawable.bg_step_inactive);
        stepView.setTextColor(getColor(R.color.text_secondary));
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
                locationSelected,
                selectedImageUri
        );
    }

    private void showValidationError(ValidationResult validationResult) {
        switch (validationResult.getField()) {
            case CATEGORY:
                Toast.makeText(this, validationResult.getMessage(), Toast.LENGTH_SHORT).show();
                break;
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
        tvLocationStatus.setText(getString(R.string.submit_location_missing));
        tvLocationStatus.setBackgroundResource(R.drawable.bg_preview);
        tvLocationStatus.setTextColor(getColor(R.color.text_secondary));
        tvLocation.setText(getString(R.string.submit_location_missing_note));
        ivPreview.setImageDrawable(null);
        layoutImagePlaceholder.setVisibility(View.VISIBLE);
        markStepCompleted(tvCategoryStep);
        markStepCompleted(tvDetailsStep);
        markStepPending(tvLocationStep);
        markStepPending(tvPhotoStep);
        markStepPending(tvSubmitStep);

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
