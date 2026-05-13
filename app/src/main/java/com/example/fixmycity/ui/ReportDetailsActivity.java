package com.example.fixmycity.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportDetailsActivity extends AppCompatActivity {

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
    private ImageView ivDetailImage;

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
        ivDetailImage = findViewById(R.id.ivDetailImage);

        topAppBar.setNavigationOnClickListener(v -> finish());
        bindReportDetails();
    }

    private void bindReportDetails() {
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String status = getIntent().getStringExtra(EXTRA_STATUS);
        double latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0.0);
        double longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0.0);
        long createdAt = getIntent().getLongExtra(EXTRA_CREATED_AT, 0L);
        boolean hasImage = getIntent().getBooleanExtra(EXTRA_HAS_IMAGE, false);
        String imageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);

        tvDetailTitle.setText(title);
        tvDetailCategory.setText(getString(R.string.report_category_format, category));
        tvDetailStatus.setText(getString(R.string.report_status_format, status));
        tvDetailDate.setText(getString(R.string.report_date_format, formatDate(createdAt)));
        tvDetailDescription.setText(description);
        tvDetailLocation.setText(getString(R.string.report_location_format, latitude, longitude));

        if (hasImage && imageUri != null && !imageUri.trim().isEmpty()) {
            tvDetailNoPhoto.setVisibility(View.GONE);
            ivDetailImage.setImageURI(Uri.parse(imageUri));
        } else {
            tvDetailNoPhoto.setVisibility(View.VISIBLE);
            ivDetailImage.setImageDrawable(null);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }
}
