package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.CartPage;
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
 * Test suite covering all shopping cart operations on the SauceDemo products page.
 *
 * <p>This class validates the add-to-cart and remove-from-cart functionality,
 * cart badge behaviour, cart page contents, and cart state persistence across
 * navigation. Tests are grouped into three categories:
 * <ul>
 *   <li><strong>Positive</strong> — adding products and verifying they appear
 *       correctly in the cart</li>
 *   <li><strong>Negative</strong> — removing products and verifying the cart
 *       updates correctly</li>
 *   <li><strong>Edge cases</strong> — cart persistence after navigation and
 *       total product count validation</li>
 * </ul>
 *
 * <h3>Pre-condition:</h3>
 * <p>Each test starts from a freshly logged-in products page via
 * {@link #loginAndOpenProducts()}. The {@code standard_user} account is used
 * because it has unrestricted access to all products and cart operations.
 *
 * <h3>Tags:</h3>
 * <ul>
 *   <li>{@code cart} — groups all cart-related tests</li>
 *   <li>{@code regression} — included in the full regression suite</li>
 * </ul>
 *
 * @see ProductsPage
 * @see CartPage
 * @see BaseTest
 */
@Feature("Shopping Cart")
@Owner("QA Team")
@Tag("cart")
@Tag("regression")
class AddToCartTest extends BaseTest {

    /**
     * The products page instance shared across all test methods in this class.
     * Initialised in {@link #loginAndOpenProducts()} before each test.
     */
    private ProductsPage productsPage;

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Logs in as {@code standard_user} and navigates to the products page
     * before each test method runs.
     *
     * <p>This ensures every test starts from a clean session with an empty
     * cart and a fully loaded products page, regardless of what the previous
     * test did.
     */
    @BeforeEach
    void loginAndOpenProducts() {
        open("/");
        productsPage = new LoginPage().isLoaded()
                .loginAs("standard_user", "secret_sauce");
    }

    // ── Positive scenarios ────────────────────────────────────────────────────

    /**
     * Verifies that adding a single product to the cart increments the cart
     * badge counter to 1.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Add "Sauce Labs Backpack" to the cart</li>
     *   <li>Assert the cart badge shows the number 1</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Cart badge displays {@code 1}.
     */
    @Test
    @Story("Add to cart")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Adding one product increments cart badge to 1")
    void addOneProductUpdatesCartBadge() {
        productsPage.addToCartByName("Sauce Labs Backpack");

        assertEquals(1, productsPage.getCartCount(),
                "Cart badge should show 1 after adding one product");
    }

    /**
     * Verifies that adding two different products to the cart results in a
     * cart badge count of 2.
     *
     * <p>This test confirms the cart accumulates items correctly across
     * multiple add-to-cart actions in the same session.
     *
     * <p><strong>Expected result:</strong> Cart badge displays {@code 2}.
     */
    @Test
    @Story("Add to cart")
    @DisplayName("Adding multiple products reflects correct cart count")
    void addMultipleProductsUpdatesCartCount() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.addToCartByName("Sauce Labs Bike Light");

        assertEquals(2, productsPage.getCartCount(),
                "Cart badge should show 2 after adding two products");
    }

    /**
     * Verifies that a product added from the products page actually appears
     * in the cart when the cart page is opened.
     *
     * <p>This is an end-to-end validation of the add-to-cart flow — it
     * confirms not just the badge count but also that the correct item is
     * stored in the cart.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Add "Sauce Labs Backpack" to the cart</li>
     *   <li>Navigate to the cart page</li>
     *   <li>Assert the cart contains exactly 1 item</li>
     *   <li>Assert the item is "Sauce Labs Backpack"</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Cart page shows 1 item with the
     * correct product name.
     */
    @Test
    @Story("Add to cart")
    @DisplayName("Added product appears in cart")
    void addedProductAppearsInCart() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        CartPage cart = productsPage.openCart();

        assertTrue(cart.containsProduct("Sauce Labs Backpack"),
                "Cart should contain 'Sauce Labs Backpack'");
        assertEquals(1, cart.getItemCount(),
                "Cart should have exactly 1 item");
    }

    /**
     * Verifies that all six products available on the products page can be
     * added to the cart simultaneously.
     *
     * <p>This test validates the maximum cart capacity for the SauceDemo
     * inventory and confirms the cart badge accurately reflects the total.
     *
     * <p><strong>Expected result:</strong> Cart badge displays {@code 6}
     * after all products are added.
     */
    @Test
    @Story("Add to cart")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("All six products can be added to cart")
    void allProductsCanBeAddedToCart() {
        // Add all 6 products available in the SauceDemo inventory
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.addToCartByName("Sauce Labs Bike Light");
        productsPage.addToCartByName("Sauce Labs Bolt T-Shirt");
        productsPage.addToCartByName("Sauce Labs Fleece Jacket");
        productsPage.addToCartByName("Sauce Labs Onesie");
        productsPage.addToCartByName("Test.allTheThings() T-Shirt (Red)");

        assertEquals(6, productsPage.getCartCount(),
                "Cart badge should show 6 after adding all products");
    }

    // ── Negative scenarios ────────────────────────────────────────────────────

    /**
     * Verifies that removing one product from a two-item cart decrements the
     * cart badge to 1.
     *
     * <p>This test confirms the remove-from-cart action on the products page
     * correctly updates the cart badge without requiring a page reload.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Add "Sauce Labs Backpack" and "Sauce Labs Bike Light"</li>
     *   <li>Remove "Sauce Labs Backpack" via its Remove button</li>
     *   <li>Assert the cart badge shows 1</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Cart badge displays {@code 1}.
     */
    @Test
    @Story("Remove from cart")
    @DisplayName("Removing a product decrements cart badge")
    void removeProductDecrementsCartBadge() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.addToCartByName("Sauce Labs Bike Light");
        productsPage.removeFromCartByName("Sauce Labs Backpack");

        assertEquals(1, productsPage.getCartCount(),
                "Cart badge should show 1 after removing one of two products");
    }

    /**
     * Verifies that removing the only item in the cart causes the cart badge
     * to disappear (count returns to 0).
     *
     * <p>The cart badge element is only rendered when the cart contains at
     * least one item. This test confirms it is hidden when the cart is empty.
     *
     * <p><strong>Expected result:</strong> Cart badge is not visible;
     * {@link ProductsPage#getCartCount()} returns {@code 0}.
     */
    @Test
    @Story("Remove from cart")
    @DisplayName("Removing all products hides cart badge")
    void removeAllProductsHidesCartBadge() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.removeFromCartByName("Sauce Labs Backpack");

        assertEquals(0, productsPage.getCartCount(),
                "Cart badge should not be visible when cart is empty");
    }

    /**
     * Verifies that removing a product from the cart page removes it from
     * the cart item list entirely.
     *
     * <p>This test validates the remove action on the cart page itself
     * (as opposed to the products page), confirming the item count drops
     * to zero after removal.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Add "Sauce Labs Backpack" to the cart</li>
     *   <li>Navigate to the cart page</li>
     *   <li>Remove "Sauce Labs Backpack" from the cart page</li>
     *   <li>Assert the cart is now empty</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Cart page shows 0 items.
     */
    @Test
    @Story("Remove from cart")
    @DisplayName("Product removed from cart page is no longer listed")
    void productRemovedFromCartIsGone() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        CartPage cart = productsPage.openCart();
        cart.removeItem("Sauce Labs Backpack");

        assertEquals(0, cart.getItemCount(),
                "Cart should be empty after removing the only item");
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    /**
     * Verifies that cart contents are preserved when the user navigates away
     * from the cart back to the products page.
     *
     * <p>This test validates session state persistence — the cart should not
     * be cleared when the user clicks "Continue Shopping" and returns to the
     * products page.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Add "Sauce Labs Backpack" to the cart</li>
     *   <li>Open the cart page</li>
     *   <li>Click "Continue Shopping" to return to the products page</li>
     *   <li>Assert the cart badge still shows 1</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Cart badge still displays {@code 1}
     * after returning to the products page.
     */
    @Test
    @Story("Cart persistence")
    @DisplayName("Cart contents persist after navigating back to products")
    void cartPersistsAfterNavigation() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        CartPage cart = productsPage.openCart();
        ProductsPage backToProducts = cart.continueShopping();

        assertEquals(1, backToProducts.getCartCount(),
                "Cart badge should still show 1 after returning to products page");
    }

    /**
     * Verifies that the products page displays exactly 6 products, which is
     * the expected inventory size for the SauceDemo application.
     *
     * <p>This test acts as a data integrity check — if the product count
     * changes, it may indicate a test data or environment issue that could
     * affect other cart tests.
     *
     * <p><strong>Expected result:</strong> Products page shows exactly
     * {@code 6} product cards.
     */
    @Test
    @Story("Add to cart")
    @DisplayName("Products page shows correct total product count")
    void productsPageShowsSixItems() {
        assertEquals(6, productsPage.getProductCount(),
                "SauceDemo inventory should always contain exactly 6 products");
    }
}
