package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for https://www.saucedemo.com (login screen).
 */
public class LoginPage extends BasePage<LoginPage> {

    private final SelenideElement usernameField   = $("#user-name");
    private final SelenideElement passwordField   = $("#password");
    private final SelenideElement loginButton     = $("#login-button");
    private final SelenideElement errorMessage    = $("[data-test='error']");

    @Override
    public LoginPage isLoaded() {
        loginButton.shouldBe(visible);
        return this;
    }

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        usernameField.shouldBe(visible).setValue(username);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        passwordField.shouldBe(visible).setValue(password);
        return this;
    }

    @Step("Click login button")
    public ProductsPage clickLogin() {
        loginButton.click();
        return new ProductsPage().isLoaded();
    }

    @Step("Login as {username}")
    public ProductsPage loginAs(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .clickLogin();
    }

    @Step("Attempt login with invalid credentials")
    public LoginPage loginWithInvalidCredentials(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        loginButton.click();
        return this;
    }

    @Step("Get error message text")
    public String getErrorMessage() {
        return errorMessage.shouldBe(visible).getText();
    }

    public boolean isErrorDisplayed() {
        return errorMessage.is(visible);
    }
}
