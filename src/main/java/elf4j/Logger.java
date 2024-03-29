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
 * <p>
 * All {@link Logger} instances from this API should be thread-safe.
 */
public interface Logger {
    /**
     * Service access API
     *
     * @return Logger instance with default name and Level
     */
    static Logger instance() {
        return LogServiceProviderLocator.INSTANCE.logServiceProvider().logger();
    }

    /**
     * Service access API
     *
     * @param level of the requested Logger instance
     * @return Logger instance of the specified level
     */
    Logger atLevel(Level level);

    /**
     * Service interface API
     *
     * @return Severity level of the logger instance
     */
    Level getLevel();

    /**
     * Service interface API
     *
     * @return true if the active logging of this instance enabled per configuration, false otherwise
     */
    boolean isEnabled();

    /**
     * Service interface API
     *
     * @param message to be logged. If the actual type is {@link java.util.function.Supplier}, the result of
     * {@link Supplier#get()}, instead of the {@code message} itself, should be used to construct the final log
     * message.
     */
    void log(Object message);

    /**
     * Service interface API
     *
     * @param message to be logged, may contain argument placeholders, denoted as `{}` tokens, to be replaced by the
     * values of the specified arguments. Placeholders are positional - the order they appear in the message should
     * match the same order in which their corresponding replacement values appear in the specified arguments array.
     * @param arguments whose values will replace the corresponding placeholders in the specified message, in the same
     * matching order. If any of the argument's actual type is {@link java.util.function.Supplier}, then the result of
     * {@link Supplier#get()}, instead of the argument itself, should be used to compute the final log message.
     */
    void log(String message, Object... arguments);

    /**
     * Service interface API
     *
     * @param throwable the Throwable to be logged
     */
    void log(Throwable throwable);

    /**
     * Service interface API
     *
     * @param throwable the Throwable to be logged
     * @param message the message to be logged. If the actual type is {@link java.util.function.Supplier}, the result of
     * {@link Supplier#get()}, instead of the {@code message} itself, should be used to compute the final log message.
     */
    void log(Throwable throwable, Object message);

    /**
     * Service interface API
     *
     * @param throwable the Throwable to be logged
     * @param message See Javadoc of {@link #log(String, Object...)}
     * @param arguments See Javadoc of {@link #log(String, Object...)}
     */
    void log(Throwable throwable, String message, Object... arguments);

    /**
     * Service access API
     *
     * @return Logger instance with {@link Level#TRACE} severity level
     */
    default Logger atTrace() {
        return this.atLevel(Level.TRACE);
    }

    /**
     * Service access API
     *
     * @return Logger instance with {@link Level#DEBUG} severity level
     */
    default Logger atDebug() {
        return this.atLevel(Level.DEBUG);
    }

    /**
     * Service access API
     *
     * @return Logger instance with {@link Level#INFO} severity level
     */
    default Logger atInfo() {
        return this.atLevel(Level.INFO);
    }

    /**
     * Service access API
     *
     * @return Logger instance with {@link Level#WARN} severity level
     */
    default Logger atWarn() {
        return this.atLevel(Level.WARN);
    }

    /**
     * Service access API
     *
     * @return Logger instance with {@link Level#ERROR} severity level
     */
    default Logger atError() {
        return this.atLevel(Level.ERROR);
    }
}
