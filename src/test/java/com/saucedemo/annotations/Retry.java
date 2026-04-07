package com.saucedemo.annotations;

import com.saucedemo.extensions.RetryExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test or class for automatic retry on failure.
 * Uses {@link RetryExtension} — retry count controlled by {@code test.retry.count} system property.
 *
 * Usage:
 * <pre>
 *   {@literal @}Test
 *   {@literal @}Retry
 *   void flakyTest() { ... }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RetryExtension.class)
public @interface Retry {}
