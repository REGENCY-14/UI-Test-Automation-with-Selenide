package com.saucedemo.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single source of truth for runtime configuration.
 * Reads config/config.properties from classpath; system properties override file values.
 */
public final class ConfigProvider {

    private static final Properties PROPS = loadProperties();

    private ConfigProvider() {}

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

    private static String get(String key) {
        // Priority: system property → environment variable → properties file
        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.isBlank()) return sysVal;

        // Map property keys to env var names (e.g. base.url → BASE_URL)
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) return envVal;

        return PROPS.getProperty(key);
    }

    public static String getBaseUrl() {
        return get("base.url");
    }

    public static String getBrowser() {
        return get("browser");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static long getTimeoutMs() {
        return Long.parseLong(get("timeout.ms"));
    }
}
