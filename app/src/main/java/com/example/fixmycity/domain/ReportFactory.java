package com.example.fixmycity.domain;

import android.net.Uri;

import com.example.fixmycity.model.Report;
import com.example.fixmycity.utils.Constants;

public class ReportFactory {

    public Report createFromForm(ReportFormData formData) {
        Uri imageUri = formData.getImageUri();
        boolean hasImage = imageUri != null;

        return new Report(
                formData.getTitle().trim(),
                formData.getDescription().trim(),
                formData.getCategory(),
                formData.getLatitude(),
                formData.getLongitude(),
                hasImage,
                hasImage ? imageUri.toString() : "",
                Constants.DEFAULT_REPORT_STATUS,
                System.currentTimeMillis(),
                Constants.CURRENT_USER_EMAIL
        );
    }
}
