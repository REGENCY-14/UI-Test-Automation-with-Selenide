package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for Checkout Step One — the customer information form
 * ({@code /checkout-step-one.html}).
 *
 * <p>This is the first step of the three-step checkout flow. The user must
 * provide their first name, last name, and postal/zip code before proceeding
 * to the order summary.
 *
 * <h3>Validation behaviour:</h3>
 * <p>If any required field is left empty and the "Continue" button is clicked,
 * the page displays an inline error message and does not navigate away.
 * Use {@link #getErrorMessage()} to read the validation text.
 *
 * <h3>Navigation paths from this page:</h3>
 * <ul>
 *   <li>Continue (valid form) → {@link CheckoutStepTwoPage}</li>
 *   <li>Cancel → {@link CartPage}</li>
 * </ul>
 */
public class CheckoutStepOnePage extends BasePage<CheckoutStepOnePage> {

    // ── Element locators ──────────────────────────────────────────────────────

    /** Page title element — used to verify the page is loaded. */
    private final SelenideElement pageTitle      = $(".title");

    /** First name input field. */
    private final SelenideElement firstNameField = $("[data-test='firstName']");

    /** Last name input field. */
    private final SelenideElement lastNameField  = $("[data-test='lastName']");

    /** Postal / zip code input field. */
    private final SelenideElement postalCodeField = $("[data-test='postalCode']");

    /** "Continue" button that submits the form and proceeds to step two. */
    private final SelenideElement continueButton = $("[data-test='continue']");

    /** "Cancel" button that returns the user to the cart. */
    private final SelenideElement cancelButton   = $("[data-test='cancel']");

    /** Inline validation error message shown when required fields are missing. */
    private final SelenideElement errorMessage   = $("[data-test='error']");

    // ── Page readiness ────────────────────────────────────────────────────────

    /**
     * Asserts that Checkout Step One is fully loaded by verifying the page
     * title reads "Checkout: Your Information".
     *
     * @return this {@code CheckoutStepOnePage} instance for fluent chaining
     */
    @Override
    public CheckoutStepOnePage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Checkout: Your Information"));
        return this;
    }

    // ── Form field actions ────────────────────────────────────────────────────

    /**
     * Types the given first name into the first name field.
     *
     * @param firstName the first name to enter
     * @return this {@code CheckoutStepOnePage} instance for fluent chaining
     */
    @Step("Enter first name: {firstName}")
    public CheckoutStepOnePage enterFirstName(String firstName) {
        firstNameField.shouldBe(visible).setValue(firstName);
        return this;
    }

    /**
     * Types the given last name into the last name field.
     *
     * @param lastName the last name to enter
     * @return this {@code CheckoutStepOnePage} instance for fluent chaining
     */
    @Step("Enter last name: {lastName}")
    public CheckoutStepOnePage enterLastName(String lastName) {
        lastNameField.shouldBe(visible).setValue(lastName);
        return this;
    }

    /**
     * Types the given postal code into the postal code field.
     *
     * @param postalCode the postal / zip code to enter
     * @return this {@code CheckoutStepOnePage} instance for fluent chaining
     */
    @Step("Enter postal code: {postalCode}")
    public CheckoutStepOnePage enterPostalCode(String postalCode) {
        postalCodeField.shouldBe(visible).setValue(postalCode);
        return this;
    }

    /**
     * Convenience method that fills all three required fields in one call.
     *
     * <p>Equivalent to calling {@link #enterFirstName(String)},
     * {@link #enterLastName(String)}, and {@link #enterPostalCode(String)}
     * in sequence.
     *
     * @param firstName  the first name to enter
     * @param lastName   the last name to enter
     * @param postalCode the postal / zip code to enter
     * @return this {@code CheckoutStepOnePage} instance for fluent chaining
     */
    @Step("Fill checkout information")
    public CheckoutStepOnePage fillInformation(String firstName, String lastName, String postalCode) {
        return enterFirstName(firstName)
                .enterLastName(lastName)
                .enterPostalCode(postalCode);
    }

    // ── Navigation actions ────────────────────────────────────────────────────

    /**
     * Clicks the "Continue" button and waits for the order summary page to load.
     *
     * <p>This method assumes all required fields have been filled with valid
     * values. For validation error scenarios, call {@link #continueButton()}
     * directly and then inspect {@link #getErrorMessage()}.
     *
     * @return a new {@link CheckoutStepTwoPage} instance after navigation
     */
    @Step("Continue to order summary")
    public CheckoutStepTwoPage continueToSummary() {
        continueButton.shouldBe(visible).shouldBe(enabled).click();
        return new CheckoutStepTwoPage().isLoaded();
    }

    /**
     * Clicks the "Cancel" button and returns to the cart page.
     *
     * @return a new {@link CartPage} instance after navigation
     */
    @Step("Cancel checkout")
    public CartPage cancel() {
        cancelButton.click();
        return new CartPage().isLoaded();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns the text of the inline validation error message.
     *
     * <p>Waits for the error element to become visible before reading its text.
     * Use this after clicking {@link #continueButton()} with missing fields.
     *
     * @return the validation error message string
     * @throws com.codeborne.selenide.ex.ElementNotFound if no error is shown
     */
    public String getErrorMessage() {
        return errorMessage.shouldBe(visible).getText();
    }

    /**
     * Exposes the "Continue" button element for negative test scenarios that
     * need to click it and then assert a validation error without navigating.
     *
     * @return the {@link SelenideElement} for the continue button
     */
    public SelenideElement continueButton() {
        return continueButton;
    }
}
