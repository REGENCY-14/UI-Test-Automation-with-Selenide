package com.saucedemo.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.saucedemo.config.ConfigProvider;
import io.qameta.allure.junit5.AllureJunit5;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Abstract base class for all test classes.
 * Handles Selenide configuration, Allure listener registration, and browser teardown.
 */
@ExtendWith(AllureJunit5.class)
public abstract class BaseTest {

    @BeforeEach
    public void setUp() {
        Configuration.baseUrl  = ConfigProvider.getBaseUrl();
        Configuration.browser  = ConfigProvider.getBrowser();
        Configuration.headless = ConfigProvider.isHeadless();
        Configuration.timeout  = ConfigProvider.getTimeoutMs();

        // Enable screenshots and page source capture on failure
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(true));
    }

    @AfterEach
    public void tearDown() {
        SelenideLogger.removeListener("AllureSelenide");
        Selenide.closeWebDriver();
    }
}
