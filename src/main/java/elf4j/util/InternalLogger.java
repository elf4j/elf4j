package elf4j.util;

import elf4j.Level;

import java.util.Objects;

/**
 *
 */
public enum InternalLogger {
    /**
     *
     */
    INSTANCE;
    /**
     * System property to set minimum severity level of the internal logger's output
     */
    public static final String ELF4J_INTERNAL_LOG_LEVEL = "elf4j.internal.log.level";
    private final Level mininumLevel;

    InternalLogger() {
        String minLevel = System.getProperty(ELF4J_INTERNAL_LOG_LEVEL);
        mininumLevel =
                minLevel == null || minLevel.trim().isEmpty() ? Level.TRACE : Level.valueOf(minLevel.toUpperCase());
    }

    /**
     * @param level   to log
     * @param message to log
     */
    public void log(Level level, String message) {
        if (level.compareTo(mininumLevel) < 0) {
            return;
        }
        System.err.printf("ELF4J status %s: %s%n", level, message);
    }

    /**
     * @param level     to log
     * @param throwable to log
     * @param message   to log
     */
    public void log(Level level, Throwable throwable, String message) {
        if (level.compareTo(mininumLevel) < 0) {
            return;
        }
        log(level, message);
        Objects.requireNonNull(throwable).printStackTrace();
    }
}
