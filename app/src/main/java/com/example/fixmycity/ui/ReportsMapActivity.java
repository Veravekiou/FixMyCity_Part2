package com.example.fixmycity.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fixmycity.R;
import com.example.fixmycity.data.ReportRepository;
import com.example.fixmycity.model.Report;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.util.List;

public class ReportsMapActivity extends AppCompatActivity {

    public static final String EXTRA_FOCUS_REPORT = "extra_focus_report";
    public static final String EXTRA_REPORT_LATITUDE = "extra_report_latitude";
    public static final String EXTRA_REPORT_LONGITUDE = "extra_report_longitude";
    public static final String EXTRA_REPORT_TITLE = "extra_report_title";
    public static final String EXTRA_REPORT_CATEGORY = "extra_report_category";
    public static final String EXTRA_REPORT_STATUS = "extra_report_status";
    public static final String EXTRA_REPORT_ADDRESS = "extra_report_address";

    private static final double DEFAULT_LATITUDE = 37.9838;
    private static final double DEFAULT_LONGITUDE = 23.7275;

    private WebView reportsMap;
    private ReportRepository reportRepository;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_map);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        reportsMap = findViewById(R.id.reportsMap);
        reportRepository = new ReportRepository();
        topAppBar.setNavigationOnClickListener(v -> finish());

        WebSettings settings = reportsMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        reportsMap.setWebViewClient(new WebViewClient());

        loadMapHtml(buildMapHtml("[]"));
        if (getIntent().getBooleanExtra(EXTRA_FOCUS_REPORT, false)) {
            loadFocusedReportOnMap();
        } else {
            loadReportsOnMap();
        }
    }

    private void loadFocusedReportOnMap() {
        double latitude = getIntent().getDoubleExtra(EXTRA_REPORT_LATITUDE, 0.0);
        double longitude = getIntent().getDoubleExtra(EXTRA_REPORT_LONGITUDE, 0.0);

        if (!hasValidLocation(latitude, longitude)) {
            Toast.makeText(this, "No location found for this report", Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle(R.string.reports_map_report_title);
        loadMapHtml(buildMapHtml(buildSingleReportMarkerJson(latitude, longitude)));
    }

    private void loadReportsOnMap() {
        reportRepository.getAllReports(new ReportRepository.ReportsCallback() {
            @Override
            public void onSuccess(List<Report> reports) {
                String reportMarkers = buildReportMarkersJson(reports);
                loadMapHtml(buildMapHtml(reportMarkers));

                if ("[]".equals(reportMarkers)) {
                    Toast.makeText(
                            ReportsMapActivity.this,
                            "No reports with location found",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        ReportsMapActivity.this,
                        "Failed to load reports: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String buildReportMarkersJson(List<Report> reports) {
        StringBuilder markersJson = new StringBuilder("[");
        int markerCount = 0;

        for (Report report : reports) {
            if (!hasValidLocation(report)) {
                continue;
            }

            if (markerCount > 0) {
                markersJson.append(",");
            }

            markersJson.append("{")
                    .append("\"lat\":").append(report.getLatitude()).append(",")
                    .append("\"lng\":").append(report.getLongitude()).append(",")
                    .append("\"title\":").append(JSONObject.quote(nullToEmpty(report.getTitle()))).append(",")
                    .append("\"category\":").append(JSONObject.quote(nullToEmpty(report.getCategory()))).append(",")
                    .append("\"status\":").append(JSONObject.quote(nullToEmpty(report.getStatus()))).append(",")
                    .append("\"address\":").append(JSONObject.quote(nullToEmpty(report.getLocationAddress())))
                    .append("}");
            markerCount++;
        }

        markersJson.append("]");
        return markersJson.toString();
    }

    private String buildSingleReportMarkerJson(double latitude, double longitude) {
        return "[{"
                + "\"lat\":" + latitude + ","
                + "\"lng\":" + longitude + ","
                + "\"title\":" + JSONObject.quote(nullToEmpty(getIntent().getStringExtra(EXTRA_REPORT_TITLE))) + ","
                + "\"category\":" + JSONObject.quote(nullToEmpty(getIntent().getStringExtra(EXTRA_REPORT_CATEGORY))) + ","
                + "\"status\":" + JSONObject.quote(nullToEmpty(getIntent().getStringExtra(EXTRA_REPORT_STATUS))) + ","
                + "\"address\":" + JSONObject.quote(nullToEmpty(getIntent().getStringExtra(EXTRA_REPORT_ADDRESS)))
                + "}]";
    }

    private String buildMapHtml(String reportMarkersJson) {
        return "<!doctype html>"
                + "<html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'>"
                + "<style>"
                + "html,body,#map{height:100%;margin:0;}"
                + ".leaflet-control-attribution{font-size:10px;}"
                + ".report-arrow{width:40px;height:48px;position:relative;}"
                + ".report-arrow:before{content:'';position:absolute;left:11px;top:0;"
                + "border-left:9px solid transparent;border-right:9px solid transparent;"
                + "border-bottom:30px solid #d50000;transform:rotate(180deg);}"
                + ".report-arrow:after{content:'';position:absolute;left:15px;top:21px;"
                + "width:10px;height:20px;background:#d50000;}"
                + ".popup-title{font-weight:bold;margin-bottom:4px;}"
                + ".popup-line{margin-top:2px;}"
                + "</style>"
                + "</head><body>"
                + "<div id='map'></div>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<script>"
                + "var reports=" + reportMarkersJson + ";"
                + "var map=L.map('map').setView([" + DEFAULT_LATITUDE + "," + DEFAULT_LONGITUDE + "],13);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{"
                + "maxZoom:19,attribution:'OpenStreetMap'}).addTo(map);"
                + "var reportIcon=L.divIcon({className:'',html:'<div class=\"report-arrow\"></div>',"
                + "iconSize:[40,48],iconAnchor:[20,48],popupAnchor:[0,-48]});"
                + "var bounds=[];"
                + "function esc(value){return String(value||'').replace(/[&<>'\"]/g,function(c){"
                + "return {'&':'&amp;','<':'&lt;','>':'&gt;',\"'\":'&#39;','\"':'&quot;'}[c];});}"
                + "reports.forEach(function(report){"
                + "var position=[report.lat,report.lng];"
                + "var address=report.address?'<div class=\"popup-line\">'+esc(report.address)+'</div>':'';"
                + "var popup='<div class=\"popup-title\">'+esc(report.title||'Report')+'</div>'"
                + "+'<div class=\"popup-line\">'+esc(report.category)+' - '+esc(report.status)+'</div>'+address;"
                + "var marker=L.marker(position,{icon:reportIcon}).addTo(map).bindPopup(popup);"
                + "marker.on('click',function(){map.setView(position,18,{animate:true});});"
                + "bounds.push(position);"
                + "});"
                + "if(bounds.length===1){map.setView(bounds[0],17);}"
                + "else if(bounds.length>1){map.fitBounds(bounds,{padding:[40,40]});}"
                + "</script></body></html>";
    }

    private void loadMapHtml(String html) {
        reportsMap.loadDataWithBaseURL(
                "https://fixmycity.local/",
                html,
                "text/html",
                "UTF-8",
                null
        );
    }

    private boolean hasValidLocation(Report report) {
        return hasValidLocation(report.getLatitude(), report.getLongitude());
    }

    private boolean hasValidLocation(double latitude, double longitude) {
        return latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180
                && !(latitude == 0.0 && longitude == 0.0);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    protected void onDestroy() {
        reportsMap.destroy();
        super.onDestroy();
    }
}
