package com.example.fixmycity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;

public class MainActivity extends AppCompatActivity {

    private Button btnCreateReport, btnViewReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreateReport = findViewById(R.id.btnCreateReport);
        btnViewReports = findViewById(R.id.btnViewReports);

        btnCreateReport.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SubmitReportActivity.class)));

        btnViewReports.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyReportsActivity.class)));
    }
}