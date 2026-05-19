package com.example.fixmycity;

import com.example.fixmycity.model.Report;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExampleUnitTest {

    // Test 1: Ελέγχει ότι το Report model αποθηκεύει σωστά τα δεδομένα
    @Test
    public void report_StoresDataCorrectly() {
        Report report = new Report(
                "Broken Streetlight",
                "The streetlight on Main St is broken",
                "Broken Streetlight",
                37.9838,
                23.7275,
                true,
                "https://example.com/image.jpg",
                "Pending",
                System.currentTimeMillis(),
                "student@example.com"
        );

        assertEquals("Broken Streetlight", report.getTitle());
        assertEquals("Broken Streetlight", report.getCategory());
        assertEquals("Pending", report.getStatus());
        assertEquals("student@example.com", report.getUserEmail());
        assertTrue(report.isHasImage());
    }

    // Test 2: Ελέγχει ότι το Report χωρίς εικόνα έχει σωστές τιμές
    @Test
    public void report_WithNoImage_HasCorrectDefaults() {
        Report report = new Report(
                "Pothole",
                "Large pothole on side road",
                "Pothole",
                37.9838,
                23.7275,
                false,
                "",
                "Pending",
                System.currentTimeMillis(),
                "student@example.com"
        );

        assertFalse(report.isHasImage());
        assertEquals("", report.getLocalImageUri());
        assertEquals("Pothole", report.getTitle());
    }

    // Test 3: Ελέγχει ότι οι συντεταγμένες αποθηκεύονται σωστά
    @Test
    public void report_StoresCoordinatesCorrectly() {
        double lat = 37.9838;
        double lng = 23.7275;

        Report report = new Report(
                "Test",
                "Test description",
                "Garbage",
                lat,
                lng,
                false,
                "",
                "Pending",
                System.currentTimeMillis(),
                "student@example.com"
        );

        assertEquals(lat, report.getLatitude(), 0.0001);
        assertEquals(lng, report.getLongitude(), 0.0001);
    }
}