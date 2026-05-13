package com.example.fixmycity.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.ui.adapter.ReportAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerReports;
    private TextView tvEmptyReports;
    private ReportAdapter reportAdapter;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        recyclerReports = findViewById(R.id.recyclerReports);
        tvEmptyReports = findViewById(R.id.tvEmptyReports);
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));

        reportAdapter = new ReportAdapter(new ArrayList<>());
        recyclerReports.setAdapter(reportAdapter);

        reportRepository = new ReportRepository();

        loadReports();
    }

    private void loadReports() {
        reportRepository.getAllReports(new ReportRepository.ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                Toast.makeText(MyReportsActivity.this,
                        "Reports found: " + reports.size(),
                        Toast.LENGTH_LONG).show();

                reportAdapter.submitList(reports);
                updateEmptyState(reports.isEmpty());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MyReportsActivity.this,
                        getRepositoryErrorMessage(e),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        tvEmptyReports.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerReports.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private String getRepositoryErrorMessage(Exception exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            return "Failed to load reports. Please try again.";
        }

        return "Failed to load reports: " + detail;
    }
}
