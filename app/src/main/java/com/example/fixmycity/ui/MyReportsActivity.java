package com.example.fixmycity.ui;

import android.os.Bundle;
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
    private ReportAdapter reportAdapter;
    private List<Report> reportList;
    private ReportRepository reportRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        recyclerReports = findViewById(R.id.recyclerReports);
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));

        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(reportList);
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

                reportList.clear();
                reportList.addAll(reports);
                reportAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MyReportsActivity.this,
                        "Failed to load reports: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }
}