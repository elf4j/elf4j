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

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Per the "Effective Java" book, <q>There are three essential components in a service provider framework: a service
 * interface, which represents an implementation; a provider registration API, which providers use to register
 * implementations; and a service access API, which clients use to obtain instances of the service.... An optional
 * fourth component of a service provider framework is a service provider interface, which describes a factory object
 * that produce instances of the service interface.</q>
 *
 * <p>In case of this ELF4J Service Provider Framework,
 *
 * <ul>
 *   <li>The {@link Logger} serves as both the "service interface" and "service access API".
 *   <li>The Java standard "provider registration API" is used. See the Javadoc of the <a
 *       href="https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">{@link java.util.ServiceLoader}</a>.
 *   <li>The {@link elf4j.spi.LoggerFactory} is the "service provider interface" (SPI).
 * </ul>
 *
 * <p>
 *
 * <p>
 *
 * <p>All {@link Logger} instances from this API should be thread-safe.
 */
public interface Logger {
    /**
     * Static factory method serving as the "service access API", returning a default Logger instance to the caller
     * class.
     *
     * @return Logger instance with default severity Level
     * @apiNote Calling this method can be performance-wise expensive. It is highly recommended only using it when
     *     assigning the returned value to a static or instance class variable, as opposed to a local one.
     * @implNote It is up to the logging service provider to determine the default level (and name if needed) of the
     *     logger instance to be returned.
     */
    static Logger instance() {
        return LoggerFactoryLocator.INSTANCE.getLoggerFactory().getLogger();
    }

    /**
     * Instance factory method that returns a Logger instance for the specified log level as this Logger instance
     *
     * @param level the level of the requested Logger instance
     * @return Logger instance of the specified level
     * @apiNote Unlike the static factory method {@link Logger#instance()}, this and all other instance factory methods
     *     should be performance-wise inexpensive to call; they can be used anywhere convenient for the caller.
     * @implNote A Logger instance's severity level is immutable and cannot be changed after creation. Therefore, this
     *     method can return the current instance itself only if the specified level is the same as the current
     *     instance's; otherwise, it will have to be a different Logger instance to be returned.
     */
    Logger atLevel(Level level);

    /**
     * Retrieves the severity level of the Logger instance.
     *
     * @return the severity level of the logger instance
     */
    Level getLevel();

    /**
     * Checks if logging is enabled for this Logger instance at its own severity level.
     *
     * @return true if logging is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Checks if logging is enabled for the Logger instance obtained by calling one of the instance factory methods of
     * this Logger instance at the specified level
     *
     * @apiNote This returns the same value as {@link Logger#isEnabled()} only when the specified level is the same as
     *     that of the current Logger instance.
     * @param level the logging level to check
     * @return true if logging is enabled for a target logger instance at the specified level, false otherwise
     */
    default boolean isEnabled(Level level) {
        return atLevel(level).isEnabled();
    }

    /**
     * Logs a message.
     *
     * @param message the message to be logged. If provided as a lazy lambda expression (of type
     *     {@link java.util.function.Supplier}), the result of {@link Supplier#get()} is used to construct the final log
     *     message.
     */
    void log(Object message);

    /**
     * Logs a message provided by a lazy lambda expression. Convenience overloading method of {@link #log(Object)}, so
     * no downcast to {@link Supplier} is required for the lambda Supplier.
     *
     * @param message Supplier of the message to be logged
     */
    default void log(Supplier<?> message) {
        if (!isEnabled()) {
            return;
        }
        log(message.get());
    }

    /**
     * Logs a formatted message with arguments.
     *
     * @param message the message to be logged, which may contain argument placeholders denoted as `{}` tokens
     * @param arguments The argument values will replace the `{}` placeholders in the message. The arguments can be a
     *     mixture of both eager {@code Object} and lazy lambda expression {@code Supplier<?>} types. When both types
     *     are present, lambda arguments need to be downcast to {@code Supplier<?>} per the Java lambda expression
     *     syntax requirement.
     */
    void log(String message, Object... arguments);

    /**
     * Logs a formatted message with all arguments provided by lambda expressions (of the {@link Supplier} type).
     *
     * @param message the message to be logged
     * @param arguments lazy Suppliers arguments to replace placeholders in the message; no downcast needed as all
     *     arguments are of {@code Supplier<?>} type.
     * @apiNote Convenience overloading method of {@link #log(String, Object...)}, so that no downcast is required
     *     because all arguments are lazy lambda expressions of {@link Supplier} type.
     */
    default void log(String message, Supplier<?>... arguments) {
        if (!isEnabled()) {
            return;
        }
        log(message, supply(arguments));
    }

    static Object[] supply(Supplier<?>[] arguments) {
        return Arrays.stream(arguments).map(Supplier::get).toArray(Object[]::new);
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
     * @param message the accompanying message to be logged. If the actual type is {@link java.util.function.Supplier}
     *     as when provided via a lazy lambda expression, the result of {@link Supplier#get()} is used to compute the
     *     final log message.
     */
    void log(Throwable throwable, Object message);

    /**
     * Logs a Throwable with an accompanying message provided by a lazy Supplier.
     *
     * @param throwable the Throwable to be logged
     * @param message Supplier of the accompanying message to be logged
     */
    default void log(Throwable throwable, Supplier<?> message) {
        if (!isEnabled()) {
            return;
        }
        log(throwable, message.get());
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
     * Logs a Throwable with a formatted message and all arguments provided by lazy Suppliers.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void log(Throwable throwable, String message, Supplier<?>... arguments) {
        if (!isEnabled()) {
            return;
        }
        log(throwable, message, supply(arguments));
    }

    /**
     * Instance factory method that provides a Logger instance with TRACE severity level.
     *
     * @return Logger instance with {@link Level#TRACE} severity level
     */
    default Logger atTrace() {
        return atLevel(Level.TRACE);
    }

    /**
     * Instance factory method that provides a Logger instance with DEBUG severity level.
     *
     * @return Logger instance with {@link Level#DEBUG} severity level
     */
    default Logger atDebug() {
        return atLevel(Level.DEBUG);
    }

    /**
     * Instance factory method that provides a Logger instance with INFO severity level.
     *
     * @return Logger instance with {@link Level#INFO} severity level
     */
    default Logger atInfo() {
        return atLevel(Level.INFO);
    }

    /**
     * Instance factory method that provides a Logger instance with WARN severity level.
     *
     * @return Logger instance with {@link Level#WARN} severity level
     */
    default Logger atWarn() {
        return atLevel(Level.WARN);
    }

    /**
     * Instance factory method that provides a Logger instance with ERROR severity level.
     *
     * @return Logger instance with {@link Level#ERROR} severity level
     */
    default Logger atError() {
        return atLevel(Level.ERROR);
    }

    // The following methods are convenience shorthands added to resemble other logging APIs.

    /**
     * Checks if logging is enabled at the TRACE level.
     *
     * @return true if TRACE level logging is enabled, false otherwise
     */
    default boolean isTraceEnabled() {
        return atTrace().isEnabled();
    }

    /**
     * Checks if logging is enabled at the DEBUG level.
     *
     * @return true if DEBUG level logging is enabled, false otherwise
     */
    default boolean isDebugEnabled() {
        return atDebug().isEnabled();
    }

    /**
     * Checks if logging is enabled at the INFO level.
     *
     * @return true if INFO level logging is enabled, false otherwise
     */
    default boolean isInfoEnabled() {
        return atInfo().isEnabled();
    }

    /**
     * Checks if logging is enabled at the WARN level.
     *
     * @return true if WARN level logging is enabled, false otherwise
     */
    default boolean isWarnEnabled() {
        return atWarn().isEnabled();
    }

    /**
     * Checks if logging is enabled at the ERROR level.
     *
     * @return true if ERROR level logging is enabled, false otherwise
     */
    default boolean isErrorEnabled() {
        return atError().isEnabled();
    }

    /**
     * Convenience shorthand method that first gets a logger instance at TRACE level, and then uses the instance to log
     * the specified message.
     *
     * @param message the message to be logged
     */
    default void trace(Object message) {
        atTrace().log(message);
    }

    /**
     * Convenience shorthand method that first gets a logger instance at TRACE level, and then uses the instance to log
     * the formatted message with the specified arguments.
     *
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void trace(String message, Object... arguments) {
        atTrace().log(message, arguments);
    }

    /**
     * Logs a message provided by a lazy Supplier at TRACE level.
     *
     * @param message Supplier of the message to be logged
     */
    default void trace(Supplier<?> message) {
        logSuppliedAtLevel(Level.TRACE, message);
    }

    default void logSuppliedAtLevel(Level level, Supplier<?> message) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(message.get());
    }

    /**
     * Logs a formatted message at TRACE level with arguments provided by lazy Suppliers.
     *
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void trace(String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.TRACE, message, arguments);
    }

    default void logSuppliedAtLevel(Level level, String message, Supplier<?>[] arguments) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(message, supply(arguments));
    }

    /**
     * Logs a Throwable at TRACE level.
     *
     * @param throwable the Throwable to be logged
     */
    default void trace(Throwable throwable) {
        atTrace().log(throwable);
    }

    /**
     * Logs a Throwable with an accompanying message at TRACE level.
     *
     * @param throwable the Throwable to be logged
     * @param message the accompanying message to be logged
     */
    default void trace(Throwable throwable, Object message) {
        atTrace().log(throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments at TRACE level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void trace(Throwable throwable, String message, Object... arguments) {
        atTrace().log(throwable, message, arguments);
    }

    /**
     * Logs a Throwable with an accompanying message provided by a Supplier at TRACE level.
     *
     * @param throwable the Throwable to be logged
     * @param message Supplier of the accompanying message to be logged
     */
    default void trace(Throwable throwable, Supplier<?> message) {
        logSuppliedAtLevel(Level.TRACE, throwable, message);
    }

    default void logSuppliedAtLevel(Level level, Throwable throwable, Supplier<?> message) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(throwable, message.get());
    }

    /**
     * Logs a Throwable with a formatted message and arguments provided by Suppliers at TRACE level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void trace(Throwable throwable, String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.TRACE, throwable, message, arguments);
    }

    default void logSuppliedAtLevel(Level level, Throwable throwable, String message, Supplier<?>[] arguments) {
        if (!isEnabled(level)) {
            return;
        }
        atLevel(level).log(throwable, message, supply(arguments));
    }

    /**
     * Logs a message at DEBUG level.
     *
     * @param message the message to be logged
     */
    default void debug(Object message) {
        atDebug().log(message);
    }

    /**
     * Logs a formatted message at DEBUG level with arguments.
     *
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void debug(String message, Object... arguments) {
        atDebug().log(message, arguments);
    }

    /**
     * Logs a message provided by a Supplier at DEBUG level.
     *
     * @param message Supplier of the message to be logged
     */
    default void debug(Supplier<?> message) {
        logSuppliedAtLevel(Level.DEBUG, message);
    }

    /**
     * Logs a formatted message at DEBUG level with arguments provided by Suppliers.
     *
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void debug(String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.DEBUG, message, arguments);
    }

    /**
     * Logs a Throwable at DEBUG level.
     *
     * @param throwable the Throwable to be logged
     */
    default void debug(Throwable throwable) {
        atDebug().log(throwable);
    }

    /**
     * Logs a Throwable with an accompanying message at DEBUG level.
     *
     * @param throwable the Throwable to be logged
     * @param message the accompanying message to be logged
     */
    default void debug(Throwable throwable, Object message) {
        atDebug().log(throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments at DEBUG level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void debug(Throwable throwable, String message, Object... arguments) {
        atDebug().log(throwable, message, arguments);
    }

    /**
     * Logs a Throwable with an accompanying message provided by a Supplier at DEBUG level.
     *
     * @param throwable the Throwable to be logged
     * @param message Supplier of the accompanying message to be logged
     */
    default void debug(Throwable throwable, Supplier<?> message) {
        logSuppliedAtLevel(Level.DEBUG, throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments provided by Suppliers at DEBUG level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void debug(Throwable throwable, String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.DEBUG, throwable, message, arguments);
    }

    /**
     * Logs a message at INFO level.
     *
     * @param message the message to be logged
     */
    default void info(Object message) {
        atInfo().log(message);
    }

    /**
     * Logs a formatted message at INFO level with arguments.
     *
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void info(String message, Object... arguments) {
        atInfo().log(message, arguments);
    }

    /**
     * Logs a message provided by a Supplier at INFO level.
     *
     * @param message Supplier of the message to be logged
     */
    default void info(Supplier<?> message) {
        logSuppliedAtLevel(Level.INFO, message);
    }

    /**
     * Logs a formatted message at INFO level with arguments provided by Suppliers.
     *
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void info(String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.INFO, message, arguments);
    }

    /**
     * Logs a Throwable at INFO level.
     *
     * @param throwable the Throwable to be logged
     */
    default void info(Throwable throwable) {
        atInfo().log(throwable);
    }

    /**
     * Logs a Throwable with an accompanying message at INFO level.
     *
     * @param throwable the Throwable to be logged
     * @param message the accompanying message to be logged
     */
    default void info(Throwable throwable, Object message) {
        atInfo().log(throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments at INFO level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void info(Throwable throwable, String message, Object... arguments) {
        atInfo().log(throwable, message, arguments);
    }

    /**
     * Logs a Throwable with an accompanying message provided by a Supplier at INFO level.
     *
     * @param throwable the Throwable to be logged
     * @param message Supplier of the accompanying message to be logged
     */
    default void info(Throwable throwable, Supplier<?> message) {
        logSuppliedAtLevel(Level.INFO, throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments provided by Suppliers at INFO level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void info(Throwable throwable, String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.INFO, throwable, message, arguments);
    }

    /**
     * Logs a message at WARN level.
     *
     * @param message the message to be logged
     */
    default void warn(Object message) {
        atWarn().log(message);
    }

    /**
     * Logs a formatted message at WARN level with arguments.
     *
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void warn(String message, Object... arguments) {
        atWarn().log(message, arguments);
    }

    /**
     * Logs a message provided by a Supplier at WARN level.
     *
     * @param message Supplier of the message to be logged
     */
    default void warn(Supplier<?> message) {
        logSuppliedAtLevel(Level.WARN, message);
    }

    /**
     * Logs a formatted message at WARN level with arguments provided by Suppliers.
     *
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void warn(String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.WARN, message, arguments);
    }

    /**
     * Logs a Throwable at WARN level.
     *
     * @param throwable the Throwable to be logged
     */
    default void warn(Throwable throwable) {
        atWarn().log(throwable);
    }

    /**
     * Logs a Throwable with an accompanying message at WARN level.
     *
     * @param throwable the Throwable to be logged
     * @param message the accompanying message to be logged
     */
    default void warn(Throwable throwable, Object message) {
        atWarn().log(throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments at WARN level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void warn(Throwable throwable, String message, Object... arguments) {
        atWarn().log(throwable, message, arguments);
    }

    /**
     * Logs a Throwable with an accompanying message provided by a Supplier at WARN level.
     *
     * @param throwable the Throwable to be logged
     * @param message Supplier of the accompanying message to be logged
     */
    default void warn(Throwable throwable, Supplier<?> message) {
        logSuppliedAtLevel(Level.WARN, throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments provided by Suppliers at WARN level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void warn(Throwable throwable, String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.WARN, throwable, message, arguments);
    }

    /**
     * Logs a message at ERROR level.
     *
     * @param message the message to be logged
     */
    default void error(Object message) {
        atError().log(message);
    }

    /**
     * Logs a formatted message at ERROR level with arguments.
     *
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void error(String message, Object... arguments) {
        atError().log(message, arguments);
    }

    /**
     * Logs a message provided by a Supplier at ERROR level.
     *
     * @param message Supplier of the message to be logged
     */
    default void error(Supplier<?> message) {
        logSuppliedAtLevel(Level.ERROR, message);
    }

    /**
     * Logs a formatted message at ERROR level with arguments provided by Suppliers.
     *
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void error(String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.ERROR, message, arguments);
    }

    /**
     * Logs a Throwable at ERROR level.
     *
     * @param throwable the Throwable to be logged
     */
    default void error(Throwable throwable) {
        atError().log(throwable);
    }

    /**
     * Logs a Throwable with an accompanying message at ERROR level.
     *
     * @param throwable the Throwable to be logged
     * @param message the accompanying message to be logged
     */
    default void error(Throwable throwable, Object message) {
        atError().log(throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments at ERROR level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments the arguments whose values will replace the placeholders in the message
     */
    default void error(Throwable throwable, String message, Object... arguments) {
        atError().log(throwable, message, arguments);
    }

    /**
     * Logs a Throwable with an accompanying message provided by a Supplier at ERROR level.
     *
     * @param throwable the Throwable to be logged
     * @param message Supplier of the accompanying message to be logged
     */
    default void error(Throwable throwable, Supplier<?> message) {
        logSuppliedAtLevel(Level.ERROR, throwable, message);
    }

    /**
     * Logs a Throwable with a formatted message and arguments provided by Suppliers at ERROR level.
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged
     * @param arguments Suppliers of the arguments to replace placeholders in the message
     */
    default void error(Throwable throwable, String message, Supplier<?>... arguments) {
        logSuppliedAtLevel(Level.ERROR, throwable, message, arguments);
    }
}
