package com.saucedemo.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for the SauceDemo Shopping Cart page ({@code /cart.html}).
 *
 * <p>Represents the cart review screen where users can inspect items before
 * proceeding to checkout, remove individual items, or return to shopping.
 *
 * <h3>Navigation paths from this page:</h3>
 * <ul>
 *   <li>Proceed to checkout → {@link CheckoutStepOnePage}</li>
 *   <li>Continue shopping → {@link ProductsPage}</li>
 * </ul>
 */
public class CartPage extends BasePage<CartPage> {

    // ── Element locators ──────────────────────────────────────────────────────

    /** Page title element — used to verify the page is loaded. */
    private final SelenideElement pageTitle           = $(".title");

    /** "Checkout" button that proceeds to the checkout information form. */
    private final SelenideElement checkoutButton      = $("[data-test='checkout']");

    /** "Continue Shopping" button that returns to the products page. */
    private final SelenideElement continueShoppingBtn = $("[data-test='continue-shopping']");

    /** Collection of all cart item row containers. */
    private final ElementsCollection cartItems        = $$(".cart_item");

    /** Collection of product name elements within cart item rows. */
    private final ElementsCollection itemNames        = $$(".inventory_item_name");

    // ── Page readiness ────────────────────────────────────────────────────────

    /**
     * Asserts that the Cart page is fully loaded by verifying the page title
     * reads "Your Cart".
     *
     * @return this {@code CartPage} instance for fluent chaining
     */
    @Override
    public CartPage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Your Cart"));
        return this;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns the number of items currently in the cart.
     *
     * @return the count of cart item rows visible on the page
     */
    @Step("Get number of items in cart")
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * Checks whether a product with the given name is present in the cart.
     *
     * @param productName the exact display name of the product to look for
     * @return {@code true} if the product is visible in the cart; {@code false} otherwise
     */
    @Step("Check if product is in cart: {productName}")
    public boolean containsProduct(String productName) {
        return itemNames.findBy(text(productName)).is(visible);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Removes the product with the given name from the cart by clicking its
     * "Remove" button.
     *
     * @param productName the exact display name of the product to remove
     * @return this {@code CartPage} instance for fluent chaining
     */
    @Step("Remove item from cart: {productName}")
    public CartPage removeItem(String productName) {
        itemNames.findBy(text(productName))
                .closest(".cart_item")
                .$("[data-test^='remove']")
                .click();
        return this;
    }

    /**
     * Clicks the "Checkout" button and waits for the checkout information
     * form to load.
     *
     * @return a new {@link CheckoutStepOnePage} instance after navigation
     */
    @Step("Proceed to checkout")
    public CheckoutStepOnePage proceedToCheckout() {
        checkoutButton.shouldBe(visible).shouldBe(enabled).click();
        return new CheckoutStepOnePage().isLoaded();
    }

    /**
     * Clicks the "Continue Shopping" button and returns to the products page.
     *
     * @return a new {@link ProductsPage} instance after navigation
     */
    @Step("Continue shopping")
    public ProductsPage continueShopping() {
        continueShoppingBtn.click();
        return new ProductsPage().isLoaded();
    }
}
