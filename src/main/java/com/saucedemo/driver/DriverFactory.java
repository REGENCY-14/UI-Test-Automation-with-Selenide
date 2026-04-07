package com.saucedemo.driver;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Configures Chrome options to suppress password manager popups,
 * data breach alerts, and other UI interruptions during test execution.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static void configureChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Suppress "password found in data breach" and save password prompts
        options.addArguments("--password-store=basic");
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--disable-save-password-bubble");

        // Suppress other UI noise
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");

        // Headless-safe flags
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");

        // Disable Chrome's password manager entirely via prefs
        options.setExperimentalOption("prefs", java.util.Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false
        ));

        Configuration.browserCapabilities = options;
    }
}
