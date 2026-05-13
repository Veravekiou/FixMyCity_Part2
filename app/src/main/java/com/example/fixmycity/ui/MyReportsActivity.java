package com.example.fixmycity.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.ui.adapter.ReportAdapter;
import com.example.fixmycity.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerReports;
    private LinearLayout layoutLoadingReports;
    private LinearLayout layoutEmptyReports;
    private LinearLayout layoutErrorReports;
    private TextView tvEmptyReportsTitle;
    private TextView tvEmptyReports;
    private TextView tvReportsError;
    private Button btnRetryReports;
    private SwipeRefreshLayout swipeRefreshReports;
    private ChipGroup chipGroupReportFilters;
    private ReportAdapter reportAdapter;
    private ReportRepository reportRepository;
    private final List<Report> allReports = new ArrayList<>();
    private ReportFilter selectedFilter = ReportFilter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        recyclerReports = findViewById(R.id.recyclerReports);
        layoutLoadingReports = findViewById(R.id.layoutLoadingReports);
        layoutEmptyReports = findViewById(R.id.layoutEmptyReports);
        layoutErrorReports = findViewById(R.id.layoutErrorReports);
        tvEmptyReportsTitle = findViewById(R.id.tvEmptyReportsTitle);
        tvEmptyReports = findViewById(R.id.tvEmptyReports);
        tvReportsError = findViewById(R.id.tvReportsError);
        btnRetryReports = findViewById(R.id.btnRetryReports);
        swipeRefreshReports = findViewById(R.id.swipeRefreshReports);
        chipGroupReportFilters = findViewById(R.id.chipGroupReportFilters);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));

        reportAdapter = new ReportAdapter(new ArrayList<>());
        recyclerReports.setAdapter(reportAdapter);

        reportRepository = new ReportRepository();

        BottomNavigationHelper.setup(this, bottomNavigation, R.id.navMyReports);
        swipeRefreshReports.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefreshReports.setOnRefreshListener(() -> loadReports(false));
        chipGroupReportFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }

            selectedFilter = getFilterForChip(checkedIds.get(0));
            applySelectedFilter();
        });
        btnRetryReports.setOnClickListener(v -> loadReports(true));
        loadReports(true);
    }

    private void loadReports(boolean showLoadingState) {
        if (showLoadingState) {
            showLoadingState();
        } else {
            swipeRefreshReports.setRefreshing(true);
        }

        reportRepository.getAllReports(new ReportRepository.ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                swipeRefreshReports.setRefreshing(false);
                allReports.clear();
                allReports.addAll(reports);

                applySelectedFilter();
            }

            @Override
            public void onFailure(Exception e) {
                swipeRefreshReports.setRefreshing(false);
                showErrorState(getRepositoryErrorMessage(e));
            }
        });
    }

    private void showLoadingState() {
        chipGroupReportFilters.setVisibility(View.GONE);
        layoutLoadingReports.setVisibility(View.VISIBLE);
        layoutEmptyReports.setVisibility(View.GONE);
        layoutErrorReports.setVisibility(View.GONE);
        recyclerReports.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean filtered) {
        chipGroupReportFilters.setVisibility(allReports.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyReportsTitle.setText(filtered
                ? getString(R.string.reports_empty_filtered_title)
                : getString(R.string.reports_empty_title));
        tvEmptyReports.setText(filtered
                ? getString(R.string.reports_empty_filtered)
                : getString(R.string.reports_empty));
        layoutLoadingReports.setVisibility(View.GONE);
        layoutEmptyReports.setVisibility(View.VISIBLE);
        layoutErrorReports.setVisibility(View.GONE);
        recyclerReports.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        chipGroupReportFilters.setVisibility(View.GONE);
        tvReportsError.setText(message);
        layoutLoadingReports.setVisibility(View.GONE);
        layoutEmptyReports.setVisibility(View.GONE);
        layoutErrorReports.setVisibility(View.VISIBLE);
        recyclerReports.setVisibility(View.GONE);
    }

    private void showContentState() {
        chipGroupReportFilters.setVisibility(View.VISIBLE);
        layoutLoadingReports.setVisibility(View.GONE);
        layoutEmptyReports.setVisibility(View.GONE);
        layoutErrorReports.setVisibility(View.GONE);
        recyclerReports.setVisibility(View.VISIBLE);
    }

    private String getRepositoryErrorMessage(Exception exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            return getString(R.string.reports_load_failed);
        }

        return getString(R.string.reports_load_failed_detail, detail);
    }

    private void applySelectedFilter() {
        List<Report> filteredReports = filterReports(allReports, selectedFilter);
        reportAdapter.submitList(filteredReports);

        if (filteredReports.isEmpty()) {
            showEmptyState(!allReports.isEmpty());
        } else {
            showContentState();
        }
    }

    private List<Report> filterReports(List<Report> reports, ReportFilter filter) {
        List<Report> filteredReports = new ArrayList<>();

        for (Report report : reports) {
            if (matchesFilter(report, filter)) {
                filteredReports.add(report);
            }
        }

        return filteredReports;
    }

    private boolean matchesFilter(Report report, ReportFilter filter) {
        if (filter == ReportFilter.ALL) {
            return true;
        }

        String status = report.getStatus();
        if (status == null) {
            return false;
        }

        if (filter == ReportFilter.PENDING) {
            return Constants.DEFAULT_REPORT_STATUS.equalsIgnoreCase(status);
        }

        return "Resolved".equalsIgnoreCase(status);
    }

    private ReportFilter getFilterForChip(int checkedChipId) {
        if (checkedChipId == R.id.chipFilterPending) {
            return ReportFilter.PENDING;
        }

        if (checkedChipId == R.id.chipFilterResolved) {
            return ReportFilter.RESOLVED;
        }

        return ReportFilter.ALL;
    }

    private enum ReportFilter {
        ALL,
        PENDING,
        RESOLVED
    }
}
