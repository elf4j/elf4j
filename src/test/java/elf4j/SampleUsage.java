package elf4j;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

class SampleUsage {
    @Nested
    class plainText {
        Logger logger = Logger.instance();

        @Test
        void declarationsAndLevels() {
            logger.log("Logger instance is thread-safe so it can be declared as a local, instance, or static variable");
            logger.log("Default severity level is decided by the logging provider implementation");
            Logger trace = logger.atTrace();
            trace.log("Explicit severity level is specified by user i.e. TRACE");
            Logger.instance().atTrace().log("Same explicit level TRACE");
            logger.atDebug().log("Severity level is DEBUG");
            logger.atInfo().log("Severity level is INFO");
            trace.atWarn()
                    .log("Severity level is WARN i.e. The atWarn() method on ANY Logger instance returns a Logger instance at WARN level, regardless the level of the original Logger instance");
            logger.atError().log("Severity level is ERROR");
            Logger.instance()
                    .atDebug()
                    .atError()
                    .atTrace()
                    .atWarn()
                    .atInfo()
                    .log("Not a practical example but the severity level is INFO");
        }
    }

    @Nested
    class textWithArguments {
        Logger info = Logger.instance().atInfo();

        @Test
        void lazyAndEagerArgumentsCanBeMixed() {
            info.log("Message can have any number of arguments of {} type", Object.class.getTypeName());
            info.log(
                    "Lazy arguments (of {} type) whose values may be {} can be mixed with eager arguments of non-Supplier types",
                    Supplier.class.getTypeName(),
                    (Supplier) () -> "expensive to compute");
            info.atWarn()
                    .log("Note that the Supplier downcast is mandatory per lambda syntax because the arguments are declared as Object type rather than functional interface");
        }
    }

    @Nested
    class throwable {
        Logger logger = Logger.instance();

        @Test
        void asTheFirstArgument() {
            Exception exception = new Exception("Exception message");
            logger.atWarn().log(exception);
            logger.atError()
                    .log(exception,
                            "Exception is always passed in as the first argument to a logging method. The {} message and arguments that follow work the same way {}.",
                            "optional",
                            (Supplier) () -> "as usual");
        }
    }
}
