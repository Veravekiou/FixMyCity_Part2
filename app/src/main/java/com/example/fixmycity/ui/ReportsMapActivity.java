package com.example.fixmycity.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.model.Report;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class ReportsMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_map);

        reportRepository = new ReportRepository();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.reportsMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        loadReportsOnMap();
    }

    private void loadReportsOnMap() {
        reportRepository.getAllReports(new ReportRepository.ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                showReports(reports);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ReportsMapActivity.this,
                        "Failed to load reports: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showReports(List<Report> reports) {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();

        LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
        int markerCount = 0;

        for (Report report : reports) {
            LatLng position = new LatLng(report.getLatitude(), report.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(report.getTitle())
                    .snippet(report.getCategory() + " - " + report.getStatus()));
            boundsBuilder.include(position);
            markerCount++;
        }

        if (markerCount == 0) {
            Toast.makeText(this, "No reports with location found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (markerCount == 1) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    boundsBuilder.build().getCenter(), 15f));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
        }
    }
}
