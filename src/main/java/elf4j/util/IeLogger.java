package elf4j.util;

import elf4j.Level;
import elf4j.Logger;

import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 */
public enum IeLogger implements Logger {
    /**
     *
     */
    TRACE(Level.TRACE),
    /**
     *
     */
    DEBUG(Level.DEBUG),
    /**
     *
     */
    INFO(Level.INFO),
    /**
     *
     */
    WARN(Level.WARN),
    /**
     *
     */
    ERROR(Level.ERROR),
    /**
     *
     */
    OFF(Level.OFF);
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final PrintStream printStream;
    private final Level level;
    private final Level mininumLevel;

    IeLogger(Level level) {
        this.level = level;
        String outStreamType = System.getProperty("elf4j.internal.log.out.stream");
        if (outStreamType != null && outStreamType.trim().equalsIgnoreCase("stdout")) {
            this.printStream = System.out;
        } else {
            this.printStream = System.err;
        }
        String minLevel = System.getProperty("elf4j.internal.log.min.level");
        this.mininumLevel =
                minLevel == null || minLevel.trim().isEmpty() ? Level.INFO : Level.valueOf(minLevel.toUpperCase());
    }

    private static Object supply(Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

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

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isEnabled() {
        return this.level.compareTo(mininumLevel) >= 0;
    }

    @Override
    public void log(Object message) {
        if (isEnabled()) {
            printStream.println(resolve(message));
        }
    }

    @Override
    public void log(String message, Object... arguments) {
        if (isEnabled()) {
            printStream.println(resolve(message, arguments));
        }
    }

    @Override
    public void log(Throwable throwable) {
        if (isEnabled()) {
            this.log(throwable, null);
        }
    }

    @Override
    public void log(Throwable throwable, Object message) {
        if (isEnabled()) {
            this.log(throwable, (String) supply(message), (Object) null);
        }
    }

    @Override
    public void log(Throwable throwable, String message, Object... arguments) {
        if (isEnabled()) {
            synchronized (printStream) {
                printStream.println(resolve(message, arguments));
                throwable.printStackTrace(printStream);
            }
        }
    }

    private String prefix() {
        Thread thread = Thread.currentThread();
        return DATE_TIME_FORMATTER.format(OffsetDateTime.now()) + " " + this.level + " [" + thread.getName() + ","
                + thread.getId() + "] elf4j - ";
    }

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
            if (character == '{' && ((i + 1) < messageLength && suppliedMessage.charAt(i + 1) == '}')
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
