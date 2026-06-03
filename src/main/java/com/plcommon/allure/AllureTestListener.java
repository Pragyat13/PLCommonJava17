package com.plcommon.allure;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * AllureTestListener - TestNG listener that replaces EPAM Report Portal's test lifecycle integration.
 * <p>
 * Automatically captures errors, screenshots, and test metadata for Allure TestOps reporting.
 * Register this listener in your testng.xml or via {@code @Listeners} annotation.
 * </p>
 *
 * <h3>Migration from Report Portal:</h3>
 * <ul>
 *   <li>Replace {@code @Listeners(ReportPortalTestNGListener.class)} with {@code @Listeners(AllureTestListener.class)}</li>
 *   <li>Remove Report Portal agent from testng.xml listener configuration</li>
 *   <li>This listener auto-attaches screenshots on failure (same as RP behavior)</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>
 * // Option 1: Annotation-based
 * {@literal @}Listeners(AllureTestListener.class)
 * public class MyTestClass { ... }
 *
 * // Option 2: testng.xml
 * &lt;listeners&gt;
 *   &lt;listener class-name="com.plcommon.allure.AllureTestListener"/&gt;
 * &lt;/listeners&gt;
 * </pre>
 */
public class AllureTestListener implements ITestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllureTestListener.class);
    private static final String WEBDRIVER_ATTRIBUTE = "WebDriver";

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info("Test started: {}", result.getMethod().getMethodName());
        AllureReportManager.logInfo("Test started: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info("Test passed: {}", result.getMethod().getMethodName());
        AllureReportManager.logInfo("Test passed: " + result.getMethod().getQualifiedName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();

        LOGGER.error("Test failed: {}", testName, throwable);

        // Log error details to Allure (replaces RP error logging)
        if (throwable != null) {
            AllureReportManager.logError("Test failed: " + testName, throwable);
        }

        // Capture screenshot on failure (replaces RP automatic screenshot)
        captureFailureScreenshot(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warn("Test skipped: {}", result.getMethod().getMethodName());

        if (result.getThrowable() != null) {
            AllureReportManager.logWarn("Test skipped due to: " + result.getThrowable().getMessage());
        } else {
            AllureReportManager.logWarn("Test skipped: " + result.getMethod().getQualifiedName());
        }
    }

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info("Test suite started: {}", context.getName());
        Allure.suite(context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info("Test suite finished: {} | Passed: {} | Failed: {} | Skipped: {}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());

        // Attach suite summary (replaces RP launch summary)
        String summary = String.format(
                "Suite: %s%nPassed: %d%nFailed: %d%nSkipped: %d",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());

        AllureReportManager.attachText("Suite Summary - " + context.getName(), summary);
    }

    /**
     * Captures a screenshot on test failure.
     * Looks for WebDriver instance in the test context attributes.
     */
    private void captureFailureScreenshot(ITestResult result) {
        WebDriver driver = getWebDriverFromContext(result);
        if (driver != null) {
            AllureScreenshotManager.captureScreenshotOnFailure(driver);
        } else {
            LOGGER.debug("No WebDriver found in test context, skipping failure screenshot for: {}",
                    result.getMethod().getMethodName());
        }
    }

    /**
     * Retrieves WebDriver from the test context.
     * Tests should set the WebDriver attribute using:
     * {@code ITestContext.setAttribute("WebDriver", driver)}
     */
    private WebDriver getWebDriverFromContext(ITestResult result) {
        // Try from test context
        Object driverObj = result.getTestContext().getAttribute(WEBDRIVER_ATTRIBUTE);
        if (driverObj instanceof WebDriver driver) {
            return driver;
        }

        // Try from test class instance (if it implements HasWebDriver)
        Object testInstance = result.getInstance();
        if (testInstance instanceof HasWebDriver hasDriver) {
            return hasDriver.getWebDriver();
        }

        return null;
    }
}
