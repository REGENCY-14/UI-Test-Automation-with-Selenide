package com.saucedemo.utils;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class providing static helper methods for attaching diagnostic
 * artifacts to Allure test reports.
 *
 * <p>Attachments captured by this class appear in the Allure report under
 * each test's "Attachments" section, making it easy to diagnose failures
 * without re-running tests.
 *
 * <h3>Available attachment types:</h3>
 * <ul>
 *   <li>PNG screenshot of the browser viewport</li>
 *   <li>Full HTML page source</li>
 *   <li>Current page URL</li>
 *   <li>Arbitrary plain-text content</li>
 *   <li>JSON payloads</li>
 * </ul>
 *
 * <p>Methods annotated with {@link Attachment} return the raw bytes or string
 * directly to Allure's aspect-oriented interceptor, which handles writing the
 * file to the results directory. Methods using {@link Allure#addAttachment}
 * stream content directly for more control over MIME type and file extension.
 *
 * <p>All methods require an active WebDriver session. They are typically
 * called from {@code BaseTest.tearDown()} or inside {@code @AfterEach} hooks.
 */
public final class AllureAttachmentUtils {

    /** Utility class — prevent instantiation. */
    private AllureAttachmentUtils() {}

    // ── Screenshot ────────────────────────────────────────────────────────────

    /**
     * Captures a PNG screenshot of the current browser viewport and attaches
     * it to the Allure report.
     *
     * <p>Uses the WebDriver {@link TakesScreenshot} interface to capture the
     * screenshot as a byte array. The {@link Attachment} annotation instructs
     * the Allure AspectJ agent to store the returned bytes as an image
     * attachment named "Screenshot".
     *
     * @return the screenshot as a PNG byte array
     */
    @Attachment(value = "Screenshot", type = "image/png")
    public static byte[] takeScreenshot() {
        return ((TakesScreenshot) WebDriverRunner.getWebDriver())
                .getScreenshotAs(OutputType.BYTES);
    }

    // ── Page source ───────────────────────────────────────────────────────────

    /**
     * Captures the full HTML source of the current page and attaches it to
     * the Allure report.
     *
     * <p>Useful for diagnosing DOM state at the time of a test failure,
     * especially when the screenshot alone is insufficient.
     *
     * @return the page source encoded as UTF-8 bytes
     */
    @Attachment(value = "Page Source", type = "text/html")
    public static byte[] attachPageSource() {
        return WebDriverRunner.getWebDriver()
                .getPageSource()
                .getBytes(StandardCharsets.UTF_8);
    }

    // ── Current URL ───────────────────────────────────────────────────────────

    /**
     * Captures the current browser URL and attaches it to the Allure report
     * as a plain-text attachment.
     *
     * <p>Helpful for confirming which page the browser was on at the time of
     * a failure, particularly in multi-step checkout flows.
     *
     * @return the current URL string
     */
    @Attachment(value = "Page URL", type = "text/plain")
    public static String attachCurrentUrl() {
        return WebDriverRunner.getWebDriver().getCurrentUrl();
    }

    // ── Arbitrary text ────────────────────────────────────────────────────────

    /**
     * Attaches arbitrary plain-text content to the Allure report under the
     * given name.
     *
     * <p>Use this for attaching log snippets, request/response bodies, or
     * any other diagnostic text that doesn't fit the other attachment types.
     *
     * @param name    the display name for the attachment in the report
     * @param content the text content to attach
     */
    public static void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain",
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), ".txt");
    }

    // ── JSON ──────────────────────────────────────────────────────────────────

    /**
     * Attaches a JSON string to the Allure report under the given name.
     *
     * <p>The attachment is stored with the {@code application/json} MIME type,
     * allowing Allure to render it with syntax highlighting in the report UI.
     *
     * @param name the display name for the attachment in the report
     * @param json the JSON string to attach
     */
    public static void attachJson(String name, String json) {
        Allure.addAttachment(name, "application/json",
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), ".json");
    }
}
