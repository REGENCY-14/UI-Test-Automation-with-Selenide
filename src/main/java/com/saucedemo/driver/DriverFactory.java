package com.saucedemo.driver;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

/**
 * Factory class responsible for configuring the WebDriver browser options
 * used by Selenide throughout the test suite.
 *
 * <p>The primary purpose of this class is to suppress Chrome UI interruptions
 * that would otherwise block test execution, including:
 * <ul>
 *   <li>Password manager "save password" prompts</li>
 *   <li>"Password found in a data breach" security alerts</li>
 *   <li>Browser notifications and info-bars</li>
 *   <li>First-run setup dialogs</li>
 * </ul>
 *
 * <p>The configured {@link ChromeOptions} are assigned to
 * {@link Configuration#browserCapabilities}, which Selenide picks up
 * automatically before opening the first browser window.
 *
 * <p>This class is called once per test in {@code BaseTest.setUp()} to ensure
 * every browser session starts with a clean, interruption-free configuration.
 *
 * <p>Usage:
 * <pre>{@code
 *   // In BaseTest.setUp()
 *   DriverFactory.configureChromeOptions();
 * }</pre>
 */
public final class DriverFactory {

    /** Utility class — prevent instantiation. */
    private DriverFactory() {}

    /**
     * Builds and applies a {@link ChromeOptions} configuration to Selenide's
     * global {@link Configuration#browserCapabilities}.
     *
     * <h3>Applied settings:</h3>
     * <ul>
     *   <li>{@code --password-store=basic} — use basic (non-keychain) password
     *       store to avoid OS-level credential dialogs</li>
     *   <li>{@code --disable-features=PasswordLeakDetection} — suppress the
     *       "password found in data breach" Chrome alert</li>
     *   <li>{@code --disable-save-password-bubble} — hide the save-password
     *       info-bar after form submission</li>
     *   <li>{@code --disable-notifications} — block browser notification
     *       permission prompts</li>
     *   <li>{@code --disable-popup-blocking} — prevent popup blocker from
     *       interfering with test navigation</li>
     *   <li>{@code --disable-infobars} — remove Chrome automation info-bar</li>
     *   <li>{@code --no-first-run} — skip Chrome's first-run setup wizard</li>
     *   <li>{@code --no-default-browser-check} — skip default browser prompt</li>
     *   <li>{@code --no-sandbox} — required for running Chrome inside Docker
     *       containers as root</li>
     *   <li>{@code --disable-dev-shm-usage} — use {@code /tmp} instead of
     *       {@code /dev/shm} to avoid shared memory issues in Docker</li>
     *   <li>{@code --disable-gpu} — disable GPU hardware acceleration
     *       (required for headless mode in some environments)</li>
     *   <li>{@code --remote-allow-origins=*} — allow WebDriver remote
     *       connections from any origin (required for Chrome 111+)</li>
     * </ul>
     *
     * <h3>Experimental preferences applied:</h3>
     * <ul>
     *   <li>{@code credentials_enable_service=false} — disable the credential
     *       service entirely</li>
     *   <li>{@code profile.password_manager_enabled=false} — disable the
     *       built-in password manager</li>
     *   <li>{@code profile.password_manager_leak_detection=false} — disable
     *       password leak detection checks</li>
     * </ul>
     */
    public static void configureChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // ── Suppress password manager and data breach alerts ──────────────────
        options.addArguments("--password-store=basic");
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--disable-save-password-bubble");

        // ── Suppress other UI interruptions ───────────────────────────────────
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--no-first-run");
        options.addArguments("--no-default-browser-check");

        // ── Docker / headless compatibility flags ─────────────────────────────
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");

        // ── Chrome profile preferences (disable password manager via prefs) ───
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false
        ));

        // Apply options to Selenide's global browser capabilities
        Configuration.browserCapabilities = options;
    }
}
