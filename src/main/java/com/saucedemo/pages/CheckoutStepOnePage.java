package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for Checkout Step One — customer information form.
 */
public class CheckoutStepOnePage extends BasePage<CheckoutStepOnePage> {

    private final SelenideElement pageTitle    = $(".title");
    private final SelenideElement firstNameField = $("[data-test='firstName']");
    private final SelenideElement lastNameField  = $("[data-test='lastName']");
    private final SelenideElement postalCodeField = $("[data-test='postalCode']");
    private final SelenideElement continueButton = $("[data-test='continue']");
    private final SelenideElement cancelButton   = $("[data-test='cancel']");
    private final SelenideElement errorMessage   = $("[data-test='error']");

    @Override
    public CheckoutStepOnePage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Checkout: Your Information"));
        return this;
    }

    @Step("Enter first name: {firstName}")
    public CheckoutStepOnePage enterFirstName(String firstName) {
        firstNameField.shouldBe(visible).setValue(firstName);
        return this;
    }

    @Step("Enter last name: {lastName}")
    public CheckoutStepOnePage enterLastName(String lastName) {
        lastNameField.shouldBe(visible).setValue(lastName);
        return this;
    }

    @Step("Enter postal code: {postalCode}")
    public CheckoutStepOnePage enterPostalCode(String postalCode) {
        postalCodeField.shouldBe(visible).setValue(postalCode);
        return this;
    }

    @Step("Fill checkout information")
    public CheckoutStepOnePage fillInformation(String firstName, String lastName, String postalCode) {
        return enterFirstName(firstName)
                .enterLastName(lastName)
                .enterPostalCode(postalCode);
    }

    @Step("Continue to order summary")
    public CheckoutStepTwoPage continueToSummary() {
        continueButton.click();
        return new CheckoutStepTwoPage().isLoaded();
    }

    @Step("Cancel checkout")
    public CartPage cancel() {
        cancelButton.click();
        return new CartPage().isLoaded();
    }

    public String getErrorMessage() {
        return errorMessage.shouldBe(visible).getText();
    }

    /** Exposed for negative test scenarios that expect validation errors. */
    public SelenideElement continueButton() {
        return continueButton;
    }
}
