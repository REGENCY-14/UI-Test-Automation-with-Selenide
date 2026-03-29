package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.*;
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

@Feature("Checkout")
@Owner("QA Team")
@Tag("checkout")
@Tag("regression")
class CheckoutTest extends BaseTest {

    private ProductsPage productsPage;

    @BeforeEach
    void loginAndAddProduct() {
        open("/");
        productsPage = new LoginPage().isLoaded()
                .loginAs("standard_user", "secret_sauce");
        productsPage.addToCartByName("Sauce Labs Backpack");
    }

    // ── Positive ─────────────────────────────────────────────────────────────

    @Test
    @Story("Complete checkout")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("User can complete full checkout flow")
    void completeCheckoutFlow() {
        CheckoutCompletePage complete = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary()
                .finishOrder();

        assertTrue(complete.isOrderConfirmed(),
                "Order confirmation header should contain 'Thank you for your order'");
    }

    @Test
    @Story("Complete checkout")
    @DisplayName("Order summary shows correct item count")
    void orderSummaryShowsCorrectItemCount() {
        CheckoutStepTwoPage summary = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("Jane", "Smith", "67890")
                .continueToSummary();

        assertEquals(1, summary.getItemCount());
    }

    @Test
    @Story("Complete checkout")
    @DisplayName("Order summary displays total and tax")
    void orderSummaryDisplaysTotalAndTax() {
        CheckoutStepTwoPage summary = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("Jane", "Smith", "67890")
                .continueToSummary();

        assertFalse(summary.getTotal().isBlank(), "Total label should not be empty");
        assertFalse(summary.getTax().isBlank(), "Tax label should not be empty");
    }

    @Test
    @Story("Complete checkout")
    @DisplayName("After order completion user can return to products")
    void afterOrderUserCanReturnToProducts() {
        ProductsPage products = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary()
                .finishOrder()
                .backToProducts();

        assertTrue(products.getProductCount() > 0);
    }

    // ── Negative ─────────────────────────────────────────────────────────────

    @Test
    @Story("Checkout validation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Missing first name shows error on checkout step one")
    void missingFirstNameShowsError() {
        CheckoutStepOnePage stepOne = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("", "Doe", "12345");

        stepOne.continueButton().click();
        assertTrue(stepOne.getErrorMessage().contains("First Name is required"),
                "Unexpected error: " + stepOne.getErrorMessage());
    }

    @Test
    @Story("Checkout validation")
    @DisplayName("Missing last name shows error on checkout step one")
    void missingLastNameShowsError() {
        CheckoutStepOnePage stepOne = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "", "12345");

        stepOne.continueButton().click();
        assertTrue(stepOne.getErrorMessage().contains("Last Name is required"),
                "Unexpected error: " + stepOne.getErrorMessage());
    }

    @Test
    @Story("Checkout validation")
    @DisplayName("Missing postal code shows error on checkout step one")
    void missingPostalCodeShowsError() {
        CheckoutStepOnePage stepOne = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "");

        stepOne.continueButton().click();
        assertTrue(stepOne.getErrorMessage().contains("Postal Code is required"),
                "Unexpected error: " + stepOne.getErrorMessage());
    }

    @Test
    @Story("Checkout navigation")
    @DisplayName("Cancelling checkout step one returns to cart")
    void cancelCheckoutStepOneReturnsToCart() {
        CartPage cart = productsPage
                .openCart()
                .proceedToCheckout()
                .cancel();

        assertEquals(1, cart.getItemCount());
    }

    @Test
    @Story("Checkout navigation")
    @DisplayName("Cancelling checkout step two returns to products")
    void cancelCheckoutStepTwoReturnsToProducts() {
        ProductsPage products = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary()
                .cancelOrder();

        assertTrue(products.getProductCount() > 0);
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    @Story("Complete checkout")
    @DisplayName("Checkout works with multiple items in cart")
    void checkoutWithMultipleItems() {
        productsPage.addToCartByName("Sauce Labs Bike Light");

        CheckoutStepTwoPage summary = productsPage
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary();

        assertEquals(2, summary.getItemCount());
    }
}
