package com.saucedemo.extensions;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.util.logging.Logger;

/**
 * JUnit 5 extension that automatically retries a failing test method up to a
 * configurable maximum number of times before marking it as failed.
 *
 * <p>This extension is useful for handling intermittently failing ("flaky")
 * tests caused by network latency, timing issues, or transient UI state
 * problems in browser automation.
 *
 * <h3>How it works:</h3>
 * <p>The extension implements {@link TestExecutionExceptionHandler}, which
 * JUnit 5 calls whenever a test method throws an exception. On each invocation,
 * the extension checks how many retry attempts have already been made for the
 * current test (tracked in the JUnit {@link ExtensionContext.Store}). If the
 * attempt count is below {@link #MAX_RETRIES}, the browser is closed for a
 * clean state, the counter is incremented, and the test method is re-invoked
 * via reflection. If the maximum is reached, the original exception is
 * re-thrown and the test is marked as failed.
 *
 * <h3>Configuration:</h3>
 * <p>The retry count is controlled by the {@code test.retry.count} system
 * property (default: {@code 2}):
 * <pre>{@code
 *   mvn test -Dtest.retry.count=3
 * }</pre>
 *
 * <h3>Registration:</h3>
 * <p>Registered globally in {@code BaseTest} via
 * {@code @ExtendWith(RetryExtension.class)}, so all test classes that extend
 * {@code BaseTest} automatically benefit from retry behaviour.
 *
 * <p>Individual tests can also opt in via the {@code @Retry} annotation:
 * <pre>{@code
 *   @Test
 *   @Retry
 *   void flakyTest() { ... }
 * }</pre>
 *
 * <h3>Limitation:</h3>
 * <p>Because JUnit 5 does not re-run {@code @BeforeEach} during a retry
 * invoked via this extension, test methods must be self-contained (i.e. they
 * must not rely on state set up by {@code @BeforeEach}). The browser is closed
 * between retries to ensure a clean session.
 */
public class RetryExtension implements TestExecutionExceptionHandler {

    /** Logger for retry warning messages. */
    private static final Logger LOG = Logger.getLogger(RetryExtension.class.getName());

    /**
     * Maximum number of retry attempts per test method.
     * Configurable via the {@code test.retry.count} system property.
     */
    private static final int MAX_RETRIES = Integer.parseInt(
            System.getProperty("test.retry.count", "2"));

    /**
     * Namespace used to isolate retry counters in the JUnit extension store,
     * preventing collisions with other extensions.
     */
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(RetryExtension.class);

    // ── TestExecutionExceptionHandler ─────────────────────────────────────────

    /**
     * Handles a test execution exception by retrying the test method if the
     * retry limit has not been reached.
     *
     * <p>On each retry:
     * <ol>
     *   <li>The attempt counter for this test is incremented in the store.</li>
     *   <li>A warning is logged with the attempt number and failure reason.</li>
     *   <li>The WebDriver is closed to ensure a clean browser state.</li>
     *   <li>The test method is re-invoked via reflection.</li>
     * </ol>
     *
     * <p>If the maximum retry count is exceeded, the original {@code throwable}
     * is re-thrown, causing JUnit to mark the test as failed.
     *
     * @param context   the JUnit 5 extension context for the current test
     * @param throwable the exception thrown by the test method
     * @throws Throwable the original exception if retries are exhausted
     */
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable)
            throws Throwable {

        // Retrieve the current attempt count from the store (default 0)
        int attempt = context.getStore(NAMESPACE)
                .getOrDefault(context.getUniqueId(), Integer.class, 0);

        if (attempt < MAX_RETRIES) {
            // Increment and persist the attempt counter
            attempt++;
            context.getStore(NAMESPACE).put(context.getUniqueId(), attempt);

            LOG.warning(String.format("[Retry] %s — attempt %d/%d failed: %s",
                    context.getDisplayName(), attempt, MAX_RETRIES, throwable.getMessage()));

            // Close the browser to get a clean state before the next attempt
            try {
                com.codeborne.selenide.Selenide.closeWebDriver();
            } catch (Exception ignored) {
                // Ignore errors during cleanup — proceed with retry regardless
            }

            // Re-invoke the test method on the same test instance
            context.getRequiredTestMethod().invoke(context.getRequiredTestInstance());

        } else {
            // Retry limit reached — propagate the failure
            throw throwable;
        }
    }
}
