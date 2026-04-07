package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for the SauceDemo login screen ({@code /}).
 *
 * <p>Encapsulates all element locators and user-facing actions available on
 * the login page. Tests interact with the application exclusively through
 * the methods defined here — no raw Selenide selectors appear in test classes.
 *
 * <h3>Supported flows:</h3>
 * <ul>
 *   <li>Successful login → returns a {@link ProductsPage}</li>
 *   <li>Failed login → stays on {@code LoginPage} with an error message</li>
 * </ul>
 *
 * <h3>Known SauceDemo users:</h3>
 * <ul>
 *   <li>{@code standard_user} — normal user, all features available</li>
 *   <li>{@code locked_out_user} — blocked; login always fails</li>
 *   <li>{@code performance_glitch_user} — slow but functional</li>
 * </ul>
 *
 * <p>All passwords for the demo site are {@code secret_sauce}.
 */
public class LoginPage extends BasePage<LoginPage> {

    // ── Element locators ──────────────────────────────────────────────────────

    /** Username input field. */
    private final SelenideElement usernameField = $("#user-name");

    /** Password input field. */
    private final SelenideElement passwordField = $("#password");

    /** Login submit button. */
    private final SelenideElement loginButton   = $("#login-button");

    /** Error message container shown on failed login attempts. */
    private final SelenideElement errorMessage  = $("[data-test='error']");

    // ── Page readiness ────────────────────────────────────────────────────────

    /**
     * Asserts that the login page is fully loaded by verifying the login
     * button is visible.
     *
     * @return this {@code LoginPage} instance for fluent chaining
     */
    @Override
    public LoginPage isLoaded() {
        loginButton.shouldBe(visible);
        return this;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Types the given username into the username field.
     *
     * @param username the username to enter
     * @return this {@code LoginPage} instance for fluent chaining
     */
    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        usernameField.shouldBe(visible).setValue(username);
        return this;
    }

    /**
     * Types the given password into the password field.
     *
     * @param password the password to enter
     * @return this {@code LoginPage} instance for fluent chaining
     */
    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        passwordField.shouldBe(visible).setValue(password);
        return this;
    }

    /**
     * Clicks the login button and waits for the {@link ProductsPage} to load.
     *
     * <p>This method assumes valid credentials have already been entered.
     * For invalid credential scenarios use
     * {@link #loginWithInvalidCredentials(String, String)} instead.
     *
     * @return a new {@link ProductsPage} instance after successful navigation
     */
    @Step("Click login button")
    public ProductsPage clickLogin() {
        loginButton.click();
        return new ProductsPage().isLoaded();
    }

    /**
     * Convenience method that enters credentials and clicks login in one step.
     *
     * <p>Equivalent to calling {@link #enterUsername(String)},
     * {@link #enterPassword(String)}, and {@link #clickLogin()} in sequence.
     *
     * @param username the username to log in with
     * @param password the password to log in with
     * @return a new {@link ProductsPage} instance after successful navigation
     */
    @Step("Login as {username}")
    public ProductsPage loginAs(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .clickLogin();
    }

    /**
     * Attempts to log in with the given credentials without asserting success.
     *
     * <p>Use this method for negative test scenarios where login is expected
     * to fail (e.g. wrong password, locked user). The page remains on the
     * login screen and the error message can be inspected via
     * {@link #getErrorMessage()}.
     *
     * @param username the username to attempt
     * @param password the password to attempt
     * @return this {@code LoginPage} instance for fluent chaining
     */
    @Step("Attempt login with invalid credentials")
    public LoginPage loginWithInvalidCredentials(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        loginButton.click();
        return this;
    }

    // ── Assertions / queries ──────────────────────────────────────────────────

    /**
     * Returns the text of the error message displayed after a failed login.
     *
     * <p>Waits for the error element to become visible before reading its text.
     *
     * @return the error message string
     * @throws com.codeborne.selenide.ex.ElementNotFound if no error is shown
     */
    @Step("Get error message text")
    public String getErrorMessage() {
        return errorMessage.shouldBe(visible).getText();
    }

    /**
     * Returns whether an error message is currently visible on the page.
     *
     * @return {@code true} if the error container is visible; {@code false} otherwise
     */
    public boolean isErrorDisplayed() {
        return errorMessage.is(visible);
    }
}
