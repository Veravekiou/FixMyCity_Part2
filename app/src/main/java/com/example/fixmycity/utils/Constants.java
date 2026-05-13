package com.example.fixmycity.utils;

public final class Constants {

    public static final String REPORTS_COLLECTION = "reports";
    public static final String DEFAULT_REPORT_STATUS = "Pending";
    public static final String CANCELLED_REPORT_STATUS = "Cancelled";
    public static final String CURRENT_USER_EMAIL = "student@example.com";
    public static final String CATEGORY_PLACEHOLDER = "Select issue";

    public static final String[] REPORT_CATEGORIES = {
            CATEGORY_PLACEHOLDER,
            "Pothole",
            "Broken Streetlight",
            "Garbage",
            "Sidewalk Damage",
            "Vandalism"
    };

    private Constants() {
        // Utility class
    }
}
