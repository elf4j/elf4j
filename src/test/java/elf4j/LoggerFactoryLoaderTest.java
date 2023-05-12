package elf4j;

import elf4j.spi.LoggerFactory;
import elf4j.util.NoopLoggerFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LoggerFactoryLoaderTest {

    @Nested
    class noop {

        @Test
        void noopTrue() {
            ServiceProviderLocator.LoggerFactoryLoader loggerFactoryLoader =
                    new ServiceProviderLocator.LoggerFactoryLoader(null, true);

            assertTrue(loggerFactoryLoader.getLoggerFactory() instanceof NoopLoggerFactory);
        }
    }

    @Nested
    class notNoop {
        @Mock ServiceLoader<LoggerFactory> mockLoggerFactoryServiceLoader;
        @Mock Iterator<LoggerFactory> mockLoggerFactoryIterator;

        @Mock LoggerFactory mockLoggerFactory;

        @Test
        void noopFalse() {
            ServiceProviderLocator.LoggerFactoryLoader loggerFactoryLoader =
                    new ServiceProviderLocator.LoggerFactoryLoader(mockLoggerFactoryServiceLoader, false);
            given(mockLoggerFactoryServiceLoader.iterator()).willReturn(mockLoggerFactoryIterator);
            given(mockLoggerFactoryIterator.next()).willReturn(mockLoggerFactory);

            LoggerFactory loggerFactory = loggerFactoryLoader.getLoggerFactory();

            then(mockLoggerFactoryIterator).should().next();
            assertSame(mockLoggerFactory, loggerFactory);
        }
    }
}