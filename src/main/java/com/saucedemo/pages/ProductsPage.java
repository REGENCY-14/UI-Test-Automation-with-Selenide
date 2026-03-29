package com.saucedemo.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for the Products / Inventory page.
 */
public class ProductsPage extends BasePage<ProductsPage> {

    private final SelenideElement pageTitle       = $(".title");
    private final SelenideElement cartIcon        = $(".shopping_cart_link");
    private final SelenideElement cartBadge       = $(".shopping_cart_badge");
    private final SelenideElement sortDropdown    = $("[data-test='product-sort-container']");
    private final ElementsCollection productCards = $$(".inventory_item");
    private final ElementsCollection productNames = $$(".inventory_item_name");
    private final ElementsCollection addToCartBtns = $$("[data-test^='add-to-cart']");

    @Override
    public ProductsPage isLoaded() {
        pageTitle.shouldBe(visible).shouldHave(text("Products"));
        return this;
    }

    @Step("Add product to cart by name: {productName}")
    public ProductsPage addToCartByName(String productName) {
        productNames.findBy(text(productName))
                .closest(".inventory_item")
                .$("[data-test^='add-to-cart']")
                .click();
        return this;
    }

    @Step("Remove product from cart by name: {productName}")
    public ProductsPage removeFromCartByName(String productName) {
        productNames.findBy(text(productName))
                .closest(".inventory_item")
                .$("[data-test^='remove']")
                .click();
        return this;
    }

    @Step("Get cart item count")
    public int getCartCount() {
        if (cartBadge.is(visible)) {
            return Integer.parseInt(cartBadge.getText());
        }
        return 0;
    }

    @Step("Open cart")
    public CartPage openCart() {
        cartIcon.click();
        return new CartPage().isLoaded();
    }

    @Step("Sort products by: {option}")
    public ProductsPage sortBy(String option) {
        sortDropdown.selectOption(option);
        return this;
    }

    public int getProductCount() {
        return productCards.size();
    }

    public String getFirstProductName() {
        return productNames.first().shouldBe(visible).getText();
    }
}
