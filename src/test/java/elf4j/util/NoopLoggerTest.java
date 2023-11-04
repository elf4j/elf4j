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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import elf4j.Level;
import elf4j.Logger;
import org.junit.jupiter.api.Test;

class NoopLoggerTest {

    @Test
    void levels() {
        Logger defaultInstance = new NoopLogServiceProvider().logger();
        assertEquals(Level.OFF, defaultInstance.getLevel());
        assertEquals(Level.TRACE, defaultInstance.atTrace().getLevel());
        assertEquals(Level.DEBUG, defaultInstance.atDebug().getLevel());
        assertEquals(Level.INFO, defaultInstance.atInfo().getLevel());
        assertEquals(Level.WARN, defaultInstance.atWarn().getLevel());
        assertEquals(Level.ERROR, defaultInstance.atError().getLevel());
    }

    @Test
    void noLevelEnabled() {
        assertFalse(NoopLogger.OFF.isEnabled());
        assertFalse(NoopLogger.TRACE.isEnabled());
        assertFalse(NoopLogger.DEBUG.isEnabled());
        assertFalse(NoopLogger.INFO.isEnabled());
        assertFalse(NoopLogger.WARN.isEnabled());
        assertFalse(NoopLogger.ERROR.isEnabled());
    }
}
