package com.saucedemo.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.saucedemo.config.BasePage;

import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for the SauceDemo Products / Inventory page ({@code /inventory.html}).
 *
 * <p>This page is the landing page after a successful login. It displays all
 * available products and provides controls for adding/removing items from the
 * shopping cart, sorting the product list, and navigating to the cart.
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Add or remove individual products from the cart by name</li>
 *   <li>Read the current cart item count from the badge</li>
 *   <li>Sort the product list by the available sort options</li>
 *   <li>Navigate to the {@link CartPage}</li>
 * </ul>
 */
public class ProductsPage extends BasePage<ProductsPage> {

    // ── Element locators ──────────────────────────────────────────────────────

    /** Page title element — used to verify the page is loaded. */
    private final SelenideElement pageTitle        = $(".title");

    /** Shopping cart icon in the top-right navigation bar. */
    private final SelenideElement cartIcon         = $(".shopping_cart_link");

    /** Badge on the cart icon showing the number of items in the cart. */
    private final SelenideElement cartBadge        = $(".shopping_cart_badge");

    /** Sort dropdown for ordering the product list. */
    private final SelenideElement sortDropdown     = $("[data-test='product-sort-container']");

    /** Collection of all product card containers on the page. */
    private final ElementsCollection productCards  = $$(".inventory_item");

    /** Collection of all product name elements — used for lookup by name. */
    private final ElementsCollection productNames  = $$(".inventory_item_name");

    /** Collection of all "Add to cart" buttons across all products. */
    private final ElementsCollection addToCartBtns = $$("[data-test^='add-to-cart']");

    // ── Page readiness ────────────────────────────────────────────────────────

    /**
     * Asserts that the Products page is fully loaded by verifying the page
     * title reads "Products".
     *
     * @return this {@code ProductsPage} instance for fluent chaining
     */
    @Override
    public ProductsPage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Products"));
        return this;
    }

    // ── Cart actions ──────────────────────────────────────────────────────────

    /**
     * Adds the product with the given name to the shopping cart.
     *
     * <p>Locates the product card by matching the product name text, then
     * clicks its "Add to cart" button.
     *
     * @param productName the exact display name of the product to add
     * @return this {@code ProductsPage} instance for fluent chaining
     */
    @Step("Add product to cart by name: {productName}")
    public ProductsPage addToCartByName(String productName) {
        productNames.findBy(text(productName))
                .closest(".inventory_item")
                .$("[data-test^='add-to-cart']")
                .click();
        return this;
    }

    /**
     * Removes the product with the given name from the shopping cart.
     *
     * <p>Locates the product card by matching the product name text, then
     * clicks its "Remove" button (only visible after the item has been added).
     *
     * @param productName the exact display name of the product to remove
     * @return this {@code ProductsPage} instance for fluent chaining
     */
    @Step("Remove product from cart by name: {productName}")
    public ProductsPage removeFromCartByName(String productName) {
        productNames.findBy(text(productName))
                .closest(".inventory_item")
                .$("[data-test^='remove']")
                .click();
        return this;
    }

    // ── Cart badge ────────────────────────────────────────────────────────────

    /**
     * Returns the current number of items in the shopping cart as shown by
     * the cart badge.
     *
     * <p>Returns {@code 0} if the badge is not visible (i.e. the cart is empty).
     *
     * @return the cart item count; {@code 0} if the cart is empty
     */
    @Step("Get cart item count")
    public int getCartCount() {
        if (cartBadge.is(visible)) {
            return Integer.parseInt(cartBadge.getText());
        }
        return 0;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Clicks the cart icon to navigate to the {@link CartPage}.
     *
     * @return a new {@link CartPage} instance after navigation
     */
    @Step("Open cart")
    public CartPage openCart() {
        cartIcon.click();
        return new CartPage().isLoaded();
    }

    // ── Sorting ───────────────────────────────────────────────────────────────

    /**
     * Selects a sort option from the product sort dropdown.
     *
     * <p>Valid option values match the visible text in the dropdown, e.g.:
     * <ul>
     *   <li>{@code "Name (A to Z)"}</li>
     *   <li>{@code "Name (Z to A)"}</li>
     *   <li>{@code "Price (low to high)"}</li>
     *   <li>{@code "Price (high to low)"}</li>
     * </ul>
     *
     * @param option the visible text of the sort option to select
     * @return this {@code ProductsPage} instance for fluent chaining
     */
    @Step("Sort products by: {option}")
    public ProductsPage sortBy(String option) {
        sortDropdown.selectOption(option);
        return this;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns the total number of product cards displayed on the page.
     *
     * @return product count (typically 6 for the standard SauceDemo inventory)
     */
    public int getProductCount() {
        return productCards.size();
    }

    /**
     * Returns the display name of the first product in the current list order.
     *
     * @return the text of the first product name element
     */
    public String getFirstProductName() {
        return productNames.first().shouldBe(visible).getText();
    }
}
