package com.example.fixmycity.domain;

public class ReportFormValidator {

    public ValidationResult validate(ReportFormData formData) {
        if (isBlank(formData.getTitle())) {
            return ValidationResult.error(Field.TITLE, "Title is required");
        }

        if (isBlank(formData.getDescription())) {
            return ValidationResult.error(Field.DESCRIPTION, "Description is required");
        }

        if (!formData.isLocationSelected()) {
            return ValidationResult.error(Field.LOCATION, "Please get location first");
        }

        return ValidationResult.success();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public enum Field {
        NONE,
        TITLE,
        DESCRIPTION,
        LOCATION
    }

    public static class ValidationResult {
        private final boolean valid;
        private final Field field;
        private final String message;

        private ValidationResult(boolean valid, Field field, String message) {
            this.valid = valid;
            this.field = field;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, Field.NONE, "");
        }

        public static ValidationResult error(Field field, String message) {
            return new ValidationResult(false, field, message);
        }

        public boolean isValid() {
            return valid;
        }

        public Field getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
