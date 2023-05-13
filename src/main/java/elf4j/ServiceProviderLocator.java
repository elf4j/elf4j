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
import elf4j.util.IeLogger;
import elf4j.util.NoopLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Locates a concrete elf4j logging service provider for the client application at launch time - either the properly
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
     * @return the provisioned elf4j logger factory for the client application
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
        private static final ServiceLoader<LoggerFactory> SERVICE_LOADER = ServiceLoader.load(LoggerFactory.class);
        private static final NoopLoggerFactory NOOP_LOGGER_FACTORY = new NoopLoggerFactory();
        private final ServiceLoader<LoggerFactory> loggerFactoryServiceLoader;
        private final boolean noop;

        LoggerFactoryLoader() {
            this(SERVICE_LOADER,
                    new LoggerFactoryProvision(getAllLoaded(), getUserSelectedLoggerFactoryName()).isNoop());
        }

        LoggerFactoryLoader(ServiceLoader<LoggerFactory> loggerFactoryServiceLoader, boolean noop) {
            this.loggerFactoryServiceLoader = loggerFactoryServiceLoader;
            this.noop = noop;
        }

        private static List<LoggerFactory> getAllLoaded() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            SERVICE_LOADER.forEach(loggerFactories::add);
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
            if (this.noop) {
                return NOOP_LOGGER_FACTORY;
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
                    IeLogger.ERROR.log(
                            "Expected one and only one selected elf4j logger factory '{}' but not so in the {} provisioned {}, falling back to NO-OP logging...",
                            selectedLoggerFactoryName,
                            provisionedFactories.size(),
                            provisionedFactories);
                    return true;
                }
                IeLogger.INFO.log("As selected, using elf4j logger factory: {}", selectedLoggerFactoryName);
                return false;
            }
            if (provisionedFactories.isEmpty()) {
                IeLogger.WARN.log(
                        "No elf4j logger factory discovered, this is OK only when no logging is expected via elf4j, falling back to NO-OP logging...");
                return true;
            }
            if (provisionedFactories.size() != 1) {
                IeLogger.ERROR.log(
                        "Expected one and only one elf4j logger factory but loaded {}: {}, please either re-provision to have only one logging provider, or select the desired factory in the provisioned ones by its fully qualified class name using system property '{}', falling back to NO-OP logging...",
                        provisionedFactories.size(),
                        provisionedFactories,
                        LoggerFactoryLoader.ELF4J_LOGGER_FACTORY_FQCN);
                return true;
            }
            IeLogger.INFO.log("As provisioned, using elf4j logger factory: {}", provisionedFactories.get(0));
            return false;
        }
    }
}
