package elf4j.util;

import elf4j.Level;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 */
public enum InternalLogger {
    /**
     *
     */
    INSTANCE;
    private final Level mininumLevel;

    InternalLogger() {
        String minLevel = System.getProperty("elf4j.internal.log.min.level");
        mininumLevel =
                minLevel == null || minLevel.trim().isEmpty() ? Level.TRACE : Level.valueOf(minLevel.toUpperCase());
    }

    /**
     * @param level
     *         to log
     * @param message
     *         to log
     */
    public void log(Level level, String message) {
        if (level.compareTo(mininumLevel) < 0) {
            return;
        }
        System.err.printf("ELF4J status %s: %s%n", level, message);
    }

    /**
     * @param level
     *         to log
     * @param throwable
     *         to log
     * @param message
     *         to log
     */
    public void log(Level level, Throwable throwable, String message) {
        if (level.compareTo(mininumLevel) < 0) {
            return;
        }
        StringWriter stringWriter = new StringWriter();
        stringWriter.append(message).append(System.lineSeparator());
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
        }
        log(level, stringWriter.toString());
    }
}
