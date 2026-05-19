package com.example.fixmycity.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmycity.R;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.ui.ReportDetailsActivity;
import com.example.fixmycity.ui.ReportsMapActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Report> reportList;

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    public void submitList(List<Report> reports) {
        reportList.clear();
        reportList.addAll(reports);
        notifyDataSetChanged();
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
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(report.getTitle());
        holder.tvCategory.setText(context.getString(R.string.report_category_format, report.getCategory()));
        holder.tvStatus.setText(context.getString(R.string.report_status_format, report.getStatus()));
        styleStatus(context, holder.tvStatus, report.getStatus());
        holder.tvDescription.setText(report.getDescription());
        holder.tvDate.setText(context.getString(R.string.report_date_format, formatDate(report.getCreatedAt())));

        String locationText = report.getLocationAddress() != null
                && !report.getLocationAddress().trim().isEmpty()
                ? report.getLocationAddress()
                : context.getString(R.string.report_location_format, report.getLatitude(), report.getLongitude());
        holder.tvLocation.setText(locationText);

        String imageUri = report.getLocalImageUri();
        if (imageUri != null && !imageUri.trim().isEmpty()) {
            holder.ivReportImage.setVisibility(View.VISIBLE);
            holder.ivReportImage.setImageURI(Uri.parse(imageUri));
        } else {
            holder.ivReportImage.setVisibility(View.GONE);
        }

        holder.btnOpenInMaps.setOnClickListener(v -> openReportInAppMap(v, report));
        holder.itemView.setOnClickListener(v -> openReportDetails(context, report));
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvStatus;
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvLocation;
        private final ImageView ivReportImage;
        private final Button btnOpenInMaps;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportTitle);
            tvCategory = itemView.findViewById(R.id.tvReportCategory);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
            tvDescription = itemView.findViewById(R.id.tvReportDescription);
            tvDate = itemView.findViewById(R.id.tvReportDate);
            tvLocation = itemView.findViewById(R.id.tvReportLocation);
            ivReportImage = itemView.findViewById(R.id.ivReportImage);
            btnOpenInMaps = itemView.findViewById(R.id.btnOpenInMaps);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

    private void openReportDetails(Context context, Report report) {
        Intent intent = new Intent(context, ReportDetailsActivity.class);
        intent.putExtra(ReportDetailsActivity.EXTRA_REPORT_ID, report.getId());
        intent.putExtra(ReportDetailsActivity.EXTRA_TITLE, report.getTitle());
        intent.putExtra(ReportDetailsActivity.EXTRA_DESCRIPTION, report.getDescription());
        intent.putExtra(ReportDetailsActivity.EXTRA_CATEGORY, report.getCategory());
        intent.putExtra(ReportDetailsActivity.EXTRA_STATUS, report.getStatus());
        intent.putExtra(ReportDetailsActivity.EXTRA_LATITUDE, report.getLatitude());
        intent.putExtra(ReportDetailsActivity.EXTRA_LONGITUDE, report.getLongitude());
        intent.putExtra(ReportDetailsActivity.EXTRA_CREATED_AT, report.getCreatedAt());
        intent.putExtra(ReportDetailsActivity.EXTRA_HAS_IMAGE, report.isHasImage());
        intent.putExtra(ReportDetailsActivity.EXTRA_IMAGE_URI, report.getLocalImageUri());
        context.startActivity(intent);
    }

    private void openReportInAppMap(View view, Report report) {
        if (!hasValidLocation(report)) {
            Toast.makeText(view.getContext(), R.string.report_no_location, Toast.LENGTH_SHORT).show();
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

    private void styleStatus(Context context, TextView statusView, String status) {
        if ("Resolved".equalsIgnoreCase(status)) {
            statusView.setBackgroundResource(R.drawable.bg_status_resolved);
            statusView.setTextColor(context.getColor(R.color.primary_dark));
            return;
        }

        if ("Cancelled".equalsIgnoreCase(status)) {
            statusView.setBackgroundResource(R.drawable.bg_status_cancelled);
            statusView.setTextColor(context.getColor(R.color.error));
            return;
        }

        statusView.setBackgroundResource(R.drawable.bg_status_pending);
        statusView.setTextColor(context.getColor(R.color.primary_dark));
    }
}
