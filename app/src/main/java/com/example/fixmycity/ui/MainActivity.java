package com.example.fixmycity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnCreateReport, btnViewReports;
    private TextView tvPendingReportsCount, tvSubmittedReportsCount, tvResolvedReportsCount,
            tvDashboardStatus;
    private BottomNavigationView bottomNavigation;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreateReport = findViewById(R.id.btnCreateReport);
        btnViewReports = findViewById(R.id.btnViewReports);
        tvPendingReportsCount = findViewById(R.id.tvPendingReportsCount);
        tvSubmittedReportsCount = findViewById(R.id.tvSubmittedReportsCount);
        tvResolvedReportsCount = findViewById(R.id.tvResolvedReportsCount);
        tvDashboardStatus = findViewById(R.id.tvDashboardStatus);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        reportRepository = new ReportRepository();

        btnCreateReport.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SubmitReportActivity.class)));

        btnViewReports.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyReportsActivity.class)));

        BottomNavigationHelper.setup(this, bottomNavigation, R.id.navHome);
        loadDashboardSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationHelper.syncSelectedItem(bottomNavigation, R.id.navHome);
    }

    private void loadDashboardSummary() {
        reportRepository.getAllReports(new ReportRepository.ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                int pendingReports = countPendingReports(reports);
                int resolvedReports = countResolvedReports(reports);
                tvPendingReportsCount.setText(String.valueOf(pendingReports));
                tvSubmittedReportsCount.setText(String.valueOf(reports.size()));
                tvResolvedReportsCount.setText(String.valueOf(resolvedReports));
                tvDashboardStatus.setText(R.string.home_summary_ready);
            }

            @Override
            public void onFailure(Exception e) {
                tvPendingReportsCount.setText("0");
                tvSubmittedReportsCount.setText("0");
                tvResolvedReportsCount.setText("0");
                tvDashboardStatus.setText(R.string.home_summary_error);
            }
        });
    }

    private int countPendingReports(List<Report> reports) {
        int pendingReports = 0;

        for (Report report : reports) {
            if (Constants.DEFAULT_REPORT_STATUS.equalsIgnoreCase(report.getStatus())) {
                pendingReports++;
            }
        }

        return pendingReports;
    }

    private int countResolvedReports(List<Report> reports) {
        int resolvedReports = 0;

        for (Report report : reports) {
            if ("Resolved".equalsIgnoreCase(report.getStatus())) {
                resolvedReports++;
            }
        }

        return resolvedReports;
    }
}
