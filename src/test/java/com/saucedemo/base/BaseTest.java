package com.saucedemo.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.saucedemo.config.ConfigProvider;
import com.saucedemo.driver.DriverFactory;
import com.saucedemo.extensions.RetryExtension;
import com.saucedemo.utils.AllureAttachmentUtils;
import io.qameta.allure.Allure;
import io.qameta.allure.junit5.AllureJunit5;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Abstract base class that every concrete test class must extend.
 *
 * <p>Centralises all cross-cutting concerns for the test suite:
 * <ul>
 *   <li><strong>Selenide configuration</strong> — applies browser, baseUrl,
 *       headless mode, and timeout from {@link ConfigProvider} before each test.</li>
 *   <li><strong>Chrome options</strong> — disables password manager popups and
 *       data-breach alerts via {@link DriverFactory}.</li>
 *   <li><strong>Allure integration</strong> — registers the {@link AllureSelenide}
 *       listener for automatic screenshot and page-source capture on failure,
 *       and copies {@code environment.properties} into the results directory
 *       so the Allure Overview tab shows environment metadata.</li>
 *   <li><strong>Retry support</strong> — registers {@link RetryExtension} to
 *       automatically retry failing tests up to {@code test.retry.count} times.</li>
 *   <li><strong>Teardown</strong> — attaches the current URL and a screenshot
 *       to the report after every test, then closes the browser to prevent
 *       WebDriver process leaks between tests.</li>
 * </ul>
 *
 * <h3>Parallel execution:</h3>
 * <p>Annotated with {@link Execution @Execution(SAME_THREAD)} so that all
 * test methods within a single class run sequentially on the same thread,
 * avoiding WebDriver state conflicts. Test <em>classes</em> still run in
 * parallel as configured in {@code junit-platform.properties}.
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 *   class LoginTest extends BaseTest {
 *       @Test
 *       void userCanLogin() {
 *           open("/");
 *           new LoginPage().isLoaded().loginAs("standard_user", "secret_sauce");
 *       }
 *   }
 * }</pre>
 */
@ExtendWith({AllureJunit5.class, RetryExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
public abstract class BaseTest {

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Runs before each test method to configure Selenide and register
     * Allure listeners.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Apply {@link ConfigProvider} values to {@link Configuration}</li>
     *   <li>Configure Chrome options via {@link DriverFactory}</li>
     *   <li>Register {@link AllureSelenide} listener with screenshots and
     *       page-source capture enabled</li>
     *   <li>Copy {@code environment.properties} to {@code target/allure-results}
     *       so the Allure Overview tab shows environment metadata</li>
     *   <li>Set the Allure test description to the display name</li>
     * </ol>
     *
     * @param testInfo JUnit 5 metadata about the currently running test,
     *                 used to set the Allure description
     */
    @BeforeEach
    public void setUp(TestInfo testInfo) {
        // Apply runtime configuration to Selenide
        Configuration.baseUrl  = ConfigProvider.getBaseUrl();
        Configuration.browser  = ConfigProvider.getBrowser();
        Configuration.headless = ConfigProvider.isHeadless();
        Configuration.timeout  = ConfigProvider.getTimeoutMs();

        // Suppress Chrome password manager and data-breach popups
        DriverFactory.configureChromeOptions();

        // Register AllureSelenide listener — captures screenshot + page source on failure
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(true));

        // Copy environment.properties into allure-results for the Overview tab
        copyEnvironmentProperties();

        // Set Allure test description from JUnit display name
        Allure.description("Test: " + testInfo.getDisplayName());
    }

    // ── Teardown ──────────────────────────────────────────────────────────────

    /**
     * Runs after each test method (regardless of pass/fail) to capture
     * diagnostic artifacts and clean up the browser session.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Attach the current URL to the Allure report (if browser is open)</li>
     *   <li>Attach a screenshot to the Allure report (if browser is open)</li>
     *   <li>Remove the {@link AllureSelenide} listener</li>
     *   <li>Close the WebDriver session to prevent process leaks</li>
     * </ol>
     *
     * @param testInfo JUnit 5 metadata about the test that just completed
     */
    @AfterEach
    public void tearDown(TestInfo testInfo) {
        // Attach URL and screenshot for every test (pass or fail)
        if (WebDriverRunner.hasWebDriverStarted()) {
            AllureAttachmentUtils.attachCurrentUrl();
            AllureAttachmentUtils.takeScreenshot();
        }

        // Clean up the Allure listener
        SelenideLogger.removeListener("AllureSelenide");

        // Close the browser — prevents WebDriver process leaks between tests
        Selenide.closeWebDriver();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Copies {@code environment.properties} from the test classpath into the
     * {@code target/allure-results} directory.
     *
     * <p>Allure reads this file when generating the report and displays the
     * key-value pairs on the Overview tab under "Environment". This provides
     * at-a-glance visibility of the browser, Java version, and target URL
     * used for the test run.
     *
     * <p>Failure to copy the file is non-fatal — the report will still be
     * generated, just without the environment panel.
     */
    private void copyEnvironmentProperties() {
        try {
            Path resultsDir = Paths.get("target/allure-results");
            Files.createDirectories(resultsDir);

            try (InputStream in = getClass().getClassLoader()
                    .getResourceAsStream("environment.properties")) {
                if (in != null) {
                    Files.copy(in, resultsDir.resolve("environment.properties"),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            // Non-fatal — log and continue
            System.err.println("[BaseTest] Could not copy environment.properties: "
                    + e.getMessage());
        }
    }
}
