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
import elf4j.util.InternalLogger;
import elf4j.util.NoopLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import static elf4j.Level.*;

/**
 * Locates a concrete ELF4J logging service provider for the client application at launch time - either the properly
 * configured or the default no-op logging provider.
 */
enum ServiceProviderLocator {
    /**
     * Sole instance
     */
    INSTANCE;
    /**
     * Discovered and loaded only once
     */
    final LoggerFactory provisionedFactory;

    ServiceProviderLocator() {
        this.provisionedFactory = new LoggerFactoryLocator(new LoggerFactoryLoader()).getLoggerFactory();
    }

    /**
     * @return the provisioned ELF4J logger factory for the client application
     */
    LoggerFactory loggerFactory() {
        return this.provisionedFactory;
    }

    static final class LoggerFactoryLoader {
        /**
         * System property to select the desired logger factory by its fully qualified class name in case multiple
         * logging provider candidates are available
         */
        static final String ELF4J_LOGGER_FACTORY_FQCN = "elf4j.logger.factory.fqcn";

        List<LoggerFactory> loadAll() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            ServiceLoader.load(LoggerFactory.class).forEach(loggerFactories::add);
            return loggerFactories;
        }

        Optional<String> getSelectedLoggerFactoryName() {
            String selectedLoggerFactoryFqcn = System.getProperty(ELF4J_LOGGER_FACTORY_FQCN);
            if (selectedLoggerFactoryFqcn == null || selectedLoggerFactoryFqcn.trim().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(selectedLoggerFactoryFqcn.trim());
        }
    }

    static final class LoggerFactoryLocator {

        private final LoggerFactoryLoader loggerFactoryLoader;

        LoggerFactoryLocator(LoggerFactoryLoader loggerFactoryLoader) {
            this.loggerFactoryLoader = loggerFactoryLoader;
        }

        LoggerFactory getLoggerFactory() {
            List<LoggerFactory> provisionedFactories = loggerFactoryLoader.loadAll();
            Optional<String> selectedLoggerFactoryName = loggerFactoryLoader.getSelectedLoggerFactoryName();
            if (selectedLoggerFactoryName.isPresent()) {
                for (LoggerFactory provisionedFactory : provisionedFactories) {
                    if (provisionedFactory.getClass().getName().equals(selectedLoggerFactoryName.get())) {
                        InternalLogger.INSTANCE.log(INFO,
                                "As selected, using ELF4J logger factory: " + provisionedFactory);
                        return provisionedFactory;
                    }
                }
                InternalLogger.INSTANCE.log(ERROR,
                        "Configuration error: Selected ELF4J logger factory '" + selectedLoggerFactoryName.get()
                                + "' not found in discovered factories: " + provisionedFactories
                                + ": Falling back to NO-OP logging...");
                return new NoopLoggerFactory();
            }
            if (provisionedFactories.isEmpty()) {
                InternalLogger.INSTANCE.log(WARN,
                        "No ELF4J logger factory discovered: This is OK only if no logging is expected via ELF4J: Falling back to NO-OP logging...");
                return new NoopLoggerFactory();
            }
            if (provisionedFactories.size() == 1) {
                LoggerFactory provisionedLoggerFactory = provisionedFactories.get(0);
                InternalLogger.INSTANCE.log(INFO,
                        "As provisioned, using ELF4J logger factory: " + provisionedLoggerFactory);
                return provisionedLoggerFactory;
            }
            InternalLogger.INSTANCE.log(ERROR,
                    "Configuration error: Expected only one ELF4J logger factory but discovered "
                            + provisionedFactories.size() + ": " + provisionedFactories
                            + ": Please either re-provision to have only one logging provider, or select the desired factory by its fully qualified class name using the system property '"
                            + LoggerFactoryLoader.ELF4J_LOGGER_FACTORY_FQCN + "': Falling back to NO-OP logging...");
            return new NoopLoggerFactory();
        }
    }
}
