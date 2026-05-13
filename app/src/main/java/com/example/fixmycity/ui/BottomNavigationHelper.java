package com.example.fixmycity.ui;

import android.app.Activity;
import android.content.Intent;

import com.example.fixmycity.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class BottomNavigationHelper {

    private BottomNavigationHelper() {
        // Utility class
    }

    public static void setup(Activity activity,
                             BottomNavigationView bottomNavigationView,
                             int selectedItemId) {
        syncSelectedItem(bottomNavigationView, selectedItemId);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == selectedItemId) {
                return true;
            }

            if (itemId == R.id.navHome) {
                open(activity, MainActivity.class);
                return true;
            }

            if (itemId == R.id.navNewReport) {
                open(activity, SubmitReportActivity.class);
                return true;
            }

            if (itemId == R.id.navMyReports) {
                open(activity, MyReportsActivity.class);
                return true;
            }

            return false;
        });
    }

    public static void syncSelectedItem(BottomNavigationView bottomNavigationView, int selectedItemId) {
        if (bottomNavigationView.getSelectedItemId() != selectedItemId) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }
    }

    private static void open(Activity activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }
}
