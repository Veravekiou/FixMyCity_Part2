package com.example.fixmycity.domain;

import android.net.Uri;

public class ReportFormData {

    private final String title;
    private final String description;
    private final String category;
    private final double latitude;
    private final double longitude;
    private final boolean locationSelected;
    private final Uri imageUri;

    public ReportFormData(String title,
                          String description,
                          String category,
                          double latitude,
                          double longitude,
                          boolean locationSelected,
                          Uri imageUri) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationSelected = locationSelected;
        this.imageUri = imageUri;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isLocationSelected() {
        return locationSelected;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
