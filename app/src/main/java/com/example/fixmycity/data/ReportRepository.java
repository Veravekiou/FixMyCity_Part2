package com.example.fixmycity.data;

import com.example.fixmycity.model.Report;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    private final FirebaseFirestore db;

    public ReportRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveReport(Report report,
                           OnSuccessListener<Void> onSuccess,
                           OnFailureListener onFailure) {
        db.collection("reports")
                .document()
                .set(report)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public interface ReportsCallback {
        void onSuccess(List<Report> reports);
        void onFailure(Exception e);
    }

    public void getReportsByUser(String userEmail, ReportsCallback callback) {
        db.collection("reports")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Report> reports = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Report report = document.toObject(Report.class);
                        reports.add(report);
                    }

                    callback.onSuccess(reports);
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void getAllReports(ReportsCallback callback) {
        db.collection("reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Report> reports = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Report report = document.toObject(Report.class);
                        reports.add(report);
                    }

                    callback.onSuccess(reports);
                })
                .addOnFailureListener(callback::onFailure);
    }
}