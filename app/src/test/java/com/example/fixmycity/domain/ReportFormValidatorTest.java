package com.example.fixmycity.domain;

import com.example.fixmycity.domain.ReportFormValidator.Field;
import com.example.fixmycity.domain.ReportFormValidator.ValidationResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ReportFormValidatorTest {

    private final ReportFormValidator validator = new ReportFormValidator();

    @Test
    public void validate_returnsTitleError_whenTitleIsBlank() {
        ValidationResult result = validator.validate(formData("", "Broken light", true));

        assertFalse(result.isValid());
        assertEquals(Field.TITLE, result.getField());
    }

    @Test
    public void validate_returnsDescriptionError_whenDescriptionIsBlank() {
        ValidationResult result = validator.validate(formData("Street light", " ", true));

        assertFalse(result.isValid());
        assertEquals(Field.DESCRIPTION, result.getField());
    }

    @Test
    public void validate_returnsLocationError_whenLocationIsMissing() {
        ValidationResult result = validator.validate(formData("Street light", "Broken light", false));

        assertFalse(result.isValid());
        assertEquals(Field.LOCATION, result.getField());
    }

    @Test
    public void validate_returnsSuccess_whenRequiredFieldsAreValid() {
        ValidationResult result = validator.validate(formData("Street light", "Broken light", true));

        assertTrue(result.isValid());
        assertEquals(Field.NONE, result.getField());
    }

    private ReportFormData formData(String title, String description, boolean locationSelected) {
        return new ReportFormData(
                title,
                description,
                "Broken Streetlight",
                38.0,
                23.0,
                locationSelected,
                null
        );
    }
}
