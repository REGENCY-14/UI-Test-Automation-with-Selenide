package com.saucedemo.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for Checkout Step Two — the order overview / summary page
 * ({@code /checkout-step-two.html}).
 *
 * <p>This is the second step of the three-step checkout flow. It displays a
 * read-only summary of the items in the order, the subtotal, tax, and total
 * price. The user can either confirm the order or cancel and return to the
 * products page.
 *
 * <h3>Navigation paths from this page:</h3>
 * <ul>
 *   <li>Finish → {@link CheckoutCompletePage}</li>
 *   <li>Cancel → {@link ProductsPage}</li>
 * </ul>
 */
public class CheckoutStepTwoPage extends BasePage<CheckoutStepTwoPage> {

    // ── Element locators ──────────────────────────────────────────────────────

    /** Page title element — used to verify the page is loaded. */
    private final SelenideElement pageTitle     = $(".title");

    /** "Finish" button that confirms and places the order. */
    private final SelenideElement finishButton  = $("[data-test='finish']");

    /** "Cancel" button that aborts checkout and returns to the products page. */
    private final SelenideElement cancelButton  = $("[data-test='cancel']");

    /** Label displaying the order total including tax. */
    private final SelenideElement totalLabel    = $(".summary_total_label");

    /** Label displaying the calculated tax amount. */
    private final SelenideElement taxLabel      = $(".summary_tax_label");

    /** Collection of all order line item rows in the summary. */
    private final ElementsCollection cartItems  = $$(".cart_item");

    // ── Page readiness ────────────────────────────────────────────────────────

    /**
     * Asserts that Checkout Step Two is fully loaded by verifying the page
     * title reads "Checkout: Overview".
     *
     * @return this {@code CheckoutStepTwoPage} instance for fluent chaining
     */
    @Override
    public CheckoutStepTwoPage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Checkout: Overview"));
        return this;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns the full text of the order total label (e.g. "Total: $32.39").
     *
     * @return the total label text including the "Total:" prefix
     */
    @Step("Get order total text")
    public String getTotal() {
        return totalLabel.shouldBe(visible).getText();
    }

    /**
     * Returns the full text of the tax label (e.g. "Tax: $2.50").
     *
     * @return the tax label text including the "Tax:" prefix
     */
    @Step("Get tax text")
    public String getTax() {
        return taxLabel.shouldBe(visible).getText();
    }

    /**
     * Returns the number of line items displayed in the order summary.
     *
     * @return the count of cart item rows in the summary
     */
    @Step("Get number of items in order summary")
    public int getItemCount() {
        return cartItems.size();
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Clicks the "Finish" button to confirm and place the order, then waits
     * for the order confirmation page to load.
     *
     * @return a new {@link CheckoutCompletePage} instance after navigation
     */
    @Step("Finish order")
    public CheckoutCompletePage finishOrder() {
        finishButton.shouldBe(visible).click();
        return new CheckoutCompletePage().isLoaded();
    }

    /**
     * Clicks the "Cancel" button to abort the checkout and return to the
     * products page.
     *
     * @return a new {@link ProductsPage} instance after navigation
     */
    @Step("Cancel order")
    public ProductsPage cancelOrder() {
        cancelButton.click();
        return new ProductsPage().isLoaded();
    }
}
