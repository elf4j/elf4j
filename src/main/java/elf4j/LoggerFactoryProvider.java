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

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;

enum LoggerFactoryProvider {
    INSTANCE;
    static final String ELF4J_LOGGER_FACTORY_FQCN = "elf4j.logger.factory.fqcn";
    private final java.util.logging.Logger internalLogger =
            java.util.logging.Logger.getLogger(LoggerFactoryProvider.class.getName());
    private final LoggerFactory loggerFactory;

    LoggerFactoryProvider() {
        this.loggerFactory = getLoggerFactory();
    }

    private static Optional<String> getLoggerFactorySelection() {
        String desiredLoggerFactoryFqcn = System.getProperty(ELF4J_LOGGER_FACTORY_FQCN);
        if (desiredLoggerFactoryFqcn == null || desiredLoggerFactoryFqcn.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(desiredLoggerFactoryFqcn.trim());
    }

    private static List<LoggerFactory> loadLoggerFactories() {
        List<LoggerFactory> loggerFactories = new ArrayList<>();
        ServiceLoader.load(LoggerFactory.class).forEach(loggerFactories::add);
        return loggerFactories;
    }

    LoggerFactory loggerFactory() {
        return loggerFactory;
    }

    private LoggerFactory getLoggerFactory() {
        List<LoggerFactory> loadedFactories = loadLoggerFactories();
        Optional<String> desiredLoggerFactoryFqcn = getLoggerFactorySelection();
        if (desiredLoggerFactoryFqcn.isPresent()) {
            for (LoggerFactory loadedFactory : loadedFactories) {
                if (loadedFactory.getClass().getName().equals(desiredLoggerFactoryFqcn.get())) {
                    internalLogger.log(Level.INFO,
                            "setup success. as selected, using ELF4J logger factory: {0}",
                            loadedFactory);
                    return loadedFactory;
                }
            }
            internalLogger.log(Level.SEVERE,
                    "configuration error! desired ELF4J logger factory [{0}] not found in discovered factories: {1}. falling back to NO-OP logging...",
                    new Object[] { desiredLoggerFactoryFqcn.get(), loadedFactories });
            return new NoopLoggerFactory();
        }
        if (loadedFactories.isEmpty()) {
            internalLogger.log(Level.WARNING,
                    "no ELF4J logger factory discovered - this is OK only if no logging is desired. falling back to NO-OP logging...");
            return new NoopLoggerFactory();
        }
        if (loadedFactories.size() == 1) {
            LoggerFactory provisionedLoggerFactory = loadedFactories.get(0);
            internalLogger.log(Level.INFO,
                    "setup success. as provisioned, using ELF4J logger factory: {0}",
                    provisionedLoggerFactory);
            return provisionedLoggerFactory;
        }
        internalLogger.log(Level.SEVERE,
                "configuration error! expected only one ELF4J logger factory but discovered {0}: {1}. please either re-provision to have only one factory in the classpath, or select the desired factory by using the `{2}` system property. falling back to NO-OP logging...",
                new Object[] { loadedFactories.size(), loadedFactories, ELF4J_LOGGER_FACTORY_FQCN });
        return new NoopLoggerFactory();
    }
}
