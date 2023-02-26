package elf4j;

import elf4j.spi.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

final class LoggerFactoryConfiguration {
    /**
     * System property to select the desired logger factory by its fully qualified class name in case multiple logging
     * provider candidates are available
     */
    static final String ELF4J_LOGGER_FACTORY_FQCN = "elf4j.logger.factory.fqcn";

    private static List<LoggerFactory> provisionedLoggerFactories() {
        List<LoggerFactory> loggerFactories = new ArrayList<>();
        ServiceLoader.load(LoggerFactory.class).forEach(loggerFactories::add);
        return loggerFactories;
    }

    private static Optional<String> selectedLoggerFactoryName() {
        String selectedLoggerFactoryFqcn = System.getProperty(ELF4J_LOGGER_FACTORY_FQCN);
        if (selectedLoggerFactoryFqcn == null || selectedLoggerFactoryFqcn.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(selectedLoggerFactoryFqcn.trim());
    }

    List<LoggerFactory> getProvisionedLoggerFactories() {
        return provisionedLoggerFactories();
    }

    Optional<String> getSelectedLoggerFactoryName() {
        return selectedLoggerFactoryName();
    }
}
