package com.plcommon.allure;

import org.openqa.selenium.WebDriver;

/**
 * Interface for test classes that manage a WebDriver instance.
 * <p>
 * Implement this interface in your test base class to enable automatic
 * screenshot capture on failure by {@link AllureTestListener}.
 * </p>
 *
 * <h3>Migration from Report Portal:</h3>
 * <p>
 * If your tests previously relied on RP's automatic WebDriver detection,
 * implement this interface in your base test class to achieve the same behavior
 * with Allure TestOps.
 * </p>
 */
public interface HasWebDriver {

    /**
     * Returns the WebDriver instance managed by the test class.
     *
     * @return the current WebDriver instance, or null if not available
     */
    WebDriver getWebDriver();
}
