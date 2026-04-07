package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;

/**
 * Base class for all Page Objects.
 * Provides common navigation and element interaction helpers.
 */
public abstract class BasePage<T extends BasePage<T>> {

    /**
     * Navigate to the page's relative path.
     */
    @SuppressWarnings("unchecked")
    public T open(String relativePath) {
        com.codeborne.selenide.Selenide.open(relativePath);
        return (T) this;
    }

    /**
     * Assert the page is fully loaded. Each subclass defines its own anchor element.
     */
    public abstract T isLoaded();

    /**
     * Wait for an element to be visible.
     */
    protected SelenideElement waitForVisible(SelenideElement element) {
        return element.shouldBe(visible);
    }
}
