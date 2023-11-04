package elf4j;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoggerFactoryLoaderTest {

    @Nested
    class notProvisioned {

        @Test
        void whenNoLoggerFactoryLoadedAndNotProvisioned() {
            ServiceProviderLocator.LoggerFactoryLoader loggerFactoryLoader =
                    new ServiceProviderLocator.LoggerFactoryLoader(null, false);

            assertTrue(loggerFactoryLoader.getLoggerFactory() instanceof NoopLoggerFactory);
        }
    }

    @Nested
    class provisioned {
        @Mock
        ServiceLoader<LoggerFactory> mockLoggerFactoryServiceLoader;

        @Mock
        Iterator<LoggerFactory> mockLoggerFactoryIterator;

        @Mock
        LoggerFactory mockLoggerFactory;

        @Test
        void whenLoggerFactoryLoadedAndProvisioned() {
            ServiceProviderLocator.LoggerFactoryLoader loggerFactoryLoader =
                    new ServiceProviderLocator.LoggerFactoryLoader(mockLoggerFactoryServiceLoader, true);
            given(mockLoggerFactoryServiceLoader.iterator()).willReturn(mockLoggerFactoryIterator);
            given(mockLoggerFactoryIterator.next()).willReturn(mockLoggerFactory);

            LoggerFactory loggerFactory = loggerFactoryLoader.getLoggerFactory();

            then(mockLoggerFactoryIterator).should().next();
            assertSame(mockLoggerFactory, loggerFactory);
        }
    }
}
