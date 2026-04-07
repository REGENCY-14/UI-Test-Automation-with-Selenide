package com.saucedemo.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;

/**
 * Abstract base class for all Page Object classes in the framework.
 *
 * <p>Implements the <em>Page Object Model (POM)</em> pattern by providing a
 * common contract and shared helper methods that every page must honour.
 * Concrete page classes extend this class and implement {@link #isLoaded()}
 * to assert that the page is fully rendered before any actions are performed.
 *
 * <h3>Design principles:</h3>
 * <ul>
 *   <li>Pages never contain assertions — those belong in the test layer.</li>
 *   <li>Action methods return {@code this} or the next page for fluent chaining.</li>
 *   <li>All element interactions go through Selenide's smart-wait API.</li>
 * </ul>
 *
 * <h3>Generic type parameter:</h3>
 * <p>The self-referential generic {@code <T extends BasePage<T>>} enables
 * fluent method chaining in subclasses without requiring unchecked casts.
 *
 * <p>Example subclass:
 * <pre>{@code
 *   public class LoginPage extends BasePage<LoginPage> {
 *       public LoginPage isLoaded() {
 *           loginButton.shouldBe(visible);
 *           return this;
 *       }
 *   }
 * }</pre>
 *
 * @param <T> the concrete page type (self-referential for fluent chaining)
 */
public abstract class BasePage<T extends BasePage<T>> {

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Navigates the browser to the given relative path and returns this page.
     *
     * <p>The path is appended to {@code Configuration.baseUrl} set in
     * {@code BaseTest.setUp()}. For example, passing {@code "/"} opens the
     * application root.
     *
     * @param relativePath the URL path relative to the base URL (e.g. {@code "/"})
     * @return this page instance for fluent chaining
     */
    @SuppressWarnings("unchecked")
    public T open(String relativePath) {
        com.codeborne.selenide.Selenide.open(relativePath);
        return (T) this;
    }

    // ── Page readiness contract ───────────────────────────────────────────────

    /**
     * Asserts that this page is fully loaded and ready for interaction.
     *
     * <p>Each concrete page must implement this method by waiting for its
     * primary anchor element (e.g. a page title or unique button) to become
     * visible. Tests should call this method immediately after navigation to
     * ensure the page is in a stable state before performing actions.
     *
     * @return this page instance for fluent chaining
     * @throws com.codeborne.selenide.ex.ElementNotFound if the page does not
     *         load within {@code Configuration.timeout} milliseconds
     */
    public abstract T isLoaded();

    // ── Shared helpers ────────────────────────────────────────────────────────

    /**
     * Waits for the given element to become visible and returns it.
     *
     * <p>Delegates to Selenide's built-in smart wait, which polls the DOM
     * until the element satisfies the {@code visible} condition or the global
     * timeout expires.
     *
     * @param element the {@link SelenideElement} to wait for
     * @return the same element once it is visible
     * @throws com.codeborne.selenide.ex.ElementNotFound if the element does
     *         not become visible within the configured timeout
     */
    protected SelenideElement waitForVisible(SelenideElement element) {
        return element.shouldBe(visible);
    }
}
