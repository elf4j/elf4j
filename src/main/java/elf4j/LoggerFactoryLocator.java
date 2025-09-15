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
import elf4j.util.UtilLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.jspecify.annotations.Nullable;

/**
 * Locates a concrete elf4j logging service provider for the client application at launch time - either the properly
 * configured or the default no-op logging provider.
 */
enum LoggerFactoryLocator {
    /** Sole instance */
    INSTANCE;

    final ServiceLoader<LoggerFactory> loggerFactoryServiceLoader;

    LoggerFactoryLocator() {
        this.loggerFactoryServiceLoader = ServiceLoader.load(LoggerFactory.class);
    }

    public static final String ELF4J_SERVICE_PROVIDER_FQCN = "elf4j.service.provider.fqcn";
    private static final Logger LOGGER = UtilLogger.INFO;

    /**
     * This method is called each time the {@link elf4j.Logger#instance} service access API is invoked, assuming the
     * Java {@link ServiceLoader} performs proper caching.
     *
     * @return the loaded elf4j logger factory implementation for creation of elf4j Logger instances
     */
    LoggerFactory getLoggerFactory() {
        return locate(
                System.getProperty(ELF4J_SERVICE_PROVIDER_FQCN), getLoadedLoggerFactories(loggerFactoryServiceLoader));
    }

    private static LoggerFactory locate(
            @Nullable String specifiedLoggerFactoryClassName, List<LoggerFactory> loadedLoggerFactories) {
        if (specifiedLoggerFactoryClassName == null
                || specifiedLoggerFactoryClassName.trim().isEmpty()) {
            LOGGER.trace(String.format(
                    "No elf4j SPI implementation specified using system property '%s'", ELF4J_SERVICE_PROVIDER_FQCN));
            switch (loadedLoggerFactories.size()) {
                case 0: {
                    LOGGER.trace("Default to NOP: No elf4j provider configured");
                    return new NoopLoggerFactory();
                }
                case 1: {
                    LoggerFactory loggerFactory = loadedLoggerFactories.get(0);
                    LOGGER.trace(String.format(
                            "Using loaded elf4j SPI implementation '%s'",
                            loggerFactory.getClass().getName()));
                    return loggerFactory;
                }
                default: {
                    LOGGER.error(String.format(
                            "Falling back to NOP: Nothing specified to select from multiple loaded elf4j SPI implementations %s: Either select one via system property '%s', or set up to load only one SPI implementation",
                            loadedLoggerFactories, ELF4J_SERVICE_PROVIDER_FQCN));
                    return new NoopLoggerFactory();
                }
            }
        }
        String specifiedLoggerFactoryFqcn = specifiedLoggerFactoryClassName.trim();
        LOGGER.trace(String.format("Specified elf4j SPI implementation '%s'", specifiedLoggerFactoryFqcn));
        for (LoggerFactory loggerFactory : loadedLoggerFactories) {
            if (specifiedLoggerFactoryFqcn.equals(loggerFactory.getClass().getName())) {
                LOGGER.trace(String.format(
                        "Using specified elf4j SPI implementation '%s' from loaded collection %s",
                        loggerFactory, loadedLoggerFactories));
                return loggerFactory;
            }
        }
        LOGGER.error(String.format(
                "Falling back to NOP: Specified elf4j SPI implementation not found: specified fqcn='%s', loaded collection=%s",
                specifiedLoggerFactoryFqcn, loadedLoggerFactories));
        return new NoopLoggerFactory();
    }

    private static List<LoggerFactory> getLoadedLoggerFactories(
            ServiceLoader<LoggerFactory> loggerFactoryServiceLoader) {
        List<LoggerFactory> loaded = new ArrayList<>();
        for (LoggerFactory loggerFactory : loggerFactoryServiceLoader) {
            loaded.add(loggerFactory);
        }
        return loaded;
    }
}
