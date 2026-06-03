# PLCommonJava17

Common Java 17 library with **Allure TestOps** integration for test reporting, error logging, and screenshot management.

## Overview

This library replaces EPAM Report Portal with Allure TestOps while maintaining the same test reporting capabilities:

| Feature | Report Portal (Before) | Allure TestOps (After) |
|---------|----------------------|----------------------|
| Error Logging | `ReportPortal.emitLog(msg, "ERROR", date)` | `AllureReportManager.logError(msg, throwable)` |
| Info Logging | `ReportPortal.emitLog(msg, "INFO", date)` | `AllureReportManager.logInfo(msg)` |
| Screenshot on Failure | RP Agent auto-capture | `AllureScreenshotManager.captureScreenshotOnFailure(driver)` |
| Screenshot Attachment | `ReportPortal.emitLog(msg, level, date, file)` | `AllureScreenshotManager.captureScreenshot(driver, name)` |
| Test Listener | `ReportPortalTestNGListener` | `AllureTestListener` |
| Step Logging | RP nested steps | `AllureReportManager.logStep(name, status)` |
| File Attachments | RP log attachments | `AllureReportManager.attachText/Json/Html(name, content)` |

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.plcommon</groupId>
    <artifactId>PLCommonJava17</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Register the Test Listener

**Option A: Annotation-based**
```java
@Listeners(AllureTestListener.class)
public class MyTestClass extends BaseTest { ... }
```

**Option B: testng.xml**
```xml
<listeners>
    <listener class-name="com.plcommon.allure.AllureTestListener"/>
</listeners>
```

### 3. Enable Automatic Screenshot on Failure

Implement `HasWebDriver` in your base test class:

```java
public class BaseTest implements HasWebDriver {
    protected WebDriver driver;

    @Override
    public WebDriver getWebDriver() {
        return driver;
    }
}
```

### 4. Use Logging in Tests

```java
// Info logging
AllureReportManager.logInfo("Starting checkout flow");

// Error logging with exception
AllureReportManager.logError("Payment failed", exception);

// Manual screenshot
AllureScreenshotManager.captureScreenshot(driver, "After Login");

// Attach data
AllureReportManager.attachJson("API Response", responseBody);
```

## Migration Guide from EPAM Report Portal

### Step 1: Update Dependencies

Remove from `pom.xml`:
```xml
<!-- REMOVE these Report Portal dependencies -->
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>agent-java-testng</artifactId>
</dependency>
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>logger-java-logback</artifactId>
</dependency>
```

Add Allure TestOps dependencies (included in this library's POM).

### Step 2: Remove RP Configuration Files

Delete:
- `reportportal.properties`
- Any RP-specific logback appender configuration

### Step 3: Add Allure Configuration

The `allure.properties` file is included in this library. For TestOps integration, add `allure-testops.properties`:

```properties
allure.testops.endpoint=https://your-testops-instance.example.com
allure.testops.project.id=YOUR_PROJECT_ID
allure.testops.token=YOUR_API_TOKEN
```

### Step 4: Replace Listener References

Replace all references to `ReportPortalTestNGListener` with `AllureTestListener`.

### Step 5: Replace Logging Calls

Use find-and-replace across your test suites:

| Search | Replace With |
|--------|-------------|
| `ReportPortal.emitLog(msg, "INFO", date)` | `AllureReportManager.logInfo(msg)` |
| `ReportPortal.emitLog(msg, "ERROR", date)` | `AllureReportManager.logError(msg, exception)` |
| `ReportPortal.emitLog(msg, "WARN", date)` | `AllureReportManager.logWarn(msg)` |
| `ReportPortal.emitLog(msg, "DEBUG", date)` | `AllureReportManager.logDebug(msg)` |

## Building

```bash
mvn clean compile
```

## Running Tests with Allure Report

```bash
mvn clean test
mvn allure:serve   # Opens Allure report in browser
```

## Project Structure

```
src/main/java/com/plcommon/allure/
├── AllureReportManager.java      # Logging & attachments (replaces RP logging)
├── AllureScreenshotManager.java  # Screenshot capture & management
├── AllureTestListener.java       # TestNG listener (replaces RP listener)
└── HasWebDriver.java             # Interface for WebDriver access
```

## Requirements

- Java 17+
- Maven 3.8+
- Allure 2.25+