package elf4j;

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;
import org.junit.jupiter.api.BeforeEach;
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
class ServiceProviderLocatorTest {
    ServiceProviderLocator.LoggerFactoryLocator loggerFactoryLocator;
    @Mock ServiceProviderLocator.LoggerFactoryLoader mockLoggerFactoryLoader;
    @Mock LoggerFactory mockLoggerFactory;

    @BeforeEach
    void setUp() {
        loggerFactoryLocator = new ServiceProviderLocator.LoggerFactoryLocator(mockLoggerFactoryLoader);
    }

    @Nested
    class fallback {
        @Test
        void moreThanOneFactoriesYetNoSelection() {
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            factories.add(mockLoggerFactory);
            when(mockLoggerFactoryLoader.loadAll()).thenReturn(factories);

            assertTrue(loggerFactoryLocator.getLoggerFactory() instanceof NoopLoggerFactory);
        }

        @Test
        void noProvider() {
            when(mockLoggerFactoryLoader.loadAll()).thenReturn(Collections.emptyList());
            when(mockLoggerFactoryLoader.getSelectedLoggerFactoryName()).thenReturn(Optional.empty());
            assertTrue(loggerFactoryLocator.getLoggerFactory() instanceof NoopLoggerFactory);
        }

        @Test
        void selectedProviderNotFound() {
            when(mockLoggerFactoryLoader.getSelectedLoggerFactoryName()).thenReturn(Optional.of(
                    "non.existing.logger.factory.class.Name"));
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            when(mockLoggerFactoryLoader.loadAll()).thenReturn(factories);

            assertTrue(loggerFactoryLocator.getLoggerFactory() instanceof NoopLoggerFactory);
        }
    }

    @Nested
    class validProvisions {
        @Test
        void onlyOneLoggerFactoryProvisioned() {
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            when(mockLoggerFactoryLoader.loadAll()).thenReturn(factories);

            assertSame(mockLoggerFactory, loggerFactoryLocator.getLoggerFactory());
        }

        @Test
        void selectedLoggerFactoryFqcnAmongProvisionedFactories() {
            when(mockLoggerFactoryLoader.getSelectedLoggerFactoryName()).thenReturn(Optional.of(NoopLoggerFactory.class.getName()));
            List<LoggerFactory> factories = new ArrayList<>();
            factories.add(mockLoggerFactory);
            factories.add(new NoopLoggerFactory());
            when(mockLoggerFactoryLoader.loadAll()).thenReturn(factories);

            assertTrue(loggerFactoryLocator.getLoggerFactory() instanceof NoopLoggerFactory);
        }
    }
}