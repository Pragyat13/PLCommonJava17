package com.plcommon.allure;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AllureScreenshotManager - Replaces EPAM Report Portal screenshot management.
 * <p>
 * Provides screenshot capture and attachment capabilities for Allure TestOps.
 * Screenshots are automatically attached to the current test context in Allure reports.
 * </p>
 *
 * <h3>Migration from Report Portal:</h3>
 * <ul>
 *   <li>{@code ReportPortal.emitLog(message, "INFO", date, file)} → {@code AllureScreenshotManager.captureScreenshot(driver, name)}</li>
 *   <li>RP screenshot on failure → {@code AllureScreenshotManager.captureScreenshotOnFailure(driver)}</li>
 * </ul>
 */
public final class AllureScreenshotManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllureScreenshotManager.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private AllureScreenshotManager() {
        // Utility class
    }

    /**
     * Capture a screenshot and attach it to the current Allure report step.
     * Replaces: Report Portal screenshot attachment via emitLog with file
     *
     * @param driver the WebDriver instance
     * @param name   descriptive name for the screenshot
     */
    public static void captureScreenshot(WebDriver driver, String name) {
        if (driver == null) {
            LOGGER.warn("WebDriver is null, cannot capture screenshot: {}", name);
            return;
        }

        if (!(driver instanceof TakesScreenshot)) {
            LOGGER.warn("WebDriver does not support screenshot capture (TakesScreenshot interface not implemented): {}", name);
            return;
        }

        try {
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshotBytes), "png");
            LOGGER.debug("Screenshot captured and attached: {}", name);
        } catch (Exception e) {
            LOGGER.error("Failed to capture screenshot: {}", name, e);
            AllureReportManager.logError("Screenshot capture failed: " + name, e);
        }
    }

    /**
     * Capture a screenshot on test failure with automatic timestamp naming.
     * Replaces: Report Portal's automatic screenshot on failure mechanism
     *
     * @param driver the WebDriver instance
     */
    public static void captureScreenshotOnFailure(WebDriver driver) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        captureScreenshot(driver, "Failure_Screenshot_" + timestamp);
    }

    /**
     * Capture a screenshot with a custom step name for better report readability.
     * Replaces: RP nested step with screenshot attachment
     *
     * @param driver   the WebDriver instance
     * @param stepName the step name to display in the report
     * @param name     the screenshot attachment name
     */
    public static void captureScreenshotWithStep(WebDriver driver, String stepName, String name) {
        Allure.step(stepName, () -> captureScreenshot(driver, name));
    }

    /**
     * Capture a full-page screenshot as bytes without attaching to report.
     * Useful for custom processing or conditional attachment.
     *
     * @param driver the WebDriver instance
     * @return screenshot bytes, or empty array if capture fails
     */
    public static byte[] captureScreenshotAsBytes(WebDriver driver) {
        if (driver == null) {
            LOGGER.warn("WebDriver is null, cannot capture screenshot");
            return new byte[0];
        }

        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            LOGGER.error("Failed to capture screenshot as bytes", e);
            return new byte[0];
        }
    }

    /**
     * Attach an existing screenshot (byte array) to the Allure report.
     * Replaces: RP log attachment from pre-captured screenshot data
     *
     * @param screenshotBytes the screenshot data
     * @param name            descriptive name for the screenshot
     */
    public static void attachScreenshot(byte[] screenshotBytes, String name) {
        if (screenshotBytes == null || screenshotBytes.length == 0) {
            LOGGER.warn("Empty screenshot data, skipping attachment: {}", name);
            return;
        }

        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshotBytes), "png");
        LOGGER.debug("Screenshot attached: {}", name);
    }

    /**
     * Capture and attach a screenshot with page URL information.
     * Provides additional context similar to RP's automatic URL logging.
     *
     * @param driver the WebDriver instance
     */
    public static void captureScreenshotWithContext(WebDriver driver) {
        if (driver == null) {
            LOGGER.warn("WebDriver is null, cannot capture screenshot with context");
            return;
        }

        String currentUrl = driver.getCurrentUrl();
        String title = driver.getTitle();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        String contextName = String.format("Screenshot [%s] - %s", timestamp, title);
        captureScreenshot(driver, contextName);

        AllureReportManager.attachText("Page Context",
                String.format("URL: %s%nTitle: %s%nTimestamp: %s", currentUrl, title, timestamp));
    }
}
