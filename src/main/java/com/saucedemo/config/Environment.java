package com.saucedemo.config;

/**
 * Supported target environments.
 * Select via -Denv=staging (or ENV=staging docker env var).
 *
 * Each environment maps to its own base URL; all other config
 * (browser, headless, timeout) is shared from config.properties.
 */
public enum Environment {

    PROD("https://www.saucedemo.com"),
    STAGING("https://staging.saucedemo.com"),
    DEV("https://dev.saucedemo.com");

    private final String baseUrl;

    Environment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Resolves the active environment from the {@code env} system property
     * or {@code ENV} environment variable. Defaults to {@link #PROD}.
     */
    public static Environment active() {
        String val = System.getProperty("env");
        if (val == null || val.isBlank()) {
            val = System.getenv("ENV");
        }
        if (val == null || val.isBlank()) {
            return PROD;
        }
        try {
            return valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown environment '" + val + "'. Valid values: PROD, STAGING, DEV");
        }
    }
}
