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
    private TextView tvPendingReportsCount, tvSubmittedReportsCount;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreateReport = findViewById(R.id.btnCreateReport);
        btnViewReports = findViewById(R.id.btnViewReports);
        tvPendingReportsCount = findViewById(R.id.tvPendingReportsCount);
        tvSubmittedReportsCount = findViewById(R.id.tvSubmittedReportsCount);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        reportRepository = new ReportRepository();

        btnCreateReport.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SubmitReportActivity.class)));

        btnViewReports.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyReportsActivity.class)));

        BottomNavigationHelper.setup(this, bottomNavigation, R.id.navHome);
        loadDashboardSummary();
    }

    private void loadDashboardSummary() {
        reportRepository.getAllReports(new ReportRepository.ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                int pendingReports = countPendingReports(reports);
                tvPendingReportsCount.setText(String.valueOf(pendingReports));
                tvSubmittedReportsCount.setText(String.valueOf(reports.size()));
            }

            @Override
            public void onFailure(Exception e) {
                tvPendingReportsCount.setText("0");
                tvSubmittedReportsCount.setText("0");
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
}
