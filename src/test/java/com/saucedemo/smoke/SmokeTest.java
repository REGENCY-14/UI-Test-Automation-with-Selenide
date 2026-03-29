package com.saucedemo.smoke;

import com.codeborne.selenide.Selenide;
import com.saucedemo.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.title;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Feature("Smoke")
class SmokeTest extends BaseTest {

    @Test
    @DisplayName("Application loads successfully")
    @Description("Verifies that the base URL is reachable and the page title is correct")
    void applicationLoads() {
        Selenide.open("/");
        assertTrue(title().contains("Swag Labs"),
                "Expected page title to contain 'Swag Labs' but was: " + title());
    }
}
