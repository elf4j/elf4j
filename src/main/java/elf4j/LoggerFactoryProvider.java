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

import java.util.List;
import java.util.Optional;

/**
 * Provisions one single ELF4J logging provider for the client application - either the properly configured or the
 * default no-op logging provider.
 */
enum LoggerFactoryProvider {
    /**
     * Sole instance
     */
    INSTANCE;

    final Implementation implementation;

    LoggerFactoryProvider() {
        this.implementation = new Implementation(new LoggerFactoryConfiguration());
    }

    LoggerFactory loggerFactory() {
        return this.implementation.loggerFactory();
    }

    static final class Implementation {

        /**
         * Discovered and loaded only once
         */
        private final LoggerFactory loggerFactory;
        private final LoggerFactoryConfiguration loggerFactoryConfiguration;

        Implementation(LoggerFactoryConfiguration loggerFactoryConfiguration) {
            this.loggerFactoryConfiguration = loggerFactoryConfiguration;
            this.loggerFactory = getLoggerFactory();
        }

        private static void log(String message) {
            System.err.println("ELF4J status: " + message);
        }

        /**
         * @return the provisioned ELF4J logger factory for the client application
         */
        LoggerFactory loggerFactory() {
            return loggerFactory;
        }

        private LoggerFactory getLoggerFactory() {
            List<LoggerFactory> provisionedFactories = loggerFactoryConfiguration.getProvisionedLoggerFactories();
            Optional<String> selectedLoggerFactoryName = loggerFactoryConfiguration.getSelectedLoggerFactoryName();
            if (selectedLoggerFactoryName.isPresent()) {
                for (LoggerFactory provisionedFactory : provisionedFactories) {
                    if (provisionedFactory.getClass().getName().equals(selectedLoggerFactoryName.get())) {
                        log("Setup success: As selected, using ELF4J logger factory: " + provisionedFactory);
                        return provisionedFactory;
                    }
                }
                log("Configuration error: Selected ELF4J logger factory '" + selectedLoggerFactoryName.get()
                        + "' not found in discovered factories: " + provisionedFactories
                        + ": Falling back to NO-OP logging...");
                return new NoopLoggerFactory();
            }
            if (provisionedFactories.isEmpty()) {
                log("No ELF4J logger factory discovered: This is OK only if no logging is expected via ELF4J: Falling back to NO-OP logging...");
                return new NoopLoggerFactory();
            }
            if (provisionedFactories.size() == 1) {
                LoggerFactory provisionedLoggerFactory = provisionedFactories.get(0);
                log("Setup success: As provisioned, using ELF4J logger factory: " + provisionedLoggerFactory);
                return provisionedLoggerFactory;
            }
            log("Configuration error: Expected only one ELF4J logger factory but discovered "
                    + provisionedFactories.size() + ": " + provisionedFactories
                    + ": Please either re-provision to have only one logging provider, or select the desired factory by its fully qualified class name using the system property '"
                    + LoggerFactoryConfiguration.ELF4J_LOGGER_FACTORY_FQCN + "': Falling back to NO-OP logging...");
            return new NoopLoggerFactory();
        }
    }
}
