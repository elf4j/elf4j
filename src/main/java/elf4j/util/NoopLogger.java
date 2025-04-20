/*
 * MIT License
 *
 * Copyright (c) 2022 ELF4J
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package elf4j.util;

import elf4j.Level;
import elf4j.Logger;

/**
 * No-op implementation of the {@link Logger} API. This implementation does nothing and is used as a placeholder or
 * default logger.
 */
public enum NoopLogger implements Logger {
    /** No-op logger for TRACE level */
    TRACE(Level.TRACE),
    /** No-op logger for DEBUG level */
    DEBUG(Level.DEBUG),
    /** No-op logger for INFO level */
    INFO(Level.INFO),
    /** No-op logger for WARN level */
    WARN(Level.WARN),
    /** No-op logger for ERROR level */
    ERROR(Level.ERROR),
    /** No-op logger for OFF level */
    OFF(Level.OFF);

    private final Level level;

    /**
     * Constructor for the NoopLogger.
     *
     * @param level the logging level associated with this logger
     */
    NoopLogger(Level level) {
        this.level = level;
    }

    /**
     * Returns a NoopLogger instance for the specified logging level.
     *
     * @param level the logging level
     * @return the corresponding NoopLogger instance
     * @throws IllegalArgumentException if the specified level is not recognized
     */
    @Override
    public Logger atLevel(Level level) {
        switch (level) {
            case TRACE:
                return NoopLogger.TRACE;
            case DEBUG:
                return NoopLogger.DEBUG;
            case INFO:
                return NoopLogger.INFO;
            case WARN:
                return NoopLogger.WARN;
            case ERROR:
                return NoopLogger.ERROR;
            case OFF:
                return NoopLogger.OFF;
            default:
                throw new IllegalArgumentException(level.toString());
        }
    }

    /**
     * Gets the logging level associated with this NoopLogger.
     *
     * @return the logging level
     */
    @Override
    public Level getLevel() {
        return this.level;
    }

    /**
     * Indicates whether logging is enabled for this logger.
     *
     * @return always {@code false} as this is a no-op logger
     */
    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * Logs a message. This implementation does nothing.
     *
     * @param message the message to log
     */
    @Override
    public void log(Object message) {
        // noop
    }

    /**
     * Logs a formatted message with arguments. This implementation does nothing.
     *
     * @param message the message format
     * @param arguments the arguments to format into the message
     */
    @Override
    public void log(String message, Object... arguments) {
        // noop
    }

    /**
     * Logs a throwable. This implementation does nothing.
     *
     * @param throwable the throwable to log
     */
    @Override
    public void log(Throwable throwable) {
        // noop
    }

    /**
     * Logs a throwable with an accompanying message. This implementation does nothing.
     *
     * @param throwable the throwable to log
     * @param message the accompanying message
     */
    @Override
    public void log(Throwable throwable, Object message) {
        // noop
    }

    /**
     * Logs a throwable with a formatted message and arguments. This implementation does nothing.
     *
     * @param throwable the throwable to log
     * @param message the message format
     * @param arguments the arguments to format into the message
     */
    @Override
    public void log(Throwable throwable, String message, Object... arguments) {
        // noop
    }
}
