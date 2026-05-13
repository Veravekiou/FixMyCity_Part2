package com.example.fixmycity.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_REPORT_ID = "extra_report_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_CREATED_AT = "extra_created_at";
    public static final String EXTRA_HAS_IMAGE = "extra_has_image";
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";

    private TextView tvDetailTitle;
    private TextView tvDetailCategory;
    private TextView tvDetailStatus;
    private TextView tvDetailDate;
    private TextView tvDetailDescription;
    private TextView tvDetailLocation;
    private TextView tvDetailNoPhoto;
    private Button btnCancelReport;
    private ImageView ivDetailImage;
    private ReportRepository reportRepository;
    private String reportId;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailNoPhoto = findViewById(R.id.tvDetailNoPhoto);
        btnCancelReport = findViewById(R.id.btnCancelReport);
        ivDetailImage = findViewById(R.id.ivDetailImage);
        reportRepository = new ReportRepository();

        topAppBar.setNavigationOnClickListener(v -> finish());
        btnCancelReport.setOnClickListener(v -> showCancelConfirmation());
        bindReportDetails();
    }

    private void bindReportDetails() {
        reportId = getIntent().getStringExtra(EXTRA_REPORT_ID);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        status = getIntent().getStringExtra(EXTRA_STATUS);
        double latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0.0);
        double longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0.0);
        long createdAt = getIntent().getLongExtra(EXTRA_CREATED_AT, 0L);
        boolean hasImage = getIntent().getBooleanExtra(EXTRA_HAS_IMAGE, false);
        String imageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);

        tvDetailTitle.setText(title);
        tvDetailCategory.setText(getString(R.string.report_category_format, category));
        tvDetailStatus.setText(getString(R.string.report_status_format, status));
        styleStatus();
        tvDetailDate.setText(getString(R.string.report_date_format, formatDate(createdAt)));
        tvDetailDescription.setText(description);
        tvDetailLocation.setText(getString(R.string.report_location_format, latitude, longitude));
        updateCancelVisibility();

        if (hasImage && imageUri != null && !imageUri.trim().isEmpty()) {
            tvDetailNoPhoto.setVisibility(View.GONE);
            ivDetailImage.setImageURI(Uri.parse(imageUri));
        } else {
            tvDetailNoPhoto.setVisibility(View.VISIBLE);
            ivDetailImage.setImageDrawable(null);
        }
    }

    private void updateCancelVisibility() {
        boolean canCancel = reportId != null
                && !reportId.trim().isEmpty()
                && Constants.DEFAULT_REPORT_STATUS.equalsIgnoreCase(status);
        btnCancelReport.setVisibility(canCancel ? View.VISIBLE : View.GONE);
    }

    private void showCancelConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.report_details_cancel_title)
                .setMessage(R.string.report_details_cancel_message)
                .setNegativeButton(R.string.report_details_cancel_dismiss, null)
                .setPositiveButton(R.string.report_details_cancel_confirm, (dialog, which) -> cancelReport())
                .show();
    }

    private void cancelReport() {
        btnCancelReport.setEnabled(false);

        reportRepository.cancelReport(
                reportId,
                unused -> {
                    status = Constants.CANCELLED_REPORT_STATUS;
                    tvDetailStatus.setText(getString(R.string.report_status_format, status));
                    styleStatus();
                    updateCancelVisibility();
                    Toast.makeText(this, R.string.report_details_cancelled, Toast.LENGTH_SHORT).show();
                },
                e -> {
                    btnCancelReport.setEnabled(true);
                    Toast.makeText(this, getCancelErrorMessage(e), Toast.LENGTH_LONG).show();
                }
        );
    }

    private String getCancelErrorMessage(Exception exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            return getString(R.string.report_details_cancel_failed);
        }

        return getString(R.string.report_details_cancel_failed_detail, detail);
    }

    private void styleStatus() {
        if ("Resolved".equalsIgnoreCase(status)) {
            tvDetailStatus.setBackgroundResource(R.drawable.bg_status_resolved);
            tvDetailStatus.setTextColor(getColor(R.color.primary_dark));
            return;
        }

        if (Constants.CANCELLED_REPORT_STATUS.equalsIgnoreCase(status)) {
            tvDetailStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
            tvDetailStatus.setTextColor(getColor(R.color.error));
            return;
        }

        tvDetailStatus.setBackgroundResource(R.drawable.bg_status_pending);
        tvDetailStatus.setTextColor(getColor(R.color.primary_dark));
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }
}
