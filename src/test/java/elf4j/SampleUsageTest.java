package elf4j;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SampleUsageTest {
    static final Logger logger = Logger.instance();

    @Nested
    class plainText {
        @Test
        void declarationsAndLevels() {
            logger.log(
                    "Logger instance is thread-safe so it can be declared and used as a local, instance, or static variable");
            logger.log(
                    "Default severity level is decided by the logging provider implementation: {}", logger.getLevel());
            assertEquals(Level.INFO, logger.getLevel());
            Logger trace = logger.atTrace();
            assertEquals(Level.TRACE, trace.getLevel());
            assertFalse(trace.isEnabled());
            if (trace.isEnabled()) {
                trace.log("This message will Not print as TRACE is not enabled per the current configuration");
            }
            Logger.instance().atTrace().log("Nor will this message");
            Logger info = logger.atInfo();
            assertTrue(info.isInfoEnabled());
            if (info.isInfoEnabled()) {
                info.log("This message will print as INFO is enabled per the current configuration");
            }
            assertTrue(
                    logger.isInfoEnabled(),
                    "INFO is enabled by default per the current logging provider configuration");
            if (logger.isInfoEnabled()) {
                logger.info(
                        "Convenience short-hand methods combine Logger instance creation and logging, enabling similar usage of ELF4J compared to other logging APIs such as SLF4J");
            }
            trace.atWarn().log("Note that the severity level for this log is WARN, not TRACE");
            Logger.instance()
                    .atDebug()
                    .atError()
                    .atTrace()
                    .atWarn()
                    .atInfo()
                    .log("Not a practical example but the severity level for this log is INFO");
            Exception exception = new Exception("Test exception message");
            logger.error(
                    exception,
                    "Unlike other logging APIs such as SLF4J, the Throwable is always the First argument in an ELF4J multi-argument logging method signature");
        }
    }

    @Nested
    class textWithArguments {
        Logger info = logger.atInfo();

        @Test
        void lazyAndEagerArgumentsCanBeMixed() {
            info.log(
                    "Message can have any number of arguments of {} type, evaluated lazily when possible",
                    Object.class.getTypeName());
            info.atWarn()
                    .log(
                            "When eager {} and lazy {} types of arguments are mixed, Supplier downcast is required per lambda syntax because arguments are declared as generic Object rather than functional interface",
                            Object.class.getTypeName(),
                            (Supplier<String>) Supplier.class::getTypeName);
            info.atWarn()
                    .log(
                            "No downcast is required when all arguments are of lazy Supplier type e.g. via lambda expressions. In this log, the 1st lambda expression evaluates to {}, the 2nd to {}",
                            Supplier.class::getTypeName,
                            MethodHandles.lookup().lookupClass()::getTypeName);
        }
    }

    @Nested
    class throwable {
        @Test
        void asTheFirstArgument() {
            Exception exception = new Exception("Test exception message");
            logger.atError().log(exception);
            logger.atError().log(exception, "Exception is always the first argument to a log method");
            logger.atInfo().log(
                    exception, "The {} log {} and {} work the same way as usual", "Optional", "Message", (Supplier)
                            () -> "Arguments");
        }
    }
}
