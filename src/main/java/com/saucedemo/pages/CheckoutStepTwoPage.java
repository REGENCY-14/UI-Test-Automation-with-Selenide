package com.saucedemo.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for Checkout Step Two — order summary / overview.
 */
public class CheckoutStepTwoPage extends BasePage<CheckoutStepTwoPage> {

    private final SelenideElement pageTitle      = $(".title");
    private final SelenideElement finishButton   = $("[data-test='finish']");
    private final SelenideElement cancelButton   = $("[data-test='cancel']");
    private final SelenideElement totalLabel     = $(".summary_total_label");
    private final SelenideElement taxLabel       = $(".summary_tax_label");
    private final ElementsCollection cartItems  = $$(".cart_item");

    @Override
    public CheckoutStepTwoPage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Checkout: Overview"));
        return this;
    }

    @Step("Get order total text")
    public String getTotal() {
        return totalLabel.shouldBe(visible).getText();
    }

    @Step("Get tax text")
    public String getTax() {
        return taxLabel.shouldBe(visible).getText();
    }

    @Step("Get number of items in order summary")
    public int getItemCount() {
        return cartItems.size();
    }

    @Step("Finish order")
    public CheckoutCompletePage finishOrder() {
        finishButton.shouldBe(visible).click();
        return new CheckoutCompletePage().isLoaded();
    }

    @Step("Cancel order")
    public ProductsPage cancelOrder() {
        cancelButton.click();
        return new ProductsPage().isLoaded();
    }
}
