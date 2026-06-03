package com.plcommon.allure;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * AllureReportManager - Replaces EPAM Report Portal logging functionality.
 * <p>
 * Provides methods for logging messages, errors, and attaching data to Allure reports.
 * This class serves as a direct replacement for Report Portal's logging capabilities,
 * mapping RP log levels and attachments to Allure's step and attachment model.
 * </p>
 *
 * <h3>Migration from Report Portal:</h3>
 * <ul>
 *   <li>{@code ReportPortal.emitLog(message, level, date)} → {@code AllureReportManager.log(message, level)}</li>
 *   <li>{@code ReportPortal.emitLog(message, "ERROR", date)} → {@code AllureReportManager.logError(message, throwable)}</li>
 *   <li>{@code Launch.currentLaunch().log()} → {@code AllureReportManager.logStep(stepName, status)}</li>
 * </ul>
 */
public final class AllureReportManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllureReportManager.class);

    private AllureReportManager() {
        // Utility class
    }

    /**
     * Log an informational message to the Allure report.
     * Replaces: ReportPortal.emitLog(message, "INFO", Calendar.getInstance().getTime())
     *
     * @param message the log message
     */
    public static void logInfo(String message) {
        log(message, "INFO");
    }

    /**
     * Log a warning message to the Allure report.
     * Replaces: ReportPortal.emitLog(message, "WARN", Calendar.getInstance().getTime())
     *
     * @param message the log message
     */
    public static void logWarn(String message) {
        log(message, "WARN");
    }

    /**
     * Log a debug message to the Allure report.
     * Replaces: ReportPortal.emitLog(message, "DEBUG", Calendar.getInstance().getTime())
     *
     * @param message the log message
     */
    public static void logDebug(String message) {
        log(message, "DEBUG");
    }

    /**
     * Log a message with specified level to the Allure report as a step.
     * Replaces: ReportPortal.emitLog(message, level, date)
     *
     * @param message the log message
     * @param level   the log level (INFO, WARN, DEBUG, ERROR)
     */
    public static void log(String message, String level) {
        LOGGER.atLevel(mapToSlf4jLevel(level)).log("[{}] {}", level, message);

        Status status = mapLevelToStatus(level);
        Allure.step(String.format("[%s] %s", level, message), status);
    }

    /**
     * Log an error with full exception details to the Allure report.
     * Replaces: ReportPortal.emitLog(message, "ERROR", date) with attached exception
     *
     * @param message   the error description
     * @param throwable the exception that occurred
     */
    public static void logError(String message, Throwable throwable) {
        LOGGER.error(message, throwable);

        String stepName = String.format("[ERROR] %s: %s", message, throwable.getMessage());
        String uuid = UUID.randomUUID().toString();

        Allure.getLifecycle().startStep(uuid, new StepResult()
                .setName(stepName)
                .setStatus(Status.FAILED));

        // Attach the full stack trace
        attachText("Error Stack Trace", getStackTrace(throwable));

        Allure.getLifecycle().stopStep(uuid);
    }

    /**
     * Log a step with a specific status to the Allure report.
     * Replaces: Custom RP step logging
     *
     * @param stepName the name of the step
     * @param status   the Allure status
     */
    public static void logStep(String stepName, Status status) {
        String uuid = UUID.randomUUID().toString();
        Allure.getLifecycle().startStep(uuid, new StepResult()
                .setName(stepName)
                .setStatus(status));
        Allure.getLifecycle().stopStep(uuid);
    }

    /**
     * Attach text content to the current Allure test/step.
     * Replaces: ReportPortal log attachments for text content
     *
     * @param name    attachment name
     * @param content text content to attach
     */
    public static void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain", content);
    }

    /**
     * Attach HTML content to the current Allure test/step.
     *
     * @param name    attachment name
     * @param content HTML content to attach
     */
    public static void attachHtml(String name, String content) {
        Allure.addAttachment(name, "text/html", content);
    }

    /**
     * Attach JSON content to the current Allure test/step.
     *
     * @param name    attachment name
     * @param content JSON content to attach
     */
    public static void attachJson(String name, String content) {
        Allure.addAttachment(name, "application/json", content);
    }

    /**
     * Attach arbitrary byte content to the current Allure test/step.
     *
     * @param name      attachment name
     * @param mimeType  MIME type of the content
     * @param content   byte content to attach
     * @param extension file extension (e.g., "png", "json")
     */
    public static void attachBytes(String name, String mimeType, byte[] content, String extension) {
        Allure.getLifecycle().addAttachment(name, mimeType, extension, content);
    }

    /**
     * Attach content from a byte array as an input stream to the Allure report.
     *
     * @param name     attachment name
     * @param mimeType MIME type of the content
     * @param content  byte content
     */
    public static void attachStream(String name, String mimeType, byte[] content) {
        Allure.addAttachment(name, mimeType, new ByteArrayInputStream(content), "dat");
    }

    private static Status mapLevelToStatus(String level) {
        return switch (level.toUpperCase()) {
            case "ERROR", "FATAL" -> Status.FAILED;
            case "WARN" -> Status.BROKEN;
            case "DEBUG", "TRACE" -> Status.PASSED;
            default -> Status.PASSED;
        };
    }

    private static org.slf4j.event.Level mapToSlf4jLevel(String level) {
        return switch (level.toUpperCase()) {
            case "ERROR", "FATAL" -> org.slf4j.event.Level.ERROR;
            case "WARN" -> org.slf4j.event.Level.WARN;
            case "DEBUG" -> org.slf4j.event.Level.DEBUG;
            case "TRACE" -> org.slf4j.event.Level.TRACE;
            default -> org.slf4j.event.Level.INFO;
        };
    }

    private static String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        if (throwable.getCause() != null) {
            sb.append("Caused by: ").append(getStackTrace(throwable.getCause()));
        }
        return sb.toString();
    }
}
