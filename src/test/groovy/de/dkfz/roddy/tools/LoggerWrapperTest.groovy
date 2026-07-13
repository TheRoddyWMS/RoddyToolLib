/*
 * Copyright (c) 2024 DKFZ.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/TheRoddyWMS/RoddyToolLib/LICENSE).
 */

package de.dkfz.roddy.tools

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.util.logging.ConsoleHandler
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * Covers LoggerWrapper#severe, in particular the merged-message behaviour
 * introduced when the class was migrated to Groovy 4, and LoggerWrapper#setup,
 * which wires up the actual console Filter and Formatter used for real
 * logging output.
 */
class LoggerWrapperTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    private List<LogRecord> records
    private Logger javaLogger
    private Handler capturingHandler

    private List<Handler> originalRootHandlers
    private boolean originalUseParentHandlers

    @Before
    void saveRootLoggerState() {
        Logger root = Logger.getLogger("")
        originalRootHandlers = root.getHandlers().toList()
        originalUseParentHandlers = root.getUseParentHandlers()
    }

    @After
    void restoreRootLoggerState() {
        Logger root = Logger.getLogger("")
        root.getHandlers().each { root.removeHandler(it) }
        originalRootHandlers.each { root.addHandler(it) }
        root.setUseParentHandlers(originalUseParentHandlers)
    }

    private LoggerWrapper attachWrapper(String loggerName) {
        records = []
        javaLogger = Logger.getLogger(loggerName)
        javaLogger.setUseParentHandlers(false)
        capturingHandler = new Handler() {
            @Override
            void publish(LogRecord record) {
                records << record
            }

            @Override
            void flush() {
            }

            @Override
            void close() throws SecurityException {
            }
        }
        javaLogger.addHandler(capturingHandler)

        LoggerWrapper.setVerbosityLevel(LoggerWrapper.VERBOSITY_SEVERE)
        LoggerWrapper.setCentralLogFile(temporaryFolder.newFile("central.tsv"))

        return LoggerWrapper.getLogger(loggerName)
    }

    private ConsoleHandler setUpAndGetConsoleHandler() {
        LoggerWrapper.setup(temporaryFolder.newFolder())
        return (ConsoleHandler) Logger.getLogger("").getHandlers().find { it instanceof ConsoleHandler }
    }

    @Test
    void testSevereWithExceptionLogsSingleMergedRecord() {
        LoggerWrapper logger = attachWrapper("LoggerWrapperTest.withException")
        Exception exception = new RuntimeException("boom")

        logger.severe("Something failed", exception)

        assert records.size() == 1
        LogRecord record = records[0]
        assert record.level == Level.SEVERE
        assert record.message.startsWith("Something failed\nStacktrace: ")
        // getStackTraceAsString joins stack frames, so the top frame is the
        // line above where the exception was created, inside this test method.
        assert record.message.contains("LoggerWrapperTest.testSevereWithExceptionLogsSingleMergedRecord")
    }

    @Test
    void testSevereWithoutExceptionLogsPlainMessage() {
        LoggerWrapper logger = attachWrapper("LoggerWrapperTest.withoutException")

        logger.severe("Something failed")

        assert records.size() == 1
        LogRecord record = records[0]
        assert record.level == Level.SEVERE
        assert record.message == "Something failed"
        assert !record.message.contains("Stacktrace:")
    }

    @Test
    void testSevereWithExceptionWritesMergedMessageToCentralLogFile() {
        attachWrapper("LoggerWrapperTest.logFile").severe("Something failed", new RuntimeException("boom"))

        String logFileContent = LoggerWrapper.getCentralLogFile().text

        assert logFileContent.contains("Something failed")
        assert logFileContent.contains("Stacktrace:")
        assert logFileContent.contains("LoggerWrapperTest.testSevereWithExceptionWritesMergedMessageToCentralLogFile")
    }

    @Test
    void testSetupInstallsSingleConsoleHandler() {
        ConsoleHandler handler = setUpAndGetConsoleHandler()

        assert handler != null
        assert Logger.getLogger("").getHandlers().length == 1
    }

    @Test
    void testSetupFilterRejectsSshjLoggerRecords() {
        ConsoleHandler handler = setUpAndGetConsoleHandler()
        LogRecord sshjRecord = new LogRecord(Level.INFO, "connection noise")
        sshjRecord.setLoggerName("net.schmizz.sshj.transport.TransportImpl")

        assert !handler.getFilter().isLoggable(sshjRecord)
    }

    @Test
    void testSetupFilterAcceptsNonSshjLoggerRecords() {
        ConsoleHandler handler = setUpAndGetConsoleHandler()
        LogRecord appRecord = new LogRecord(Level.INFO, "hello")
        appRecord.setLoggerName("de.dkfz.roddy.tools.SomeClass")

        assert handler.getFilter().isLoggable(appRecord)
    }

    @Test
    void testSetupFormatterIncludesLevelSourceMethodAndLoggerName() {
        ConsoleHandler handler = setUpAndGetConsoleHandler()
        LoggerWrapper.setVerbosityLevel(LoggerWrapper.VERBOSITY_HIGH)
        LogRecord record = new LogRecord(Level.WARNING, "careful now")
        record.setSourceMethodName("doStuff")
        record.setLoggerName("de.dkfz.roddy.tools.SomeClass")

        String formatted = handler.getFormatter().format(record)

        assert formatted.contains("WARNING")
        assert formatted.contains("doStuff")
        assert formatted.contains("de.dkfz.roddy.tools.SomeClass")
        assert formatted.contains("careful now")
    }

    @Test
    void testSetupFormatterPrintsBareMessageAtLowestVerbosity() {
        ConsoleHandler handler = setUpAndGetConsoleHandler()
        LoggerWrapper.setVerbosityLevel(LoggerWrapper.VERBOSITY_SEVERE)
        LogRecord record = new LogRecord(Level.SEVERE, "boom message")
        record.setSourceMethodName("doStuff")
        record.setLoggerName("de.dkfz.roddy.tools.SomeClass")

        String formatted = handler.getFormatter().format(record)

        assert formatted == "boom message" + System.getProperty("line.separator")
    }

    @Test
    void testSetupFormatterAppendsThrownStacktrace() {
        ConsoleHandler handler = setUpAndGetConsoleHandler()
        LoggerWrapper.setVerbosityLevel(LoggerWrapper.VERBOSITY_HIGH)
        LogRecord record = new LogRecord(Level.SEVERE, "failure")
        record.setSourceMethodName("doStuff")
        record.setLoggerName("de.dkfz.roddy.tools.SomeClass")
        record.setThrown(new RuntimeException("kaboom"))

        String formatted = handler.getFormatter().format(record)

        assert formatted.contains("Throwable occurred:")
        assert formatted.contains("RuntimeException: kaboom")
    }

    @Test
    void testSetupWithAppConfigAppliesConfiguredDirectoryAndPrefix() {
        File configuredDir = temporaryFolder.newFolder("configured-logs")
        AppConfig appConfig = new AppConfig()
        appConfig.setProperty("applicationLogDirectory", configuredDir.absolutePath)
        appConfig.setProperty("logFilesPrefix", "myprefix")
        appConfig.setProperty("logExtensively", "true")

        LoggerWrapper.setup(appConfig)

        File centralLogFile = LoggerWrapper.getCentralLogFile(true)
        assert centralLogFile.parentFile == configuredDir
        assert centralLogFile.name.startsWith("myprefix_")
    }

    @Test
    void testSevereEndToEndWritesFormattedMergedMessageToConsole() {
        PrintStream originalErr = System.err
        ByteArrayOutputStream captured = new ByteArrayOutputStream()
        try {
            System.err = new PrintStream(captured)
            LoggerWrapper.setup(temporaryFolder.newFolder())
            LoggerWrapper.setVerbosityLevel(LoggerWrapper.VERBOSITY_SEVERE)

            LoggerWrapper.getLogger("LoggerWrapperTest.endToEnd")
                    .severe("Something failed", new RuntimeException("boom"))
        } finally {
            System.err = originalErr
        }

        String output = captured.toString()
        assert output.contains("Something failed")
        assert output.contains("Stacktrace:")
        assert output.contains("LoggerWrapperTest.testSevereEndToEndWritesFormattedMergedMessageToConsole")
    }
}
