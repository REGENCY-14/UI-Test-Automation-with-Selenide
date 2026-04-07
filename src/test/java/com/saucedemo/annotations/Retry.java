package com.saucedemo.annotations;

import com.saucedemo.extensions.RetryExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test method or test class for automatic retry on failure.
 *
 * <p>When applied, the {@link RetryExtension} will re-execute the annotated
 * test up to {@code test.retry.count} times (default: 2) before marking it
 * as failed. This is useful for tests that are known to be occasionally flaky
 * due to network latency, timing issues, or transient UI state.
 *
 * <h3>Usage on a single test method:</h3>
 * <pre>{@code
 *   @Test
 *   @Retry
 *   void flakyNetworkTest() {
 *       // This test will be retried up to 2 times on failure
 *   }
 * }</pre>
 *
 * <h3>Usage on an entire test class:</h3>
 * <pre>{@code
 *   @Retry
 *   class FlakyIntegrationTest extends BaseTest {
 *       // All tests in this class will be retried on failure
 *   }
 * }</pre>
 *
 * <h3>Configuring the retry count:</h3>
 * <pre>{@code
 *   mvn test -Dtest.retry.count=3
 * }</pre>
 *
 * <p>Note: {@code BaseTest} already registers {@link RetryExtension} globally,
 * so all tests extending {@code BaseTest} have retry behaviour by default.
 * Use this annotation on classes that do <em>not</em> extend {@code BaseTest}
 * but still need retry support.
 *
 * @see RetryExtension
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RetryExtension.class)
public @interface Retry {}
