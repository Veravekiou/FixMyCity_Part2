package com.example.fixmycity.ui.adapter;

import android.content.Intent;
import android.net.Uri;
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
        holder.tvLocation.setText("Location: " + report.getLatitude() + ", " + report.getLongitude());
        holder.btnOpenInMaps.setOnClickListener(v -> openReportInMaps(v, report));
    }

    private void openReportInMaps(View view, Report report) {
        String label = Uri.encode(report.getTitle());
        Uri uri = Uri.parse("geo:0,0?q=" + report.getLatitude() + "," + report.getLongitude()
                + "(" + label + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
            view.getContext().startActivity(intent);
        } else {
            Toast.makeText(view.getContext(), "No maps app found", Toast.LENGTH_SHORT).show();
        }
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
