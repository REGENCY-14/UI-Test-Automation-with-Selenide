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

/**
 * Test suite covering all authentication scenarios for the SauceDemo login page.
 *
 * <p>This class validates the login functionality of {@code https://www.saucedemo.com}
 * across three categories:
 * <ul>
 *   <li><strong>Positive</strong> — valid users who should be granted access</li>
 *   <li><strong>Negative</strong> — invalid credentials that should be rejected
 *       with a descriptive error message</li>
 *   <li><strong>Edge cases</strong> — special accounts (locked user) and
 *       security probes (SQL injection)</li>
 * </ul>
 *
 * <h3>SauceDemo test accounts used:</h3>
 * <ul>
 *   <li>{@code standard_user} — normal user with full access</li>
 *   <li>{@code performance_glitch_user} — slow but functional user</li>
 *   <li>{@code locked_out_user} — account blocked by the system</li>
 * </ul>
 * All accounts share the password {@code secret_sauce}.
 *
 * <h3>Tags:</h3>
 * <ul>
 *   <li>{@code authentication} — groups all login-related tests</li>
 *   <li>{@code smoke} — subset run on every build to verify core login works</li>
 * </ul>
 *
 * @see LoginPage
 * @see BaseTest
 */
@Feature("Authentication")
@Owner("QA Team")
@Tag("authentication")
@Tag("smoke")
class LoginTest extends BaseTest {

    /**
     * The login page instance shared across all test methods in this class.
     * Initialised in {@link #openLoginPage()} before each test.
     */
    private LoginPage loginPage;

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Navigates to the application root and initialises the {@link LoginPage}
     * before each test method runs.
     *
     * <p>This ensures every test starts from a clean, fully loaded login page
     * regardless of what the previous test did.
     */
    @BeforeEach
    void openLoginPage() {
        open("/");
        loginPage = new LoginPage().isLoaded();
    }

    // ── Positive scenarios ────────────────────────────────────────────────────

    /**
     * Verifies that a standard user with valid credentials can log in
     * successfully and is redirected to the products page.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Enter username {@code standard_user}</li>
     *   <li>Enter password {@code secret_sauce}</li>
     *   <li>Click the login button</li>
     *   <li>Assert the products page loads with at least one product visible</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Products page is displayed with
     * a non-empty product list.
     */
    @Test
    @Story("Valid login")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Standard user can log in successfully")
    void standardUserCanLogin() {
        ProductsPage products = loginPage.loginAs("standard_user", "secret_sauce");
        assertTrue(products.getProductCount() > 0,
                "Products page should display items after successful login");
    }

    /**
     * Verifies that the performance glitch user can log in successfully.
     *
     * <p>The {@code performance_glitch_user} account simulates a slow server
     * response. This test confirms the framework's timeout settings are
     * sufficient to handle delayed page loads without false failures.
     *
     * <p><strong>Expected result:</strong> Products page loads (possibly slowly)
     * with a non-empty product list.
     */
    @Test
    @Story("Valid login")
    @DisplayName("Performance glitch user can log in")
    void performanceGlitchUserCanLogin() {
        ProductsPage products = loginPage.loginAs("performance_glitch_user", "secret_sauce");
        assertTrue(products.getProductCount() > 0,
                "Performance glitch user should still reach the products page");
    }

    // ── Negative scenarios ────────────────────────────────────────────────────

    /**
     * Verifies that entering a correct username with an incorrect password
     * displays an appropriate error message and keeps the user on the login page.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Enter username {@code standard_user}</li>
     *   <li>Enter an incorrect password {@code wrong_password}</li>
     *   <li>Click the login button</li>
     *   <li>Assert the error message is visible</li>
     *   <li>Assert the error text contains the expected message</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Error message reads
     * "Username and password do not match".
     */
    @Test
    @Story("Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Wrong password shows error message")
    void wrongPasswordShowsError() {
        loginPage.loginWithInvalidCredentials("standard_user", "wrong_password");

        assertTrue(loginPage.isErrorDisplayed(),
                "Error message should be visible after wrong password");
        assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"),
                "Unexpected error text: " + loginPage.getErrorMessage());
    }

    /**
     * Verifies that entering an unrecognised username displays an error message.
     *
     * <p>This test ensures the system does not reveal whether a username exists
     * (i.e. the error message is the same as for a wrong password, preventing
     * username enumeration attacks).
     *
     * <p><strong>Expected result:</strong> Error message reads
     * "Username and password do not match".
     */
    @Test
    @Story("Invalid login")
    @DisplayName("Unknown username shows error message")
    void unknownUsernameShowsError() {
        loginPage.loginWithInvalidCredentials("unknown_user", "secret_sauce");

        assertTrue(loginPage.isErrorDisplayed(),
                "Error message should be visible for unknown username");
        assertTrue(loginPage.getErrorMessage().contains("Username and password do not match"),
                "Unexpected error text: " + loginPage.getErrorMessage());
    }

    /**
     * Verifies that submitting the login form with both fields empty displays
     * a validation error prompting the user to enter a username.
     *
     * <p>This test covers the form's client-side / server-side validation for
     * completely empty submissions.
     *
     * <p><strong>Expected result:</strong> Error message reads
     * "Username is required".
     */
    @Test
    @Story("Invalid login")
    @DisplayName("Empty credentials show error message")
    void emptyCredentialsShowError() {
        loginPage.loginWithInvalidCredentials("", "");

        assertTrue(loginPage.isErrorDisplayed(),
                "Error message should be visible for empty credentials");
        assertTrue(loginPage.getErrorMessage().contains("Username is required"),
                "Unexpected error text: " + loginPage.getErrorMessage());
    }

    /**
     * Verifies that entering a valid username but leaving the password field
     * empty displays a validation error prompting the user to enter a password.
     *
     * <p><strong>Expected result:</strong> Error message reads
     * "Password is required".
     */
    @Test
    @Story("Invalid login")
    @DisplayName("Empty password shows error message")
    void emptyPasswordShowsError() {
        loginPage.loginWithInvalidCredentials("standard_user", "");

        assertTrue(loginPage.isErrorDisplayed(),
                "Error message should be visible for empty password");
        assertTrue(loginPage.getErrorMessage().contains("Password is required"),
                "Unexpected error text: " + loginPage.getErrorMessage());
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    /**
     * Verifies that a locked-out user receives a specific error message
     * explaining that their account has been blocked.
     *
     * <p>The {@code locked_out_user} account is pre-configured in SauceDemo
     * to always be rejected at login. This test confirms the system returns
     * a meaningful message rather than a generic authentication failure.
     *
     * <p><strong>Expected result:</strong> Error message reads
     * "Sorry, this user has been locked out".
     */
    @Test
    @Story("Locked user")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Locked out user sees specific error")
    void lockedOutUserSeesError() {
        loginPage.loginWithInvalidCredentials("locked_out_user", "secret_sauce");

        assertTrue(loginPage.isErrorDisplayed(),
                "Error message should be visible for locked out user");
        assertTrue(loginPage.getErrorMessage().contains("Sorry, this user has been locked out"),
                "Unexpected error text: " + loginPage.getErrorMessage());
    }

    /**
     * Verifies that a basic SQL injection attempt in the username and password
     * fields does not bypass authentication.
     *
     * <p>This is a security smoke test. The payload {@code ' OR '1'='1} is a
     * classic SQL injection string that would grant access if the backend
     * constructs queries via string concatenation without parameterisation.
     *
     * <p><strong>Expected result:</strong> Login is rejected and an error
     * message is displayed — the user is NOT redirected to the products page.
     */
    @Test
    @Story("Invalid login")
    @DisplayName("SQL injection attempt does not authenticate")
    void sqlInjectionDoesNotAuthenticate() {
        loginPage.loginWithInvalidCredentials("' OR '1'='1", "' OR '1'='1");

        assertTrue(loginPage.isErrorDisplayed(),
                "SQL injection should not bypass login — error message must be shown");
    }
}
