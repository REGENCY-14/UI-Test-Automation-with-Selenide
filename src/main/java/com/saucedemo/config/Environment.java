package com.saucedemo.config;

/**
 * Enumeration of supported target environments for the test suite.
 *
 * <p>Each constant maps to a specific base URL, allowing the same tests to
 * run against different deployments without code changes. The active
 * environment is resolved at runtime from the {@code env} system property
 * or the {@code ENV} environment variable.
 *
 * <p>Selection priority:
 * <ol>
 *   <li>JVM system property: {@code -Denv=staging}</li>
 *   <li>OS environment variable: {@code ENV=staging}</li>
 *   <li>Default: {@link #PROD}</li>
 * </ol>
 *
 * <p>Usage example:
 * <pre>{@code
 *   // Switch to staging via Maven
 *   mvn test -Denv=staging
 *
 *   // Switch via Docker
 *   docker run -e ENV=staging saucedemo-tests
 * }</pre>
 *
 * @see ConfigProvider#getBaseUrl()
 */
public enum Environment {

    /**
     * Production environment — the live public SauceDemo site.
     * Used by default when no {@code env} override is provided.
     */
    PROD("https://www.saucedemo.com"),

    /**
     * Staging environment — pre-production deployment for integration testing.
     * Activate with {@code -Denv=staging} or {@code ENV=staging}.
     */
    STAGING("https://staging.saucedemo.com"),

    /**
     * Development environment — local or feature-branch deployment.
     * Activate with {@code -Denv=dev} or {@code ENV=dev}.
     */
    DEV("https://dev.saucedemo.com");

    // ── Instance fields ───────────────────────────────────────────────────────

    /** The base URL associated with this environment. */
    private final String baseUrl;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates an {@code Environment} constant with the given base URL.
     *
     * @param baseUrl the root URL for this environment (must not be null)
     */
    Environment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /**
     * Returns the base URL for this environment.
     *
     * @return non-null base URL string
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    // ── Static factory ────────────────────────────────────────────────────────

    /**
     * Resolves and returns the currently active {@code Environment}.
     *
     * <p>Checks the {@code env} system property first, then the {@code ENV}
     * environment variable. If neither is set, defaults to {@link #PROD}.
     * The value is case-insensitive (e.g. {@code "STAGING"} and
     * {@code "staging"} both resolve to {@link #STAGING}).
     *
     * @return the active {@code Environment}; never {@code null}
     * @throws IllegalArgumentException if the provided value does not match
     *         any known environment constant
     */
    public static Environment active() {
        // Check JVM system property first
        String val = System.getProperty("env");

        // Fall back to OS environment variable
        if (val == null || val.isBlank()) {
            val = System.getenv("ENV");
        }

        // Default to PROD when no override is present
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
