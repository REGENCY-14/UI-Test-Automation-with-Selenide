package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for the Checkout Complete / Order Confirmation page.
 */
public class CheckoutCompletePage extends BasePage<CheckoutCompletePage> {

    private final SelenideElement pageTitle      = $(".title");
    private final SelenideElement confirmHeader  = $(".complete-header");
    private final SelenideElement confirmText    = $(".complete-text");
    private final SelenideElement backHomeButton = $("[data-test='back-to-products']");

    @Override
    public CheckoutCompletePage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Checkout: Complete!"));
        return this;
    }

    @Step("Get order confirmation header")
    public String getConfirmationHeader() {
        return confirmHeader.shouldBe(visible).getText();
    }

    @Step("Get order confirmation text")
    public String getConfirmationText() {
        return confirmText.shouldBe(visible).getText();
    }

    public boolean isOrderConfirmed() {
        return confirmHeader.is(visible) &&
               confirmHeader.getText().contains("Thank you for your order");
    }

    @Step("Go back to products")
    public ProductsPage backToProducts() {
        backHomeButton.shouldBe(visible).click();
        return new ProductsPage().isLoaded();
    }
}
