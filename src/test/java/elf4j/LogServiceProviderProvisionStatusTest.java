package elf4j;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import elf4j.spi.LogServiceProvider;
import elf4j.util.NoopLogServiceProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LogServiceProviderProvisionStatusTest {

    @Nested
    class notSetup {
        @Test
        void moreThanOneLogServiceProvidersButNoSelection() {
            List<LogServiceProvider> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLogServiceProvider());
            loggerFactories.add(new NoopLogServiceProvider());
            LogServiceProviderLocator.LogServiceProviderSetupStatus logServiceProviderSetupStatus =
                    new LogServiceProviderLocator.LogServiceProviderSetupStatus(loggerFactories, null);

            assertFalse(logServiceProviderSetupStatus.isSetup());
        }

        @Test
        void noLogServiceProviderSetupAndNoSelection() {
            ArrayList<LogServiceProvider> emptySetupProviders = new ArrayList<>();
            LogServiceProviderLocator.LogServiceProviderSetupStatus logServiceProviderSetupStatus =
                    new LogServiceProviderLocator.LogServiceProviderSetupStatus(emptySetupProviders, null);

            assertFalse(logServiceProviderSetupStatus.isSetup());
        }

        @Test
        void selectedLogServiceProviderNotFoundInSetup() {
            List<LogServiceProvider> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLogServiceProvider());
            LogServiceProviderLocator.LogServiceProviderSetupStatus logServiceProviderSetupStatus =
                    new LogServiceProviderLocator.LogServiceProviderSetupStatus(
                            loggerFactories, "testSelectionNotAmongSetup");

            assertFalse(logServiceProviderSetupStatus.isSetup());
        }

        @Test
        void selectedLogServiceProviderButNoneSetup() {
            ArrayList<LogServiceProvider> emptySetupFactories = new ArrayList<>();
            LogServiceProviderLocator.LogServiceProviderSetupStatus logServiceProviderSetupStatus =
                    new LogServiceProviderLocator.LogServiceProviderSetupStatus(
                            emptySetupFactories, "testSelectionWhenNoneSetup");

            assertFalse(logServiceProviderSetupStatus.isSetup());
        }
    }

    @Nested
    class Setup {

        @Test
        void selectedLogServiceProviderIsTheOnlyOneSetup() {
            List<LogServiceProvider> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLogServiceProvider());
            LogServiceProviderLocator.LogServiceProviderSetupStatus logServiceProviderSetupStatus =
                    new LogServiceProviderLocator.LogServiceProviderSetupStatus(
                            loggerFactories, "elf4j.util.NoopLogServiceProvider");

            assertTrue(logServiceProviderSetupStatus.isSetup());
        }

        @Test
        void selectedLogServiceProviderIsInSetupOnes() {
            List<LogServiceProvider> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLogServiceProvider());
            loggerFactories.add(new TestLogServiceProvider());
            LogServiceProviderLocator.LogServiceProviderSetupStatus logServiceProviderSetupStatus =
                    new LogServiceProviderLocator.LogServiceProviderSetupStatus(
                            loggerFactories, "elf4j.util.NoopLogServiceProvider");

            assertTrue(logServiceProviderSetupStatus.isSetup());
        }

        @Test
        void onlyOneLogServiceProviderSetupWithNoSelection() {
            List<LogServiceProvider> loggerFactories = new ArrayList<>();
            loggerFactories.add(new NoopLogServiceProvider());
            LogServiceProviderLocator.LogServiceProviderSetupStatus logServiceProviderSetupStatus =
                    new LogServiceProviderLocator.LogServiceProviderSetupStatus(loggerFactories, null);

            assertTrue(logServiceProviderSetupStatus.isSetup());
        }
    }
}
