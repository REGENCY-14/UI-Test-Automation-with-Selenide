package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite covering the complete three-step checkout flow on SauceDemo.
 *
 * <p>The SauceDemo checkout flow consists of three pages:
 * <ol>
 *   <li><strong>Step 1</strong> ({@link CheckoutStepOnePage}) — Customer information
 *       form (first name, last name, postal code)</li>
 *   <li><strong>Step 2</strong> ({@link CheckoutStepTwoPage}) — Order overview
 *       showing items, subtotal, tax, and total</li>
 *   <li><strong>Complete</strong> ({@link CheckoutCompletePage}) — Order confirmation
 *       with "Thank you for your order!" message</li>
 * </ol>
 *
 * <p>Tests are grouped into three categories:
 * <ul>
 *   <li><strong>Positive</strong> — happy-path flows that complete successfully</li>
 *   <li><strong>Negative</strong> — validation errors and cancellation flows</li>
 *   <li><strong>Edge cases</strong> — checkout with multiple items in the cart</li>
 * </ul>
 *
 * <h3>Design decision — self-contained test methods:</h3>
 * <p>Each test method calls {@link #loginAndAddBackpack()} to set up its own
 * browser session from scratch. This avoids shared state between tests and
 * ensures retry behaviour (via {@code RetryExtension}) works correctly, since
 * {@code @BeforeEach} does not re-run on retry invocations.
 *
 * <h3>Execution mode:</h3>
 * <p>Annotated with {@link Execution @Execution(SAME_THREAD)} to run all
 * methods in this class sequentially. The multi-step checkout flow is
 * stateful and sensitive to browser session state, making parallel execution
 * within the class unsafe.
 *
 * <h3>Tags:</h3>
 * <ul>
 *   <li>{@code checkout} — groups all checkout-related tests</li>
 *   <li>{@code regression} — included in the full regression suite</li>
 * </ul>
 *
 * @see CheckoutStepOnePage
 * @see CheckoutStepTwoPage
 * @see CheckoutCompletePage
 * @see BaseTest
 */
@Feature("Checkout")
@Owner("QA Team")
@Tag("checkout")
@Tag("regression")
@Execution(ExecutionMode.SAME_THREAD)
class CheckoutTest extends BaseTest {

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Sets up a fresh browser session by logging in as {@code standard_user}
     * and adding "Sauce Labs Backpack" to the cart.
     *
     * <p>This helper is called at the start of each test method instead of
     * using {@code @BeforeEach}, so that the setup re-runs correctly on retry
     * attempts triggered by {@code RetryExtension}.
     *
     * @return a {@link ProductsPage} instance with one item already in the cart
     */
    private ProductsPage loginAndAddBackpack() {
        open("/");
        ProductsPage products = new LoginPage().isLoaded()
                .loginAs("standard_user", "secret_sauce");
        products.addToCartByName("Sauce Labs Backpack");
        return products;
    }

    // ── Positive scenarios ────────────────────────────────────────────────────

    /**
     * Verifies the complete end-to-end checkout flow from cart to order confirmation.
     *
     * <p>This is the most critical test in the suite — it validates the entire
     * purchase journey works without interruption.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Login and add "Sauce Labs Backpack" to the cart</li>
     *   <li>Open the cart and click "Checkout"</li>
     *   <li>Fill in customer information (first name, last name, postal code)</li>
     *   <li>Click "Continue" to reach the order summary</li>
     *   <li>Click "Finish" to place the order</li>
     *   <li>Assert the confirmation page shows "Thank you for your order!"</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Order confirmation page is displayed
     * with the success header visible.
     */
    @Test
    @Story("Complete checkout")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("User can complete full checkout flow")
    void completeCheckoutFlow() {
        CheckoutCompletePage complete = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary()
                .finishOrder();

        assertTrue(complete.isOrderConfirmed(),
                "Order confirmation header should contain 'Thank you for your order'");
    }

    /**
     * Verifies that the order summary page (Step 2) displays the correct
     * number of line items matching what was added to the cart.
     *
     * <p>This test ensures the cart contents are accurately transferred to
     * the checkout summary without items being lost or duplicated.
     *
     * <p><strong>Expected result:</strong> Order summary shows exactly
     * {@code 1} line item (the Sauce Labs Backpack).
     */
    @Test
    @Story("Complete checkout")
    @DisplayName("Order summary shows correct item count")
    void orderSummaryShowsCorrectItemCount() {
        CheckoutStepTwoPage summary = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("Jane", "Smith", "67890")
                .continueToSummary();

        assertEquals(1, summary.getItemCount(),
                "Order summary should show exactly 1 item");
    }

    /**
     * Verifies that the order summary page displays non-empty total and tax
     * labels, confirming the pricing calculation is rendered.
     *
     * <p>This test does not validate the exact monetary values (which would
     * require test data management), but confirms the labels are present and
     * populated — a regression guard against pricing display bugs.
     *
     * <p><strong>Expected result:</strong> Both the total label and tax label
     * contain non-blank text.
     */
    @Test
    @Story("Complete checkout")
    @DisplayName("Order summary displays total and tax")
    void orderSummaryDisplaysTotalAndTax() {
        CheckoutStepTwoPage summary = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("Jane", "Smith", "67890")
                .continueToSummary();

        assertFalse(summary.getTotal().isBlank(),
                "Total label should not be empty on the order summary");
        assertFalse(summary.getTax().isBlank(),
                "Tax label should not be empty on the order summary");
    }

    /**
     * Verifies that after completing an order, the user can navigate back to
     * the products page using the "Back Home" button on the confirmation page.
     *
     * <p>This test validates the post-order navigation flow and confirms the
     * products page is accessible and functional after a completed purchase.
     *
     * <p><strong>Expected result:</strong> Products page is displayed with
     * a non-empty product list after clicking "Back Home".
     */
    @Test
    @Story("Complete checkout")
    @DisplayName("After order completion user can return to products")
    void afterOrderUserCanReturnToProducts() {
        ProductsPage products = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary()
                .finishOrder()
                .backToProducts();

        assertTrue(products.getProductCount() > 0,
                "Products page should be accessible after completing an order");
    }

    // ── Negative scenarios ────────────────────────────────────────────────────

    /**
     * Verifies that leaving the first name field empty and clicking "Continue"
     * on Checkout Step 1 displays a validation error.
     *
     * <p>The SauceDemo application validates all three fields (first name,
     * last name, postal code) before allowing progression to Step 2. This
     * test confirms the first name validation fires correctly.
     *
     * <p><strong>Expected result:</strong> Validation error reads
     * "First Name is required" and the user remains on Step 1.
     */
    @Test
    @Story("Checkout validation")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Missing first name shows error on checkout step one")
    void missingFirstNameShowsError() {
        // Fill in last name and postal code but leave first name empty
        CheckoutStepOnePage stepOne = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("", "Doe", "12345");

        // Click continue to trigger validation without navigating away
        stepOne.continueButton().click();

        assertTrue(stepOne.getErrorMessage().contains("First Name is required"),
                "Unexpected error: " + stepOne.getErrorMessage());
    }

    /**
     * Verifies that leaving the last name field empty and clicking "Continue"
     * on Checkout Step 1 displays a validation error.
     *
     * <p><strong>Expected result:</strong> Validation error reads
     * "Last Name is required" and the user remains on Step 1.
     */
    @Test
    @Story("Checkout validation")
    @DisplayName("Missing last name shows error on checkout step one")
    void missingLastNameShowsError() {
        // Fill in first name and postal code but leave last name empty
        CheckoutStepOnePage stepOne = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "", "12345");

        stepOne.continueButton().click();

        assertTrue(stepOne.getErrorMessage().contains("Last Name is required"),
                "Unexpected error: " + stepOne.getErrorMessage());
    }

    /**
     * Verifies that leaving the postal code field empty and clicking "Continue"
     * on Checkout Step 1 displays a validation error.
     *
     * <p><strong>Expected result:</strong> Validation error reads
     * "Postal Code is required" and the user remains on Step 1.
     */
    @Test
    @Story("Checkout validation")
    @DisplayName("Missing postal code shows error on checkout step one")
    void missingPostalCodeShowsError() {
        // Fill in first and last name but leave postal code empty
        CheckoutStepOnePage stepOne = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "");

        stepOne.continueButton().click();

        assertTrue(stepOne.getErrorMessage().contains("Postal Code is required"),
                "Unexpected error: " + stepOne.getErrorMessage());
    }

    /**
     * Verifies that clicking "Cancel" on Checkout Step 1 returns the user
     * to the cart page with their items still present.
     *
     * <p>This test confirms the cancellation flow does not clear the cart,
     * allowing the user to resume shopping without losing their selections.
     *
     * <p><strong>Expected result:</strong> Cart page is displayed with
     * {@code 1} item still in the cart.
     */
    @Test
    @Story("Checkout navigation")
    @DisplayName("Cancelling checkout step one returns to cart")
    void cancelCheckoutStepOneReturnsToCart() {
        CartPage cart = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .cancel();  // Click Cancel on Step 1

        assertEquals(1, cart.getItemCount(),
                "Cart should still contain 1 item after cancelling checkout step 1");
    }

    /**
     * Verifies that clicking "Cancel" on Checkout Step 2 (order overview)
     * returns the user to the products page.
     *
     * <p>Cancelling from the order overview is a different flow from
     * cancelling on Step 1 — it navigates to the products page rather than
     * back to the cart.
     *
     * <p><strong>Expected result:</strong> Products page is displayed with
     * a non-empty product list.
     */
    @Test
    @Story("Checkout navigation")
    @DisplayName("Cancelling checkout step two returns to products")
    void cancelCheckoutStepTwoReturnsToProducts() {
        ProductsPage products = loginAndAddBackpack()
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary()
                .cancelOrder();  // Click Cancel on Step 2

        assertTrue(products.getProductCount() > 0,
                "Products page should be displayed after cancelling checkout step 2");
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    /**
     * Verifies that the checkout flow works correctly when the cart contains
     * multiple items, and that the order summary accurately reflects all items.
     *
     * <p>This test validates that the checkout is not limited to single-item
     * carts and that the order summary line item count matches the cart contents.
     *
     * <p><strong>Steps:</strong>
     * <ol>
     *   <li>Login and add "Sauce Labs Backpack" and "Sauce Labs Bike Light"</li>
     *   <li>Proceed through checkout to the order summary</li>
     *   <li>Assert the summary shows exactly 2 line items</li>
     * </ol>
     *
     * <p><strong>Expected result:</strong> Order summary displays {@code 2}
     * line items.
     */
    @Test
    @Story("Complete checkout")
    @DisplayName("Checkout works with multiple items in cart")
    void checkoutWithMultipleItems() {
        // Start fresh and add two products
        open("/");
        ProductsPage products = new LoginPage().isLoaded()
                .loginAs("standard_user", "secret_sauce");
        products.addToCartByName("Sauce Labs Backpack");
        products.addToCartByName("Sauce Labs Bike Light");

        CheckoutStepTwoPage summary = products
                .openCart()
                .proceedToCheckout()
                .fillInformation("John", "Doe", "12345")
                .continueToSummary();

        assertEquals(2, summary.getItemCount(),
                "Order summary should show 2 items when 2 products were added to cart");
    }
}
