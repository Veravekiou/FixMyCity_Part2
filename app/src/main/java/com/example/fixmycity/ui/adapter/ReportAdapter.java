package com.example.fixmycity.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmycity.R;
import com.example.fixmycity.model.Report;
import com.example.fixmycity.ui.ReportDetailsActivity;

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
        holder.tvDescription.setText(report.getDescription());
        holder.tvDate.setText(context.getString(R.string.report_date_format, formatDate(report.getCreatedAt())));
        holder.tvLocation.setText(context.getString(
                R.string.report_location_format,
                report.getLatitude(),
                report.getLongitude()
        ));
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

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportTitle);
            tvCategory = itemView.findViewById(R.id.tvReportCategory);
            tvStatus = itemView.findViewById(R.id.tvReportStatus);
            tvDescription = itemView.findViewById(R.id.tvReportDescription);
            tvDate = itemView.findViewById(R.id.tvReportDate);
            tvLocation = itemView.findViewById(R.id.tvReportLocation);
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

    private void openReportDetails(Context context, Report report) {
        Intent intent = new Intent(context, ReportDetailsActivity.class);
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
}
