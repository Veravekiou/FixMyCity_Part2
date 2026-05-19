package com.example.fixmycity.data;

import com.example.fixmycity.model.Report;
import com.example.fixmycity.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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
        db.collection(Constants.REPORTS_COLLECTION)
                .document()
                .set(report)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void cancelReport(String reportId,
                             OnSuccessListener<Void> onSuccess,
                             OnFailureListener onFailure) {
        db.collection(Constants.REPORTS_COLLECTION)
                .document(reportId)
                .update("status", Constants.CANCELLED_REPORT_STATUS)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public interface ReportsCallback {
        void onSuccess(List<Report> reports);
        void onFailure(Exception e);
    }

    public void getReportsByUser(String userEmail, ReportsCallback callback) {
        db.collection(Constants.REPORTS_COLLECTION)
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(mapReports(querySnapshot)))
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllReports(ReportsCallback callback) {
        db.collection(Constants.REPORTS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(mapReports(querySnapshot)))
                .addOnFailureListener(callback::onFailure);
    }

    private List<Report> mapReports(QuerySnapshot querySnapshot) {
        List<Report> reports = new ArrayList<>();

        for (QueryDocumentSnapshot document : querySnapshot) {
            Report report = document.toObject(Report.class);
            report.setId(document.getId());
            reports.add(report);
        }

        reports.add(createDemoResolvedReport());

        return reports;
    }

    private Report createDemoResolvedReport() {
        Report report = new Report(
                "Repaired streetlight outside the library",
                "The streetlight was reported as broken and has now been repaired by the city maintenance team.",
                "Broken Streetlight",
                35.3387,
                25.1442,
                "Vikela Municipal Library, Iraklio 712 02, Greece",
                false,
                null,
                "Resolved",
                System.currentTimeMillis() - 86400000L,
                Constants.CURRENT_USER_EMAIL
        );
        report.setId("demo-resolved-report");
        return report;
    }
}
