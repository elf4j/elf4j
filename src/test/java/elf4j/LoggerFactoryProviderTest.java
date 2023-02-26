package elf4j;

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggerFactoryProviderTest {
    @Mock LoggerFactoryConfiguration mockLoggerFactoryConfiguration;
    @Mock LoggerFactory mockLoggerFactory;

    @Nested
    class fallback {
        @Test
        void moreThanOneFactoriesYetNoSelection() {
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            factories.add(mockLoggerFactory);
            when(mockLoggerFactoryConfiguration.getProvisionedLoggerFactories()).thenReturn(factories);

            assertTrue(new LoggerFactoryProvider.Implementation(mockLoggerFactoryConfiguration).loggerFactory() instanceof NoopLoggerFactory);
        }

        @Test
        void noProvider() {
            when(mockLoggerFactoryConfiguration.getProvisionedLoggerFactories()).thenReturn(Collections.emptyList());
            when(mockLoggerFactoryConfiguration.getSelectedLoggerFactoryName()).thenReturn(Optional.empty());
            assertTrue(new LoggerFactoryProvider.Implementation(mockLoggerFactoryConfiguration).loggerFactory() instanceof NoopLoggerFactory);
        }

        @Test
        void selectedProviderNotFound() {
            when(mockLoggerFactoryConfiguration.getSelectedLoggerFactoryName()).thenReturn(Optional.of(
                    "non.existing.logger.factory.class.Name"));
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            when(mockLoggerFactoryConfiguration.getProvisionedLoggerFactories()).thenReturn(factories);

            assertTrue(new LoggerFactoryProvider.Implementation(mockLoggerFactoryConfiguration).loggerFactory() instanceof NoopLoggerFactory);
        }
    }

    @Nested
    class validProvisions {
        @Test
        void onlyOneLoggerFactoryProvisioned() {
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            when(mockLoggerFactoryConfiguration.getProvisionedLoggerFactories()).thenReturn(factories);

            assertSame(mockLoggerFactory,
                    new LoggerFactoryProvider.Implementation(mockLoggerFactoryConfiguration).loggerFactory());
        }

        @Test
        void selectedLoggerFactoryFqcnAmongProvisionedFactories() {
            when(mockLoggerFactoryConfiguration.getSelectedLoggerFactoryName()).thenReturn(Optional.of(NoopLoggerFactory.class.getName()));
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            factories.add(new NoopLoggerFactory());
            when(mockLoggerFactoryConfiguration.getProvisionedLoggerFactories()).thenReturn(factories);

            assertTrue(new LoggerFactoryProvider.Implementation(mockLoggerFactoryConfiguration).loggerFactory() instanceof NoopLoggerFactory);
        }
    }
}