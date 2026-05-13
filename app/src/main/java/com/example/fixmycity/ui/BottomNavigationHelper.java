package com.example.fixmycity.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.fixmycity.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class BottomNavigationHelper {

    private BottomNavigationHelper() {
        // Utility class
    }

    public static void setup(Activity activity,
                             BottomNavigationView bottomNavigationView,
                             int selectedItemId) {
        if (selectedItemId != View.NO_ID) {
            syncSelectedItem(bottomNavigationView, selectedItemId);
        }

        View createReportButton = activity.findViewById(R.id.btnNavCreateReport);

        if (createReportButton != null) {
            createReportButton.setOnClickListener(v -> open(activity, SubmitReportActivity.class));
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (selectedItemId != View.NO_ID && itemId == selectedItemId) {
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
        if (selectedItemId == View.NO_ID) {
            bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
            return;
        }

        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

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
