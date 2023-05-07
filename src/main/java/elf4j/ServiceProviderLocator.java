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

    final LoggerFactoryLoader loggerFactoryLoader;

    ServiceProviderLocator() {
        this.loggerFactoryLoader = new LoggerFactoryLoader();
    }

    /**
     * @return the provisioned ELF4J logger factory for the client application
     */
    LoggerFactory loggerFactory() {
        return this.loggerFactoryLoader.getLoggerFactory();
    }

    static final class LoggerFactoryLoader {
        /**
         * System property to select the desired logger factory by its fully qualified class name in case multiple
         * logging provider candidates are available
         */
        static final String ELF4J_LOGGER_FACTORY_FQCN = "elf4j.logger.factory.fqcn";
        private final ServiceLoader<LoggerFactory> loggerFactoryServiceLoader;
        private final NoopLoggerFactory noopLoggerFactory;

        LoggerFactoryLoader() {
            this(ServiceLoader.load(LoggerFactory.class),
                    new LoggerFactoryProvision(getAllLoaded(ServiceLoader.load(LoggerFactory.class)),
                            getUserSelectedLoggerFactoryName()).isNoop() ? new NoopLoggerFactory() : null);
        }

        LoggerFactoryLoader(ServiceLoader<LoggerFactory> loggerFactoryServiceLoader,
                NoopLoggerFactory noopLoggerFactory) {
            this.loggerFactoryServiceLoader = loggerFactoryServiceLoader;
            this.noopLoggerFactory = noopLoggerFactory;
        }

        private static List<LoggerFactory> getAllLoaded(ServiceLoader<LoggerFactory> loggerFactoryServiceLoader) {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactoryServiceLoader.forEach(loggerFactories::add);
            return loggerFactories;
        }

        private static String getUserSelectedLoggerFactoryName() {
            String selectedLoggerFactoryFqcn = System.getProperty(ELF4J_LOGGER_FACTORY_FQCN);
            if (selectedLoggerFactoryFqcn == null || selectedLoggerFactoryFqcn.trim().isEmpty()) {
                return null;
            }
            return selectedLoggerFactoryFqcn.trim();
        }

        LoggerFactory getLoggerFactory() {
            if (noopLoggerFactory != null) {
                return this.noopLoggerFactory;
            }
            return loggerFactoryServiceLoader.iterator().next();
        }
    }

    static final class LoggerFactoryProvision {
        final List<LoggerFactory> provisionedFactories;
        final String selectedLoggerFactoryName;

        LoggerFactoryProvision(List<LoggerFactory> provisionedFactories, String selectedLoggerFactoryName) {
            this.provisionedFactories = provisionedFactories;
            this.selectedLoggerFactoryName = selectedLoggerFactoryName;
        }

        boolean isNoop() {
            if (selectedLoggerFactoryName != null) {
                long selectedCount = provisionedFactories.stream()
                        .filter(loggerFactory -> loggerFactory.getClass().getName().equals(selectedLoggerFactoryName))
                        .count();
                if (selectedCount != 1) {
                    InternalLogger.INSTANCE.log(ERROR,
                            "Expected one and only one selected ELF4J logger factory '" + selectedLoggerFactoryName
                                    + "' but not so in the " + provisionedFactories.size() + " provisioned "
                                    + provisionedFactories + ", falling back to NO-OP logging...");
                    return true;
                }
            }
            if (provisionedFactories.isEmpty()) {
                InternalLogger.INSTANCE.log(WARN,
                        "No ELF4J logger factory discovered, this is OK only if no logging is expected via ELF4J, falling back to NO-OP logging...");
                return true;
            }
            if (provisionedFactories.size() != 1) {
                InternalLogger.INSTANCE.log(ERROR,
                        "Expected one and only one ELF4J logger factory but loaded " + provisionedFactories.size()
                                + ": " + provisionedFactories
                                + ": Please either re-provision to have only one logging provider, or select the desired factory by its fully qualified class name using the system property '"
                                + LoggerFactoryLoader.ELF4J_LOGGER_FACTORY_FQCN
                                + "', falling back to NO-OP logging...");
                return true;
            }
            InternalLogger.INSTANCE.log(INFO,
                    "As provisioned, using ELF4J logger factory: " + provisionedFactories.get(0));
            return false;
        }
    }
}
