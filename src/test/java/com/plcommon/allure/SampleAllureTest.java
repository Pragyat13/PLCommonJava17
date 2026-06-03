package com.plcommon.allure;

import io.qameta.allure.*;
import io.qameta.allure.model.Status;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Sample test demonstrating Allure TestOps integration.
 * Shows how to replace Report Portal logging and annotations with Allure equivalents.
 */
@Listeners(AllureTestListener.class)
@Epic("Sample Tests")
@Feature("Allure TestOps Integration")
public class SampleAllureTest {

    @Test
    @Story("Logging")
    @Description("Demonstrates how to log messages using AllureReportManager (replaces RP logging)")
    @Severity(SeverityLevel.NORMAL)
    public void testLogging() {
        AllureReportManager.logInfo("Starting test execution");
        AllureReportManager.logDebug("Debug information for troubleshooting");

        // Simulate test action
        String result = "expected";

        AllureReportManager.logInfo("Test action completed with result: " + result);
        Assert.assertEquals(result, "expected", "Result should match expected value");
    }

    @Test
    @Story("Error Handling")
    @Description("Demonstrates error logging with Allure (replaces RP error logging)")
    @Severity(SeverityLevel.CRITICAL)
    public void testErrorLogging() {
        AllureReportManager.logInfo("Starting error handling test");

        try {
            // Simulate an operation that might fail
            simulateOperation();
            AllureReportManager.logInfo("Operation completed successfully");
        } catch (Exception e) {
            AllureReportManager.logError("Operation failed during test", e);
            // Re-throw or handle as needed
        }

        Assert.assertTrue(true, "Test completed");
    }

    @Test
    @Story("Steps")
    @Description("Demonstrates step-based reporting (replaces RP nested steps)")
    @Severity(SeverityLevel.MINOR)
    public void testStepReporting() {
        AllureReportManager.logStep("Initialize test data", Status.PASSED);
        AllureReportManager.logStep("Execute business logic", Status.PASSED);
        AllureReportManager.logStep("Validate results", Status.PASSED);

        // Attach additional context
        AllureReportManager.attachJson("Test Data", "{\"key\": \"value\", \"status\": \"active\"}");

        Assert.assertTrue(true);
    }

    @Step("Simulating operation")
    private void simulateOperation() {
        // Simulated operation
        AllureReportManager.logDebug("Performing simulated operation");
    }
}
