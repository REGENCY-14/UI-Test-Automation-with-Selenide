package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductsPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;

@Feature("Authentication")
@Owner("QA Team")
@Tag("authentication")
@Tag("smoke")
class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        open("/");
        loginPage = new LoginPage().isLoaded();
    }

    // ── Positive ─────────────────────────────────────────────────────────────

    @Test
    @Story("Valid login")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Standard user can log in successfully")
    void standardUserCanLogin() {
        ProductsPage products = loginPage.loginAs("standard_user", "secret_sauce");
        assertTrue(products.getProductCount() > 0, "Products page should display items");
    }

    @Test
    @Story("Valid login")
    @DisplayName("Performance glitch user can log in")
    void performanceGlitchUserCanLogin() {
        ProductsPage products = loginPage.loginAs("performance_glitch_user", "secret_sauce");
        assertTrue(products.getProductCount() > 0);
    }

    // ── Negative ─────────────────────────────────────────────────────────────

    @Test
    @Story("Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Wrong password shows error message")
    void wrongPasswordShowsError() {
        loginPage.loginWithInvalidCredentials("standard_user", "wrong_password");
        assertTrue(loginPage.isErrorDisplayed(), "Error message should be visible");
        assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"),
                "Unexpected error text: " + loginPage.getErrorMessage());
    }

    @Test
    @Story("Invalid login")
    @DisplayName("Unknown username shows error message")
    void unknownUsernameShowsError() {
        loginPage.loginWithInvalidCredentials("unknown_user", "secret_sauce");
        assertTrue(loginPage.isErrorDisplayed());
        assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"));
    }

    @Test
    @Story("Invalid login")
    @DisplayName("Empty credentials show error message")
    void emptyCredentialsShowError() {
        loginPage.loginWithInvalidCredentials("", "");
        assertTrue(loginPage.isErrorDisplayed());
        assertTrue(loginPage.getErrorMessage().contains("Username is required"));
    }

    @Test
    @Story("Invalid login")
    @DisplayName("Empty password shows error message")
    void emptyPasswordShowsError() {
        loginPage.loginWithInvalidCredentials("standard_user", "");
        assertTrue(loginPage.isErrorDisplayed());
        assertTrue(loginPage.getErrorMessage().contains("Password is required"));
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    @Story("Locked user")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Locked out user sees specific error")
    void lockedOutUserSeesError() {
        loginPage.loginWithInvalidCredentials("locked_out_user", "secret_sauce");
        assertTrue(loginPage.isErrorDisplayed());
        assertTrue(loginPage.getErrorMessage().contains("Sorry, this user has been locked out"),
                "Unexpected error text: " + loginPage.getErrorMessage());
    }

    @Test
    @Story("Invalid login")
    @DisplayName("SQL injection attempt does not authenticate")
    void sqlInjectionDoesNotAuthenticate() {
        loginPage.loginWithInvalidCredentials("' OR '1'='1", "' OR '1'='1");
        assertTrue(loginPage.isErrorDisplayed(), "SQL injection should not bypass login");
    }
}
