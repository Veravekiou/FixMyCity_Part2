package com.example.fixmycity.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onError(String message);
    }

    public LocationHelper(Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onError("Location permission not granted");
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        callback.onError("Location not available");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
