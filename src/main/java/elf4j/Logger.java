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

package elf4j;

import java.util.function.Supplier;

/**
 * Logging service interface and access API as in the <a
 * href="https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">Java Service Provider Framework</a>.
 *
 * <p>All {@link Logger} instances from this API should be thread-safe.
 */
public interface Logger {
    /**
     * Provides a default Logger instance.
     *
     * @return Logger instance with default name and Level
     */
    static Logger instance() {
        return LogServiceProviderLocator.INSTANCE.logServiceProvider().logger();
    }

    /**
     * Provides a Logger instance for the specified logging level.
     *
     * @param level the logging level of the requested Logger instance
     * @return Logger instance of the specified level
     */
    Logger atLevel(Level level);

    /**
     * Retrieves the severity level of the Logger instance.
     *
     * @return the severity level of the logger instance
     */
    Level getLevel();

    /**
     * Checks if logging is enabled for this Logger instance.
     *
     * @return true if logging is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Logs a message.
     *
     * @param message the message to be logged. If the actual type is {@link java.util.function.Supplier}, the result of
     *     {@link Supplier#get()} is used to construct the final log message.
     */
    void log(Object message);

    /**
     * Logs a message provided by a Supplier.
     *
     * @param message Supplier of the message to be logged
     */
    default void log(Supplier<?> message) {
        log((Object) message);
    }

    /**
     * Logs a formatted message with arguments.
     *
     * @param message the message to be logged, which may contain argument placeholders denoted as `{}` tokens
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    void log(String message, Object... arguments);

    /**
     * Logs a formatted message with arguments provided by Suppliers.
     *
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void log(String message, Supplier<?>... arguments) {
        log(message, (Object[]) arguments);
    }

    /**
     * Logs a Throwable.
     *
     * @param throwable the Throwable to be logged
     */
    void log(Throwable throwable);

    /**
     * Logs a Throwable with an accompanying message.
     *
     * @param throwable the Throwable to be logged
     * @param message the accompanying message to be logged. If the actual type is {@link java.util.function.Supplier},
     *     the result of {@link Supplier#get()} is used to compute the final log message.
     */
    void log(Throwable throwable, Object message);

    /**
     * Logs a Throwable with an accompanying message provided by a Supplier.
     *
     * @param throwable the Throwable to be logged
     * @param message Supplier of the accompanying message to be logged
     */
    default void log(Throwable throwable, Supplier<?> message) {
        log(throwable, (Object) message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged, which may contain argument placeholders
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    void log(Throwable throwable, String message, Object... arguments);

    /**
     * Logs a Throwable with a formatted message and arguments provided by Suppliers.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void log(Throwable throwable, String message, Supplier<?>... arguments) {
        log(throwable, message, (Object[]) arguments);
    }

    /**
     * Provides a Logger instance with TRACE severity level.
     *
     * @return Logger instance with {@link Level#TRACE} severity level
     */
    default Logger atTrace() {
        return this.atLevel(Level.TRACE);
    }

    /**
     * Provides a Logger instance with DEBUG severity level.
     *
     * @return Logger instance with {@link Level#DEBUG} severity level
     */
    default Logger atDebug() {
        return this.atLevel(Level.DEBUG);
    }

    /**
     * Provides a Logger instance with INFO severity level.
     *
     * @return Logger instance with {@link Level#INFO} severity level
     */
    default Logger atInfo() {
        return this.atLevel(Level.INFO);
    }

    /**
     * Provides a Logger instance with WARN severity level.
     *
     * @return Logger instance with {@link Level#WARN} severity level
     */
    default Logger atWarn() {
        return this.atLevel(Level.WARN);
    }

    /**
     * Provides a Logger instance with ERROR severity level.
     *
     * @return Logger instance with {@link Level#ERROR} severity level
     */
    default Logger atError() {
        return this.atLevel(Level.ERROR);
    }
}
