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

@Feature("Shopping Cart")
@Owner("QA Team")
@Tag("cart")
@Tag("regression")
class AddToCartTest extends BaseTest {

    private ProductsPage productsPage;

    @BeforeEach
    void loginAndOpenProducts() {
        open("/");
        productsPage = new LoginPage().isLoaded()
                .loginAs("standard_user", "secret_sauce");
    }
    // ── Positive ─────────────────────────────────────────────────────────────

    @Test
    @Story("Add to cart")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Adding one product increments cart badge to 1")
    void addOneProductUpdatesCartBadge() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        assertEquals(1, productsPage.getCartCount());
    }

    @Test
    @Story("Add to cart")
    @DisplayName("Adding multiple products reflects correct cart count")
    void addMultipleProductsUpdatesCartCount() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.addToCartByName("Sauce Labs Bike Light");
        assertEquals(2, productsPage.getCartCount());
    }

    @Test
    @Story("Add to cart")
    @DisplayName("Added product appears in cart")
    void addedProductAppearsInCart() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        CartPage cart = productsPage.openCart();
        assertTrue(cart.containsProduct("Sauce Labs Backpack"));
        assertEquals(1, cart.getItemCount());
    }

    @Test
    @Story("Add to cart")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("All six products can be added to cart")
    void allProductsCanBeAddedToCart() {
        // Add all products (each has its own add-to-cart button)
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.addToCartByName("Sauce Labs Bike Light");
        productsPage.addToCartByName("Sauce Labs Bolt T-Shirt");
        productsPage.addToCartByName("Sauce Labs Fleece Jacket");
        productsPage.addToCartByName("Sauce Labs Onesie");
        productsPage.addToCartByName("Test.allTheThings() T-Shirt (Red)");
        assertEquals(6, productsPage.getCartCount());
    }

    // ── Negative ─────────────────────────────────────────────────────────────

    @Test
    @Story("Remove from cart")
    @DisplayName("Removing a product decrements cart badge")
    void removeProductDecrementsCartBadge() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.addToCartByName("Sauce Labs Bike Light");
        productsPage.removeFromCartByName("Sauce Labs Backpack");
        assertEquals(1, productsPage.getCartCount());
    }

    @Test
    @Story("Remove from cart")
    @DisplayName("Removing all products hides cart badge")
    void removeAllProductsHidesCartBadge() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        productsPage.removeFromCartByName("Sauce Labs Backpack");
        assertEquals(0, productsPage.getCartCount());
    }

    @Test
    @Story("Remove from cart")
    @DisplayName("Product removed from cart page is no longer listed")
    void productRemovedFromCartIsGone() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        CartPage cart = productsPage.openCart();
        cart.removeItem("Sauce Labs Backpack");
        assertEquals(0, cart.getItemCount());
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    @Story("Cart persistence")
    @DisplayName("Cart contents persist after navigating back to products")
    void cartPersistsAfterNavigation() {
        productsPage.addToCartByName("Sauce Labs Backpack");
        CartPage cart = productsPage.openCart();
        ProductsPage backToProducts = cart.continueShopping();
        assertEquals(1, backToProducts.getCartCount());
    }

    @Test
    @Story("Add to cart")
    @DisplayName("Products page shows correct total product count")
    void productsPageShowsSixItems() {
        assertEquals(6, productsPage.getProductCount());
    }
}
