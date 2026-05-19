package com.example.fixmycity.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;

    public interface ReportLocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationFailed(Exception exception);
    }

    public LocationHelper(Activity activity) {
        this.activity = activity;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    public void getCurrentLocation(ReportLocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationFailed(new SecurityException("Location permission missing"));
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                        } else {
                            requestSingleLocationUpdate(callback);
                        }
                    })
                    .addOnFailureListener(callback::onLocationFailed);
        } catch (SecurityException e) {
            callback.onLocationFailed(e);
        }
    }

    private void requestSingleLocationUpdate(ReportLocationCallback callback) {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L
        )
                .setMaxUpdates(1)
                .setWaitForAccurateLocation(false)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                } else {
                    callback.onLocationFailed(new IllegalStateException("Could not detect current location"));
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            callback.onLocationFailed(e);
        }
    }
}
