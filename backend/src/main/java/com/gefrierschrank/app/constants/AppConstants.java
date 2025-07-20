package com.gefrierschrank.app.constants;

public final class AppConstants {

    private AppConstants() {
        // Utility class - prevent instantiation
    }

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    // Expiry
    public static final int DEFAULT_EXPIRY_WARNING_DAYS = 7;
    public static final int EXPIRY_BUFFER_DAYS = 1;
    public static final int MIN_EXPIRY_DAYS = 1;
    public static final int MAX_EXPIRY_DAYS = 365;

    // JWT
    public static final long JWT_MAX_AGE_SECONDS = 3600L;

    // CORS
    public static final String[] ALLOWED_HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

    // File upload
    public static final int IMAGE_MAX_WIDTH = 200;
    public static final int IMAGE_MAX_HEIGHT = 200;
    public static final long IMAGE_MAX_SIZE_KB = 50L;

    // Validation
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 20;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 120;
    public static final int EMAIL_MAX_LENGTH = 50;

    // Database
    public static final int MAX_BACKUPS_RETAINED = 10;

    // Categories
    public static final String CATEGORY_MEAT = "Fleisch";
    public static final String CATEGORY_VEGETABLES = "Gemüse";
    public static final String CATEGORY_READY_MEALS = "Fertiggerichte";
    public static final String CATEGORY_ICE_CREAM = "Eis";

    // Units
    public static final String UNIT_KG = "kg";
    public static final String UNIT_G = "g";
    public static final String UNIT_L = "L";
    public static final String UNIT_ML = "ml";
    public static final String UNIT_PIECES = "Stück";

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
}