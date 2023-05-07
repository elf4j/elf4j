package elf4j;

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerFactoryProvisionTest {

    @Nested
    class isNoop {
        @Test
        void moreThanOneLogFactoryButNoSelection() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvision loggerFactoryProvision =
                    new ServiceProviderLocator.LoggerFactoryProvision(loggerFactories, null);

            assertTrue(loggerFactoryProvision.isNoop());
        }

        @Test
        void noLogFactoryProvisioned() {
            ArrayList<LoggerFactory> emptyProvisionedFactories = new ArrayList<>();
            ServiceProviderLocator.LoggerFactoryProvision loggerFactoryProvision =
                    new ServiceProviderLocator.LoggerFactoryProvision(emptyProvisionedFactories, null);

            assertTrue(loggerFactoryProvision.isNoop());

            loggerFactoryProvision = new ServiceProviderLocator.LoggerFactoryProvision(emptyProvisionedFactories,
                    "testNonNullSelection");

            assertTrue(loggerFactoryProvision.isNoop());
        }

        @Test
        void selectedProviderNotFound() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvision loggerFactoryProvision =
                    new ServiceProviderLocator.LoggerFactoryProvision(loggerFactories, "testNonExistingSelection");

            assertTrue(loggerFactoryProvision.isNoop());
        }
    }

    @Nested
    class notNoop {

        @Test
        void selectedLogFactoryAmongProvisionedOnes() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvision loggerFactoryProvision =
                    new ServiceProviderLocator.LoggerFactoryProvision(loggerFactories, "elf4j.util.NoopLoggerFactory");

            assertFalse(loggerFactoryProvision.isNoop());
        }

        @Test
        void onlyOneLogFactoryProvisionedWithNoSelection() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvision loggerFactoryProvision =
                    new ServiceProviderLocator.LoggerFactoryProvision(loggerFactories, null);

            assertFalse(loggerFactoryProvision.isNoop());
        }
    }
}