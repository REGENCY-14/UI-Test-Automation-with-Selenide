package com.saucedemo.extensions;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.util.logging.Logger;

/**
 * JUnit 5 extension that retries a failed test up to {@code MAX_RETRIES} times.
 * Register via @ExtendWith(RetryExtension.class) or in BaseTest.
 *
 * The retry count is configurable via the system property {@code test.retry.count}
 * (default: 2).
 */
public class RetryExtension implements TestExecutionExceptionHandler {

    private static final Logger LOG = Logger.getLogger(RetryExtension.class.getName());
    private static final int MAX_RETRIES = Integer.parseInt(
            System.getProperty("test.retry.count", "2"));

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(RetryExtension.class);

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable)
            throws Throwable {

        int attempt = context.getStore(NAMESPACE)
                .getOrDefault(context.getUniqueId(), Integer.class, 0);

        if (attempt < MAX_RETRIES) {
            attempt++;
            context.getStore(NAMESPACE).put(context.getUniqueId(), attempt);
            LOG.warning(String.format("[Retry] %s — attempt %d/%d failed: %s",
                    context.getDisplayName(), attempt, MAX_RETRIES, throwable.getMessage()));

            // Close browser to get a clean state before retry
            try {
                com.codeborne.selenide.Selenide.closeWebDriver();
            } catch (Exception ignored) {}

            // Re-invoke the test method directly
            context.getRequiredTestMethod().invoke(context.getRequiredTestInstance());
        } else {
            throw throwable;
        }
    }
}
