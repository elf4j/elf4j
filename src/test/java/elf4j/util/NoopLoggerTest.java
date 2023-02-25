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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NoopLoggerTest {

    NoopLogger nopLogger = NoopLogger.INSTANCE;

    @Nested
    class isEnabled {

        @Test
        void noLevelEnabled() {
            assertFalse(nopLogger.isEnabled());
            assertFalse(nopLogger.atTrace().isEnabled());
            assertFalse(nopLogger.atDebug().isEnabled());
            assertFalse(nopLogger.atInfo().isEnabled());
            assertFalse(nopLogger.atWarn().isEnabled());
            assertFalse(nopLogger.atError().isEnabled());
        }
    }

    @Nested
    class level {
        @Test
        void alwaysOff() {
            assertEquals(Level.OFF, nopLogger.getLevel());
            assertEquals(Level.OFF, nopLogger.atTrace().getLevel());
            assertEquals(Level.OFF, nopLogger.atDebug().getLevel());
            assertEquals(Level.OFF, nopLogger.atInfo().getLevel());
            assertEquals(Level.OFF, nopLogger.atWarn().getLevel());
            assertEquals(Level.OFF, nopLogger.atError().getLevel());
        }
    }
}