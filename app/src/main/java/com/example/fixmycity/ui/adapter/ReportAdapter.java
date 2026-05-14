package com.example.fixmycity.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmycity.R;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.ui.ReportsMapActivity;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Report> reportList;

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.tvTitle.setText(report.getTitle());
        holder.tvCategory.setText("Issue Type: " + report.getCategory());
        holder.tvStatus.setText("Current Status: " + report.getStatus());
        String locationText = report.getLocationAddress() != null
                && !report.getLocationAddress().trim().isEmpty()
                ? report.getLocationAddress()
                : "Open the map to view the exact location";
        holder.tvLocation.setText("Location: " + locationText);
        holder.btnOpenInMaps.setOnClickListener(v -> openReportInAppMap(v, report));
    }

    private void openReportInAppMap(View view, Report report) {
        if (!hasValidLocation(report)) {
            Toast.makeText(view.getContext(), "No location found for this report", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(view.getContext(), ReportsMapActivity.class);
        intent.putExtra(ReportsMapActivity.EXTRA_FOCUS_REPORT, true);
        intent.putExtra(ReportsMapActivity.EXTRA_REPORT_LATITUDE, report.getLatitude());
        intent.putExtra(ReportsMapActivity.EXTRA_REPORT_LONGITUDE, report.getLongitude());
        intent.putExtra(ReportsMapActivity.EXTRA_REPORT_TITLE, report.getTitle());
        intent.putExtra(ReportsMapActivity.EXTRA_REPORT_CATEGORY, report.getCategory());
        intent.putExtra(ReportsMapActivity.EXTRA_REPORT_STATUS, report.getStatus());
        intent.putExtra(ReportsMapActivity.EXTRA_REPORT_ADDRESS, report.getLocationAddress());
        view.getContext().startActivity(intent);
    }

    private boolean hasValidLocation(Report report) {
        double latitude = report.getLatitude();
        double longitude = report.getLongitude();
        return latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180
                && !(latitude == 0.0 && longitude == 0.0);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvStatus, tvLocation;
        Button btnOpenInMaps;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportTitle);
            tvCategory = itemView.findViewById(R.id.tvReportCategory);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
            tvLocation = itemView.findViewById(R.id.tvReportLocation);
            btnOpenInMaps = itemView.findViewById(R.id.btnOpenInMaps);
        }
    }
}
