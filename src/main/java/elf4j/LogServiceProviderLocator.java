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

import elf4j.spi.LogServiceProvider;
import elf4j.util.IeLogger;
import elf4j.util.NoopLogServiceProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Locates a concrete elf4j logging service provider for the client application at launch time - either the properly
 * configured or the default no-op logging provider.
 */
enum LogServiceProviderLocator {
    /**
     * Sole instance
     */
    INSTANCE;

    final LogServiceProviderLoader logServiceProviderLoader;

    LogServiceProviderLocator() {
        this.logServiceProviderLoader = new LogServiceProviderLoader();
    }

    /**
     * @return the provisioned elf4j service provider for the client application
     */
    LogServiceProvider logServiceProvider() {
        return this.logServiceProviderLoader.getLogServiceProvider();
    }

    static final class LogServiceProviderLoader {
        /**
         * System property to select the desired service provider by its fully qualified class name in case multiple
         * provider candidates are available
         */
        static final String ELF4J_SERVICE_PROVIDER_FQCN = "elf4j.service.provider.fqcn";

        private static final ServiceLoader<LogServiceProvider> SERVICE_LOADER =
                ServiceLoader.load(LogServiceProvider.class);
        private static final NoopLogServiceProvider NOOP_LOG_SERVICE_PROVIDER = new NoopLogServiceProvider();
        private final ServiceLoader<LogServiceProvider> providerServiceLoader;
        private final boolean setup;

        LogServiceProviderLoader() {
            this(
                    SERVICE_LOADER,
                    new LogServiceProviderSetupStatus(getAllLoaded(), getUserSelectedLogServiceProviderName())
                            .isSetup());
        }

        LogServiceProviderLoader(ServiceLoader<LogServiceProvider> providerServiceLoader, boolean setup) {
            this.providerServiceLoader = providerServiceLoader;
            this.setup = setup;
        }

        private static @Nonnull List<LogServiceProvider> getAllLoaded() {
            List<LogServiceProvider> logServiceProviders = new ArrayList<>();
            SERVICE_LOADER.forEach(logServiceProviders::add);
            return logServiceProviders;
        }

        private static @Nullable String getUserSelectedLogServiceProviderName() {
            String selectedLogServiceProviderFqcn = System.getProperty(ELF4J_SERVICE_PROVIDER_FQCN);
            if (selectedLogServiceProviderFqcn == null
                    || selectedLogServiceProviderFqcn.trim().isEmpty()) {
                return null;
            }
            return selectedLogServiceProviderFqcn.trim();
        }

        LogServiceProvider getLogServiceProvider() {
            if (!this.setup) {
                return NOOP_LOG_SERVICE_PROVIDER;
            }
            return providerServiceLoader.iterator().next();
        }
    }

    static final class LogServiceProviderSetupStatus {
        final List<LogServiceProvider> loadedProviders;
        final String selectedProviderFqcn;

        LogServiceProviderSetupStatus(List<LogServiceProvider> loadedProviders, String selectedProviderFqcn) {
            this.loadedProviders = loadedProviders;
            this.selectedProviderFqcn = selectedProviderFqcn;
        }

        boolean isSetup() {
            if (selectedProviderFqcn != null) {
                List<LogServiceProvider> selected = loadedProviders.stream()
                        .filter(LogServiceProvider ->
                                LogServiceProvider.getClass().getName().equals(selectedProviderFqcn))
                        .collect(Collectors.toList());
                if (selected.size() != 1) {
                    IeLogger.ERROR.log(
                            "Expected one and only one selected elf4j log service provider '{}' but not so in the provisioned {}, falling back to NO-OP logging...",
                            selectedProviderFqcn,
                            loadedProviders);
                    return false;
                }
                IeLogger.INFO.log("As selected, using elf4j log service provider: {}", selected.get(0));
                return true;
            }
            if (loadedProviders.isEmpty()) {
                IeLogger.INFO.log(
                        "No elf4j log service provider discovered, this is OK only when no logging is expected via elf4j, falling back to NO-OP logging...");
                return false;
            }
            if (loadedProviders.size() != 1) {
                IeLogger.ERROR.log(
                        "Expected one and only one provisioned elf4j log service provider but loaded {}: {}, please either re-provision to have only one logging provider, or select the desired provider in the loaded ones by its fully qualified class name using system property '{}', falling back to NO-OP logging...",
                        loadedProviders.size(),
                        loadedProviders,
                        LogServiceProviderLoader.ELF4J_SERVICE_PROVIDER_FQCN);
                return false;
            }
            IeLogger.INFO.log("As set up, using elf4j log service provider: {}", loadedProviders.get(0));
            return true;
        }
    }
}
