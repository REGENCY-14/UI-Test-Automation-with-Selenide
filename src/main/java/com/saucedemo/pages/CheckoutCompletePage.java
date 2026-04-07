package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for the Checkout Complete / Order Confirmation page
 * ({@code /checkout-complete.html}).
 *
 * <p>This is the final step of the checkout flow, displayed after the user
 * clicks "Finish" on the order summary. It confirms that the order has been
 * placed successfully and provides a button to return to the products page.
 *
 * <h3>Expected confirmation content:</h3>
 * <ul>
 *   <li>Header: "Thank you for your order!"</li>
 *   <li>Body text: dispatch confirmation message</li>
 * </ul>
 *
 * <h3>Navigation paths from this page:</h3>
 * <ul>
 *   <li>Back Home → {@link ProductsPage}</li>
 * </ul>
 */
public class CheckoutCompletePage extends BasePage<CheckoutCompletePage> {

    // ── Element locators ──────────────────────────────────────────────────────

    /** Page title element — used to verify the page is loaded. */
    private final SelenideElement pageTitle      = $(".title");

    /** Large confirmation header (e.g. "Thank you for your order!"). */
    private final SelenideElement confirmHeader  = $(".complete-header");

    /** Confirmation body text with dispatch details. */
    private final SelenideElement confirmText    = $(".complete-text");

    /** "Back Home" button that returns the user to the products page. */
    private final SelenideElement backHomeButton = $("[data-test='back-to-products']");

    // ── Page readiness ────────────────────────────────────────────────────────

    /**
     * Asserts that the Checkout Complete page is fully loaded by verifying
     * the page title reads "Checkout: Complete!".
     *
     * @return this {@code CheckoutCompletePage} instance for fluent chaining
     */
    @Override
    public CheckoutCompletePage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Checkout: Complete!"));
        return this;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns the text of the order confirmation header.
     *
     * @return the confirmation header text (e.g. "Thank you for your order!")
     */
    @Step("Get order confirmation header")
    public String getConfirmationHeader() {
        return confirmHeader.shouldBe(visible).getText();
    }

    /**
     * Returns the body text of the order confirmation message.
     *
     * @return the confirmation body text
     */
    @Step("Get order confirmation text")
    public String getConfirmationText() {
        return confirmText.shouldBe(visible).getText();
    }

    /**
     * Returns whether the order was confirmed successfully by checking that
     * the confirmation header contains the expected "Thank you" text.
     *
     * @return {@code true} if the confirmation header is visible and contains
     *         "Thank you for your order"; {@code false} otherwise
     */
    public boolean isOrderConfirmed() {
        return confirmHeader.is(visible) &&
               confirmHeader.getText().contains("Thank you for your order");
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Clicks the "Back Home" button and returns to the products page.
     *
     * @return a new {@link ProductsPage} instance after navigation
     */
    @Step("Go back to products")
    public ProductsPage backToProducts() {
        backHomeButton.shouldBe(visible).click();
        return new ProductsPage().isLoaded();
    }
}
