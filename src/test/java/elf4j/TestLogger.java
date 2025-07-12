package elf4j;

import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

/** Util logger for internal usage of the elf4j API. Not meant for any external client applications. */
public enum TestLogger implements Logger {
    /** Logger instance for TRACE level logging. */
    TRACE(Level.TRACE),
    /** Logger instance for DEBUG level logging. */
    DEBUG(Level.DEBUG),
    /** Logger instance for INFO level logging. */
    INFO(Level.INFO),
    /** Logger instance for WARN level logging. */
    WARN(Level.WARN),
    /** Logger instance for ERROR level logging. */
    ERROR(Level.ERROR),
    /** Logger instance for disabling logging (OFF level). */
    OFF(Level.OFF);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final PrintStream printStream;
    private final Level level;
    private final Level thresholdOutputLevel;

    /**
     * Constructor for the TestLogger enum.
     *
     * @param level the logging level associated with this logger
     */
    TestLogger(Level level) {
        this.level = level;
        String outStreamType = System.getProperty("elf4j.internal.log.out.stream");
        if (outStreamType != null && outStreamType.trim().equalsIgnoreCase("stdout")) {
            this.printStream = System.out;
        } else {
            this.printStream = System.err;
        }
        String minLevel = System.getProperty("elf4j.internal.log.min.level");
        this.thresholdOutputLevel =
                minLevel == null || minLevel.trim().isEmpty() ? Level.INFO : Level.valueOf(minLevel.toUpperCase());
    }

    /**
     * Resolves the supplied object if it is a Supplier, otherwise returns the object itself.
     *
     * @param o the object or Supplier to resolve
     * @return the resolved object
     */
    private static Object supply(Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

    /**
     * Returns a logger instance for the specified logging level.
     *
     * @param level the logging level
     * @return the corresponding logger instance
     * @throws IllegalStateException if the specified level is not recognized
     */
    @Override
    public Logger atLevel(Level level) {
        if (this.level == level) {
            return this;
        }
        switch (level) {
            case TRACE:
                return TRACE;
            case DEBUG:
                return DEBUG;
            case INFO:
                return INFO;
            case WARN:
                return WARN;
            case ERROR:
                return ERROR;
            case OFF:
                return OFF;
            default:
                throw new IllegalStateException(level.toString());
        }
    }

    /**
     * Gets the logging level associated with this logger.
     *
     * @return the logging level
     */
    @Override
    public Level getLevel() {
        return this.level;
    }

    /**
     * Checks if logging is enabled for this logger based on the threshold output level.
     *
     * @return {@code true} if logging is enabled, {@code false} otherwise
     */
    @Override
    public boolean isEnabled() {
        return this.level.compareTo(thresholdOutputLevel) >= 0;
    }

    /**
     * Logs a message if logging is enabled.
     *
     * @param message the message to log
     */
    @Override
    public void log(Object message) {
        if (isEnabled()) {
            printStream.println(resolve(message));
        }
    }

    /**
     * Logs a formatted message with arguments if logging is enabled.
     *
     * @param message the message format
     * @param arguments the arguments to format into the message
     */
    @Override
    public void log(String message, Object... arguments) {
        if (isEnabled()) {
            printStream.println(resolve(message, arguments));
        }
    }

    /**
     * Logs a throwable if logging is enabled.
     *
     * @param throwable the throwable to log
     */
    @Override
    public void log(Throwable throwable) {
        if (isEnabled()) {
            this.log(throwable, (Object) null);
        }
    }

    /**
     * Logs a throwable with an accompanying message if logging is enabled.
     *
     * @param throwable the throwable to log
     * @param message the accompanying message
     */
    @Override
    public void log(Throwable throwable, Object message) {
        if (isEnabled()) {
            this.log(throwable, (String) supply(message), (Object) null);
        }
    }

    /**
     * Logs a throwable with a formatted message and arguments if logging is enabled.
     *
     * @param throwable the throwable to log
     * @param message the message format
     * @param arguments the arguments to format into the message
     */
    @Override
    public void log(Throwable throwable, String message, Object... arguments) {
        if (isEnabled()) {
            synchronized (printStream) {
                printStream.println(resolve(message, arguments));
                throwable.printStackTrace(printStream);
            }
        }
    }

    /**
     * Generates a log message prefix containing the current timestamp, logging level, and thread information.
     *
     * @return the log message prefix
     */
    private String prefix() {
        Thread thread = Thread.currentThread();
        return DATE_TIME_FORMATTER.format(OffsetDateTime.now()) + " " + this.level + " [" + thread.getName() + ","
                + thread.getId() + "] elf4j - ";
    }

    /**
     * Resolves a message by replacing placeholders with arguments and appending the log prefix.
     *
     * @param message the message format
     * @param arguments the arguments to replace placeholders
     * @return the resolved message
     */
    private CharSequence resolve(Object message, Object... arguments) {
        String suppliedMessage = prefix() + Objects.toString(supply(message), "");
        if (arguments == null || arguments.length == 0) {
            return suppliedMessage;
        }
        int messageLength = suppliedMessage.length();
        StringBuilder resolved = new StringBuilder();
        int i = 0;
        int j = 0;
        while (i < messageLength) {
            char character = suppliedMessage.charAt(i);
            if (character == '{'
                    && ((i + 1) < messageLength && suppliedMessage.charAt(i + 1) == '}')
                    && j < arguments.length) {
                resolved.append(supply(arguments[j++]));
                i += 2;
            } else {
                resolved.append(character);
                i += 1;
            }
        }
        return resolved;
    }
}
