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
 * No-op implementation of {@link Logger} API
 */
public enum NoopLogger implements Logger {
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

    private final Level level;

    NoopLogger(Level level) {
        this.level = level;
    }

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
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void log(Object message) {
        //noop
    }

    @Override
    public void log(String message, Object... args) {
        //noop
    }

    @Override
    public void log(Throwable t) {
        //noop
    }

    @Override
    public void log(Throwable t, Object message) {
        //noop
    }

    @Override
    public void log(Throwable t, String message, Object... args) {
        //noop
    }
}
