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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LoggerFactoryLoaderTest {

    @Nested
    class noop {

        @Test
        void noopFactoryNotNull() {
            NoopLoggerFactory noopLoggerFactory = new NoopLoggerFactory();
            ServiceProviderLocator.LoggerFactoryLoader loggerFactoryLoader =
                    new ServiceProviderLocator.LoggerFactoryLoader(null, noopLoggerFactory);

            assertSame(noopLoggerFactory, loggerFactoryLoader.getLoggerFactory());
        }
    }

    @Nested
    class notNoop {
        @Mock ServiceLoader<LoggerFactory> mockLoggerFactoryServiceLoader;
        @Mock Iterator<LoggerFactory> mockLoggerFactoryIterator;

        @Mock LoggerFactory mockLoggerFactory;

        @Test
        void noopFactoryNull() {
            ServiceProviderLocator.LoggerFactoryLoader loggerFactoryLoader =
                    new ServiceProviderLocator.LoggerFactoryLoader(mockLoggerFactoryServiceLoader, null);
            given(mockLoggerFactoryServiceLoader.iterator()).willReturn(mockLoggerFactoryIterator);
            given(mockLoggerFactoryIterator.next()).willReturn(mockLoggerFactory);

            LoggerFactory loggerFactory = loggerFactoryLoader.getLoggerFactory();

            then(mockLoggerFactoryIterator).should().next();
            assertSame(mockLoggerFactory, loggerFactory);
        }
    }
}