package com.saucedemo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration provider for the UI automation framework.
 *
 * <p>Loads runtime settings from {@code config/config.properties} on the classpath
 * and exposes them through typed accessor methods. Supports three-tier override
 * resolution in the following priority order (highest to lowest):
 * <ol>
 *   <li>JVM system property (e.g. {@code -Dbase.url=...})</li>
 *   <li>OS environment variable (e.g. {@code BASE_URL=...})</li>
 *   <li>Value from {@code config.properties} file</li>
 * </ol>
 *
 * <p>This design allows the same codebase to run locally with file defaults,
 * be overridden via Maven {@code -D} flags, and be fully controlled by Docker
 * {@code -e} environment variables in CI without any code changes.
 *
 * <p>Usage example:
 * <pre>{@code
 *   Configuration.baseUrl = ConfigProvider.getBaseUrl();
 *   Configuration.browser = ConfigProvider.getBrowser();
 * }</pre>
 *
 * @see Environment
 */
public final class ConfigProvider {

    /**
     * Eagerly loaded properties from {@code config/config.properties}.
     * Loaded once at class initialisation time and shared across all tests.
     */
    private static final Properties PROPS = loadProperties();

    /** Utility class — prevent instantiation. */
    private ConfigProvider() {}

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Reads {@code config/config.properties} from the classpath into a
     * {@link Properties} object.
     *
     * @return populated {@link Properties} instance
     * @throws IllegalStateException if the file cannot be found or read
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream stream = ConfigProvider.class
                .getClassLoader()
                .getResourceAsStream("config/config.properties")) {

            if (stream == null) {
                throw new IllegalStateException(
                        "config/config.properties not found on classpath. " +
                        "Ensure the file exists under src/main/resources/config/");
            }
            props.load(stream);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config/config.properties", e);
        }
        return props;
    }

    /**
     * Resolves a configuration value using the three-tier priority chain:
     * system property → environment variable → properties file.
     *
     * <p>Property keys use dot notation (e.g. {@code base.url}); the
     * corresponding environment variable is derived by uppercasing and
     * replacing dots with underscores (e.g. {@code BASE_URL}).
     *
     * @param key the property key in dot-notation format
     * @return the resolved value, or {@code null} if not found in any source
     */
    private static String get(String key) {
        // 1. JVM system property (-Dkey=value)
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.isBlank()) return sysVal;

        // 2. OS environment variable (KEY_NAME=value)
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) return envVal;

        // 3. Fall back to config.properties file value
        return PROPS.getProperty(key);
    }

    // ── Public accessors ──────────────────────────────────────────────────────

    /**
     * Returns the base URL of the application under test.
     *
     * <p>If an explicit {@code base.url} override is provided via system
     * property or environment variable, that value is returned directly.
     * Otherwise the URL is derived from the active {@link Environment} enum
     * value (controlled by the {@code env} / {@code ENV} property).
     *
     * @return non-null, non-empty base URL string
     */
    public static String getBaseUrl() {
        String explicit = get("base.url");
        String defaultUrl = Environment.PROD.getBaseUrl();
        // If the resolved value differs from the PROD default, an explicit
        // override was provided — honour it directly.
        if (explicit != null && !explicit.equals(defaultUrl)) {
            return explicit;
        }
        return Environment.active().getBaseUrl();
    }

    /**
     * Returns the browser identifier used by Selenide (e.g. {@code "chrome"},
     * {@code "firefox"}).
     *
     * @return browser name string; defaults to {@code "chrome"}
     */
    public static String getBrowser() {
        return get("browser");
    }

    /**
     * Returns whether the browser should run in headless mode.
     *
     * <p>Headless mode is recommended for CI environments to avoid GPU
     * overhead and reduce memory consumption.
     *
     * @return {@code true} if headless mode is enabled; defaults to {@code true}
     */
    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless"));
    }

    /**
     * Returns the global Selenide element wait timeout in milliseconds.
     *
     * <p>This value is applied to {@code Configuration.timeout} and controls
     * how long Selenide waits for elements to satisfy conditions before
     * throwing {@code ElementNotFound}.
     *
     * @return timeout in milliseconds; defaults to {@code 10000}
     */
    public static long getTimeoutMs() {
        return Long.parseLong(get("timeout.ms"));
    }
}
