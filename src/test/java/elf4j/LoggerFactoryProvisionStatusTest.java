package elf4j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LoggerFactoryProvisionStatusTest {

    @Nested
    class notProvisioned {
        @Test
        void moreThanOneLogFactoryButNoSelection() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvisionStatus loggerFactoryProvisionStatus =
                    new ServiceProviderLocator.LoggerFactoryProvisionStatus(loggerFactories, null);

            assertFalse(loggerFactoryProvisionStatus.isProvisioned());
        }

        @Test
        void noLoggerFactoryProvisionedAndNoSelection() {
            ArrayList<LoggerFactory> emptyProvisionedFactories = new ArrayList<>();
            ServiceProviderLocator.LoggerFactoryProvisionStatus loggerFactoryProvisionStatus =
                    new ServiceProviderLocator.LoggerFactoryProvisionStatus(emptyProvisionedFactories, null);

            assertFalse(loggerFactoryProvisionStatus.isProvisioned());
        }

        @Test
        void selectedLoggerFactoryNotFoundInProvisioned() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvisionStatus loggerFactoryProvisionStatus =
                    new ServiceProviderLocator.LoggerFactoryProvisionStatus(
                            loggerFactories, "testSelectionNotAmongProvisioned");

            assertFalse(loggerFactoryProvisionStatus.isProvisioned());
        }

        @Test
        void selectedLoggerFactoryButNoneProvisioned() {
            ArrayList<LoggerFactory> emptyProvisionedFactories = new ArrayList<>();
            ServiceProviderLocator.LoggerFactoryProvisionStatus loggerFactoryProvisionStatus =
                    new ServiceProviderLocator.LoggerFactoryProvisionStatus(
                            emptyProvisionedFactories, "testSelectionWhenNoneProvisioned");

            assertFalse(loggerFactoryProvisionStatus.isProvisioned());
        }
    }

    @Nested
    class provisioned {

        @Test
        void selectedLogFactoryIsTheOnlyOneProvisioned() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvisionStatus loggerFactoryProvisionStatus =
                    new ServiceProviderLocator.LoggerFactoryProvisionStatus(
                            loggerFactories, "elf4j.util.NoopLoggerFactory");

            assertTrue(loggerFactoryProvisionStatus.isProvisioned());
        }

        @Test
        void selectedLogFactoryIsInProvisionedOnes() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            loggerFactories.add(new IeLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvisionStatus loggerFactoryProvisionStatus =
                    new ServiceProviderLocator.LoggerFactoryProvisionStatus(
                            loggerFactories, "elf4j.util.NoopLoggerFactory");

            assertTrue(loggerFactoryProvisionStatus.isProvisioned());
        }

        @Test
        void onlyOneLogFactoryProvisionedWithNoSelection() {
            List<LoggerFactory> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLoggerFactory());
            ServiceProviderLocator.LoggerFactoryProvisionStatus loggerFactoryProvisionStatus =
                    new ServiceProviderLocator.LoggerFactoryProvisionStatus(loggerFactories, null);

            assertTrue(loggerFactoryProvisionStatus.isProvisioned());
        }
    }
}
