package com.saucedemo.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for the Shopping Cart page.
 */
public class CartPage extends BasePage<CartPage> {

    private final SelenideElement pageTitle        = $(".title");
    private final SelenideElement checkoutButton   = $("[data-test='checkout']");
    private final SelenideElement continueShoppingBtn = $("[data-test='continue-shopping']");
    private final ElementsCollection cartItems     = $$(".cart_item");
    private final ElementsCollection itemNames     = $$(".inventory_item_name");

    @Override
    public CartPage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Your Cart"));
        return this;
    }

    @Step("Get number of items in cart")
    public int getItemCount() {
        return cartItems.size();
    }

    @Step("Check if product is in cart: {productName}")
    public boolean containsProduct(String productName) {
        return itemNames.findBy(text(productName)).is(visible);
    }

    @Step("Remove item from cart: {productName}")
    public CartPage removeItem(String productName) {
        itemNames.findBy(text(productName))
                .closest(".cart_item")
                .$("[data-test^='remove']")
                .click();
        return this;
    }

    @Step("Proceed to checkout")
    public CheckoutStepOnePage proceedToCheckout() {
        checkoutButton.shouldBe(visible).click();
        return new CheckoutStepOnePage().isLoaded();
    }

    @Step("Continue shopping")
    public ProductsPage continueShopping() {
        continueShoppingBtn.click();
        return new ProductsPage().isLoaded();
    }
}
