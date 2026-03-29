package com.saucedemo.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.saucedemo.config.ConfigProvider;
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
 * Abstract base class for all test classes.
 * Handles Selenide configuration, Allure listener registration,
 * environment.properties propagation, and browser teardown.
 */
@ExtendWith({AllureJunit5.class, RetryExtension.class})
@Execution(ExecutionMode.SAME_THREAD)   // methods within a class run sequentially
public abstract class BaseTest {

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        Configuration.baseUrl  = ConfigProvider.getBaseUrl();
        Configuration.browser  = ConfigProvider.getBrowser();
        Configuration.headless = ConfigProvider.isHeadless();
        Configuration.timeout  = ConfigProvider.getTimeoutMs();

        // Screenshots + page source on failure via AllureSelenide
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(true));

        // Copy environment.properties into allure-results so the report
        // shows environment info on the Overview tab
        copyEnvironmentProperties();

        Allure.description("Test: " + testInfo.getDisplayName());
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        // Attach URL and screenshot on every test for traceability
        if (WebDriverRunner.hasWebDriverStarted()) {
            AllureAttachmentUtils.attachCurrentUrl();
            AllureAttachmentUtils.takeScreenshot();
        }

        SelenideLogger.removeListener("AllureSelenide");
        Selenide.closeWebDriver();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
            // Non-fatal — report still works without environment tab
            System.err.println("[BaseTest] Could not copy environment.properties: " + e.getMessage());
        }
    }
}
