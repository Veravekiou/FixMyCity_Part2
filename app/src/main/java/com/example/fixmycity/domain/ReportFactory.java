package com.example.fixmycity.domain;

import android.net.Uri;

import com.example.fixmycity.model.Report;
import com.example.fixmycity.utils.Constants;

public class ReportFactory {

    public Report createFromForm(ReportFormData formData) {
        return createFromForm(formData, "");
    }

    public Report createFromForm(ReportFormData formData, String uploadedImageUrl) {
        Uri imageUri = formData.getImageUri();
        String imageReference = uploadedImageUrl == null ? "" : uploadedImageUrl;
        if (imageReference.isEmpty() && imageUri != null) {
            imageReference = imageUri.toString();
        }

        return new Report(
                formData.getTitle().trim(),
                formData.getDescription().trim(),
                formData.getCategory(),
                formData.getLatitude(),
                formData.getLongitude(),
                formData.getLocationAddress(),
                !imageReference.isEmpty(),
                imageReference,
                Constants.DEFAULT_REPORT_STATUS,
                System.currentTimeMillis(),
                Constants.CURRENT_USER_EMAIL
        );
    }
}
